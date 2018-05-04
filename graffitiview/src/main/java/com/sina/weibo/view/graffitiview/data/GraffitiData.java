package com.sina.weibo.view.graffitiview.data;

import com.sina.weibo.view.graffitiview.bean.GraffitiBean;
import com.sina.weibo.view.graffitiview.bean.GraffitiLayerBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * <p>
 * Concepts: <br>
 * 1, READ-MODE: ie printing notes to board.
 * 2, WRITE-MODE: ie painting notes to board.
 */

public class GraffitiData {


    /**
     * 图层
     */
    public List<GraffitiLayerData> mLayers = new ArrayList<>();

    private GraffitiBean mGraffitiBean;


    /**
     * Constructor from {@link GraffitiBean}, which should be installed later checked by {@link #isShowMode()}
     *
     * @param graffitiBean
     */
    public GraffitiData(GraffitiBean graffitiBean) {
        mGraffitiBean = graffitiBean;
        for (GraffitiLayerBean bean : graffitiBean.getmLayers()) {
            GraffitiLayerData layerData = new GraffitiLayerData(bean);
            mLayers.add(layerData);
        }
    }


    /**
     * Default
     */
    public GraffitiData() {

    }


    /**
     * Whether or not we are in SHOW-MODE or note.
     * <p>
     * For some details with SHOW-MODE see {@link GraffitiData}
     *
     * @return
     */
    public boolean isShowMode() {
        return mGraffitiBean != null;
    }


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

    /**
     * Returning drawing layer
     *
     * @param layerBean
     * @return
     */
    public GraffitiLayerData getDrawingLayer(GraffitiLayerBean layerBean) {
        GraffitiLayerData layerData = new GraffitiLayerData(layerBean);
        addLayer(layerData);
        return layerData;
    }


}
