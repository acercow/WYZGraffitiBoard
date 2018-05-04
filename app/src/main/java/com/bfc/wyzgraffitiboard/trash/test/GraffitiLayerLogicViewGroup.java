package com.bfc.wyzgraffitiboard.trash.test;

import android.content.Context;
import android.graphics.Canvas;

import com.bfc.wyzgraffitiboard.trash.logic.LogicViewGroup;

/**
 * Created by fishyu on 2018/5/2.
 */

public class GraffitiLayerLogicViewGroup extends LogicViewGroup {

    public GraffitiLayerLogicViewGroup(Context context) {
        super(context);
    }


    public GraffitiLayerLogicView getLastChild() {
        if (getChildCount() > 0) {
            GraffitiLayerLogicView view = (GraffitiLayerLogicView) getChildAt(getChildCount() - 1);
            return view;
        }
        return null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
