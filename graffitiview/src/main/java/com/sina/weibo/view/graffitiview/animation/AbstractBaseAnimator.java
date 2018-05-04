package com.sina.weibo.view.graffitiview.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sina.weibo.view.graffitiview.data.GraffitiLayerData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/5/2.
 */

public abstract class AbstractBaseAnimator implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

    /**
     * Awake all updateView Runnable  at same time-line for better preference ?
     * <p>
     * With GPU profiler, this align clock seems have no apparent improvement.
     */
    private static final class InternalAlignClock extends Handler {

        private List<Runnable> mRunnables = new ArrayList<>();


        private boolean mRunning = false;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int size = mRunnables.size();

            if (size <= 0) {
                mRunning = false;
                return;
            }

            mRunning = true;
            for (int i = 0; i < size; i++) {
                Runnable runnable = mRunnables.get(i);
                runnable.run();
            }

            heartBeat(true);
        }

        void heartBeat(boolean delay) {
            sendEmptyMessageDelayed(0, delay ? ANIMATION_FRAME_TIME : 0);
        }

        public void start(Runnable runnable) {
            if (!mRunnables.contains(runnable)) {
                mRunnables.add(runnable);
            }
            if (!mRunning) {
                heartBeat(false);
            }
        }

        public void stop(Runnable runnable) {
            mRunnables.remove(runnable);
        }
    }

    protected final String TAG = getClass().getSimpleName();

    protected Runnable mUpdateViewRunnable;


    static final boolean ENABLE_ALIGN_CLOCK = false;

    static InternalAlignClock mClock = ENABLE_ALIGN_CLOCK ? new InternalAlignClock() : null;

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
        mAnimator.cancel();
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
        if (mClock != null) {
            mClock.start(mUpdateViewRunnable);
        }
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        Log.v(TAG, "onAnimationEnd");
        setValue(0);
        notifyView();
        if (mClock != null) {
            mClock.stop(mUpdateViewRunnable);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        Log.v(TAG, "onAnimationCancel");
        setValue(0);
        notifyView();
        if (mClock != null) {
            mClock.stop(mUpdateViewRunnable);
        }
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (mClock == null) {
            if (System.currentTimeMillis() - mLastUpdateTime >= ANIMATION_FRAME_TIME) {
                notifyView();
            }
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
