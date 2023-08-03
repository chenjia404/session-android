package org.thoughtcrime.securesms.mediasend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import org.session.libsession.utilities.MediaTypes;
import org.session.libsession.utilities.Stub;
import org.session.libsession.utilities.TextSecurePreferences;
import org.session.libsession.utilities.Util;
import org.session.libsession.utilities.recipients.Recipient;
import org.session.libsignal.utilities.ListenableFuture;
import org.session.libsignal.utilities.Log;
import org.session.libsignal.utilities.SettableFuture;
import org.session.libsignal.utilities.guava.Optional;
import org.thoughtcrime.securesms.components.ComposeText;
import org.thoughtcrime.securesms.components.ControllableViewPager;
import org.thoughtcrime.securesms.components.InputAwareLayout;
import org.thoughtcrime.securesms.components.emoji.EmojiEditText;
import org.thoughtcrime.securesms.components.emoji.EmojiEventListener;
import org.thoughtcrime.securesms.components.emoji.EmojiKeyboardProvider;
import org.thoughtcrime.securesms.components.emoji.EmojiToggle;
import org.thoughtcrime.securesms.components.emoji.MediaKeyboard;
import org.thoughtcrime.securesms.contactshare.SimpleTextWatcher;
import org.thoughtcrime.securesms.imageeditor.model.EditorModel;
import org.thoughtcrime.securesms.mediapreview.MediaRailAdapter;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.providers.BlobProvider;
import org.thoughtcrime.securesms.scribbles.ImageEditorFragment;
import org.thoughtcrime.securesms.util.CharacterCalculator.CharacterState;
import org.thoughtcrime.securesms.util.PushCharacterCalculator;
import org.thoughtcrime.securesms.util.Stopwatch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import network.qki.messenger.R;

/**
 * Allows the user to edit and caption a set of media items before choosing to send them.
 */
