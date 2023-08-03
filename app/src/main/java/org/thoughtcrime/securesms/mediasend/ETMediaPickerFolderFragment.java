package org.thoughtcrime.securesms.mediasend;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.session.libsession.utilities.recipients.Recipient;
import org.session.libsignal.utilities.guava.Optional;
import org.thoughtcrime.securesms.mms.GlideApp;

import network.qki.messenger.R;

/**
 * Allows the user to select a media folder to explore.
 */
public class ETMediaPickerFolderFragment extends Fragment implements MediaPickerFolderAdapter.EventListener {

    private static final String KEY_RECIPIENT_NAME = "recipient_name";

    private MediaSendViewModel viewModel;
    private Controller controller;
    private GridLayoutManager layoutManager;

    public static @NonNull ETMediaPickerFolderFragment newInstance() {
        ETMediaPickerFolderFragment fragment = new ETMediaPickerFolderFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity(), new MediaSendViewModel.Factory(requireActivity().getApplication(), new MediaRepository())).get(MediaSendViewModel.class);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (!(getActivity() instanceof Controller)) {
            throw new IllegalStateException("Parent activity must implement controller class.");
        }

        controller = (Controller) getActivity();
    }

    @Override
    public @Nullable View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.mediapicker_folder_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView list = view.findViewById(R.id.mediapicker_folder_list);
        MediaPickerFolderAdapter adapter = new MediaPickerFolderAdapter(GlideApp.with(this), this);

        layoutManager = new GridLayoutManager(requireContext(), 2);
        onScreenWidthChanged(getScreenWidth());

        list.setLayoutManager(layoutManager);
        list.setAdapter(adapter);

        viewModel.getFolders(requireContext()).observe(getViewLifecycleOwner(), adapter::setFolders);

        initToolbar(view.findViewById(R.id.mediapicker_toolbar));
    }

    @Override
    public void onResume() {
        super.onResume();

        viewModel.onFolderPickerStarted();
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        onScreenWidthChanged(getScreenWidth());
    }

    private void initToolbar(Toolbar toolbar) {
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void onScreenWidthChanged(int newWidth) {
        if (layoutManager != null) {
            layoutManager.setSpanCount(newWidth / getResources().getDimensionPixelSize(R.dimen.media_picker_folder_width));
        }
    }

    private int getScreenWidth() {
        Point size = new Point();
        requireActivity().getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    @Override
    public void onFolderClicked(@NonNull MediaFolder folder) {
        controller.onFolderSelected(folder);
    }

    public interface Controller {
        void onFolderSelected(@NonNull MediaFolder folder);
    }
}
