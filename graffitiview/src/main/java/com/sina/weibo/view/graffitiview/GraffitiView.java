package com.sina.weibo.view.graffitiview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * Two mode:(See {@link GraffitiData} for more detail)<br>
 * 1,READ-MODE<br>
 * 2,WRITE-MODE<br>
 * <p>
 * Three location descriptions:<br>
 * 1, REFERENCE 参考坐标，此数值由UI提供,如 {@link com.sina.weibo.view.graffitiview.GraffitiBean.GraffitiLayerBean}<br>
 * 2, DEVICE 礼物绘制机制的坐标，比如 IOS提供的数据，如 {@link com.sina.weibo.view.graffitiview.GraffitiBean.GraffitiLayerBean.GraffitiNoteBean} 中<br>
 * 3, PIXEL 本地坐标，所有本机器的的坐标为含有 PIXEL 字段，如 {@link com.sina.weibo.view.graffitiview.GraffitiBean.GraffitiLayerBean}<br>
 */
public class GraffitiView extends ViewGroup {
    static final String TAG = GraffitiView.class.getSimpleName();

    private INextNoteCalculator mNoteCalculator;

    private GraffitiData mGraffitiData;
    private GraffitiData.GraffitiLayerData mDrawingLayer;

    private GraffitiBean.GraffitiLayerBean mDrawObject;

    private ICallback mCallback;

    /**
     * Callback Handler
     */
    class InternalCallback extends Handler implements ICallback {

