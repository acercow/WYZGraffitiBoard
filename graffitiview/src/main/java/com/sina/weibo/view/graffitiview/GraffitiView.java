package com.sina.weibo.view.graffitiview;

import android.accounts.NetworkErrorException;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * TWO MODE:(See {@link GraffitiData} for more detail)
 * 1,READ-MODE
 * 2,WRITE-MODE
 */
public class GraffitiView extends ViewGroup {

    static final String TAG = GraffitiView.class.getSimpleName();

    private INextNoteCalculator mNoteCalculator;

    private GraffitiData mGraffitiData;
    private GraffitiData.GraffitiLayerData mDrawingLayer;

    private GraffitiBean.GraffitiLayerBean mDrawObject;

    private ICallback mCallback;

    private ICallback mInternalCallback = new ICallback() {
        @Override
        public void onDataChanged(GraffitiView graffitiView, GraffitiBean.GraffitiLayerBean drawingObject) {
            if (mCallback != null) {
                mCallback.onDataChanged(graffitiView, drawingObject);
            }
        }

        @Override
        public void onMessage(int msg) {
            if (mCallback != null) {
                mCallback.onMessage(msg);
            }
        }
    };

    private Runnable showNotes = new Runnable() {
        @Override
        public void run() {
            if (!GraffitiResourcesManager.isResourceLoaded()) {
                Log.e(TAG, "Resources is not ready, can't show!");
                postDelayed(showNotes, 300);
                return;
            } else if (!GraffitiResourcesManager.isResourcesReady(mGraffitiData.getLayerResources())) {
                Log.e(TAG, "Necessary resources is not ready, can't show!");
                postDelayed(showNotes, 300);
                return;
            }
            notifyDataChanged();
        }
    };

    public GraffitiView(Context context) {
        super(context);
    }

    public GraffitiView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GraffitiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化
     *
     * @param initData READ-MODE must not be null, otherwise would auto to be WRITE-MODE.
     */
    public void installData(GraffitiData initData) {
        if (initData == null) {
            initData = GraffitiData.generateDefault();
        }
        mNoteCalculator = new SimpleNextNoteCalculator();
        mGraffitiData = initData;

        mDrawingLayer = null;
        // set view's information if needed
        onSizeChanged(getMeasuredWidth(), getMeasuredHeight(), 0, 0);

        //reading exits data
        if (initData.isReadMode()) {
            post(showNotes);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // square this view
        heightMeasureSpec = widthMeasureSpec;
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            child.layout(l, t, r, b);
        }
    }

    /**
     * 1, Update data <br>
     * 2, Notify view updating
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            Log.e(TAG, "View has been disabled.");
            mInternalCallback.onMessage(ICallback.MSG_DISABLED);
            return true;
        }

        final float pointX = event.getX();
        final float pointY = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mDrawingLayer != null) {
                throw new RuntimeException("How could mDrawingLayer not be null !");
            }
            if (checkWritable()) {
                mDrawingLayer = mGraffitiData.getDrawingLayer(getCurrentDrawObject());
                mDrawingLayer.installView(getMeasuredWidth(), getMeasuredHeight());
                if (mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, mDrawingLayer.getLast(), pointX, pointY))) {
                    notifyDataChanged(mDrawingLayer, true);
                }
                return true;
            }
        } else if (mDrawingLayer != null) {
            // Checks for the event that occurs
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, mDrawingLayer.getLast(), pointX, pointY))) {
                        notifyDataChanged(mDrawingLayer, true);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, mDrawingLayer.getLast(), pointX, pointY))) {
                        notifyDataChanged(mDrawingLayer, true);
                    }
                    mDrawingLayer = null;
                    return true;
                default:
                    return true;
            }
        }

        return true;
    }

    /**
     * Whether we can write or not.
     *
     * @return
     */
    protected boolean checkWritable() {
        if (mGraffitiData == null) {
            throw new IllegalStateException("You have not call#installData(GraffitiData) to install data.");
        }

        if (mGraffitiData.isReadMode()) {
            Log.e(TAG, "player mode, just show notes");
            mInternalCallback.onMessage(ICallback.MSG_READ_MODE);
            return false;
        }

        if (getMeasuredHeight() == 0 || getMeasuredWidth() == 0) {
            Log.e(TAG, "width or height is 0, getMeasuredWidth() -> " + getMeasuredWidth() + " getMeasuredHeight() -> " + getMeasuredHeight());
            mInternalCallback.onMessage(ICallback.MSG_VIEW_NOT_READY);
            return false;
        }

        if (getCurrentDrawObject() == null) {
            Log.e(TAG, "GraffitiLayerBean is not set, can not draw.");
            mInternalCallback.onMessage(ICallback.MSG_RESOURCE_NOT_READY);
            return false;
        }

        if (!mGraffitiData.isEnableRiskLoadResources() && !GraffitiResourcesManager.isResourceLoaded()) {
            Log.e(TAG, "resources is not ready, nothing to draw.");
            mInternalCallback.onMessage(ICallback.MSG_RESOURCE_NOT_READY);
            return false;
        }

        if (!GraffitiResourcesManager.isResourceReady(getCurrentDrawObject().getNoteDrawableRes())) {
            Log.e(TAG, "GraffitiLayerBean's resources is not ready, can not draw.");
            mInternalCallback.onMessage(ICallback.MSG_RESOURCE_NOT_READY);
            return false;
        }

        return true;
    }

