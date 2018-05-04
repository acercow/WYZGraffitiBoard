package com.bfc.wyzgraffitiboard.bean;

import com.bfc.wyzgraffitiboard.view.data.GraffitiNoteData;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by fishyu on 2018/5/3.
 */

public class GraffitiNoteBean implements Serializable {

    @SerializedName("x")
    private float mPercentageX;

    @SerializedName("y")
    private float mPercentageY;

    public GraffitiNoteBean(GraffitiNoteData noteData) {
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
