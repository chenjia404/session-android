package org.thoughtcrime.securesms.util.statelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.constraintlayout.widget.ConstraintLayout;

import network.qki.messenger.R;


public class StateLayout extends FrameLayout {
    private View contentView;

    private View emptyView;
    private View emptyContentView;

    private View errorView;
    private View errorContentView;

    private View progressView;
    private View progressContentView;

    private TextView emptyTitleView;
    private TextView emptyTextView;
    private TextView errorTextView;
    private TextView progressTextView;
    private ConstraintLayout emptyOpt;

    private ImageView errorImageView;
    private ImageView emptyImageView;
    private ProgressBar progressBar;

    private View currentShowingView;


    public StateLayout(Context context) {
        this(context, null);
    }


    public StateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public StateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        parseAttrs(context, attrs);

        emptyView.setVisibility(View.GONE);

        errorView.setVisibility(View.GONE);

        progressView.setVisibility(View.GONE);

        currentShowingView = contentView;
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        LayoutInflater inflater = LayoutInflater.from(context);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.StateLayout, 0, 0);
        int progressViewId;
        Drawable errorDrawable;
        Drawable emptyDrawable;
        String emptyString;
        try {
            errorDrawable = a.getDrawable(R.styleable.StateLayout_errorDrawable);
            emptyDrawable = a.getDrawable(R.styleable.StateLayout_emptyDrawable);
            progressViewId = a.getResourceId(R.styleable.StateLayout_progressView, -1);
            emptyString = a.getString(R.styleable.StateLayout_emptyString);
        } finally {
            a.recycle();
        }

        /******************************************************************************************/

        if (progressViewId != -1) {
            progressView = inflater.inflate(progressViewId, this, false);
        } else {
            progressView = inflater.inflate(R.layout.layout_statelayout_progress, this, false);
            progressContentView = progressView.findViewById(R.id.progress_content);
        }

        addView(progressView);
        /******************************************************************************************/

        /******************************************************************************************/

        errorView = inflater.inflate(R.layout.layout_statelayout_error, this, false);
        errorContentView = errorView.findViewById(R.id.error_content);
        errorTextView = errorView.findViewById(R.id.errorTextView);
        errorImageView = errorView.findViewById(R.id.errorImageView);
        if (errorDrawable != null) {
            errorImageView.setImageDrawable(errorDrawable);
        } else {
            errorImageView.setImageResource(R.drawable.ic_statelayout_empty);
        }
        addView(errorView);
        /******************************************************************************************/

        /******************************************************************************************/

        emptyView = inflater.inflate(R.layout.layout_statelayout_empty, this, false);
        emptyTitleView = emptyView.findViewById(R.id.emptyTitleView);
        emptyContentView = emptyView.findViewById(R.id.empty_content);
        emptyTextView = emptyView.findViewById(R.id.emptyTextView);
        emptyOpt = emptyView.findViewById(R.id.clOpt);
        if (!TextUtils.isEmpty(emptyString)) {
            emptyTextView.setText(emptyString);
        }
        emptyImageView = emptyView.findViewById(R.id.emptyImageView);
        if (emptyDrawable != null) {
            emptyImageView.setImageDrawable(emptyDrawable);
        } else {
            emptyImageView.setImageResource(R.drawable.ic_statelayout_empty);
        }
        addView(emptyView);
        /******************************************************************************************/

    }

    private void checkIsContentView(View view) {
        if (contentView == null && view != errorView && view != progressView && view != emptyView) {
            contentView = view;
            currentShowingView = contentView;
        }
    }

    public ImageView getErrorImageView() {
        return errorImageView;
    }

    public ImageView getEmptyImageView() {
        return emptyImageView;
    }

    public void setViewSwitchAnimProvider(ViewAnimProvider viewSwitchAnimProvider) {
        if (viewSwitchAnimProvider != null) {
            this.showAnimation = viewSwitchAnimProvider.showAnimation();
            this.hideAnimation = viewSwitchAnimProvider.hideAnimation();
        }
    }


    public boolean isShouldPlayAnim() {
        return shouldPlayAnim;
    }

    public void setShouldPlayAnim(boolean shouldPlayAnim) {
        this.shouldPlayAnim = shouldPlayAnim;
    }

    private boolean shouldPlayAnim = true;
    private Animation hideAnimation;
    private Animation showAnimation;

    public Animation getShowAnimation() {
        return showAnimation;
    }

    public void setShowAnimation(Animation showAnimation) {
        this.showAnimation = showAnimation;
    }

    public Animation getHideAnimation() {
        return hideAnimation;
    }

    public void setHideAnimation(Animation hideAnimation) {
        this.hideAnimation = hideAnimation;
    }

    private void switchWithAnimation(final View toBeShown) {
        final View toBeHided = currentShowingView;
        if (toBeHided == toBeShown)
            return;
        if (shouldPlayAnim) {
            if (toBeHided != null) {
                if (hideAnimation != null) {
                    hideAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            toBeHided.setVisibility(GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    hideAnimation.setFillAfter(false);
                    toBeHided.startAnimation(hideAnimation);
                } else
                    toBeHided.setVisibility(GONE);
            }
            if (toBeShown != null) {
                if (toBeShown.getVisibility() != VISIBLE)
                    toBeShown.setVisibility(VISIBLE);
                currentShowingView = toBeShown;
                if (showAnimation != null) {
                    showAnimation.setFillAfter(false);
                    toBeShown.startAnimation(showAnimation);
                }
            }
        } else {
            if (toBeHided != null) {
                toBeHided.setVisibility(GONE);
            }
            if (toBeShown != null) {
                currentShowingView = toBeShown;
                toBeShown.setVisibility(VISIBLE);
            }
        }

    }

    public void setEmptyContentViewMargin(int left, int top, int right, int bottom) {
        ((LinearLayout.LayoutParams) emptyImageView.getLayoutParams()).setMargins(left, top, right, bottom);
    }

    public void setErrorContentViewMargin(int left, int top, int right, int bottom) {
        ((LinearLayout.LayoutParams) errorImageView.getLayoutParams()).setMargins(left, top, right, bottom);
    }

    public void setProgressContentViewMargin(int left, int top, int right, int bottom) {
        if (progressBar != null)
            ((LinearLayout.LayoutParams) progressBar.getLayoutParams()).setMargins(left, top, right, bottom);
    }

    public void setInfoContentViewMargin(int left, int top, int right, int bottom) {
        setEmptyContentViewMargin(left, top, right, bottom);
        setErrorContentViewMargin(left, top, right, bottom);
        setProgressContentViewMargin(left, top, right, bottom);
    }


    public void showContentView() {
        switchWithAnimation(contentView);
    }

    public void showEmptyView() {
        showEmptyView("");
    }

    public void showEmptyView(String msg) {
        onHideContentView();
        if (!TextUtils.isEmpty(msg))
            emptyTextView.setText(msg);
        switchWithAnimation(emptyView);
    }

    public void showEmptyView(@DrawableRes int resId, String msg) {
        onHideContentView();
        if (!TextUtils.isEmpty(msg))
            emptyTextView.setText(msg);
        if (resId != 0) {
            emptyImageView.setImageResource(resId);
        }
        switchWithAnimation(emptyView);
    }

    public void showEmptyView(@DrawableRes int resId, String msg, boolean optShow, OnClickListener listener) {
        onHideContentView();
        if (!TextUtils.isEmpty(msg))
            emptyTextView.setText(msg);
        if (resId != 0) {
            emptyImageView.setImageResource(resId);
        }
        emptyOpt.setVisibility(optShow ? VISIBLE : GONE);
        emptyOpt.setOnClickListener(listener);
        switchWithAnimation(emptyView);
    }

    public void setEmptyTitleView(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            emptyTitleView.setText(msg);
            emptyTitleView.setVisibility(VISIBLE);
        }
    }

    public void showErrorView() {
        showErrorView(null);
    }

    public void showErrorView(String msg) {
        onHideContentView();
        if (msg != null)
            errorTextView.setText(msg);
        switchWithAnimation(errorView);
    }

    public void showProgressView() {
        showProgressView(null);
    }

    public void showProgressView(String msg) {
        onHideContentView();
        if (msg != null)
            progressTextView.setText(msg);
        switchWithAnimation(progressView);
    }

    public void setErrorAction(final OnClickListener onErrorButtonClickListener) {
        errorView.setOnClickListener(onErrorButtonClickListener);
    }

    public void setEmptyAction(final OnClickListener onEmptyButtonClickListener) {
        emptyView.setOnClickListener(onEmptyButtonClickListener);
    }


    public void setErrorAndEmptyAction(final OnClickListener errorAndEmptyAction) {
        errorView.setOnClickListener(errorAndEmptyAction);
        emptyView.setOnClickListener(errorAndEmptyAction);
    }

    protected void onHideContentView() {
        //Override me
    }


    /**
     * addView
     */

    @Override
    public void addView(View child) {
        checkIsContentView(child);
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        checkIsContentView(child);
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        checkIsContentView(child);
        super.addView(child, index, params);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        checkIsContentView(child);
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int width, int height) {
        checkIsContentView(child);
        super.addView(child, width, height);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
        checkIsContentView(child);
        return super.addViewInLayout(child, index, params);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        checkIsContentView(child);
        return super.addViewInLayout(child, index, params, preventRequestLayout);
    }
}
