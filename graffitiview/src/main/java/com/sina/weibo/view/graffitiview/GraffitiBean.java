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

    /**
     * 预留
     * <p>
     * type:0; 图层类型，目前只有0，手绘
     */
    @SerializedName("type")
    private int type = 0;

    /**
     * ios需要 对应的画布宽度
     */
    @SerializedName("width")
    private float width; //图层宽度

    /**
     * ios需要 对应的画布高度
     */
    @SerializedName("height")
    private float height; //图层高度

    public GraffitiBean() {

    }


    public GraffitiBean(GraffitiView.GraffitiData graffitiData) {
        this();
        mLayers = new ArrayList<>();
        for (GraffitiView.GraffitiData.GraffitiLayerData layerData : graffitiData.getLayers()) {
            GraffitiBean.GraffitiLayerBean bean = new GraffitiBean.GraffitiLayerBean(layerData);
            mLayers.add(bean);
        }
        initializeNotesFromPercentage(graffitiData);
    }

    /**
     * For ios
     * <p>
     * Convert all notes to percentage coordinate
     */
    public void initializeNotesToPercentage() {
        if (width != 0 && height != 0) {
            if (mLayers != null) {
                for (GraffitiLayerBean layerBean : mLayers) {
                    if (layerBean.mNotes != null) {
                        for (GraffitiLayerBean.GraffitiNoteBean noteBean : layerBean.mNotes) {
                            if (noteBean.mPercentageX > 1 || noteBean.mPercentageY > 1) {
                                noteBean.mPercentageX = noteBean.mPercentageX / width;
                                noteBean.mPercentageY = noteBean.mPercentageY / height;
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * For ios
     * <p>
     * Convert all notes to percentage coordinate
     */
    public void initializeNotesFromPercentage(GraffitiView.GraffitiData graffitiData) {
        if (mLayers != null && mLayers.size() > 0) {
            if (width <= 0 || height <= 0) {
                width = graffitiData.getLastLayer().getCanvasWidth();
                height = graffitiData.getLastLayer().getCanvasHeight();
            }
            for (GraffitiLayerBean bean : mLayers) {
                if (bean.mNotes != null) {
                    for (GraffitiLayerBean.GraffitiNoteBean noteBean : bean.mNotes) {
                        if (noteBean.mPercentageX <= 1 || noteBean.mPercentageY <= 1) {
                            noteBean.mPercentageX = noteBean.mPercentageX * width;
                            noteBean.mPercentageY = noteBean.mPercentageY * height;
                        }
                    }
                }
            }
        }
    }

    /**
     * Initialize for IOS
     *
     * @param layerData
     */
    private void initializeForIOS(GraffitiView.GraffitiData.GraffitiLayerData layerData) {
        if (width <= 0 || height <= 0) {
            width = layerData.getCanvasWidth();
            height = layerData.getCanvasHeight();
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
    public List<GraffitiLayerBean> getLayers() {
        return mLayers;
    }


    public static class GraffitiLayerBean implements Serializable {

        @SerializedName("gift_id")
        private String id; //礼物id

        @SerializedName("canvas_width")
        private float mPercentageCanvasWidth = 375.0f; //画布宽度

        @SerializedName("canvas_height")
        private float mPercentageCanvasHeight = 375.0f; //画布高度

        @SerializedName("width")
        private float mPercentageNoteWidth = 38.0f;

        @SerializedName("height")
        private float mPercentageNoteHeight = 38.0f;

        @SerializedName("distance")
        private float mPercentageNoteDistance = 39.0f;

        @SerializedName("url")
        private String mNoteDrawableRes;

        @SerializedName("animation")
        private int mAnimation;

        @SerializedName("note")
        private List<GraffitiNoteBean> mNotes;

        private int mGoldCoin; //用户购买小咖币数量

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

        public String getNoteDrawableRes() {
            return mNoteDrawableRes;
        }

        public List<GraffitiNoteBean> getNotes() {
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

        public void setNoteDrawableRes(String noteDrawableRes) {
            this.mNoteDrawableRes = noteDrawableRes;
        }

        public void setAnimation(int animation) {
            this.mAnimation = animation;
        }

        public void setNotes(List<GraffitiNoteBean> notes) {
            this.mNotes = notes;
        }

        public int getGoldCoin() {
            return mGoldCoin;
        }

        public void setGoldCoin(int goldcoin) {
            this.mGoldCoin = goldcoin;
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
                    ",mAnimation:" + mAnimation +
                    ",mNotes:" + mNotes +
                    "]";
        }


        public static List<String> mTestUrls = new ArrayList();

        static {
            mTestUrls.add("https://alcdn.img.xiaoka.tv/20180315/25f/de5/0/25fde524fdc897b572691ea9d9375367.png");
            mTestUrls.add("https://alcdn.img.xiaoka.tv/20180315/79f/000/0/79f0009b39aae27b2a84b6936c9b2ad8.png");
            mTestUrls.add(" https://alcdn.img.xiaoka.tv/20180322/6f6/225/0/6f62255f7dca9be5ccacf52dcd66056f.png");
        }

        public static final GraffitiBean.GraffitiLayerBean buildTest() {
            GraffitiBean.GraffitiLayerBean bean = new GraffitiBean.GraffitiLayerBean();
            bean.mAnimation = -1;
            int p = (int) (System.currentTimeMillis() % 3);
            bean.mNoteDrawableRes = mTestUrls.get(p);
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