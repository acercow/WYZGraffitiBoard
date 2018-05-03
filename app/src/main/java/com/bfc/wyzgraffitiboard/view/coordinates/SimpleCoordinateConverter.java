package com.bfc.wyzgraffitiboard.view.coordinates;

import android.graphics.RectF;

/**
 * Created by fishyu on 2018/4/28.
 */

public class SimpleCoordinateConverter implements ICoordinateConverter {

    private RectF mTempRectF;

    final float mWidthFactor;
    final float mHeightFactor;

    public SimpleCoordinateConverter(float percentageWidth, float percentageHeight, float viewWidth, float viewHeight) {
        mWidthFactor = percentageWidth / viewWidth;
        mHeightFactor = percentageHeight / viewHeight;
    }

    @Override
    public float convertWidthPixelToPercentage(float widthPixel) {
        return widthPixel * mWidthFactor;
    }

    @Override
    public float convertWidthPercentageToPixel(float widthPercentage) {
        return widthPercentage / mWidthFactor;
    }

    @Override
    public float convertHeightPixelToPercentage(float heightPixel) {
        return heightPixel * mHeightFactor;
    }

    @Override
    public float convertHeightPercentageToPixel(float heightPercentage) {
        return heightPercentage / mHeightFactor;
    }

    @Override
    public RectF convertPercentageToPixel(RectF from, RectF to) {
        if (to == null) {
            to = getTempRectF();
        }
        to.left = convertWidthPercentageToPixel(from.left);
        to.top = convertHeightPercentageToPixel(from.top);
        to.right = convertWidthPercentageToPixel(from.right);
        to.bottom = convertHeightPercentageToPixel(from.bottom);
        return to;
    }

    @Override
    public RectF convertPixelToPercentage(RectF from, RectF to) {
        if (to == null) {
            to = getTempRectF();
        }
        to.left = convertWidthPixelToPercentage(from.left);
        to.top = convertHeightPercentageToPixel(from.top);
        to.right = convertWidthPixelToPercentage(from.right);
        to.bottom = convertHeightPixelToPercentage(from.bottom);
        return to;
    }

    protected RectF getTempRectF() {
        if (mTempRectF == null) {
            mTempRectF = new RectF();
        }
        return mTempRectF;
    }
}
