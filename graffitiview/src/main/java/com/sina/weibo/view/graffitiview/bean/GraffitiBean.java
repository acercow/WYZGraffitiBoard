package com.sina.weibo.view.graffitiview.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.sina.weibo.view.graffitiview.data.GraffitiData;
import com.sina.weibo.view.graffitiview.data.GraffitiLayerData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/5/3.
 */

public class GraffitiBean implements Serializable {

    static Gson GSON = new Gson();


    @SerializedName("layers")
    private List<GraffitiLayerBean> mLayers;


    public GraffitiBean() {

    }


    public GraffitiBean(GraffitiData graffitiData) {
        this();
        mLayers = new ArrayList<>();

        for (GraffitiLayerData layerData : graffitiData.getLayers()) {
            GraffitiLayerBean bean = new GraffitiLayerBean(layerData);
            mLayers.add(bean);
        }
    }

    /**
     * convert to string
     *
     * @return
     */
    public String toJson() {
        return GSON.toJson(this);
    }

    /**
     * Getting all {@link GraffitiLayerBean}
     *
     * @return
     */
    public List<GraffitiLayerBean> getmLayers() {
        return mLayers;
    }

}
