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
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * The philosophy of Design is Data-Drive-Mode:
 * <p>
 * Data [{@link GraffitiData}, {@link GraffitiData.GraffitiLayerData}, {@link GraffitiData.GraffitiLayerData}]
 * <p>
 * View [{@link GraffitiView}, {@link GraffitiLayerView}]
 * <p>
 * Data Drives[#notifyDataChanged()] View
 * <p>
 * <p>
 * Two mode:(See {@link GraffitiData} for more detail)<br>
 * 1,READ-MODE<br>
 * 2,WRITE-MODE<br>
 * <p>
 * Three location descriptions:<br>
 * 1, REFERENCE 参考坐标，此数值由UI提供,如 {@link GraffitiBean.GraffitiLayerBean}<br>
 * 2, DEVICE 礼物绘制机制的坐标，比如 IOS提供的数据，如 {@link GraffitiBean.GraffitiLayerBean.GraffitiNoteBean} 中<br>
 * 3, PIXEL 本地坐标，所有本机器的的坐标为含有 PIXEL 字段，如 {@link GraffitiBean.GraffitiLayerBean}<br>
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
            mGraffitiData.updateNoteTotalNumber();
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

    private ShowLayersTask mShowLayersTask;

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
     * Initializing
     *
     * @param initData Must not be null! See {@link GraffitiData}.
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
        // square this view
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (width * GraffitiData.getHeightWidthPercentage()), MeasureSpec.EXACTLY);

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
        if (mGraffitiData != null) {
            if (w != oldw || h != oldh) {
                if (w >= 0 && h >= 0) {
                    if (mGraffitiData.installView(w, h)) {
                        if (mGraffitiData.isReadMode()) {
                            mGraffitiData.installLayers();
                            showLayers();
                        }
                    }
                }
            }
        }
    }

    /**
     * Input -> Data --notifyDataChanged()--> View
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //can not write
        if (!checkWritable()) {
            return true;
        }

        final float pointX = event.getX();
        final float pointY = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (checkWritableActionDown()) {
                mDrawingLayer = mGraffitiData.getDrawingLayer(getCurrentDrawObject());
                if (mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, null, pointX, pointY, mGraffitiData.getNoteLeftNumber()))) {
                    notifyDataChanged(mDrawingLayer, true);
                }
                return true;
            }
        } else if (mDrawingLayer != null) {
            // Checks for the event that occurs
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if (mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, mDrawingLayer.getLast(), pointX, pointY, mGraffitiData.getNoteLeftNumber()))) {
                        notifyDataChanged(mDrawingLayer, true);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, mDrawingLayer.getLast(), pointX, pointY, mGraffitiData.getNoteLeftNumber()))) {
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
            mInternalCallback.onMessage(ICallback.MSG_GRAFFITI_DATA_NOT_INSTALLED);
            throw new IllegalStateException("You have not call#installData(GraffitiData) to install data.");
        }

        if (!mGraffitiData.isViewInstalled()) {
            Log.e(TAG, "GraffitiData is not installed");
            mInternalCallback.onMessage(ICallback.MSG_GRAFFITI_DATA_NOT_INSTALLED);
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
            Log.e(TAG, "player mode, just showLayers notes");
            mInternalCallback.onMessage(ICallback.MSG_READ_MODE);
            return false;
        }

        if (mGraffitiData.getNoteLeftNumber() <= 0) {
            Log.e(TAG, "Reached max note number, can not draw.");
            mInternalCallback.onMessage(ICallback.MSG_MAX_NOTE_REACHED);
            return false;
        }

        if (getCurrentDrawObject() == null) {
            Log.e(TAG, "GraffitiLayerBean is not set, can not draw.");
            mInternalCallback.onMessage(ICallback.MSG_BITMAP_NOT_READY);
            return false;
        }

        if (!mGraffitiData.isBitmapsReady()) {
            Log.e(TAG, "resources is not ready, nothing to draw.");
            mInternalCallback.onMessage(ICallback.MSG_BITMAP_NOT_READY);
            return false;
        }

        if (!mGraffitiData.isBitmapReady(getCurrentDrawObject().getNoteDrawableRes())) {
            Log.e(TAG, "GraffitiLayerBean's resources is not ready, can not draw.");
            mInternalCallback.onMessage(ICallback.MSG_BITMAP_NOT_READY);
            return false;
        }

        return true;
    }

    /**
     * Show Layers if ReadMode
     */
    private class ShowLayersTask implements Runnable {

        final long DELAY = 500;
        int retryCount = 3;
        boolean mCanceled = false;

        @Override
        public void run() {
            if (mCanceled) {
                return;
            }
            if (mGraffitiData == null) {
                //may be set null ?
                mInternalCallback.onMessage(ICallback.MSG_GRAFFITI_DATA_NOT_INSTALLED);
                return;
            }
            if (!mGraffitiData.isBitmapsReady()) {
                Log.e(TAG, "Resources is not ready, can't showLayers!");
                mInternalCallback.onMessage(ICallback.MSG_BITMAP_NOT_READY);
                retry();
                return;
            } else if (!mGraffitiData.isBitmapsReady(mGraffitiData.getLayerNoteBitmapIds())) {
                Log.e(TAG, "Necessary resources is not ready, can't showLayers!");
                mInternalCallback.onMessage(ICallback.MSG_BITMAP_NOT_READY);
                retry();
                return;
            }
            notifyDataChanged();
        }

        private final void retry() {
            if (retryCount > 0) {
                removeCallbacks(this);
                postDelayed(this, DELAY);
                retryCount--;
            }
        }

        public void cancel() {
            removeCallbacks(this);
            mCanceled = true;
        }

        public void start() {
            removeCallbacks(this);
            post(this);
        }
    }

    /**
     * Show layers
     */
    protected void showLayers() {
        if (!getGraffitiData().isReadMode()) {
            throw new RuntimeException("#showLayers can only be called when GraffitiData#isReadMode() true. " +
                    "You can update view call GraffitiView#notifyDataChanged() if you data changed when GraffitiData#isReadMode() false.");
        }
        if (mShowLayersTask != null) {
            mShowLayersTask.cancel();
        }
        mShowLayersTask = new ShowLayersTask();
        mShowLayersTask.start();
    }

    /**
     * Notify data changed, time to update view.
     * <p>
     * Drive part of Data-Drive-Mode.
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
     * Callbacks when internal data changed, errors, and etc.
     */
    public interface ICallback {

        /**
         * GraffitiView has been disabled
         */
        int MSG_DISABLED = 1;

        /**
         * GraffitiView is in ReadMode
         */
        int MSG_READ_MODE = 2;

        /**
         * GraffitiView is not ready for WriteMode
         */
        int MSG_VIEW_NOT_READY = 3;

        /**
         * Bitmap is not ready returned by {@link IBitmapProvider}
         */
        int MSG_BITMAP_NOT_READY = 4;

        /**
         * You have reached the limit in WriteMode, See {@link GraffitiData#mMaxNoteNumber}
         */
        int MSG_MAX_NOTE_REACHED = 5;

        int MSG_GRAFFITI_DATA_NOT_INSTALLED = 6;

        /**
         * @param graffitiView  GraffitiView itself
         * @param drawingObject Current drawing object set by  {@link GraffitiView#setDrawObject(GraffitiBean.GraffitiLayerBean)}.
         *                      May be null if flush change like stopAndClear or something.
         */
        void onDataChanged(GraffitiView graffitiView, GraffitiBean.GraffitiLayerBean drawingObject);


        /**
         * Notify messages.
         * <p>
         * See {@link #MSG_BITMAP_NOT_READY} ...
         *
         * @param msg
         */
        void onMessage(int msg);
    }


    /**
     * Responding for drawing layers, notes.
     */
    private static class GraffitiLayerView extends View {

        final String TAG = GraffitiLayerView.class.getSimpleName() + this;

        private int mBlankCanvas = -1;

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
        }

        /**
         * Getting {@link GraffitiData.GraffitiLayerData}
         *
         * @return
         */
        public GraffitiData.GraffitiLayerData getLayerData() {
            return (GraffitiData.GraffitiLayerData) getTag();
        }

        /**
         * Drive part of Data-Drive-Mode
         */
        public void notifyDataChanged() {
            invalidate();
        }


        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            getLayerData().startAnimatorIfExits(false);
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
                getLayerData().startAnimatorIfExits(false);
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
            Bitmap bitmap = getLayerData().getCalculateBitmap();
            if (bitmap == null || bitmap.isRecycled()) {
                Log.e(TAG, "Error !!! Bitmap is null or recycled !!! DEBUG: id -> " + getLayerData().getNoteBitmapId());
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
                drawNote(canvas, note, bitmap);
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
        protected void drawNote(Canvas canvas, GraffitiData.GraffitiLayerData.GraffitiNoteData note, Bitmap bitmap) {
            onDrawNote(canvas, note, bitmap, note.getCalculateRectF());
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
     * Data of Data-Drive-Mode.
     * <p>
     * Drives {@link GraffitiView} directly.
     * <p>
     * <p>
     * Description of GraffitiView. Managing write/read information, status, controls and etc.
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
         * Bitmap Provider
         */
        private IBitmapProvider mBitmapProvider;

        /**
         * Layers of GraffitiView
         */
        private List<GraffitiLayerData> mLayers = new ArrayList<>();

        /**
         * ReadMode's source
         */
        private GraffitiBean mGraffitiBean;

        /**
         * Max numbers of note you can write onto the GraffitiView
         */
        private int mMaxNoteNumber;

        /**
         * Current total Note number
         */
        private int mTotalNoteNumber = 0;

        /**
         * Used for calculating reference/ui-design values
         */
        private ICoordinateConverter mReferenceCoordinateConverter;

        /**
         * Used for calculating device/bean infos.
         */
        private ICoordinateConverter mDeviceCoordinateConverter;

        private float mCanvasWidth;
        private float mCanvasHeight;

        /**
         * Auto start animations if layers has any animation.
         */
        private boolean mAutoStartAnimationIfExits = false;

        /**
         * Constructor from {@link GraffitiBean}, which should be installed later checked by {@link #isReadMode()}
         *
         * @param graffitiBean ReadMode's data source
         */
        public GraffitiData(IBitmapProvider bitmapManager, GraffitiBean graffitiBean) {
            this(bitmapManager, 0, false);
            mGraffitiBean = graffitiBean;
        }

        /**
         * @param bitmapManager             See {@link IBitmapProvider}
         * @param maxNote                   See {@link GraffitiData#mMaxNoteNumber}
         * @param autoStartAnimationIfExits See {@link GraffitiData#mAutoStartAnimationIfExits}
         */
        public GraffitiData(IBitmapProvider bitmapManager, int maxNote, boolean autoStartAnimationIfExits) {
            if (bitmapManager == null) {
                throw new IllegalArgumentException("Must pass a valid IBitmapManager!");
            }
            mBitmapProvider = bitmapManager;
            mMaxNoteNumber = maxNote;
            mAutoStartAnimationIfExits = autoStartAnimationIfExits;
        }

        /**
         * Called when view's size changed.
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

            mReferenceCoordinateConverter = new SimpleCoordinateConverter(GraffitiBean.ReferenceCanvasWidth, GraffitiBean.ReferenceCanvasHeight, viewWidth, viewHeight);
            mDeviceCoordinateConverter = new SimpleCoordinateConverter(1.0f, 1.0f, viewWidth, viewHeight);

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
            return mReferenceCoordinateConverter != null;
        }

        /**
         * Install layers
         */
        public void installLayers() {
            if (isReadMode()) {
                // Used for calculate note info(most is because ios used different strategy for note)
                GraffitiLayerData layerData = null;
                for (GraffitiBean.GraffitiLayerBean bean : mGraffitiBean.getLayers()) {
                    //try merge layers
                    if (layerData == null || !layerData.isMergeAble(bean)) {
                        layerData = new GraffitiLayerData(bean);
                    }
                    addLayer(layerData);
                }
            }
        }

        /**
         * Whether or not we are in READ-MODE.
         * <p>
         * For some details with READ-MODE see {@link GraffitiData}
         *
         * @return
         */
        public boolean isReadMode() {
            return mGraffitiBean != null && mGraffitiBean.getLayers() != null && mGraffitiBean.getLayers().size() > 0;
        }

        /**
         * Canvas's height/width value.
         *
         * @return
         */
        public static float getHeightWidthPercentage() {
            return GraffitiBean.ReferenceCanvasHeight / GraffitiBean.ReferenceCanvasWidth;
        }

        /**
         * Getting the max number of notes to draw.
         *
         * @return
         */
        public int getNoteMaxNumber() {
            return mMaxNoteNumber;
        }

        /**
         * Getting the left numbers of notes to draw witch is defined by {@link #mMaxNoteNumber}
         *
         * @return
         */
        public int getNoteLeftNumber() {
            if (mMaxNoteNumber <= 0) {
                return Integer.MAX_VALUE;
            }
            return mMaxNoteNumber - getCurrentNoteTotalNumber();
        }

        /**
         * Is all bitmaps ready or not.
         * <p>
         * GraffitiView use this method to check whether we can write/read.
         * <p>
         * See also {@link IBitmapProvider}
         *
         * @return
         */
        public boolean isBitmapsReady() {
            return mBitmapProvider.isBitmapsReady();
        }

        /**
         * Is the specific bitmap ready.
         * <p>
         * GraffitiView use this method to check whether we can write/read.
         * <p>
         * See also {@link IBitmapProvider}
         *
         * @return
         */
        public boolean isBitmapReady(String id) {
            return mBitmapProvider.getBitmap(id) != null;
        }

        /**
         * Is the specific bitmaps ready.
         * <p>
         * GraffitiView use this method to check whether we can write/read.
         * <p>
         * See also {@link IBitmapProvider}
         *
         * @return
         */
        public boolean isBitmapsReady(List<String> ids) {
            if (ids == null) {
                return false;
            }
            for (String url : ids) {
                if (!isBitmapReady(url)) {
                    return false;
                }
            }
            return true;
        }


        /**
         * Start layers animation if it has.
         */
        public void startAnimationsIfExits() {
            for (GraffitiData.GraffitiLayerData layerData : getLayers()) {
                layerData.startAnimatorIfExits(true);
            }
        }

        /**
         * Adding layer
         *
         * @param data
         */
        private void addLayer(GraffitiLayerData data) {
            if (data == null) {
                return;
            }
            if (mLayers.contains(data)) {
                throw new IllegalArgumentException("layer already in GraffitiData");
            }
            mLayers.add(data);
            updateNoteTotalNumber();
        }

        /**
         * Removing layer
         *
         * @param data
         */
        public void removeLayer(GraffitiLayerData data) {
            if (data != null) {
                mLayers.remove(data);
            }
            updateNoteTotalNumber();
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
            updateNoteTotalNumber();
        }

        /**
         * Is we merge layers if possible.
         * <p>
         * Working with {@link GraffitiLayerData#isMergeAble(GraffitiBean.GraffitiLayerBean)}.
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
         * Updating current total number of note
         *
         * @return
         */
        public int updateNoteTotalNumber() {
            int total = 0;
            for (GraffitiLayerData layerData : mLayers) {
                total += layerData.getNotes().size();
            }
            mTotalNoteNumber = total;
            return mTotalNoteNumber;
        }

        /**
         * Getting current total number of note
         *
         * @return
         */
        public int getCurrentNoteTotalNumber() {
            if (mTotalNoteNumber <= 0 && getLayerCount() > 0) {
                return updateNoteTotalNumber();
            }
            return mTotalNoteNumber;
        }

        /**
         * Getting ids of {@link GraffitiLayerData#getNoteBitmapId()} of {@link #getLayers()}
         *
         * @return
         */
        public List getLayerNoteBitmapIds() {
            if (mLayers != null && mLayers.size() > 0) {
                List<String> list = new ArrayList<>();
                for (GraffitiLayerData layerData : mLayers) {
                    String id = layerData.getNoteBitmapId();
                    if (!list.contains(id)) {
                        list.add(id);
                    }
                }
                return list;
            }
            return null;
        }

        /**
         * Getting the drawing layer witch to write next.
         *
         * @param layerBean
         * @return
         */
        public GraffitiLayerData getDrawingLayer(GraffitiBean.GraffitiLayerBean layerBean) {
            GraffitiLayerData lastLayer = getLastLayer();
            if (lastLayer != null && isMergeLayer() && lastLayer.isMergeAble(layerBean)) {
                return lastLayer;
            }

            //new one
            GraffitiLayerData layerData = new GraffitiLayerData(layerBean);
            addLayer(layerData);
            return layerData;
        }

        public float getCanvasWidth() {
            return mCanvasWidth;
        }

        public float getCanvasHeight() {
            return mCanvasHeight;
        }


        /**
         * Data of Data-Drive-Mode
         * <p>
         * Drives {@link GraffitiLayerView} directly.
         * <p>
         * Managers the layers information, like Note descriptions, notes and etc.
         * <p>
         * See also {@link GraffitiBean.GraffitiLayerBean}
         */
        public class GraffitiLayerData {

            final String TAG = GraffitiLayerData.class.getSimpleName();

            private GraffitiBean.GraffitiLayerBean mLayerBean;

            private float mNoteWidth;
            private float mNoteHeight;

            private float mNoteDistance;

            private List<GraffitiNoteData> mNotes = new ArrayList<>();

            public static final int MASK_REDRAW = 0x00000011;

            public static final int FLAG_REDRAW_ALL = 0x1 << 0;
            public static final int FLAG_REDRAW_ONCE = 0x1 << 1;

            private int mFlag = 0;

            private AnimatorFactory.AbstractBaseAnimator mAnimator; // 是否有动画

            private Bitmap mBitmap;
            private Bitmap[] mBitmaps;

            public GraffitiLayerData(GraffitiBean.GraffitiLayerBean layerBean) {
                if (mReferenceCoordinateConverter == null) {
                    throw new IllegalArgumentException("ICoordinateConverter must not be null!");
                }

                if (layerBean == null) {
                    throw new IllegalArgumentException("Must support a valid GraffitiBean.GraffitiLayerBean");
                }
                mLayerBean = layerBean;

                installView();

                //install notes if needed
                if (isReadMode()) {
                    installNotes();
                }

                //init in the first place
                getNoteBitmap(-1);
            }

            private void installView() {
                mNoteWidth = GraffitiData.this.mReferenceCoordinateConverter.convertWidthTargetToPixel(mLayerBean.getPercentageNoteWidth());
                mNoteHeight = GraffitiData.this.mReferenceCoordinateConverter.convertHeightTargetToPixel(mLayerBean.getPercentageNoteHeight());
                mNoteDistance = GraffitiData.this.mReferenceCoordinateConverter.convertHeightTargetToPixel(mLayerBean.getPercentageNoteDistance());
            }


            /**
             * Is view installed or not.
             *
             * @return
             */
            public boolean isViewInstalled() {
                return GraffitiData.this.mReferenceCoordinateConverter != null;
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
            private void installNotes() {
                if (mDeviceCoordinateConverter == null) {
                    throw new IllegalStateException("How could mDeviceCoordinateConverter be null? Can not get pixel values from beans");
                }
                ICoordinateConverter noteConverter = mDeviceCoordinateConverter;
                for (GraffitiBean.GraffitiLayerBean.GraffitiNoteBean noteBean : mLayerBean.getNotes()) {
                    GraffitiNoteData noteData = new GraffitiNoteData(noteConverter.convertWidthTargetToPixel(noteBean.getDeviceX()), noteConverter.convertHeightTargetToPixel(noteBean.getDeviceY()));
                    addNote(noteData);
                }
            }

            /**
             * InstallView animator
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
            public GraffitiNoteData getLast() {
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

            public String getNoteBitmapId() {
                return mLayerBean.getNoteDrawableRes();
            }

            /**
             * Getting the Bitmap/Animated Bitmap
             *
             * @param timeLine from [0 ~ frame-duration], used for fetch frame bitmap of bitmaps
             * @return
             */
            public Bitmap getNoteBitmap(long timeLine) {
                if (mBitmap == null && mBitmaps == null) {
                    //refresh bitmaps if none
                    Object bitmapObject = mBitmapProvider.getBitmap(getNoteBitmapId());
                    if (bitmapObject instanceof Bitmap) {
                        mBitmap = (Bitmap) bitmapObject;
                    } else if (bitmapObject instanceof Bitmap[]) {
                        mBitmaps = (Bitmap[]) bitmapObject;
                    }
                }
                if (mBitmap != null) {
                    return mBitmap;
                } else if (mBitmaps != null && mBitmaps.length > 0) {
                    int frameIndex = 0;
                    if (timeLine > 0) {
                        frameIndex = (int) (timeLine / (getGraffitiLayerBean().getAnimationDuration() / mBitmaps.length));
                        frameIndex %= mBitmaps.length;
                    }
                    return mBitmaps[frameIndex];
                }
                return null;
            }

            /**
             * Getting calculate bitmap of all notes
             *
             * @return
             */
            public Bitmap getCalculateBitmap() {
                return mAnimator == null ? getNoteBitmap(-1) : mAnimator.getAnimateBitmap();
            }

            /**
             * Getting {@link GraffitiBean.GraffitiLayerBean}
             *
             * @return
             */
            public GraffitiBean.GraffitiLayerBean getGraffitiLayerBean() {
                return mLayerBean;
            }


            /**
             * Is current mergeAble to bean passed.
             * <p>
             * <p>
             * See also {@link GraffitiData#isMergeLayer()}
             *
             * @param bean
             * @return
             */
            public boolean isMergeAble(GraffitiBean.GraffitiLayerBean bean) {
                return mLayerBean.equals(bean) && mNotes.size() < 100;
            }

            /**
             * Current layer has animation or not.
             *
             * @return
             */
            public boolean isHasAnimation() {
                return mLayerBean.getAnimation() > 0;
            }

            /**
             * Getting duration of Animation
             *
             * @return
             */
            public long getAnimationDuration() {
                return mLayerBean.getAnimationDuration();
            }

            /**
             * Add note
             *
             * @param note
             */
            public void addNote(GraffitiNoteData note) {
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

            @Deprecated
            protected void setFlag(int mask, int flag) {
                mFlag = (mFlag & ~mask) | flag;
            }

            /**
             * Force draw all notes
             *
             * @param onlyOnce
             */
            @Deprecated
            public void forceDrawAll(boolean onlyOnce) {
                if (onlyOnce) {
                    setFlag(MASK_REDRAW, FLAG_REDRAW_ALL | FLAG_REDRAW_ONCE);
                } else {
                    setFlag(MASK_REDRAW, FLAG_REDRAW_ALL | ~FLAG_REDRAW_ONCE);
                }
            }

            /**
             * Is forcing drawing all
             *
             * @return
             */
            @Deprecated
            public boolean isForceDrawAll() {
                return (mFlag & FLAG_REDRAW_ALL) == 1;
            }

            /**
             * Finish draw all
             */
            @Deprecated
            public void finishForceDrawAll(boolean forceFinish) {
                if ((mFlag & FLAG_REDRAW_ONCE) == 1 || forceFinish) {
                    setFlag(MASK_REDRAW, ~FLAG_REDRAW_ALL | ~FLAG_REDRAW_ONCE);
                }
            }

            /**
             * Start animation if we has any.
             *
             * @param fromUser true ignores {@link GraffitiData#mAutoStartAnimationIfExits}.
             */
            public void startAnimatorIfExits(boolean fromUser) {
                if (!fromUser && !mAutoStartAnimationIfExits) {
                    return;
                }
                mAutoStartAnimationIfExits = true;
                if (mAnimator != null) {
                    mAnimator.start();
                }
            }

            /**
             * Stop animation if we has any.
             */
            public void stopAnimatorIfExits() {
                if (mAnimator != null) {
                    mAnimator.stop();
                }
            }

            @Override
            public String toString() {
                return "[" +
                        "mLayerBean:" + mLayerBean +
                        ",mNoteWidth:" + mNoteWidth +
                        ",mNoteHeight:" + mNoteHeight +
                        ",mNoteDistance:" + mNoteDistance +
                        ",mNotes:" + mNotes +
                        ",mReferenceCoordinateConverter:" + mReferenceCoordinateConverter +
                        ",mAnimator:" + mAnimator +
                        "]";
            }


            /**
             * Data of Data-Drive-Mode
             * <p>
             * Descriptions of location/bitmaps of our drawing note of specific {@link GraffitiLayerData}.
             * <p>
             * See also {@link GraffitiBean.GraffitiLayerBean.GraffitiNoteBean}
             */
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

                /**
                 * Get the RectF of no animations.
                 *
                 * @return
                 */
                public RectF getOriginalRectF() {
                    return mOriginalRectF;
                }

                /**
                 * Get the animated RectF
                 *
                 * @return
                 */
                public RectF getCalculateRectF() {
                    return mAnimator == null ? getOriginalRectF() : mAnimator.getAnimateRectF(getOriginalRectF(), mCalculateRectF);
                }

                /**
                 * Getting the {@link ICoordinateConverter} of this note. Needed by {@link GraffitiBean.GraffitiLayerBean.GraffitiNoteBean} for IOS.
                 *
                 * @return
                 */
                public ICoordinateConverter getCoordinateConverter() {
                    return mDeviceCoordinateConverter;
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
     * The calculator of Next points of {@link GraffitiData.GraffitiLayerData.GraffitiNoteData}.
     */
    public interface INextNoteCalculator {


        /**
         * Calculating ...
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

        private List<GraffitiData.GraffitiLayerData.GraffitiNoteData> mPool = new ArrayList<>();

        public SimpleNextNoteCalculator() {

        }

        @Override
        public List<GraffitiData.GraffitiLayerData.GraffitiNoteData> next(GraffitiData.GraffitiLayerData layer, GraffitiData.GraffitiLayerData.GraffitiNoteData relative, float x, float y, int maxNotes) {
            if (relative == null) {
                GraffitiData.GraffitiLayerData.GraffitiNoteData note = layer.new GraffitiNoteData(x, y);
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
                        mPool.add(note);
                    }
                    return mPool;
                }
            }
            return null;
        }
    }

    /**
     * Coordinate converters.
     * <p>
     * See {@link GraffitiView} 's doc for more detail.
     * <p>
     * 1,convert current-device's pixel value to/from target(REFERENCE/DEVICE/BEAN) value.
     */
    public interface ICoordinateConverter {

        /**
         * convert current-device's width pixel value to target(REFERENCE/DEVICE/BEAN) value
         *
         * @param widthPixel
         * @return
         */
        float convertWidthPixelToTarget(float widthPixel);


        /**
         * convert current-device's width pixel value from target(REFERENCE/DEVICE/BEAN) value
         *
         * @param widthTarget
         * @return
         */
        float convertWidthTargetToPixel(float widthTarget);

        /**
         * convert current-device's height pixel value to target(REFERENCE/DEVICE/BEAN) value
         *
         * @param heightPixel
         * @return
         */
        float convertHeightPixelToTarget(float heightPixel);


        /**
         * convert current-device's height pixel value from target(REFERENCE/DEVICE/BEAN) value
         *
         * @param heightTarget
         * @return
         */
        float convertHeightTargetToPixel(float heightTarget);

    }


    /**
     * Simple impl of {@link ICoordinateConverter}
     */
    public static class SimpleCoordinateConverter implements ICoordinateConverter {

        final float mWidthFactor;
        final float mHeightFactor;

        public SimpleCoordinateConverter(float targetWidth, float targetHeight, float viewWidth, float viewHeight) {
            mWidthFactor = targetWidth / viewWidth;
            mHeightFactor = targetHeight / viewHeight;
        }

        @Override
        public float convertWidthPixelToTarget(float widthPixel) {
            return widthPixel * mWidthFactor;
        }

        @Override
        public float convertWidthTargetToPixel(float widthTarget) {
            return widthTarget / mWidthFactor;
        }

        @Override
        public float convertHeightPixelToTarget(float heightPixel) {
            return heightPixel * mHeightFactor;
        }

        @Override
        public float convertHeightTargetToPixel(float heightTarget) {
            return heightTarget / mHeightFactor;
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
        public final static int FRAME = 5;
        //weibo use
        public final static int SHAKE_RETATE = 6;

        public static AbstractBaseAnimator create(GraffitiData.GraffitiLayerData data, Runnable updateViewRunnable) {
            switch (data.getGraffitiLayerBean().getAnimation()) {
                case SCALE:
                    return new ScaleAnimator(data, updateViewRunnable, data.getAnimationDuration(), 1.0f, 0.5f);
                case FRAME:
                    return new FrameAnimator(data, updateViewRunnable, data.getAnimationDuration());
            }
            return new ScaleAnimator(data, updateViewRunnable, data.getAnimationDuration(), 1.0f, 0.5f);
        }


        /**
         * Animator interaction for {@link GraffitiView}
         * <p>
         * 1, Powered by {@link ObjectAnimator}
         * 2, Size/Location animation support
         * 3, Bitmap frame support
         */
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

            static InternalAlignClock mAlignClock = ENABLE_ALIGN_CLOCK ? new InternalAlignClock() : null;

            private final ObjectAnimator mAnimator;

            private float mCurrentValue;

            private static final long ANIMATION_FRAME_TIME = 1000 / 45;

            private long mLastUpdateTime = 0;
            private float mFrom;

            private GraffitiData.GraffitiLayerData mLayerData;

            public AbstractBaseAnimator(GraffitiData.GraffitiLayerData layerData, Runnable updateViewRunnable, long duration, float from, float to) {
                if (layerData == null) {
                    throw new IllegalArgumentException("GraffitiLayerData must not be null !");
                }
                if (updateViewRunnable == null) {
                    throw new IllegalArgumentException("updateViewRunnable must not be null !");
                }
                mLayerData = layerData;
                mUpdateViewRunnable = updateViewRunnable;
                mFrom = from;

                ObjectAnimator animator = ObjectAnimator.ofFloat(AbstractBaseAnimator.this, "value", from, to);
                animator.setDuration(duration);
                animator.addListener(this);
                animator.addUpdateListener(this);
                onConfigAnimator(animator);
                mAnimator = animator;

                setValue(from);
            }


            /**
             * On configuring our internal {@link ObjectAnimator}
             *
             * @param animator
             */
            protected void onConfigAnimator(ObjectAnimator animator) {
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setRepeatMode(ValueAnimator.REVERSE);
            }


            /**
             * Start animation.
             */
            public final void start() {
                if (mAnimator.isRunning() || mAnimator.isStarted()) {
                    return;
                }
                mAnimator.start();
            }

            /**
             * Stop animation.
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
             * On calculating target rectF
             *
             * @param input
             * @param out
             * @param currentValue
             * @return target
             */
            protected abstract RectF onCalculateRectF(RectF input, RectF out, float currentValue);


            /**
             * Calculate animated Bitmap
             *
             * @return
             */
            public final Bitmap getAnimateBitmap() {
                return mLayerData.getNoteBitmap(onCalculateBitmapTimeLine(mAnimator.getDuration(), mCurrentValue));
            }

            /**
             * On calculating target Bitmap.
             * <p>
             * Default no animated bitmaps.
             *
             * @return
             */
            protected long onCalculateBitmapTimeLine(long duration, float currentValue) {
                return -1;
            }


            @SuppressWarnings("unused")
            public final void setValue(float value) {
                mCurrentValue = value;
            }


            @Override
            public void onAnimationStart(Animator animation) {
                setValue(mFrom);
                mLayerData.forceDrawAll(false);
                if (mAlignClock != null) {
                    mAlignClock.start(mUpdateViewRunnable);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setValue(mFrom);
                notifyView();
                if (mAlignClock != null) {
                    mAlignClock.stop(mUpdateViewRunnable);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                setValue(mFrom);
                notifyView();
                if (mAlignClock != null) {
                    mAlignClock.stop(mUpdateViewRunnable);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //do nothing
            }

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mAlignClock == null) {
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


        /**
         * Demo impl
         */
        public static class ScaleAnimator extends AbstractBaseAnimator {

            public ScaleAnimator(GraffitiData.GraffitiLayerData layerData, Runnable updateViewRunnable, long duration, float from, float to) {
                super(layerData, updateViewRunnable, duration, from, to);
            }

            @Override
            protected RectF onCalculateRectF(RectF input, RectF out, float currentValue) {
                //TODO 1,TransFormer 2,Matrix for test
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

        /**
         * Demo impl
         */
        public static class FrameAnimator extends AbstractBaseAnimator {

            public FrameAnimator(GraffitiData.GraffitiLayerData layerData, Runnable updateViewRunnable, long duration) {
                super(layerData, updateViewRunnable, duration, 0.0f, 1.0f);
            }

            @Override
            protected void onConfigAnimator(ObjectAnimator animator) {
                super.onConfigAnimator(animator);

                //config default animator
                animator.setInterpolator(new LinearInterpolator());
                animator.setRepeatMode(ValueAnimator.RESTART);
            }

            @Override
            protected RectF onCalculateRectF(RectF input, RectF out, float currentValue) {
                //This is a
                return input;
            }

            @Override
            protected long onCalculateBitmapTimeLine(long duration, float currentValue) {
                return (long) (duration * currentValue);
            }
        }
    }

    /**
     * Manage bitmaps for {@link GraffitiView} {@link GraffitiLayerView}, managed by {@link GraffitiData} {@link GraffitiData.GraffitiLayerData}
     * <p>
     * We use this class to get bitmaps of our layers.
     */
    public interface IBitmapProvider {

        /**
         * Getting bitmap
         *
         * @param id id of this layer. See {@link GraffitiData.GraffitiLayerData#getNoteBitmapId()}
         * @return Bitmap if only one bitmap; Bitmap[] if FrameAnimation like bitmaps
         */
        Object getBitmap(String id);

        /**
         * Have we all bitmaps ready or not.
         *
         * @return
         */
        boolean isBitmapsReady();
    }


}
