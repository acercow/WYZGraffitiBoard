package com.sina.weibo.view.graffitiview;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.sina.weibo.view.graffitiview.bean.GraffitiLayerBean;
import com.sina.weibo.view.graffitiview.calculator.INextNoteCalculator;
import com.sina.weibo.view.graffitiview.calculator.SimpleNextNoteCalculator;
import com.sina.weibo.view.graffitiview.data.GraffitiData;
import com.sina.weibo.view.graffitiview.data.GraffitiLayerData;

/**
 * Created by fishyu on 2018/4/28.
 */
public class GraffitiView extends ViewGroup {

    static final String TAG = GraffitiView.class.getSimpleName();

    private INextNoteCalculator mNoteCalculator;

    private GraffitiData mGraffitiData;
    private GraffitiLayerData mDrawingLayer;

    private GraffitiLayerBean mDrawObject;

    public GraffitiView(Context context) {
        super(context);
    }

    public GraffitiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GraffitiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化，GraffitiData 带有配置参数，做相应配置
     *
     * @param initData
     */
    public void installData(GraffitiData initData) {
        if (initData == null) {
            initData = GraffitiData.generateDefault();
        }
        mNoteCalculator = new SimpleNextNoteCalculator();
        mGraffitiData = initData;

        if (initData.isShowMode()) {
            post(new Runnable() {
                @Override
                public void run() {
                    notifyDataChanged();
                }
            });
        }

        //TODO when reading exits
        mDrawingLayer = null;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // square this view
        heightMeasureSpec = widthMeasureSpec;

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.layout(l, t, r, b);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * 1, Update data <br>
     * 2, Notify view updating
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGraffitiData.isShowMode()) {
            Log.e(TAG, "player mode, just show notes");
            return super.onTouchEvent(event);
        }

        if (getMeasuredHeight() == 0 || getMeasuredWidth() == 0) {
            Log.e(TAG, "width or height is 0, getMeasuredWidth() -> " + getMeasuredWidth() + " getMeasuredHeight() -> " + getMeasuredHeight());
            return super.onTouchEvent(event);
        }

        if (getCurrentDrawObject() == null) {
            Log.e(TAG, "no GraffitiLayerBean set, nothing to draw.");
            return super.onTouchEvent(event);
        }

        final float pointX = event.getX();
        final float pointY = event.getY();

        // Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mDrawingLayer != null) {
                    throw new RuntimeException("How could mDrawingLayer not be null !");
                }
                mDrawingLayer = mGraffitiData.getDrawingLayer(getCurrentDrawObject());
                mDrawingLayer.installView(getMeasuredWidth(), getMeasuredHeight());
            case MotionEvent.ACTION_MOVE:
                if (mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, mDrawingLayer.getLast(), pointX, pointY))) {
                    notifyDataChanged(mDrawingLayer);
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                notifyDataChanged(mDrawingLayer);
                mDrawingLayer = null;
                return true;

            default:
                return true;
        }
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /**
     * Notify data changed, time to update view
     */
    public void notifyDataChanged() {
        GraffitiData data = mGraffitiData;
        //check view deleted
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = getChildAt(i);
            if (!data.getLayers().contains(view.getTag())) {
                notifyDataChanged((GraffitiLayerData) view.getTag());
            }
        }

        for (final GraffitiLayerData layerData : data.getLayers()) {
            notifyDataChanged(layerData);
        }
    }

    /**
     * Notify data changed, time to update view
     *
     * @param layerData
     */
    public void notifyDataChanged(GraffitiLayerData layerData) {
        boolean deleted = !mGraffitiData.getLayers().contains(layerData);
        GraffitiLayerView view = findViewWithTag(layerData);
        if (view == null) {
            if (!deleted) {
                view = new GraffitiLayerView(getContext(), layerData);
                view.setTag(layerData);
                addView(view);
            }
        } else {
            if (deleted) {
                removeView(view);
            } else {
                view.notifyDataChanged();
            }
        }
    }


    /**
     * Returning selected {@link GraffitiLayerBean}
     *
     * @return
     */
    protected GraffitiLayerBean getCurrentDrawObject() {
        return mDrawObject;
    }

    /**
     * Setting {@link GraffitiLayerBean}, with witch to draw.
     *
     * @param layerBean
     */
    public void setDrawObject(GraffitiLayerBean layerBean) {
        mDrawObject = layerBean;
    }


    /**
     * Getting internal {@link GraffitiData} witch drives {@link GraffitiView}.
     *
     * @return
     */
    public GraffitiData getGraffitiData() {
        return mGraffitiData;
    }

}
