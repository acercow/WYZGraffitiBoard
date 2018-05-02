package com.bfc.wyzgraffitiboard.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.RectF;

import com.bfc.wyzgraffitiboard.data.GraffitiLayerData;
import com.bfc.wyzgraffitiboard.view.test.GraffitiLayerLogicView;

/**
 * Created by fishyu on 2018/5/2.
 */

public abstract class AbstractBaseAnimator implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    protected GraffitiLayerLogicView mLayerView;
    protected GraffitiLayerData mLayerData;

    private ObjectAnimator mAnimator;

    private float mCurrentValue;

    private static final long ANIMATION_FRAME_TIME = 1000 / 30;

    private long mLastUpdateTime = 0;

    public AbstractBaseAnimator(GraffitiLayerData data, GraffitiLayerLogicView view, long duration, float from, float to) {
        mLayerData = data;
        mLayerView = view;

        mAnimator = ObjectAnimator.ofFloat(AbstractBaseAnimator.this, "value", from, to);
        mAnimator.setDuration(duration);
        mAnimator.addListener(this);
        mAnimator.addUpdateListener(this);
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
    }


    /**
     * Start animation
     */
    public final void start() {
        if (mAnimator.isRunning() || mAnimator.isStarted()) {
            return;
        }
        mAnimator.start();
    }

    /**
     * Stop animation
     */
    public final void stop() {
        if (mAnimator != null) {
            mAnimator.cancel();
        }
    }


    /**
     * Calculate animated RectF
     *
     * @param input
     * @return
     */
    public final RectF getAnimateRectF(RectF input, RectF out) {
        return onCalculateRectF(input, out, mCurrentValue);
    }


    /**
     * On calculate target rectF
     *
     * @param input
     * @param out
     * @param currentValue
     * @return target
     */
    protected abstract RectF onCalculateRectF(RectF input, RectF out, float currentValue);


    @SuppressWarnings("unused")
    public final void setValue(float value) {
        mCurrentValue = value;
    }


    @Override
    public void onAnimationStart(Animator animation) {
        setValue(0);
        mLayerData.installAnimator(this);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        setValue(0);
        notifyView();
        mLayerData.uninstallAnimator();
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        setValue(0);
        notifyView();
        mLayerData.uninstallAnimator();
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (System.currentTimeMillis() - mLastUpdateTime >= ANIMATION_FRAME_TIME) {
            notifyView();
        }
    }

    protected void notifyView() {
        mLastUpdateTime = System.currentTimeMillis();
        mLayerView.notifyDataChanged();
    }

}
