package com.bfc.wyzgraffitiboard.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.bfc.wyzgraffitiboard.calculator.INextNoteCalculator;
import com.bfc.wyzgraffitiboard.calculator.SimpleNextNoteCalculator;
import com.bfc.wyzgraffitiboard.data.GraffitiData;
import com.bfc.wyzgraffitiboard.data.GraffitiLayerData;

/**
 * Created by fishyu on 2018/4/28.
 */
public class GrafittiView extends ViewGroup {

    private INextNoteCalculator mNoteCalculator;

    private GraffitiData mGraffitiData;

    private GraffitiLayerData mDrawingLayer;

    public GrafittiView(Context context) {
        super(context);
    }

    public GrafittiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GrafittiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    /**
     * 初始化，GraffitiData 带有配置参数，做相应配置
     *
     * @param initData
     */
    public void init(GraffitiData initData) {
        mNoteCalculator = new SimpleNextNoteCalculator();
        mGraffitiData = GraffitiData.generateDefault();

        mDrawingLayer = null;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            GrafittiView child = (GrafittiView) getChildAt(i);
            child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 1, Update data <br>
     * 2, Notify view updating
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final float pointX = event.getX();
        final float pointY = event.getY();
        // Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mDrawingLayer != null) {
                    throw new RuntimeException("How could mDrawingLayer not be null !");
                }
                mDrawingLayer = mGraffitiData.getDrawingLayer();
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
            GrafittiView child = (GrafittiView) getChildAt(i);
            child.layout(l, t, r, b);
        }
    }

    /**
     * @param data
     */
    public void notifyDataChanged(GraffitiData data) {
        for (GraffitiLayerData layerData : data.getLayers()) {
            notifyDataChanged(data);
        }
    }

    public void notifyDataChanged(GraffitiLayerData layerData) {
        GraffitiLayerView view = findViewWithTag(layerData);
        if (findViewWithTag(layerData) == null) {
            view = new GraffitiLayerView(getContext(), layerData);
            view.setTag(layerData);
            addView(view);
        } else {
            view.notifyDataChanged();
        }
    }

}