public class ETMediaSendFragment extends Fragment implements ViewTreeObserver.OnGlobalLayoutListener,
        MediaRailAdapter.RailItemListener,
        InputAwareLayout.OnKeyboardShownListener,
        InputAwareLayout.OnKeyboardHiddenListener {

    private static final String TAG = ETMediaSendFragment.class.getSimpleName();


    private InputAwareLayout hud;
    private FrameLayout sendButton;
    private View closeButton;

    private ControllableViewPager fragmentPager;
    private MediaSendFragmentPagerAdapter fragmentPagerAdapter;
    private RecyclerView mediaRail;
    private MediaRailAdapter mediaRailAdapter;

    private int visibleHeight;
    private MediaSendViewModel viewModel;
    private Controller controller;

    private final Rect visibleBounds = new Rect();


    public static ETMediaSendFragment newInstance() {
        return new ETMediaSendFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(requireActivity() instanceof Controller)) {
            throw new IllegalStateException("Parent activity must implement controller interface.");
        }

        controller = (Controller) requireActivity();
    }

    @Override
    public @Nullable View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.et_mediasend_fragment, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initViewModel();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        hud = view.findViewById(R.id.mediasend_hud);
        sendButton = view.findViewById(R.id.flSend);
        fragmentPager = view.findViewById(R.id.mediasend_pager);
        mediaRail = view.findViewById(R.id.mediasend_media_rail);
        closeButton = view.findViewById(R.id.mediasend_close_button);

        sendButton.setOnClickListener(v -> {
            //processMedia(fragmentPagerAdapter.getAllMedia(), fragmentPagerAdapter.getSavedState());
            controller.onSendClicked(fragmentPagerAdapter.getAllMedia());
        });

        fragmentPagerAdapter = new MediaSendFragmentPagerAdapter(getChildFragmentManager());
        fragmentPager.setAdapter(fragmentPagerAdapter);

        FragmentPageChangeListener pageChangeListener = new FragmentPageChangeListener();
        fragmentPager.addOnPageChangeListener(pageChangeListener);
        fragmentPager.post(() -> pageChangeListener.onPageSelected(fragmentPager.getCurrentItem()));

        mediaRailAdapter = new MediaRailAdapter(GlideApp.with(this), this, true);
        mediaRail.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        mediaRail.setAdapter(mediaRailAdapter);

        hud.getRootView().getViewTreeObserver().addOnGlobalLayoutListener(this);
        hud.addOnKeyboardShownListener(this);
        hud.addOnKeyboardHiddenListener(this);

        closeButton.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    @Override
    public void onStart() {
        super.onStart();

        fragmentPagerAdapter.restoreState(viewModel.getDrawState());
        viewModel.onImageEditorStarted();

        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onStop() {
        super.onStop();
        fragmentPagerAdapter.saveAllState();
        viewModel.saveDrawState(fragmentPagerAdapter.getSavedState());
    }

    @Override
    public void onGlobalLayout() {
        hud.getRootView().getWindowVisibleDisplayFrame(visibleBounds);

        int currentVisibleHeight = visibleBounds.height();

        if (currentVisibleHeight != visibleHeight) {
            hud.getLayoutParams().height = currentVisibleHeight;
            hud.layout(visibleBounds.left, visibleBounds.top, visibleBounds.right, visibleBounds.bottom);
            hud.requestLayout();

            visibleHeight = currentVisibleHeight;
        }
    }

    @Override
    public void onRailItemClicked(int distanceFromActive) {
        viewModel.onPageChanged(fragmentPager.getCurrentItem() + distanceFromActive);
    }

    @Override
    public void onRailItemDeleteClicked(int distanceFromActive) {
        viewModel.onMediaItemRemoved(requireContext(), fragmentPager.getCurrentItem() + distanceFromActive);
    }

    @Override
    public void onKeyboardShown() {

    }

    @Override
    public void onKeyboardHidden() {

    }


    private void initViewModel() {
        viewModel = new ViewModelProvider(requireActivity(), new MediaSendViewModel.Factory(requireActivity().getApplication(), new MediaRepository())).get(MediaSendViewModel.class);

        viewModel.getSelectedMedia().observe(this, media -> {
            if (Util.isEmpty(media)) {
                controller.onNoMediaAvailable();
                return;
            }

            fragmentPagerAdapter.setMedia(media);

            mediaRailAdapter.setMedia(media);
        });

        viewModel.getPosition().observe(this, position -> {
            if (position == null || position < 0) return;

            fragmentPager.setCurrentItem(position, true);
            mediaRailAdapter.setActivePosition(position);
            mediaRail.smoothScrollToPosition(position);
        });

        viewModel.getBucketId().observe(this, bucketId -> {
            if (bucketId == null) return;

            mediaRailAdapter.setAddButtonListener(() -> controller.onAddMediaClicked(bucketId));
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void processMedia(@NonNull List<Media> mediaList, @NonNull Map<Uri, Object> savedState) {
        Map<Media, ListenableFuture<Bitmap>> futures = new HashMap<>();

        for (Media media : mediaList) {
            Object state = savedState.get(media.getUri());

            if (state instanceof ImageEditorFragment.Data) {
                EditorModel model = ((ImageEditorFragment.Data) state).readModel();
                if (model != null && model.isChanged()) {
                    futures.put(media, render(requireContext(), model));
                }
            }
        }

        new AsyncTask<Void, Void, List<Media>>() {

            private Stopwatch renderTimer;
            private Runnable progressTimer;
            private AlertDialog dialog;

            @Override
            protected void onPreExecute() {
                renderTimer = new Stopwatch("ProcessMedia");
                progressTimer = () -> {
                    dialog = new AlertDialog.Builder(new ContextThemeWrapper(requireContext(), R.style.Theme_TextSecure_Dialog_MediaSendProgress))
                            .setView(R.layout.progress_dialog)
                            .setCancelable(false)
                            .create();
                    dialog.show();
                    dialog.getWindow().setLayout(getResources().getDimensionPixelSize(R.dimen.mediasend_progress_dialog_size),
                            getResources().getDimensionPixelSize(R.dimen.mediasend_progress_dialog_size));
                };
                Util.runOnMainDelayed(progressTimer, 250);
            }

            @Override
            protected List<Media> doInBackground(Void... voids) {
                Context context = requireContext();
                List<Media> updatedMedia = new ArrayList<>(mediaList.size());

                for (Media media : mediaList) {
                    if (futures.containsKey(media)) {
                        try {
                            Bitmap bitmap = futures.get(media).get();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);

                            Uri uri = BlobProvider.getInstance()
                                    .forData(baos.toByteArray())
                                    .withMimeType(MediaTypes.IMAGE_JPEG)
                                    .createForSingleSessionOnDisk(context, e -> Log.w(TAG, "Failed to write to disk.", e));

                            Media updated = new Media(uri, MediaTypes.IMAGE_JPEG, media.getDate(), bitmap.getWidth(), bitmap.getHeight(), baos.size(), media.getBucketId(), media.getCaption());

                            updatedMedia.add(updated);
                            renderTimer.split("item");
                        } catch (InterruptedException | ExecutionException | IOException e) {
                            Log.w(TAG, "Failed to render image. Using base image.");
                            updatedMedia.add(media);
                        }
                    } else {
                        updatedMedia.add(media);
                    }
                }
                return updatedMedia;
            }

            @Override
            protected void onPostExecute(List<Media> media) {
                controller.onSendClicked(media);
                Util.cancelRunnableOnMain(progressTimer);
                if (dialog != null) {
                    dialog.dismiss();
                }
                renderTimer.stop(TAG);
            }
        }.execute();
    }

    private static ListenableFuture<Bitmap> render(@NonNull Context context, @NonNull EditorModel model) {
        SettableFuture<Bitmap> future = new SettableFuture<>();

        AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> future.set(model.render(context)));

        return future;
    }

    private class FragmentPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            viewModel.onPageChanged(position);
        }
    }

    public interface Controller {
        void onAddMediaClicked(@NonNull String bucketId);

        void onSendClicked(@NonNull List<Media> media);

        void onNoMediaAvailable();
    }
}
