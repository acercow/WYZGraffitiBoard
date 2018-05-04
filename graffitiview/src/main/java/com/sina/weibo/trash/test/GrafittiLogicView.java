package com.sina.weibo.trash.test;

import android.content.Context;
import android.util.AttributeSet;

import com.sina.weibo.view.graffitiview.GraffitiView;
import com.sina.weibo.view.graffitiview.data.GraffitiLayerData;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fishyu on 2018/5/2.
 */

public class GrafittiLogicView extends GraffitiView {

    private Map<GraffitiLayerData, GraffitiLayerLogicView> mViews = new HashMap<>();

    public GrafittiLogicView(Context context) {
        super(context);
    }

    public GrafittiLogicView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GrafittiLogicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void notifyDataChanged(GraffitiLayerData layerData) {
        GraffitiLayerLogicView view = mViews.get(layerData);
        if (view == null) {
            view = new GraffitiLayerLogicView(getContext(), layerData);
            getLogicViewGroup(layerData).addView(view);
            mViews.put(layerData, view);
        } else {
            view.notifyDataChanged();
        }
    }

    protected GraffitiLayerLogicViewGroup getLogicViewGroup(GraffitiLayerData layerData) {
        if (layerData.isMergeAble(layerData)) {
            if (getChildCount() > 0) {
                GraffitiLayerLogicViewGroup viewGroup = (GraffitiLayerLogicViewGroup) getChildAt(getChildCount() - 1);
                GraffitiLayerLogicView lastChild = viewGroup.getLastChild();
                if (lastChild != null && lastChild.getLayerData().isMergeAble(layerData)) {
                    return viewGroup;
                }
            }
        }
        GraffitiLayerLogicViewGroup viewGroup = new GraffitiLayerLogicViewGroup(getContext());
        addView(viewGroup);
        return viewGroup;
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mViews.clear();
    }
}
