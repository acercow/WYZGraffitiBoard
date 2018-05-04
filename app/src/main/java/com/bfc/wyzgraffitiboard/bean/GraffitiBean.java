package com.bfc.wyzgraffitiboard.bean;

import com.bfc.wyzgraffitiboard.view.data.GraffitiData;
import com.bfc.wyzgraffitiboard.view.data.GraffitiLayerData;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

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

    public GraffitiBean(GraffitiData graffitiData) {
        mLayers = new ArrayList<>();

        for (GraffitiLayerData layerData : graffitiData.getLayers()) {
            GraffitiLayerBean bean = GraffitiLayerBean.buildFrom(layerData);
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
