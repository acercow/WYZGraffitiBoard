package com.bfc.wyzgraffitiboard.animation;

import android.graphics.RectF;

import com.bfc.wyzgraffitiboard.data.GraffitiLayerData;
import com.bfc.wyzgraffitiboard.view.GraffitiLayerView;

/**
 * Created by fishyu on 2018/5/2.
 */

public class ScaleAnimator extends AbstractBaseAnimator {


    public ScaleAnimator(GraffitiLayerData data, GraffitiLayerView view, long duration, float from, float to) {
        super(data, view, duration, from, to);
    }

    @Override
    protected RectF onCalculateRectF(RectF input, RectF out, float currentValue) {
        //TODO 1,TransFormer 2,Matrix for test

        float width = currentValue * input.width() / 2;
        float height = currentValue * input.height() / 2;
        float centerX = input.centerX();
        float centerY = input.centerY();

        if (out == null) {
            out = new RectF();
        }
        out.left = centerX - width;
        out.top = centerY - height;
        out.right = centerX + width;
        out.bottom = centerX + height;
        return out;
    }

}
