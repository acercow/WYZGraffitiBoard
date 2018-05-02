package com.bfc.wyzgraffitiboard.view.logic;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * 图层绘制
 * <p>
 */
public class LogicView {

    static final String TAG = LogicView.class.getSimpleName();

    private View mInternalView;

    protected boolean mRequestInvalidate;

    private Context mContext;

    public LogicView(Context context) {
        mRequestInvalidate = true;
        mContext = context;
    }

    public void setParent(LogicViewGroup parent) {
        mInternalView = parent;
    }


    public View getParent() {
        return mInternalView;
    }

    /**
     * Called by {@link View#onSizeChanged(int, int, int, int)}
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.v(TAG, "onSizeChanged w -> " + w + " h -> " + h);
    }

    /**
     * Called by {@link View#onAttachedToWindow()}
     */
    protected void onAttachedToWindow() {
        Log.v(TAG, "onAttachedToWindow");
    }

    /**
     * Called by {@link View#onDetachedFromWindow()}
     */
    protected void onDetachedFromWindow() {
        Log.v(TAG, "onDetachedFromWindow");
    }

    /**
     * Called by {@link View#onDraw(Canvas)}
     *
     * @param canvas
     */
    protected void onDraw(Canvas canvas) {
        Log.v(TAG, "onDraw");
        mRequestInvalidate = false;
    }

    /**
     * Update view
     */
    public void invalidate() {
        Log.v(TAG, "invalidate");
        mRequestInvalidate = true;
        if (getParent() != null) {
            getParent().invalidate();
        }
    }

    public boolean invalidated() {
        return mRequestInvalidate;
    }

    protected Context getContext() {
        return mContext;
    }


}
