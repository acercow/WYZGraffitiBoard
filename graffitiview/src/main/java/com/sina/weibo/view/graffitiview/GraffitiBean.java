package com.sina.weibo.view.graffitiview;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/5/7.
 * <p>
 * Data beans
 * <p>
 * {@link GraffitiBean} -> {@link GraffitiView.GraffitiData}
 * {@link GraffitiLayerBean} -> {@link GraffitiView.GraffitiData.GraffitiLayerData}
 * {@link GraffitiLayerBean.GraffitiNoteBean} -> {@link GraffitiView.GraffitiData.GraffitiLayerData.GraffitiNoteData}
 * <p>
 * <p>
 * <p>
 * PS:因为跟IOS理解上的差异，实现上 Android 比 IOS 多了一层内部 Layer。简单说 IOS 所有类型的 礼物画在同一Layer（目前IOS只有一个Layer），
 * 而Android只有一样的礼物才可能画在同一Layer。
 */
public class GraffitiBean implements Serializable {

    private static final Gson GSON = new Gson();

    @SerializedName("node")
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
    private float drawDeviceWidth = ReferenceCanvasWidth; //图层宽度

    /**
     * ios需要 对应的画布高度
     */
    @SerializedName("height")
    private float drawDeviceHeight = ReferenceCanvasHeight; //图层高度


    private transient int maxNoteNumber;

    /**
     * {@link GraffitiLayerBean} 's reference canvas drawDeviceWidth/devices drawDeviceWidth
     * <p>
     * 标注图画布宽度
     */
    public transient static final float ReferenceCanvasWidth = 375.0f;

    /**
     * {@link GraffitiLayerBean} 's reference canvas drawDeviceHeight/devices drawDeviceHeight
     * <p>
     * 标注图画布高度
     */
    public transient static final float ReferenceCanvasHeight = 332.0f;

    public GraffitiBean() {

    }

    @Override
    public String toString() {
        return "[" +
                "type:" + type +
                ",ReferenceCanvasWidth:" + ReferenceCanvasWidth +
                ",ReferenceCanvasHeight:" + ReferenceCanvasWidth +
                ",maxNoteNumber:" + maxNoteNumber +
                ",drawDeviceWidth:" + drawDeviceWidth +
                ",drawDeviceHeight:" + drawDeviceHeight +
                ",mLayers:" + mLayers +
                "]";
    }

