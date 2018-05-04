package com.sina.weibo.trash.logic;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by fishyu on 2018/5/2.
 */

public class LogicViewGroup extends View {

    private List<LogicView> mChildren = new ArrayList<>();

    private boolean mAttachedToWindow = false;

    public LogicViewGroup(Context context) {
        super(context);
    }

    /**
     * Adding view
     *
     * @param view
     */
    public void addView(LogicView view) {
        if (mChildren.contains(view)) {
            throw new IllegalStateException("Target " + view + " has already in LogicViewGroup");
        }
        mChildren.add(view);
        view.setParent(this);
        if (getMeasuredWidth() > 0 || getMeasuredHeight() > 0) {
            view.onSizeChanged(getMeasuredWidth(), getMeasuredHeight(), 0, 0);
        }
        if (mAttachedToWindow) {
            view.onAttachedToWindow();
        }
        view.invalidate();
    }

    /**
     * Removing view
     *
     * @param view
     */
    public void removeView(LogicView view) {
        if (mChildren.contains(view)) {
            mChildren.remove(view);
            view.setParent(null);
            view.onDetachedFromWindow();
        }
    }

    public int getChildCount() {
        return mChildren.size();
    }

    public LogicView getChildAt(int i) {
        return mChildren.get(i);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        for (LogicView view : mChildren) {
            view.onSizeChanged(w, h, oldw, oldh);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        for (LogicView view : mChildren) {
            view.onAttachedToWindow();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
        for (LogicView view : mChildren) {
            view.onDetachedFromWindow();
        }
    }

    @DebugLog
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (LogicView view : mChildren) {
            if (view.invalidated() || true) {
                view.onDraw(canvas);
            }
        }
    }


}
