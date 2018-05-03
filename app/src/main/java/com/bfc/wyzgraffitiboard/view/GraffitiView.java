package com.bfc.wyzgraffitiboard.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bfc.wyzgraffitiboard.view.calculator.INextNoteCalculator;
import com.bfc.wyzgraffitiboard.view.calculator.SimpleNextNoteCalculator;
import com.bfc.wyzgraffitiboard.view.data.GraffitiDataObject;
import com.bfc.wyzgraffitiboard.view.data.GraffitiLayerDataObject;

/**
 * Created by fishyu on 2018/4/28.
 */
public class GraffitiView extends ViewGroup {

    static final String TAG = GraffitiView.class.getSimpleName();

    private INextNoteCalculator mNoteCalculator;

    private GraffitiDataObject mGraffitiData;

    private GraffitiLayerDataObject mDrawingLayer;

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
    public void init(GraffitiDataObject initData) {
        if (initData == null) {
            initData = GraffitiDataObject.generateDefault();
        }
        mNoteCalculator = new SimpleNextNoteCalculator();
        mGraffitiData = initData;

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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * 1, Update data <br>
     * 2, Notify view updating
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getMeasuredHeight() == 0 || getMeasuredWidth() == 0) {
            Log.e(TAG, "width or height is 0, getMeasuredWidth() -> " + getMeasuredWidth() + " getMeasuredHeight() -> " + getMeasuredHeight());
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
                mDrawingLayer = mGraffitiData.getDrawingLayer();
                mDrawingLayer.initialize(getMeasuredWidth(), getMeasuredHeight());
                mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, null, pointX, pointY));
                notifyDataChanged(mDrawingLayer);
                return true;
            case MotionEvent.ACTION_MOVE:
                mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, mDrawingLayer.getLast(), pointX, pointY));
                notifyDataChanged(mDrawingLayer);
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
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.layout(l, t, r, b);
        }
    }

    /**
     * @param data
     */
    public void notifyDataChanged(GraffitiDataObject data) {
        for (GraffitiLayerDataObject layerData : data.getLayers()) {
            notifyDataChanged(data);
        }
    }

    public void notifyDataChanged(GraffitiLayerDataObject layerData) {
        GraffitiLayerView view = findViewWithTag(layerData);
        if (view == null) {
            view = new GraffitiLayerView(getContext(), layerData);
            view.setTag(layerData);
            addView(view);
        } else {
            view.notifyDataChanged();
        }
    }


}