    /**
     * Notify data changed, time to update view
     */
    public void notifyDataChanged() {
        GraffitiData data = mGraffitiData;

        //check view deleted
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            if (!data.getLayers().contains(view.getTag())) {
                //must use handler
                post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataChanged((GraffitiData.GraffitiLayerData) view.getTag(), false);
                    }
                });
            }
        }

        //check view not added
        for (final GraffitiData.GraffitiLayerData layerData : data.getLayers()) {
            if (findViewWithTag(layerData) == null) {
                notifyDataChanged(layerData, false);
            }
        }

        post(new Runnable() {
            @Override
            public void run() {
                mInternalCallback.onDataChanged(GraffitiView.this, null);
            }
        });
    }

    /**
     * Notify data changed, time to update view
     *
     * @param layerData
     */
    public void notifyDataChanged(GraffitiData.GraffitiLayerData layerData, boolean notifyListener) {
        boolean deleted = !mGraffitiData.getLayers().contains(layerData);
        GraffitiLayerView view = findViewWithTag(layerData);
        if (view == null) {
            if (!deleted) {
                view = new GraffitiLayerView(getContext(), layerData);
                view.setTag(layerData);
                addView(view);
            }
        } else {
            if (deleted) {
                removeView(view);
            } else {
                view.notifyDataChanged();
            }
        }

        if (notifyListener) {
            mInternalCallback.onDataChanged(this, layerData.getGraffitiLayerBean());
        }
    }


    /**
     * Returning selected {@link GraffitiBean.GraffitiLayerBean}
     *
     * @return
     */
    protected GraffitiBean.GraffitiLayerBean getCurrentDrawObject() {
        return mDrawObject;
    }

    /**
     * Setting {@link GraffitiBean.GraffitiLayerBean}, with witch to draw.
     *
     * @param layerBean
     */
    public void setDrawObject(GraffitiBean.GraffitiLayerBean layerBean) {
        mDrawObject = layerBean;
    }


    /**
     * Getting internal {@link GraffitiData} witch drives {@link GraffitiView}.
     *
     * @return
     */
    public GraffitiData getGraffitiData() {
        return mGraffitiData;
    }


    /**
     * Setting {@link ICallback}
     *
     * @param callback
     */
    public void setOnDataChangedCallback(ICallback callback) {
        mCallback = callback;
    }

    /**
     * Callbacks when internal data changed.
     */
    public interface ICallback {

        int MSG_DISABLED = 1;

        int MSG_READ_MODE = 2;

        int MSG_VIEW_NOT_READY = 3;

        int MSG_RESOURCE_NOT_READY = 4;

        /**
         * @param graffitiView  GraffitiView itself
         * @param drawingObject Current drawing object set by  {@link GraffitiView#setDrawObject(GraffitiBean.GraffitiLayerBean)}.
         *                      May be null if flush change like stopAndClear or something.
         */
        void onDataChanged(GraffitiView graffitiView, GraffitiBean.GraffitiLayerBean drawingObject);


        /**
         * Notify messages
         *
         * @param msg
         */
        void onMessage(int msg);
    }


    /**
     * Layer view
     */
    private static class GraffitiLayerView extends View {

        final String TAG = GraffitiLayerView.class.getSimpleName() + this;

        private int mBlankCanvas = -1;
        private Bitmap mNoteBitmap;

        public GraffitiLayerView(Context context, GraffitiData.GraffitiLayerData data) {
            super(context);
            setTag(data);

            // installView animator
            getLayerData().installAnimator(new Runnable() {
                @Override
                public void run() {
                    notifyDataChanged();
                }
            });

            mNoteBitmap = getLayerData().getNoteBitmap();
        }


        /**
         * Getting layer data
         *
         * @return
         */
        public GraffitiData.GraffitiLayerData getLayerData() {
            return (GraffitiData.GraffitiLayerData) getTag();
        }


        /**
         * Update view
         */
        public void notifyDataChanged() {
            invalidate();
        }


        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            if (w != oldw || h != oldh) {
                if (w >= 0 && h >= 0) {
                    if (getLayerData().installView(w, h)) {
                        if (getLayerData().isReadMode()) {
                            getLayerData().installNotes();
                            notifyDataChanged();
                        }
                    }
                }
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            getLayerData().startAnimatorIfExits();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            getLayerData().stopAnimatorIfExits();
        }

        @Override
        protected void onWindowVisibilityChanged(int visibility) {
            super.onWindowVisibilityChanged(visibility);
            if (visibility == VISIBLE) {
                getLayerData().startAnimatorIfExits();
            } else {
                getLayerData().stopAnimatorIfExits();
            }
        }

        /**
         * @param canvas
         */
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (mNoteBitmap == null || mNoteBitmap.isRecycled()) {
                Log.e(TAG, "How could note bitmap be null or recycled ?!");
                return;
            }

            if (getLayerData().isForceDrawAll()) {
                //TODO clean the canvas ??
//            Log.e(TAG, " restore canvas to -> " + mBlankCanvas);
            }

            int size = getLayerData().getNotes().size();
            for (int i = size - 1; i >= 0; i--) {
                GraffitiData.GraffitiLayerData.GraffitiNoteData note = getLayerData().getNotes().get(i);
                note.mDrawn = false;
                if (note.mDrawn && !getLayerData().isForceDrawAll()) {
                    break;
                }
                drawNote(canvas, note);
                note.mDrawn = true;
            }

            //if force draw all, reset it's status
            getLayerData().finishForceDrawAll(false);
        }

        /**
         * Called when draw your note
         *
         * @param canvas
         * @param note   Note information
         */
        protected void drawNote(Canvas canvas, GraffitiData.GraffitiLayerData.GraffitiNoteData note) {
            onDrawNote(canvas, note, mNoteBitmap, note.getCalculateRectF());
        }


        /**
         * Do what u want to draw your note
         *
         * @param canvas
         * @param note
         * @param bitmap
         * @param rectF
         */
        protected void onDrawNote(Canvas canvas, GraffitiData.GraffitiLayerData.GraffitiNoteData note, Bitmap bitmap, RectF rectF) {
            canvas.drawBitmap(bitmap, null, rectF, null);
        }

    }


    /* ########################################## Datas ########################################### */

    /**
     * Created by fishyu on 2018/4/28.
     * <p>
     * <p>
     * Concepts: <br>
     * 1, READ-MODE: ie printing notes to board.
     * 2, WRITE-MODE: ie painting notes to board.
     * <p>
     * <p>
     * Current only can cooperate with {@link GraffitiBean}
     */
    public static class GraffitiData {

        /**
         * 图层
         */
        private List<GraffitiLayerData> mLayers = new ArrayList<>();

        private GraffitiBean mSource;


        /**
         * Whether we risk loading resources when {@link GraffitiResourcesManager#isResourceLoaded()} is negative.
         * <p>
         * When enable this, we get faster drawing, but unexcepted touch/read delay may occur sometimes.
         */
        private boolean mEnableRiskLoadResource = true;

        /**
         * Constructor from {@link GraffitiBean}, which should be installed later checked by {@link #isReadMode()}
         *
         * @param graffitiBean
         */
        public GraffitiData(GraffitiBean graffitiBean) {
            mSource = graffitiBean;
            graffitiBean.initializeNotesToPercentage();
            for (GraffitiBean.GraffitiLayerBean bean : graffitiBean.getLayers()) {
                GraffitiData.GraffitiLayerData layerData = new GraffitiData.GraffitiLayerData(bean);
                addLayer(layerData);
            }
        }


        /**
         * Default
         */
        public GraffitiData() {

        }


        /**
         * Whether or not we are in READ-MODE or note.
         * <p>
         * For some details with READ-MODE see {@link GraffitiData}
         *
         * @return
         */
        public boolean isReadMode() {
            return mSource != null;
        }


        public boolean isEnableRiskLoadResources() {
            return mEnableRiskLoadResource;
        }


        public void setEnableRiskLoadResources(boolean value) {
            mEnableRiskLoadResource = value;
        }

        /**
         * Adding layer
         *
         * @param data
         */
        private void addLayer(GraffitiData.GraffitiLayerData data) {
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
        public void removeLayer(GraffitiData.GraffitiLayerData data) {
            if (data != null) {
                mLayers.remove(data);
            }
        }

        /**
         * Getting last layer
         *
         * @return
         */
        public GraffitiLayerData getLastLayer() {
            if (mLayers != null && mLayers.size() > 0) {
                GraffitiLayerData last = mLayers.get(mLayers.size() - 1);
                return last;
            }
            return null;
        }

        /**
         * Clear all layers
         */
        public void clearLayers() {
            mLayers.clear();
        }

        public List<GraffitiLayerData> getLayers() {
            return mLayers;
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
         * Getting total note
         *
         * @return
         */
        public int getCurrentTotalNote() {
            int total = 0;
            for (GraffitiLayerData layerData : mLayers) {
                total += layerData.getNotes().size();
            }
            return total;
        }

        /**
         * Get layer resources
         *
         * @return
         */
        public List getLayerResources() {
            if (mLayers != null && mLayers.size() > 0) {
                List<String> list = new ArrayList<>();
                for (GraffitiLayerData layerData : mLayers) {
                    String url = layerData.getNoteDrawableRes();
                    if (!list.contains(url)) {
                        list.add(url);
                    }
                }
                return list;
            }
            return null;
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
        public GraffitiData.GraffitiLayerData getDrawingLayer(GraffitiBean.GraffitiLayerBean layerBean) {
            GraffitiData.GraffitiLayerData layerData = new GraffitiData.GraffitiLayerData(layerBean);
            addLayer(layerData);
            return layerData;
        }


        public static class GraffitiLayerData {

            static final String TAG = GraffitiData.GraffitiLayerData.class.getSimpleName();

            private GraffitiBean.GraffitiLayerBean mLayerBean;

            private float mCanvasWidth; //画布宽度
            private float mCanvasHeight; //画布高度

            private float mNoteWidth;
            private float mNoteHeight;

            private float mNoteDistance;

            private List<GraffitiNoteData> mNotes = new ArrayList<>();

            public static final int MASK_REDRAW = 0x00000011;

            public static final int FLAG_REDRAW_ALL = 0x1 << 0;
            public static final int FLAG_REDRAW_ONCE = 0x1 << 1;

            private int mFlag = 0;

            AnimatorFactory.AbstractBaseAnimator mAnimator; // 是否有动画
            ICoordinateConverter mCoordinateConverter;

            public GraffitiLayerData(GraffitiBean.GraffitiLayerBean layerBean) {
                if (layerBean == null) {
                    throw new IllegalArgumentException("Must support a valid GraffitiBean.GraffitiLayerBean");
                }
                mLayerBean = layerBean;
            }

            /**
             * Must called when view size changed
             *
             * @param viewWidth
             * @param viewHeight
             * @return any {@link GraffitiData.GraffitiLayerData.GraffitiNoteData} has been added from {@link GraffitiBean.GraffitiLayerBean}
             */
            public boolean installView(float viewWidth, float viewHeight) {
                if (viewWidth <= 0 || viewHeight <= 0) {
                    throw new IllegalArgumentException("viewWidth or viewHeight must > 0, viewWidth -> " + viewWidth + " viewHeight -> " + viewHeight);
                }

                if (viewWidth == mCanvasWidth && viewHeight == mCanvasHeight) {
                    //installData already
                    return false;
                }

                mCoordinateConverter = new SimpleCoordinateConverter(mLayerBean.getPercentageCanvasWidth(), mLayerBean.getPercentageCanvasHeight(), viewWidth, viewHeight);

                mCanvasWidth = viewWidth;
                mCanvasHeight = viewHeight;

                mNoteWidth = mCoordinateConverter.convertWidthPercentageToPixel(mLayerBean.getPercentageNoteWidth());
                mNoteHeight = mCoordinateConverter.convertHeightPercentageToPixel(mLayerBean.getPercentageNoteHeight());
                mNoteDistance = mCoordinateConverter.convertHeightPercentageToPixel(mLayerBean.getPercentageNoteDistance());

                // GraffitiNoteData can only been initialized after view installed

                Log.e(TAG, "initialized... \n" + this.toString());
                return true;
            }


            /**
             * Cooperate with {@link GraffitiData#isReadMode()}.
             *
             * @return
             */
            public boolean isReadMode() {
                return mLayerBean.getNotes() != null && mLayerBean.getNotes().size() > 0;
            }


            /**
             * Install notes
             */
            public void installNotes() {
                if (isReadMode()) {
                    for (GraffitiBean.GraffitiLayerBean.GraffitiNoteBean noteBean : mLayerBean.getNotes()) {
                        GraffitiData.GraffitiLayerData.GraffitiNoteData noteData = new GraffitiData.GraffitiLayerData.GraffitiNoteData(this, noteBean);
                        addNote(noteData);
                    }
                }
            }

            /**
             * installView animator
             *
             * @param updateRunnable
             */
            public void installAnimator(Runnable updateRunnable) {
                if (isHasAnimation()) {
                    mAnimator = AnimatorFactory.create(this, updateRunnable);
                }
            }

            public List<GraffitiNoteData> getNotes() {
                return mNotes;
            }

            /**
             * Getting last note
             *
             * @return
             */
            public GraffitiData.GraffitiLayerData.GraffitiNoteData getLast() {
                if (mNotes.size() > 0) {
                    return mNotes.get(mNotes.size() - 1);
                }
                return null;
            }

            public float getCanvasWidth() {
                return mCanvasWidth;
            }

            public float getCanvasHeight() {
                return mCanvasHeight;
            }

            public float getNoteWidth() {
                return mNoteWidth;
            }

            public float getNoteHeight() {
                return mNoteHeight;
            }

            public float getNoteDistance() {
                return mNoteDistance;
            }

            public String getNoteDrawableRes() {
                return mLayerBean.getNoteDrawableRes();
            }

            /**
             * Getting note bitmap
             *
             * @return
             */
            public Bitmap getNoteBitmap() {
                return GraffitiResourcesManager.getBitmapProvider() != null ? GraffitiResourcesManager.getBitmapProvider().loadBitmap(getNoteDrawableRes(), null) : null;
            }


            /**
             * Getting internal {@link GraffitiBean.GraffitiLayerBean}
             *
             * @return
             */
            public GraffitiBean.GraffitiLayerBean getGraffitiLayerBean() {
                return mLayerBean;
            }

            @Deprecated
            public boolean isMergeAble(GraffitiData.GraffitiLayerData layerData) {
                return !isHasAnimation() && mLayerBean.equals(layerData.getGraffitiLayerBean());
            }

            public boolean isHasAnimation() {
                return mLayerBean.getAnimation() > 0;
            }

            public void addNote(GraffitiData.GraffitiLayerData.GraffitiNoteData note) {
                if (note == null || mNotes.contains(note)) {
                    return;
                }
                mNotes.add(note);
            }

            /**
             * Add notes for once
             *
             * @param notes
             */
            public boolean addNote(List<GraffitiNoteData> notes) {
                if (notes == null || notes.size() < 0) {
                    return false;
                }
                mNotes.addAll(notes);
                return true;
            }

            protected void setFlag(int mask, int flag) {
                mFlag = (mFlag & ~mask) | flag;
            }

            /**
             * Force draw all notes
             *
             * @param onlyOnce
             */
            public void forceDrawAll(boolean onlyOnce) {
                if (onlyOnce) {
                    setFlag(MASK_REDRAW, FLAG_REDRAW_ALL | FLAG_REDRAW_ONCE);
                } else {
                    setFlag(MASK_REDRAW, FLAG_REDRAW_ALL | ~FLAG_REDRAW_ONCE);
                }
            }

            /**
             * Finish draw all
             */
            public void finishForceDrawAll(boolean forceFinish) {
                if ((mFlag & FLAG_REDRAW_ONCE) == 1 || forceFinish) {
                    setFlag(MASK_REDRAW, ~FLAG_REDRAW_ALL | ~FLAG_REDRAW_ONCE);
                }
            }


            public void startAnimatorIfExits() {
                if (mAnimator != null) {
                    mAnimator.start();
                }
            }

            public void stopAnimatorIfExits() {
                if (mAnimator != null) {
                    mAnimator.stop();
                }
            }

            /**
             * Is forcing drawing all
             *
             * @return
             */
            public boolean isForceDrawAll() {
                return (mFlag & FLAG_REDRAW_ALL) == 1;
            }

            @Override
            public String toString() {
                return "[" +
                        "mLayerBean:" + mLayerBean +
                        "mCanvasWidth:" + mCanvasWidth +
                        ",mCanvasHeight:" + mCanvasHeight +
                        ",mNoteWidth:" + mNoteWidth +
                        ",mNoteHeight:" + mNoteHeight +
                        ",mNoteDistance:" + mNoteDistance +
                        ",mNotes:" + mNotes +
                        ",mCoordinateConverter:" + mCoordinateConverter +
                        ",mAnimator:" + mAnimator +
                        "]";
            }

            /**
             * Getting {@link ICoordinateConverter}
             *
             * @return
             */
            public ICoordinateConverter getCoordinateConverter() {
                return mCoordinateConverter;
            }


            public static class GraffitiNoteData {

                private GraffitiData.GraffitiLayerData mLayerData;

                /**
                 * Whether we are drawn or not
                 */
                public boolean mDrawn;

                private RectF mOriginalRectF;
                private RectF mCalculateRectF;

                /**
                 * @param layerData
                 * @param bean
                 */
                public GraffitiNoteData(GraffitiData.GraffitiLayerData layerData, GraffitiBean.GraffitiLayerBean.GraffitiNoteBean bean) {
                    this(layerData, layerData.mCoordinateConverter.convertWidthPercentageToPixel(bean.getPercentageX()), layerData.mCoordinateConverter.convertHeightPercentageToPixel(bean.getPercentageY()));
                }

                public GraffitiNoteData(GraffitiData.GraffitiLayerData layerData, float centerX, float centerY) {
                    if (layerData == null) {
                        throw new IllegalArgumentException("layerData can not be null !");
                    }
                    if (layerData.getCoordinateConverter() == null) {
                        throw new IllegalArgumentException("has GraffitiLayerData called #installView(viewWidth,viewHeight) yet? ICoordinateConverter has not been installed yet.");
                    }
                    mLayerData = layerData;
                    mOriginalRectF = new RectF(
                            centerX - layerData.getNoteWidth() / 2,
                            centerY - layerData.getNoteHeight() / 2,
                            centerX + layerData.getNoteWidth() / 2,
                            centerY + layerData.getNoteHeight() / 2
                    );
                    mCalculateRectF = new RectF(mOriginalRectF);
                    mDrawn = false;
                }

                public RectF getOriginalRectF() {
                    return mOriginalRectF;
                }

                public RectF getCalculateRectF() {
                    return mLayerData.mAnimator == null ? getOriginalRectF() : mLayerData.mAnimator.getAnimateRectF(getOriginalRectF(), mCalculateRectF);
                }

                public GraffitiData.GraffitiLayerData getLayerData() {
                    return mLayerData;
                }

                @Override
                public String toString() {
                    return "[mDrawn:" + mDrawn + ",mOriginalRectF:" + mOriginalRectF + "]";
                }

            }

        }


    }



    /* ########################################## Tools ########################################### */

    /**
     * Calculate next note
     */
    public interface INextNoteCalculator {


        /**
         * 根据当前点 计算接下来的点
         *
         * @param layer
         * @param relative
         * @return
         */
        List<GraffitiData.GraffitiLayerData.GraffitiNoteData> next(GraffitiData.GraffitiLayerData layer, GraffitiData.GraffitiLayerData.GraffitiNoteData relative, float x, float y);
    }


    /**
     * Simple impl of {@link INextNoteCalculator}
     */
    public static class SimpleNextNoteCalculator implements INextNoteCalculator {


        static final String TAG = SimpleNextNoteCalculator.class.getSimpleName();

        /**
         * 优化内存，用于返回 数据
         */
        private List<GraffitiData.GraffitiLayerData.GraffitiNoteData> mPool = new ArrayList<>();

        public SimpleNextNoteCalculator() {

        }

        @Override
        public List<GraffitiData.GraffitiLayerData.GraffitiNoteData> next(GraffitiData.GraffitiLayerData layer, GraffitiData.GraffitiLayerData.GraffitiNoteData relative, float x, float y) {
            if (relative == null) {
                GraffitiData.GraffitiLayerData.GraffitiNoteData note = new GraffitiData.GraffitiLayerData.GraffitiNoteData(layer, x, y);
                Log.e(TAG, "add note -> " + note);
                mPool.clear();
                mPool.add(note);
                return mPool;
            } else {
                float lastX = relative.getOriginalRectF().centerX();
                float lastY = relative.getOriginalRectF().centerY();

                float distance = (float) Math.sqrt(Math.pow(x - lastX, 2) + Math.pow(y - lastY, 2));
                if (distance > layer.getNoteDistance()) {
                    mPool.clear();
                    float ratio = distance / layer.getNoteDistance();
                    float gapX = (x - lastX) / ratio;
                    float gapY = (y - lastY) / ratio;
                    for (int i = 1; i <= ratio; i++) {
                        GraffitiData.GraffitiLayerData.GraffitiNoteData note = new GraffitiData.GraffitiLayerData.GraffitiNoteData(layer, lastX + gapX * i, lastY + gapY * i);
                        Log.e(TAG, "add note -> " + note);
                        mPool.add(note);
                    }
                    return mPool;
                }
            }
            return null;
        }
    }


    /**
     * Created by fishyu on 2018/4/28.
     * <p>
     * 坐标系统
     * <p>
     * 概念：
     * 1，Percentage 比例坐标 用于传输的抽象坐标，用比例值来描述
     * 2，Pixel 像素坐标 用于计算机显示的坐标，用实际像素来描述
     */

    public interface ICoordinateConverter {

        float convertWidthPixelToPercentage(float widthPixel);

        float convertWidthPercentageToPixel(float widthPercentage);

        float convertHeightPixelToPercentage(float heightPixel);

        float convertHeightPercentageToPixel(float heightPercentage);

        /**
         * Convert Percentage to Pixel
         *
         * @param from
         * @param to
         * @return
         */
        RectF convertPercentageToPixel(RectF from, RectF to);


        /**
         * Convert Pixel to Percentage
         *
         * @param from
         * @param to
         * @return
         */
        RectF convertPixelToPercentage(RectF from, RectF to);
    }


    public static class SimpleCoordinateConverter implements ICoordinateConverter {

        private RectF mTempRectF;

        final float mWidthFactor;
        final float mHeightFactor;

        public SimpleCoordinateConverter(float percentageWidth, float percentageHeight, float viewWidth, float viewHeight) {
            mWidthFactor = percentageWidth / viewWidth;
            mHeightFactor = percentageHeight / viewHeight;
        }

        @Override
        public float convertWidthPixelToPercentage(float widthPixel) {
            return widthPixel * mWidthFactor;
        }

        @Override
        public float convertWidthPercentageToPixel(float widthPercentage) {
            return widthPercentage / mWidthFactor;
        }

        @Override
        public float convertHeightPixelToPercentage(float heightPixel) {
            return heightPixel * mHeightFactor;
        }

        @Override
        public float convertHeightPercentageToPixel(float heightPercentage) {
            return heightPercentage / mHeightFactor;
        }

        @Override
        public RectF convertPercentageToPixel(RectF from, RectF to) {
            if (to == null) {
                to = getTempRectF();
            }
            to.left = convertWidthPercentageToPixel(from.left);
            to.top = convertHeightPercentageToPixel(from.top);
            to.right = convertWidthPercentageToPixel(from.right);
            to.bottom = convertHeightPercentageToPixel(from.bottom);
            return to;
        }

        @Override
        public RectF convertPixelToPercentage(RectF from, RectF to) {
            if (to == null) {
                to = getTempRectF();
            }
            to.left = convertWidthPixelToPercentage(from.left);
            to.top = convertHeightPercentageToPixel(from.top);
            to.right = convertWidthPixelToPercentage(from.right);
            to.bottom = convertHeightPixelToPercentage(from.bottom);
            return to;
        }

        protected RectF getTempRectF() {
            if (mTempRectF == null) {
                mTempRectF = new RectF();
            }
            return mTempRectF;
        }
    }


    /**
     * Animation Factory, managing all animators
     */
    public static class AnimatorFactory {

        public final static int SCALE = 1;
        public final static int TRANSLATE = 2;
        public final static int ALPHA = 3;
        public final static int ROTATE = 4;

        public static AnimatorFactory.AbstractBaseAnimator create(GraffitiData.GraffitiLayerData data, Runnable updateViewRunnable) {
            return new AnimatorFactory.ScaleAnimator(data, updateViewRunnable, 1000, 1.0f, 0.5f);
        }


        public static abstract class AbstractBaseAnimator implements Animator.AnimatorListener, ValueAnimator.AnimatorUpdateListener {

            /**
             * Awake all updateView Runnable  at same time-line for better preference ?
             * <p>
             * With GPU profiler, this align clock seems have no apparent improvement.
             */
            private static final class InternalAlignClock extends Handler {

                private List<Runnable> mRunnables = new ArrayList<>();


                private boolean mRunning = false;

                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);

                    int size = mRunnables.size();

                    if (size <= 0) {
                        mRunning = false;
                        return;
                    }

                    mRunning = true;
                    for (int i = 0; i < size; i++) {
                        Runnable runnable = mRunnables.get(i);
                        runnable.run();
                    }

                    heartBeat(true);
                }

                void heartBeat(boolean delay) {
                    sendEmptyMessageDelayed(0, delay ? ANIMATION_FRAME_TIME : 0);
                }

                public void start(Runnable runnable) {
                    if (!mRunnables.contains(runnable)) {
                        mRunnables.add(runnable);
                    }
                    if (!mRunning) {
                        heartBeat(false);
                    }
                }

                public void stop(Runnable runnable) {
                    mRunnables.remove(runnable);
                }
            }

            protected final String TAG = getClass().getSimpleName();

            protected Runnable mUpdateViewRunnable;


            static final boolean ENABLE_ALIGN_CLOCK = false;

            static AnimatorFactory.AbstractBaseAnimator.InternalAlignClock mClock = ENABLE_ALIGN_CLOCK ? new AnimatorFactory.AbstractBaseAnimator.InternalAlignClock() : null;

            private ObjectAnimator mAnimator;

            private float mCurrentValue;

            private static final long ANIMATION_FRAME_TIME = 1000 / 45;

            private long mLastUpdateTime = 0;

            private GraffitiData.GraffitiLayerData mLayerData;

            public AbstractBaseAnimator(GraffitiData.GraffitiLayerData layerData, Runnable updateViewRunnable, long duration, float from, float to) {
                mLayerData = layerData;
                mUpdateViewRunnable = updateViewRunnable;

                mAnimator = ObjectAnimator.ofFloat(AnimatorFactory.AbstractBaseAnimator.this, "value", from, to);
                mAnimator.setDuration(duration);
                mAnimator.addListener(this);
                mAnimator.addUpdateListener(this);
                mAnimator.setRepeatCount(ValueAnimator.INFINITE);
                mAnimator.setRepeatMode(ValueAnimator.REVERSE);
            }


            /**
             * Start animation
             */
            public final void start() {
                if (mAnimator.isRunning() || mAnimator.isStarted()) {
                    return;
                }
                mAnimator.start();
            }

            /**
             * Stop animation
             */
            public final void stop() {
                mAnimator.cancel();
            }


            /**
             * Calculate animated RectF
             *
             * @param input
             * @return
             */
            public final RectF getAnimateRectF(RectF input, RectF out) {
                return onCalculateRectF(input, out, mCurrentValue);
            }


            /**
             * On calculate target rectF
             *
             * @param input
             * @param out
             * @param currentValue
             * @return target
             */
            protected abstract RectF onCalculateRectF(RectF input, RectF out, float currentValue);


            @SuppressWarnings("unused")
            public final void setValue(float value) {
                mCurrentValue = value;
            }


            @Override
            public void onAnimationStart(Animator animation) {
                Log.v(TAG, "onAnimationStart");
                setValue(0);
                mLayerData.forceDrawAll(false);
                if (mClock != null) {
                    mClock.start(mUpdateViewRunnable);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.v(TAG, "onAnimationEnd");
                setValue(0);
                notifyView();
                if (mClock != null) {
                    mClock.stop(mUpdateViewRunnable);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.v(TAG, "onAnimationCancel");
                setValue(0);
                notifyView();
                if (mClock != null) {
                    mClock.stop(mUpdateViewRunnable);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mClock == null) {
                    if (System.currentTimeMillis() - mLastUpdateTime >= ANIMATION_FRAME_TIME) {
                        notifyView();
                    }
                }

            }

            /**
             * Notify view time to update
             */
            protected void notifyView() {
                mLastUpdateTime = System.currentTimeMillis();
                mUpdateViewRunnable.run();
            }

        }


        public static class ScaleAnimator extends AnimatorFactory.AbstractBaseAnimator {

            public ScaleAnimator(GraffitiData.GraffitiLayerData layerData, Runnable updateViewRunnable, long duration, float from, float to) {
                super(layerData, updateViewRunnable, duration, from, to);
            }

            @Override
            protected RectF onCalculateRectF(RectF input, RectF out, float currentValue) {
                //TODO 1,TransFormer 2,Matrix for test
//        Log.v(TAG, "onCalculateRectF -> " + input + " currentValue -> " + currentValue);

                float width = currentValue * input.width() / 2;
                float height = currentValue * input.height() / 2;
                float centerX = input.centerX();
                float centerY = input.centerY();

                if (out == null) {
                    out = new RectF();
                }
                out.left = centerX - width;
                out.top = centerY - height;
                out.right = centerX + width;
                out.bottom = centerY + height;
                return out;
            }
        }
    }


    /**
     * GraffitiNoteBean's resources manager.
     * <p>
     * GraffitiView can be functional only when {@link #isResourceLoaded()} {@link #isResourcesReady(List)}
     * {@link #isResourceReady(String)}
     */
    public static class GraffitiResourcesManager {

        static IBitmapManager mBitmapManager;

        /**
         * Bitmap downloader for {@link GraffitiResourcesManager}
         */
        public interface IBitmapDownloader {

            /**
             * Download image, sync call
             *
             * @param url
             * @param listener
             * @return
             */
            void download(String url, IBitmapManager.IBitmapListener listener);
        }

        /**
         * Manage caches for {@link GraffitiResourcesManager}
         */
        public interface IBitmapManager {

            /**
             * Listeners for {@link MemoryBitmapManager}
             */
            interface IBitmapListener {

                /**
                 * ImageLoading start
                 *
                 * @param url
                 */
                void onStart(String url);

                /**
                 * ImageLoading complete
                 *
                 * @param url
                 * @param bitmap Bitmap we want
                 * @param e      not null if load failed
                 */
                void onComplete(String url, Bitmap bitmap, Throwable e);

                /**
                 * For batch of urls
                 *
                 * @param e
                 */
                void onComplete(Throwable e);

            }

            /**
             * Load bitmap
             *
             * @param url
             * @return
             */
            Bitmap loadBitmap(final String url, IBitmapManager.IBitmapListener listener);


            /**
             * Load batch of bitmaps
             *
             * @param urls
             */
            void loadBitmaps(List<String> urls, IBitmapManager.IBitmapListener listener, boolean forceReload);


            /**
             * Have we load all bitmaps
             *
             * @return
             */
            boolean loadFinished();


            /**
             * Cache bitmap
             *
             * @param url
             * @param bitmap
             */
            void cache(String url, Bitmap bitmap);

            /**
             * Clear caches
             */
            void clear();

            /**
             * Should be asyn call
             *
             * @param listener
             * @return
             */
            void download(String url, IBitmapManager.IBitmapListener listener);

        }


        /**
         * batch of urls loading task
         */
        private static class LoadsBitmapTask implements IBitmapManager.IBitmapListener {

            int mRetryCounter = 3;

            List<IBitmapManager.IBitmapListener> mListeners = new ArrayList<>();

            List<String> mUrls;
            int mCompleteCounter;

            IBitmapManager mBitmapManager;
            Exception mLastException;

            public LoadsBitmapTask(IBitmapManager bitmapCache, List<String> urls) {
                mBitmapManager = bitmapCache;
                mUrls = new ArrayList<>(urls);
                mCompleteCounter = urls.size();
                Log.e(TAG, "mUrls -> " + mUrls + " mCompleteCounter -> " + mCompleteCounter);
            }

            public void addListener(IBitmapManager.IBitmapListener listener) {
                if (isTaskFinished()) {
                    return;
                }
                if (!mListeners.contains(listener)) {
                    mListeners.add(listener);
                }
            }

            @Override
            public void onStart(String url) {
                for (IBitmapManager.IBitmapListener listener : mListeners) {
                    if (listener != null) {
                        listener.onStart(url);
                    }
                }
            }

            @Override
            public void onComplete(String url, Bitmap bitmap, Throwable e) {
                for (IBitmapManager.IBitmapListener listener : mListeners) {
                    if (listener != null) {
                        listener.onComplete(url, bitmap, e);
                    }
                }
                //complete counter
                mCompleteCounter--;

                if (bitmap != null) {
                    mUrls.remove(url);
                }

                if (mCompleteCounter == 0) {
                    if (isAllCompleted()) {
                        mLastException = null;
                    } else {
                        //error when
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("The flowing urls can not be downloaded:\n");
                        for (String value : mUrls) {
                            stringBuilder.append(value + "\n");
                        }
                        mLastException = new NetworkErrorException(stringBuilder.toString());
                    }
                    onComplete(mLastException);
                }
            }

            @Override
            public void onComplete(Throwable e) {
                Log.e(TAG, "Finally download all e -> " + e);
                for (IBitmapManager.IBitmapListener listener : mListeners) {
                    if (listener != null) {
                        listener.onComplete(e);
                    }
                }

                if (e != null && mRetryCounter > 0 && mBitmapManager != null) {
                    Log.e(TAG, "Failure checked, retry ...");
                    start();
                    mRetryCounter--;
                    return;
                }

                //stopAndClear all listeners
                stopAndClear();
            }


            public boolean isTaskFinished() {
                return mCompleteCounter == 0 || mBitmapManager == null || mListeners == null;
            }


            public boolean isAllCompleted() {
                return mUrls.size() == 0;
            }

            /**
             * Clear all
             */
            public void stopAndClear() {
                mListeners.clear();
                mListeners = null;
                mRetryCounter = 0;
                mCompleteCounter = 0;
                mBitmapManager = null;
            }

            /**
             * Start task
             */
            public void start() {
                Log.v(TAG, "start");
                if (mBitmapManager != null) {
                    for (String url : mUrls) {
                        mBitmapManager.loadBitmap(url, this);
                    }
                }
            }
        }


        private static class MemoryBitmapManager implements IBitmapManager {

            private Map<String, Bitmap> mCaches = new HashMap<>();

            private LoadsBitmapTask mCurrentLoadBitmapsTask;

            private IBitmapDownloader mBitmapDownloader;

            public MemoryBitmapManager(IBitmapDownloader downloader) {
                if (downloader == null) {
                    throw new IllegalArgumentException("IBitmapDownloader can not be null !");
                }
                mBitmapDownloader = downloader;
            }

            @Override
            public Bitmap loadBitmap(final String url, final IBitmapListener listener) {
                Log.v(TAG, "loadBitmap url -> " + url + " listener -> " + listener);
                if (TextUtils.isEmpty(url)) {
                    return null;
                }
                if (mCaches.containsKey(url)) {
                    return mCaches.get(url);
                }
                download(url, new IBitmapListener() {
                    @Override
                    public void onStart(String url) {
                        Log.v(TAG, "onStart -> " + url);
                        if (listener != null) {
                            listener.onStart(url);
                        }
                    }

                    @Override
                    public void onComplete(String url, Bitmap bitmap, Throwable e) {
                        Log.v(TAG, "onComplete url -> " + url + " bitmap -> " + bitmap + " e -> " + e);
                        if (listener != null) {
                            listener.onComplete(url, bitmap, e);
                        }
                        cache(url, bitmap);
                    }

                    @Override
                    public void onComplete(Throwable e) {
                        if (listener != null) {
                            listener.onComplete(e);
                        }
                    }
                });
                return null;
            }

            @Override
            public void loadBitmaps(final List<String> urls, final IBitmapListener listener, boolean forceReload) {
                Log.e(TAG, "loadBitmaps urls -> " + urls + " listener -> " + listener + " forceReload -> " + forceReload);
                if (urls == null) {
                    Log.e(TAG, "\t urls is null!");
                    listener.onComplete(new IllegalArgumentException("urls is null"));
                    return;
                }

                if (forceReload && mCurrentLoadBitmapsTask != null) {
                    Log.e(TAG, "\t forceReload, clear current task");
                    mCurrentLoadBitmapsTask.stopAndClear();
                    mCurrentLoadBitmapsTask = null;
                }

                if (mCurrentLoadBitmapsTask != null) {
                    Log.e(TAG, "LoadsBitmapTask already in flight. isAllCompleted -> " + mCurrentLoadBitmapsTask.isAllCompleted());
                    if (mCurrentLoadBitmapsTask.isTaskFinished()) {
                        listener.onComplete(mCurrentLoadBitmapsTask.mLastException);
                    } else {
                        Log.v(TAG, "add listener to task ...");
                        mCurrentLoadBitmapsTask.addListener(listener);
                    }
                    return;
                }

                //downloads all
                mCurrentLoadBitmapsTask = new LoadsBitmapTask(MemoryBitmapManager.this, urls);
                mCurrentLoadBitmapsTask.addListener(new IBitmapListener() {
                    @Override
                    public void onStart(String url) {
                        if (listener != null) {
                            listener.onStart(url);
                        }
                    }

                    @Override
                    public void onComplete(String url, Bitmap bitmap, Throwable e) {
                        if (listener != null) {
                            listener.onComplete(url, bitmap, e);
                        }
                    }

                    @Override
                    public void onComplete(Throwable e) {
                        if (listener != null) {
                            listener.onComplete(e);
                        }
                    }
                });
                Log.e(TAG, "LoadsBitmapTask start successfully!");
                mCurrentLoadBitmapsTask.start();
            }

            @Override
            public boolean loadFinished() {
                return (mCurrentLoadBitmapsTask != null && mCurrentLoadBitmapsTask.isAllCompleted());
            }

            @Override
            public void cache(String url, Bitmap bitmap) {
                Log.v(TAG, "cache url -> " + url + " bitmap -> " + bitmap);
                if (TextUtils.isEmpty(url) || bitmap == null) {
                    return;
                }
                mCaches.put(url, bitmap);
            }

            @Override
            public void clear() {
                mCaches.clear();
                if (mCurrentLoadBitmapsTask != null) {
                    mCurrentLoadBitmapsTask.stopAndClear();
                    mCurrentLoadBitmapsTask = null;
                }
            }

            @Override
            public void download(String url, IBitmapListener listener) {
                mBitmapDownloader.download(url, listener);
            }
        }


        /**
         * Init {@link IBitmapManager} and all
         *
         * @param downloader
         */
        public static void init(IBitmapDownloader downloader) {
            if (mBitmapManager == null) {
                mBitmapManager = new MemoryBitmapManager(downloader);
            }
        }

        /**
         * Loads urls
         *
         * @param listener
         * @param urls
         */
        public static void loadResources(List<String> urls, IBitmapManager.IBitmapListener listener, boolean forceReload) {
            necessary();
            mBitmapManager.loadBitmaps(urls, listener, forceReload);
        }

        /**
         * Is all urls loaded finished or not.
         *
         * @return
         */
        public static boolean isResourceLoaded() {
            necessary();
            return mBitmapManager.loadFinished();
        }

        /**
         * Is resource ready
         *
         * @return
         */
        public static boolean isResourceReady(String url) {
            necessary();
            Log.e(TAG, "isResourceReady -> " + url);
            if (TextUtils.isEmpty(url)) {
                return false;
            }
            Bitmap bitmap = mBitmapManager.loadBitmap(url, null);
            if (bitmap == null) {
                Log.e(TAG, "\t bitmap is null !");
                return false;
            }
            return true;
        }

        /**
         * Is all resources ready
         *
         * @return
         */
        public static boolean isResourcesReady(List<String> urls) {
            necessary();
            if (urls == null) {
                return false;
            }
            for (String url : urls) {
                if (!isResourceReady(url)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Returning internal {@link IBitmapManager}
         *
         * @return
         */
        public static IBitmapManager getBitmapProvider() {
            necessary();
            return mBitmapManager;
        }


        private static void necessary() {
            if (mBitmapManager == null) {
                throw new IllegalArgumentException("Not init yet! Please call #init(Context) in the first place.");
            }
        }
    }


}
