package com.sina.weibo.view.graffitiview;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/5/7.
 */

public class GraffitiBean implements Serializable {

    static Gson GSON = new Gson();


    @SerializedName("layer")
    private List<GraffitiLayerBean> mLayers;

    public GraffitiBean() {

    }


    public GraffitiBean(GraffitiView.GraffitiData graffitiData) {
        this();
        mLayers = new ArrayList<>();
        for (GraffitiView.GraffitiData.GraffitiLayerData layerData : graffitiData.getLayers()) {
            GraffitiBean.GraffitiLayerBean bean = new GraffitiBean.GraffitiLayerBean(layerData);
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
     * Getting all {@link GraffitiBean.GraffitiLayerBean}
     *
     * @return
     */
    public List<GraffitiBean.GraffitiLayerBean> getLayers() {
        return mLayers;
    }


    public static class GraffitiLayerBean implements Serializable {

        @SerializedName("gift_id")
        private String id; //礼物id

        @SerializedName("canvas_width")
        private float mPercentageCanvasWidth = 1.0f; //画布宽度

        @SerializedName("canvas_height")
        private float mPercentageCanvasHeight = 1.0f; //画布高度

        @SerializedName("width")
        private float mPercentageNoteWidth = 0.06f;

        @SerializedName("height")
        private float mPercentageNoteHeight = 0.06f;

        @SerializedName("distance")
        private float mPercentageNoteDistance = 0.14f;

        @SerializedName("type")
        private int mNoteType; // 0手绘 1图片 2其他

        @SerializedName("url")
        private int mNoteDrawableRes = R.drawable.shield_icon;

        @SerializedName("animation")
        private int mAnimation;

        @SerializedName("note")
        private List<GraffitiBean.GraffitiLayerBean.GraffitiNoteBean> mNotes;

        public GraffitiLayerBean() {

        }

        /**
         * Build buildFrom {@link GraffitiView.GraffitiData.GraffitiLayerData}
         *
         * @param layerData
         * @return
         */
        public GraffitiLayerBean(GraffitiView.GraffitiData.GraffitiLayerData layerData) {
            this();
            GraffitiBean.GraffitiLayerBean bean = layerData.getGraffitiLayerBean();
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
            for (GraffitiView.GraffitiData.GraffitiLayerData.GraffitiNoteData noteData : layerData.getNotes()) {
                GraffitiBean.GraffitiLayerBean.GraffitiNoteBean b = new GraffitiBean.GraffitiLayerBean.GraffitiNoteBean(noteData);
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

        public List<GraffitiBean.GraffitiLayerBean.GraffitiNoteBean> getNotes() {
            return mNotes;
        }

        public int getAnimation() {
            return mAnimation;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setPercentageCanvasWidth(float percentageCanvasWidth) {
            this.mPercentageCanvasWidth = percentageCanvasWidth;
        }

        public void setPercentageCanvasHeight(float percentageCanvasHeight) {
            this.mPercentageCanvasHeight = percentageCanvasHeight;
        }

        public void setPercentageNoteWidth(float percentageNoteWidth) {
            this.mPercentageNoteWidth = percentageNoteWidth;
        }

        public void setPercentageNoteHeight(float percentageNoteHeight) {
            this.mPercentageNoteHeight = percentageNoteHeight;
        }

        public void setPercentageNoteDistance(float percentageNoteDistance) {
            this.mPercentageNoteDistance = percentageNoteDistance;
        }

        public void setNoteType(int noteType) {
            this.mNoteType = noteType;
        }

        public void setmNoteDrawableRes(int noteDrawableRes) {
            this.mNoteDrawableRes = noteDrawableRes;
        }

        public void setAnimation(int animation) {
            this.mAnimation = animation;
        }

        public void setNotes(List<GraffitiNoteBean> notes) {
            this.mNotes = notes;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof GraffitiBean.GraffitiLayerBean) {
                if (TextUtils.isEmpty(id) && id.equals(((GraffitiBean.GraffitiLayerBean) obj).getId())) {
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

        public static final GraffitiBean.GraffitiLayerBean buildTest() {
            GraffitiBean.GraffitiLayerBean bean = new GraffitiBean.GraffitiLayerBean();
            bean.mAnimation = 1;
            return bean;
        }


        public class GraffitiNoteBean implements Serializable {

            @SerializedName("x")
            private float mPercentageX;

            @SerializedName("y")
            private float mPercentageY;

            @SerializedName("time")
            private long mTime;

            public GraffitiNoteBean() {
                // do nothing by default
            }

            public GraffitiNoteBean(GraffitiView.GraffitiData.GraffitiLayerData.GraffitiNoteData noteData) {
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
    }

}