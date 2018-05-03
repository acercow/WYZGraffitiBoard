package com.bfc.wyzgraffitiboard.view.data;

import android.graphics.RectF;

import com.bfc.wyzgraffitiboard.bean.GraffitiNoteBean;

/**
 * Created by fishyu on 2018/5/2.
 */
public class GraffitiNoteDataObject {

    private GraffitiLayerDataObject mLayerData;

    static final RectF RECTF_EMPTY = new RectF();

    /**
     * 是否渲染
     */
    public boolean mDrawn = false;

    private RectF mOriginalRectF;
    private RectF mCalculateRectF;

    /**
     * @param layerData
     * @param bean
     */
    public GraffitiNoteDataObject(GraffitiLayerDataObject layerData, GraffitiNoteBean bean) {
        this(layerData, layerData.mCoordinateConverter.convertWidthPercentageToPixel(bean.mPercentageX), layerData.mCoordinateConverter.convertHeightPercentageToPixel(bean.mPercentageY));
    }

    public GraffitiNoteDataObject(GraffitiLayerDataObject layerData, float centerX, float centerY) {
        mLayerData = layerData;
        mOriginalRectF = new RectF(
                centerX - layerData.getNoteWidth() / 2,
                centerY - layerData.getNoteHeight() / 2,
                centerX + layerData.getNoteWidth() / 2,
                centerY + layerData.getNoteHeight() / 2
        );
        mCalculateRectF = new RectF(mOriginalRectF);
        mDrawn = false;
    }

    public RectF getOriginalRectF() {
        return mOriginalRectF;
    }

    public RectF getCalculateRectF() {
        return mLayerData.mAnimator == null ? getOriginalRectF() : mLayerData.mAnimator.getAnimateRectF(getOriginalRectF(), mCalculateRectF);
    }

    public GraffitiLayerDataObject getLayerData() {
        return mLayerData;
    }

    @Override
    public String toString() {
        return "[mDrawn:" + mDrawn + ",mOriginalRectF:" + mOriginalRectF + "]";
    }
}
