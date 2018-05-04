package com.sina.weibo.view.graffitiview.bean;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.sina.weibo.view.graffitiview.R;
import com.sina.weibo.view.graffitiview.data.GraffitiLayerData;
import com.sina.weibo.view.graffitiview.data.GraffitiNoteData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/5/3.
 */

public class GraffitiLayerBean implements Serializable {

    @SerializedName("id")
    private String id; //礼物id

    @SerializedName("canvas_width")
    private float mPercentageCanvasWidth = 100.0f; //画布宽度

    @SerializedName("canvas_height")
    private float mPercentageCanvasHeight = 100.0f; //画布高度

    @SerializedName("width")
    private float mPercentageNoteWidth = 6.0f;

    @SerializedName("height")
    private float mPercentageNoteHeight = 6.0f;

    @SerializedName("distance")
    private float mPercentageNoteDistance = 14.0f;

    @SerializedName("type")
    private int mNoteType; // 0手绘 1图片 2其他

    @SerializedName("url")
    private int mNoteDrawableRes = R.drawable.shield_icon;

    @SerializedName("animation")
    private int mAnimation;

    @SerializedName("notes")
    private List<GraffitiNoteBean> mNotes;

    public GraffitiLayerBean() {

    }

    /**
     * Build buildFrom {@link GraffitiLayerData}
     *
     * @param layerData
     * @return
     */
    public GraffitiLayerBean(GraffitiLayerData layerData) {
        this();
        GraffitiLayerBean bean = layerData.getGraffitiLayerBean();
        this.id = bean.id;
        this.mPercentageCanvasWidth = bean.mPercentageCanvasWidth;
        this.mPercentageCanvasHeight = bean.mPercentageCanvasHeight;
        this.mPercentageNoteWidth = bean.mPercentageNoteWidth;
        this.mPercentageNoteHeight = bean.mPercentageNoteHeight;
        this.mPercentageNoteDistance = bean.mPercentageNoteDistance;
        this.mNoteType = bean.mNoteType;
        this.mNoteDrawableRes = bean.mNoteDrawableRes;
        this.mAnimation = bean.mAnimation;
        mNotes = new ArrayList<>();
        for (GraffitiNoteData noteData : layerData.getNotes()) {
            GraffitiNoteBean b = new GraffitiNoteBean(noteData);
            mNotes.add(b);
        }
    }


    public String getId() {
        return id;
    }

    public float getPercentageCanvasWidth() {
        return mPercentageCanvasWidth;
    }

    public float getPercentageCanvasHeight() {
        return mPercentageCanvasHeight;
    }

    public float getPercentageNoteWidth() {
        return mPercentageNoteWidth;
    }

    public float getPercentageNoteHeight() {
        return mPercentageNoteHeight;
    }

    public float getPercentageNoteDistance() {
        return mPercentageNoteDistance;
    }

    public int getNoteType() {
        return mNoteType;
    }

    public int getNoteDrawableRes() {
        return mNoteDrawableRes;
    }

    public List<GraffitiNoteBean> getNotes() {
        return mNotes;
    }

    public int getAnimation() {
        return mAnimation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GraffitiLayerBean) {
            if (TextUtils.isEmpty(id) && id.equals(((GraffitiLayerBean) obj).getId())) {
                return true;
            }
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return "[" +
                "id:" + id +
                "mPercentageCanvasWidth:" + mPercentageCanvasWidth +
                ",mPercentageCanvasWidth:" + mPercentageCanvasWidth +
                ",mPercentageNoteWidth:" + mPercentageNoteWidth +
                ",mPercentageNoteHeight:" + mPercentageNoteHeight +
                ",mPercentageNoteDistance:" + mPercentageNoteDistance +
                ",mNoteDrawableRes:" + mNoteDrawableRes +
                ",mNoteType:" + mNoteType +
                ",mAnimation:" + mAnimation +
                ",mNotes:" + mNotes +
                "]";
    }

    public static final GraffitiLayerBean buildTest() {
        GraffitiLayerBean bean = new GraffitiLayerBean();
        bean.mAnimation = 1;
        return bean;
    }
}
