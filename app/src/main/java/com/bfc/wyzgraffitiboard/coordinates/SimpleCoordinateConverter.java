package com.bfc.wyzgraffitiboard.coordinates;

import android.graphics.RectF;
import android.view.View;

import com.bfc.wyzgraffitiboard.data.GraffitiLayerData;

/**
 * Created by fishyu on 2018/4/28.
 */

public class SimpleCoordinateConverter implements ICoordinateConverter {

    private int mViewWidth;
    private int mViewHeight;

    private RectF mValueToReturn;

    final float mWidthFactor;
    final float mHeightFactor;

    public SimpleCoordinateConverter(GraffitiLayerData layerData, View view) {
        mViewWidth = view.getMeasuredWidth();
        mViewHeight = view.getMeasuredHeight();

        mWidthFactor = (float) mViewWidth / layerData.getWidth();
        mHeightFactor = (float) mViewHeight / layerData.getHeight();
    }

    @Override
    public RectF convert(RectF from, RectF to) {
        if (to == null) {
            if (mValueToReturn == null) {
                mValueToReturn = new RectF();
            }
            to = mValueToReturn;
        }

        to.left = from.left * mWidthFactor;
        to.top = from.top * mHeightFactor;

        to.right = from.right * mWidthFactor;
        to.bottom = from.bottom * mHeightFactor;
        return to;
    }
}
