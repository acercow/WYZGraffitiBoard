package com.sina.weibo.view.graffitiview.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.RectF;
import android.util.Log;

import com.sina.weibo.view.graffitiview.data.GraffitiLayerData;

/**
 * Created by fishyu on 2018/5/2.
 */

public abstract class AbstractBaseAnimator implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    protected final String TAG = getClass().getSimpleName();

    protected Runnable mUpdateViewRunnable;

    private ObjectAnimator mAnimator;

    private float mCurrentValue;

    private static final long ANIMATION_FRAME_TIME = 1000 / 45;

    private long mLastUpdateTime = 0;

    private GraffitiLayerData mLayerData;

    public AbstractBaseAnimator(GraffitiLayerData layerData, Runnable updateViewRunnable, long duration, float from, float to) {
        mLayerData = layerData;
        mUpdateViewRunnable = updateViewRunnable;

        mAnimator = ObjectAnimator.ofFloat(AbstractBaseAnimator.this, "value", from, to);
        mAnimator.setDuration(duration);
        mAnimator.addListener(this);
        mAnimator.addUpdateListener(this);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
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
        Log.v(TAG, "onAnimationStart");
        setValue(0);
        mLayerData.forceDrawAll(false);
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        Log.v(TAG, "onAnimationEnd");
        setValue(0);
        notifyView();
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        Log.v(TAG, "onAnimationCancel");
        setValue(0);
        notifyView();
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        Log.v(TAG, "onAnimationRepeat");
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (System.currentTimeMillis() - mLastUpdateTime >= ANIMATION_FRAME_TIME) {
            notifyView();
        }
    }

    /**
     * Notify view time to update
     */
    protected void notifyView() {
        mLastUpdateTime = System.currentTimeMillis();
        mUpdateViewRunnable.run();
    }


    /**
     * Getting internal {@link Animator}
     *
     * @return
     */
    public Animator getAnimator() {
        return mAnimator;
    }

}
