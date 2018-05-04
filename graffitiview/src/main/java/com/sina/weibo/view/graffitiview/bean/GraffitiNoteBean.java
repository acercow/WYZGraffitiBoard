package com.sina.weibo.view.graffitiview.bean;

import com.google.gson.annotations.SerializedName;
import com.sina.weibo.view.graffitiview.data.GraffitiNoteData;

import java.io.Serializable;

/**
 * Created by fishyu on 2018/5/3.
 */

public class GraffitiNoteBean implements Serializable {

    @SerializedName("x")
    private float mPercentageX;

    @SerializedName("y")
    private float mPercentageY;

    public GraffitiNoteBean() {
    }

    public GraffitiNoteBean(GraffitiNoteData noteData) {
        this();
        mPercentageX = noteData.getLayerData().getCoordinateConverter().convertWidthPixelToPercentage(noteData.getOriginalRectF().centerX());
        mPercentageY = noteData.getLayerData().getCoordinateConverter().convertHeightPixelToPercentage(noteData.getOriginalRectF().centerY());
    }

    public float getPercentageX() {
        return mPercentageX;
    }

    public float getPercentageY() {
        return mPercentageY;
    }
}
