package com.bfc.wyzgraffitiboard.data;

import android.graphics.RectF;

/**
 * Created by fishyu on 2018/5/2.
 */

public class GraffitiNoteData {

    public GraffitiLayerData mLayerData;

    /**
     * 是否渲染
     */
    public boolean mDrawn = false;

    private RectF mOriginalRectF;
    private RectF mCalculateRectF;

    public GraffitiNoteData(GraffitiLayerData layerData, float centerX, float centerY) {
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
}
