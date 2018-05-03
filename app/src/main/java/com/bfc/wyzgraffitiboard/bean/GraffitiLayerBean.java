package com.bfc.wyzgraffitiboard.bean;

import com.bfc.wyzgraffitiboard.R;

import java.util.List;

/**
 * Created by fishyu on 2018/5/3.
 */

public class GraffitiLayerBean {

    private String id; //礼物id

    private float mPercentageCanvasWidth = 100.0f; //画布宽度
    private float mPercentageCanvasHeight = 100.0f; //画布高度

    private float mPercentageNoteWidth = 6.0f;

    private float mPercentageNoteHeight = 6.0f;

    private float mPercentageNoteDistance = 14.0f;

    private int mNoteType; // 0手绘 1图片 2其他

    private int mNoteDrawableRes = R.drawable.shield_icon;

    private int mAnimation;

    private List<GraffitiNoteBean> mNotes;

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

    public static final GraffitiLayerBean buildTest() {
        GraffitiLayerBean bean = new GraffitiLayerBean();
        bean.mAnimation = 1;
        return bean;
    }

}