        final int ON_DATA_CHANGED = 1;
        final int ON_MESSAGE = 2;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ON_DATA_CHANGED:
                    if (mCallback != null) {
                        mCallback.onDataChanged(GraffitiView.this, (GraffitiBean.GraffitiLayerBean) msg.obj);
                    }
                    break;
                case ON_MESSAGE:
                    if (mCallback != null) {
                        mCallback.onMessage(msg.arg1);
                    }
                    break;
            }
        }

        @Override
        public void onDataChanged(GraffitiView graffitiView, GraffitiBean.GraffitiLayerBean drawingObject) {
            mGraffitiData.updateTotalNoteNumber();
            Message message = Message.obtain();
            message.what = ON_DATA_CHANGED;
            message.obj = drawingObject;
            sendMessage(message);
        }

        @Override
        public void onMessage(int msg) {
            Message message = Message.obtain();
            message.what = ON_MESSAGE;
            message.arg1 = msg;
            sendMessage(message);
        }
    }

    private ICallback mInternalCallback = new InternalCallback();

    private Runnable showNotes = new Runnable() {
        @Override
        public void run() {
            if (mGraffitiData == null) {
                //may be set null ?
                return;
            }
            if (!mGraffitiData.isBitmapsLoaded()) {
                Log.e(TAG, "Resources is not ready, can't show!");
                postDelayed(showNotes, 300);
                return;
            } else if (!mGraffitiData.isBitmapsReady(mGraffitiData.getLayerResources())) {
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
            throw new IllegalArgumentException("GraffitiData must not be null!");
        }

        Log.e(TAG, "installData -> " + initData);
        if (mGraffitiData != null) {
            // clear all views
            removeAllViews();
        }

        mNoteCalculator = new SimpleNextNoteCalculator();
        mGraffitiData = initData;

        mDrawingLayer = null;

        //data installed, refresh view
        invalidate();
        requestLayout();

        //invoke on size changed
        if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
            post(new Runnable() {
                @Override
                public void run() {
                    onSizeChanged(getMeasuredWidth(), getMeasuredHeight(), 0, 0);
                }
            });
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e(TAG, "onMeasure mGraffitiData -> " + mGraffitiData);
        // square this view
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (width / GraffitiData.getWidthHeightPercentage()), MeasureSpec.EXACTLY);

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
        Log.e(TAG, "onLayout mGraffitiData -> " + mGraffitiData);

        if (mGraffitiData != null) {
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e(TAG, "onSizeChanged mGraffitiData -> " + mGraffitiData);

        if (mGraffitiData != null) {
            if (w != oldw || h != oldh) {
                if (w >= 0 && h >= 0) {
                    if (mGraffitiData.installView(w, h)) {
                        Log.e(TAG, "View installed, all data is ready.");
                        if (mGraffitiData.isReadMode()) {
                            Log.e(TAG, "read mode, ready to show layers.");
                            mGraffitiData.installLayers();
                            post(showNotes);
                        }
                    }
                }
            }
        }
    }

    /**
     * 1, Update data <br>
     * 2, Notify view updating
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.v(TAG, "onTouchEvent");
        //can not write
        if (!checkWritable()) {
            return true;
        }

        final float pointX = event.getX();
        final float pointY = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (checkWritableActionDown()) {
                mDrawingLayer = mGraffitiData.getDrawingLayer(getCurrentDrawObject());
                if (mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, null, pointX, pointY, mGraffitiData.getLeftNoteNumber()))) {
                    notifyDataChanged(mDrawingLayer, true);
                }
                return true;
            }
        } else if (mDrawingLayer != null) {
            // Checks for the event that occurs
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, mDrawingLayer.getLast(), pointX, pointY, mGraffitiData.getLeftNoteNumber()))) {
                        notifyDataChanged(mDrawingLayer, true);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, mDrawingLayer.getLast(), pointX, pointY, mGraffitiData.getLeftNoteNumber()))) {
                        notifyDataChanged(mDrawingLayer, true);
                    }
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
        //check any time
        if (!isEnabled()) {
            Log.e(TAG, "View has been disabled.");
            mInternalCallback.onMessage(ICallback.MSG_DISABLED);
            return false;
        }

        if (getMeasuredHeight() == 0 || getMeasuredWidth() == 0) {
            Log.e(TAG, "width or height is 0, getMeasuredWidth() -> " + getMeasuredWidth() + " getMeasuredHeight() -> " + getMeasuredHeight());
            mInternalCallback.onMessage(ICallback.MSG_VIEW_NOT_READY);
            return false;
        }

        if (mGraffitiData == null) {
            throw new IllegalStateException("You have not call#installData(GraffitiData) to install data.");
        }

        if (!mGraffitiData.isViewInstalled()) {
            Log.e(TAG, "GraffitiData is not installed");
            mInternalCallback.onMessage(ICallback.MSG_GRAFFITIDATA_NOT_INSTALLED);
            return false;
        }
        return true;
    }


    /**
     * Whether we can write or not.
     *
     * @return
     */
    protected boolean checkWritableActionDown() {
        if (mGraffitiData.isReadMode()) {
            Log.e(TAG, "player mode, just show notes");
            mInternalCallback.onMessage(ICallback.MSG_READ_MODE);
            return false;
        }

        if (mGraffitiData.getLeftNoteNumber() <= 0) {
            Log.e(TAG, "Reached max note number, can not draw.");
            mInternalCallback.onMessage(ICallback.MSG_MAX_NOTE_REACHED);
            return false;
        }

        if (getCurrentDrawObject() == null) {
            Log.e(TAG, "GraffitiLayerBean is not set, can not draw.");
            mInternalCallback.onMessage(ICallback.MSG_RESOURCE_NOT_READY);
            return false;
        }

        if (!mGraffitiData.isEnableRiskLoadBitmap() && !mGraffitiData.isBitmapsLoaded()) {
            Log.e(TAG, "resources is not ready, nothing to draw.");
            mInternalCallback.onMessage(ICallback.MSG_RESOURCE_NOT_READY);
            return false;
        }

        if (!mGraffitiData.isBitmapReady(getCurrentDrawObject().getNoteDrawableRes())) {
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
        return GraffitiBean.GraffitiLayerBean.buildTest();
    }

    /**
     * Setting {@link GraffitiBean.GraffitiLayerBean}, with witch to draw.
     *
     * @param layerBean
     */
    public void setDrawObject(GraffitiBean.GraffitiLayerBean layerBean) {
        if (layerBean != mDrawObject) {
            mDrawingLayer = null;
        }
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
    public void setCallbacks(ICallback callback) {
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

        int MSG_MAX_NOTE_REACHED = 5;

        int MSG_GRAFFITIDATA_NOT_INSTALLED = 6;

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

        private IBitmapProvider mBitmapManager;

        /**
         * 图层
         */
        private List<GraffitiLayerData> mLayers = new ArrayList<>();

        private GraffitiBean mGraffitiBean;

        private int mMaxNoteNumber;

        private int mTotalNoteNumber = 0;

        /**
         * Whether we risk loading bitmap when {@link #isBitmapsLoaded()} is negative.
         * <p>
         * When enable this, we get faster drawing, but unexcepted touch/read delay may occur sometimes.
         */
        private boolean mEnableRiskLoadBitmap = true;

        /**
         * Used for calculate layer info
         */
        private ICoordinateConverter mCoordinateConverter;

        private float mCanvasWidth;

        private float mCanvasHeight;

        /**
         * Constructor from {@link GraffitiBean}, which should be installed later checked by {@link #isReadMode()}
         *
         * @param graffitiBean
         */
        public GraffitiData(IBitmapProvider bitmapManager, GraffitiBean graffitiBean) {
            this(bitmapManager, 0);
            mGraffitiBean = graffitiBean;
        }

        /**
         * Default
         */
        public GraffitiData(IBitmapProvider bitmapManager, int maxNote) {
            if (bitmapManager == null) {
                throw new IllegalArgumentException("Must pass a valid IBitmapManager!");
            }
            mBitmapManager = bitmapManager;
            mMaxNoteNumber = maxNote;
        }

        /**
         * Install view
         *
         * @param viewWidth
         * @param viewHeight
         */
        public boolean installView(float viewWidth, float viewHeight) {
            if (viewWidth <= 0 || viewHeight <= 0) {
                throw new IllegalArgumentException("viewWidth or viewHeight must > 0, viewWidth -> " + viewWidth + " viewHeight -> " + viewHeight);
            }

            if (viewWidth == mCanvasWidth && viewHeight == mCanvasHeight) {
                //installData already
                return false;
            }

            mCoordinateConverter = new SimpleCoordinateConverter(GraffitiBean.ReferenceCanvasWidth, GraffitiBean.ReferenceCanvasHeight, viewWidth, viewHeight);

            mCanvasWidth = viewWidth;
            mCanvasHeight = viewHeight;

            // GraffitiNoteData can only been initialized after view installed
            installLayers();

            Log.e(TAG, "initialized...");
            return true;
        }

        /**
         * Is {@link #installView(float, float)} called or not.
         *
         * @return
         */
        public boolean isViewInstalled() {
            return mCoordinateConverter != null;
        }

        public void installLayers() {
            if (isReadMode()) {
                // Used for calculate note info(most is because ios used different strategy for note)
                ICoordinateConverter noteCoordinateConverter = new SimpleCoordinateConverter(mGraffitiBean.getDrawDeviceWidth(), mGraffitiBean.getDrawDeviceHeight(), mCanvasWidth, mCanvasHeight);
                for (GraffitiBean.GraffitiLayerBean bean : mGraffitiBean.getLayers()) {
                    GraffitiData.GraffitiLayerData layerData = new GraffitiData.GraffitiLayerData(noteCoordinateConverter, bean);
                    addLayer(layerData);
                }
            }
        }

        /**
         * Whether or not we are in READ-MODE or note.
         * <p>
         * For some details with READ-MODE see {@link GraffitiData}
         *
         * @return
         */
        public boolean isReadMode() {
            return mGraffitiBean != null && mGraffitiBean.getLayers() != null && mGraffitiBean.getLayers().size() > 0;
        }

        /**
         * @return
         */
        public static float getWidthHeightPercentage() {
            return GraffitiBean.ReferenceCanvasWidth / GraffitiBean.ReferenceCanvasHeight;
        }

        /**
         * Getting max note number.
         *
         * @return
         */
        public int getMaxNoteNumber() {
            return mMaxNoteNumber;
        }

        /**
         * Reached max note number or not.
         *
         * @return
         */
        public int getLeftNoteNumber() {
            if (mMaxNoteNumber <= 0) {
                return Integer.MAX_VALUE;
            }
            return mMaxNoteNumber - getCurrentTotalNote();
        }

        /**
         * See {@link #mEnableRiskLoadBitmap} for detail
         *
         * @return
         */
        public boolean isEnableRiskLoadBitmap() {
            return mEnableRiskLoadBitmap;
        }

        /**
         * Is all urls loaded finished or not.
         *
         * @return
         */
        public boolean isBitmapsLoaded() {
            return mBitmapManager.isBitmapsReady();
        }

        /**
         * Is resource ready
         *
         * @return
         */
        public boolean isBitmapReady(String url) {
            return mBitmapManager.getBitmap(url) != null;
        }

        /**
         * Is all resources ready
         *
         * @return
         */
        public boolean isBitmapsReady(List<String> urls) {
            if (urls == null) {
                return false;
            }
            for (String url : urls) {
                if (!isBitmapReady(url)) {
                    return false;
                }
            }
            return true;
        }


        /**
         * See {@link #mEnableRiskLoadBitmap} for detail
         *
         * @return
         */
        public void setEnableRiskLoadResources(boolean value) {
            mEnableRiskLoadBitmap = value;
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
            updateTotalNoteNumber();
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
            updateTotalNoteNumber();
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
            updateTotalNoteNumber();
        }

        /**
         * Is merge same layer
         *
         * @return
         */
        public boolean isMergeLayer() {
            return true;
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
         * Update total note
         *
         * @return
         */
        public int updateTotalNoteNumber() {
            int total = 0;
            for (GraffitiLayerData layerData : mLayers) {
                total += layerData.getNotes().size();
            }
            mTotalNoteNumber = total;
            return mTotalNoteNumber;
        }

        /**
         * Getting total note
         *
         * @return
         */
        public int getCurrentTotalNote() {
            if (mTotalNoteNumber <= 0 && getLayerCount() > 0) {
                return updateTotalNoteNumber();
            }
            return mTotalNoteNumber;
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

        /**
         * Returning drawing layer
         *
         * @param layerBean
         * @return
         */
        public GraffitiData.GraffitiLayerData getDrawingLayer(GraffitiBean.GraffitiLayerBean layerBean) {
            GraffitiLayerData lastLayer = getLastLayer();
            if (lastLayer != null && isMergeLayer() && lastLayer.isMergeAble(layerBean)) {
                return lastLayer;
            }

            //new one
            GraffitiData.GraffitiLayerData layerData = new GraffitiData.GraffitiLayerData(null, layerBean);
            addLayer(layerData);
            return layerData;
        }

        public float getCanvasWidth() {
            return mCanvasWidth;
        }

        public float getCanvasHeight() {
            return mCanvasHeight;
        }


        public class GraffitiLayerData {

            final String TAG = GraffitiData.GraffitiLayerData.class.getSimpleName();

            private GraffitiBean.GraffitiLayerBean mLayerBean;

            private float mNoteWidth;
            private float mNoteHeight;

            private float mNoteDistance;

            private List<GraffitiNoteData> mNotes = new ArrayList<>();

            public static final int MASK_REDRAW = 0x00000011;

            public static final int FLAG_REDRAW_ALL = 0x1 << 0;
            public static final int FLAG_REDRAW_ONCE = 0x1 << 1;

            private int mFlag = 0;

            AnimatorFactory.AbstractBaseAnimator mAnimator; // 是否有动画

            public GraffitiLayerData(ICoordinateConverter noteConverter, GraffitiBean.GraffitiLayerBean layerBean) {
                if (mCoordinateConverter == null) {
                    throw new IllegalArgumentException("ICoordinateConverter must not be null!");
                }

                if (layerBean == null) {
                    throw new IllegalArgumentException("Must support a valid GraffitiBean.GraffitiLayerBean");
                }
                mLayerBean = layerBean;

                installView();

                //install note if needed
                if (isReadMode()) {
                    installNotes(noteConverter);
                }
            }

            private void installView() {
                mNoteWidth = GraffitiData.this.mCoordinateConverter.convertWidthReferenceToPixel(mLayerBean.getPercentageNoteWidth());
                mNoteHeight = GraffitiData.this.mCoordinateConverter.convertHeightReferenceToPixel(mLayerBean.getPercentageNoteHeight());
                mNoteDistance = GraffitiData.this.mCoordinateConverter.convertHeightReferenceToPixel(mLayerBean.getPercentageNoteDistance());
            }

            /**
             * Is view installed or not.
             *
             * @return
             */
            public boolean isViewInstalled() {
                return GraffitiData.this.mCoordinateConverter != null;
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
            private void installNotes(ICoordinateConverter noteConverter) {
                if (noteConverter == null) {
                    throw new IllegalArgumentException("How could noteConverter be null when read mode ?");
                }
                for (GraffitiBean.GraffitiLayerBean.GraffitiNoteBean noteBean : mLayerBean.getNotes()) {
                    GraffitiData.GraffitiLayerData.GraffitiNoteData noteData = new GraffitiNoteData(noteConverter.convertWidthReferenceToPixel(noteBean.getDeviceX()), noteConverter.convertHeightReferenceToPixel(noteBean.getDeviceY()));
                    addNote(noteData);
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
                return GraffitiData.this.mBitmapManager.getBitmap(getNoteDrawableRes());
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
            public boolean isMergeAble(GraffitiBean.GraffitiLayerBean bean) {
                return !isHasAnimation() && mLayerBean.equals(bean) && mNotes.size() < 10;
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


            public class GraffitiNoteData {

                /**
                 * Whether we are drawn or not
                 */
                public boolean mDrawn;

                private RectF mOriginalRectF;
                private RectF mCalculateRectF;

                public GraffitiNoteData(float centerX, float centerY) {
                    mOriginalRectF = new RectF(
                            centerX - getNoteWidth() / 2,
                            centerY - getNoteHeight() / 2,
                            centerX + getNoteWidth() / 2,
                            centerY + getNoteHeight() / 2
                    );
                    mCalculateRectF = new RectF(mOriginalRectF);
                    mDrawn = false;
                }

                public RectF getOriginalRectF() {
                    return mOriginalRectF;
                }

                public RectF getCalculateRectF() {
                    return mAnimator == null ? getOriginalRectF() : mAnimator.getAnimateRectF(getOriginalRectF(), mCalculateRectF);
                }

                public GraffitiData.GraffitiLayerData getLayerData() {
                    return GraffitiData.GraffitiLayerData.this;
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
        List<GraffitiData.GraffitiLayerData.GraffitiNoteData> next(GraffitiData.GraffitiLayerData layer, GraffitiData.GraffitiLayerData.GraffitiNoteData relative, float x, float y, int maxNotes);
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
        public List<GraffitiData.GraffitiLayerData.GraffitiNoteData> next(GraffitiData.GraffitiLayerData layer, GraffitiData.GraffitiLayerData.GraffitiNoteData relative, float x, float y, int maxNotes) {
            if (relative == null) {
                GraffitiData.GraffitiLayerData.GraffitiNoteData note = layer.new GraffitiNoteData(x, y);
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
                    for (int i = 1; i <= ratio && i <= maxNotes; i++) {
                        GraffitiData.GraffitiLayerData.GraffitiNoteData note = layer.new GraffitiNoteData(lastX + gapX * i, lastY + gapY * i);
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

        float convertWidthPixelToReference(float widthPixel);

        float convertWidthReferenceToPixel(float widthPercentage);

        float convertHeightPixelToReference(float heightPixel);

        float convertHeightReferenceToPixel(float heightPercentage);

    }


    public static class SimpleCoordinateConverter implements ICoordinateConverter {

        final float mWidthFactor;
        final float mHeightFactor;

        public SimpleCoordinateConverter(float referenceWidth, float referenceHeight, float viewWidth, float viewHeight) {
            mWidthFactor = referenceWidth / viewWidth;
//            mHeightFactor = percentageHeight / viewHeight;
            mHeightFactor = mWidthFactor;
        }

        @Override
        public float convertWidthPixelToReference(float widthPixel) {
            return widthPixel * mWidthFactor;
        }

        @Override
        public float convertWidthReferenceToPixel(float widthPercentage) {
            return widthPercentage / mWidthFactor;
        }

        @Override
        public float convertHeightPixelToReference(float heightPixel) {
            return heightPixel * mHeightFactor;
        }

        @Override
        public float convertHeightReferenceToPixel(float heightPercentage) {
            return heightPercentage / mHeightFactor;
        }

        public static float convertPixelToReference(float reference, float pixel, float value) {
            if (pixel == 0 || reference == 0) {
                return 0;
            }
            float factor = reference / pixel;
            return value * factor;
        }

        public static float convertReferenceToPixel(float reference, float pixel, float value) {
            if (pixel == 0 || reference == 0) {
                return 0;
            }
            float factor = reference / pixel;
            return value / factor;
        }

        public static float convertPixelToReference(float factor, float value) {
            if (factor == 0) {
                return 0;
            }
            return value * factor;
        }

        public static float convertReferenceToPixel(float factor, float value) {
            if (factor == 0) {
                return 0;
            }
            return value / factor;
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
     * Manage bitmaps for {@link GraffitiView} {@link GraffitiData}.
     * <p>
     * We use this class to fetch bitmaps of our urls.
     */
    public interface IBitmapProvider {


        /**
         * Load bitmap
         *
         * @param url
         * @return
         */
        Bitmap getBitmap(final String url);


        /**
         * Have we download all bitmaps
         *
         * @return
         */
        boolean isBitmapsReady();
    }


}
