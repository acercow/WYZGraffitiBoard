package com.bfc.wyzgraffitiboard.view.data;

import com.bfc.wyzgraffitiboard.bean.GraffitiLayerBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/4/28.
 */

public class GraffitiDataObject {

    public static final boolean mOptimizeMergeLayer = false;

    /**
     * 图层
     */
    public List<GraffitiLayerDataObject> mLayers = new ArrayList<>();


    /**
     * Getting count of Layers
     *
     * @return
     */
    public int getLayerCount() {
        return mLayers == null ? 0 : mLayers.size();
    }


    /**
     * Adding layer
     *
     * @param data
     */
    private void addLayer(GraffitiLayerDataObject data) {
        if (data == null) {
            return;
        }
        if (mLayers.contains(data)) {
            throw new IllegalArgumentException("layer already in GraffitiData");
        }
        mLayers.add(data);
    }


    /**
     * Removing layer
     *
     * @param data
     */
    public void removeLayer(GraffitiLayerDataObject data) {
        mLayers.remove(data);
    }


    public List<GraffitiLayerDataObject> getLayers() {
        return mLayers;
    }


    public static final GraffitiDataObject generateDefault() {
        return new GraffitiDataObject();
    }


    public GraffitiLayerDataObject getDrawingLayer() {
        if (mOptimizeMergeLayer) {
            int size = getLayerCount();
            for (int i = size - 1; i >= 0; i--) {
                GraffitiLayerDataObject layerData = getLayers().get(i);
                if (layerData.isMergeAble()) {
                    return layerData;
                }
            }
        }
        GraffitiLayerDataObject layerData = new GraffitiLayerDataObject(GraffitiLayerBean.buildTest());
        addLayer(layerData);
        return layerData;
    }


}
