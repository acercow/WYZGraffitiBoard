package com.bfc.wyzgraffitiboard.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/4/28.
 */

public class GraffitiData {

    public static final boolean mOptimizeMergeLayer = true;

    /**
     * 图层
     */
    public List<GraffitiLayerData> mLayers = new ArrayList<>();


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
    private void addLayer(GraffitiLayerData data) {
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
    public void removeLayer(GraffitiLayerData data) {
        mLayers.remove(data);
    }


    public List<GraffitiLayerData> getLayers() {
        return mLayers;
    }


    public static final GraffitiData generateDefault() {
        return new GraffitiData();
    }


    public GraffitiLayerData getDrawingLayer() {
        if (mOptimizeMergeLayer) {
            int size = getLayerCount();
            for (int i = size - 1; i >= 0; i--) {
                GraffitiLayerData layerData = getLayers().get(i);
                if (layerData.mergeAble()) {
                    return layerData;
                }
            }
        }
        GraffitiLayerData layerData = GraffitiLayerData.generateDefault();
        addLayer(layerData);
        return layerData;
    }


}