    public GraffitiBean(GraffitiView.GraffitiData graffitiData) {
        this();
        drawDeviceWidth = graffitiData.getCanvasWidth();
        drawDeviceHeight = graffitiData.getCanvasHeight();
        mLayers = new ArrayList<>();
        maxNoteNumber = graffitiData.getNoteMaxNumber();
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

    public static GraffitiBean fromJson(String jsonObject) {
        return GSON.fromJson(jsonObject, GraffitiBean.class);
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
        private String id = "123"; //礼物id

        private transient final static float mReferenceNoteWidth = 38.0f;

        private transient final static float mReferenceNoteHeight = 38.0f;

        private transient final static float mReferenceNoteDistance = 36.0f;

        @SerializedName("url")
        private String mNoteDrawableRes;

        private transient final static int mAnimation = 5;

        private transient final static int mAnimationDuration = 1000;

        @SerializedName("points")
        private List<GraffitiNoteBean> mNotes;

        /**
         * Gift Bean
         */
        private transient int mGoldCoin; //用户购买小咖币数量

        public GraffitiLayerBean() {

        }

        /**
         * Build buildFrom {@link GraffitiView.GraffitiData.GraffitiLayerData}
         *
         * @param layerData
         */
        public GraffitiLayerBean(GraffitiView.GraffitiData.GraffitiLayerData layerData) {
            this();
            GraffitiBean.GraffitiLayerBean bean = layerData.getGraffitiLayerBean();
            id = bean.id;
            mNoteDrawableRes = bean.mNoteDrawableRes;
            mGoldCoin = bean.mGoldCoin;
            mNotes = new ArrayList<>();
            for (GraffitiView.GraffitiData.GraffitiLayerData.GraffitiNoteData noteData : layerData.getNotes()) {
                GraffitiBean.GraffitiLayerBean.GraffitiNoteBean b = new GraffitiBean.GraffitiLayerBean.GraffitiNoteBean(noteData);
                mNotes.add(b);
            }
        }

        public String getId() {
            return id;
        }

        public float getPercentageNoteWidth() {
            return mReferenceNoteWidth;
        }

        public float getPercentageNoteHeight() {
            return mReferenceNoteHeight;
        }

        public float getPercentageNoteDistance() {
            return mReferenceNoteDistance;
        }

        private String getNoteDrawableRes() {
            if (mNoteDrawableRes == null) {
                int p = (int) (System.currentTimeMillis() % 2);
                mNoteDrawableRes = mTestUrls.get(p);
            }
            return mNoteDrawableRes;
        }

        public String getNoteBitmapId() {
            return getNoteDrawableRes();
        }

        public List<GraffitiNoteBean> getNotes() {
            return mNotes;
        }

        public int getAnimation() {
            return mAnimation;
        }

        public long getAnimationDuration() {
            return mAnimationDuration;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setNoteDrawableRes(String noteDrawableRes) {
            this.mNoteDrawableRes = noteDrawableRes;
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
                if (!TextUtils.isEmpty(id) && id.equals(((GraffitiBean.GraffitiLayerBean) obj).getId())) {
                    return true;
                }
            }
            return super.equals(obj);
        }

        @Override
        public String toString() {
            return "[" +
                    "id:" + id +
                    ",mReferenceNoteWidth:" + mReferenceNoteWidth +
                    ",mReferenceNoteHeight:" + mReferenceNoteHeight +
                    ",mReferenceNoteDistance:" + mReferenceNoteDistance +
                    ",mNoteDrawableRes:" + mNoteDrawableRes +
                    ",mAnimation:" + mAnimation +
                    ",mNotes:" + mNotes +
                    "]";
        }


        public static final List<String> mTestUrls = new ArrayList<>();

        static {
            mTestUrls.add("https://alcdn.img.xiaoka.tv/20180315/25f/de5/0/25fde524fdc897b572691ea9d9375367.png");
            mTestUrls.add("https://alcdn.img.xiaoka.tv/20180315/79f/000/0/79f0009b39aae27b2a84b6936c9b2ad8.png");
        }

        public static GraffitiBean.GraffitiLayerBean buildTest() {
            GraffitiBean.GraffitiLayerBean bean = new GraffitiBean.GraffitiLayerBean();
            int p = (int) (System.currentTimeMillis() % 2);
            bean.mNoteDrawableRes = mTestUrls.get(p);
            bean.id = String.valueOf(p);
            return bean;
        }


        public class GraffitiNoteBean implements Serializable {

            @SerializedName("x")
            private float mDeviceX;

            @SerializedName("y")
            private float mDeviceY;

            public GraffitiNoteBean() {
                // do nothing by default
            }

            public GraffitiNoteBean(GraffitiView.GraffitiData.GraffitiLayerData.GraffitiNoteData noteData) {
                this();
                mDeviceX = noteData.getOriginalRectF().centerX();
                mDeviceY = noteData.getOriginalRectF().centerY();

                // convert values
                mDeviceX = noteData.getCoordinateConverter().convertWidthPixelToTarget(mDeviceX);
                mDeviceY = noteData.getCoordinateConverter().convertHeightPixelToTarget(mDeviceY);
            }


            /**
             * DEVICE descriptions. See {@link GraffitiView} 's doc.
             *
             * @return device x
             */
            public float getDeviceX() {
                return mDeviceX;
            }

            /**
             * DEVICE descriptions. See {@link GraffitiView} 's doc.
             *
             * @return device y
             */
            public float getDeviceY() {
                return mDeviceY;
            }
        }
    }

}