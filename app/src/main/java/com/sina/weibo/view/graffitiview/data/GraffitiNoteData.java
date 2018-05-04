package com.sina.weibo.view.graffitiview.data;

import android.graphics.RectF;

import com.sina.weibo.view.graffitiview.bean.GraffitiNoteBean;

/**
 * Created by fishyu on 2018/5/2.
 */
public class GraffitiNoteData {

    private GraffitiLayerData mLayerData;

    /**
     * Whether we are drawn or not
     */
    public boolean mDrawn;

    private RectF mOriginalRectF;
    private RectF mCalculateRectF;

    /**
     * @param layerData
     * @param bean
     */
    public GraffitiNoteData(GraffitiLayerData layerData, GraffitiNoteBean bean) {
        this(layerData, layerData.mCoordinateConverter.convertWidthPercentageToPixel(bean.getPercentageX()), layerData.mCoordinateConverter.convertHeightPercentageToPixel(bean.getPercentageY()));
    }

    public GraffitiNoteData(GraffitiLayerData layerData, float centerX, float centerY) {
        if (layerData == null) {
            throw new IllegalArgumentException("layerData can not be null !");
        }
        if (layerData.getCoordinateConverter() == null) {
            throw new IllegalArgumentException("has GraffitiLayerData called #installView(viewWidth,viewHeight) yet? ICoordinateConverter has not been installed yet.");
        }
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

    public GraffitiLayerData getLayerData() {
        return mLayerData;
    }

    @Override
    public String toString() {
        return "[mDrawn:" + mDrawn + ",mOriginalRectF:" + mOriginalRectF + "]";
    }

}
