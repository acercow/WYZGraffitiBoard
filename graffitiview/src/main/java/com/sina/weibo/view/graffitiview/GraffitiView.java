package com.sina.weibo.view.graffitiview;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
 */
public class GraffitiView extends ViewGroup {

    static final String TAG = GraffitiView.class.getSimpleName();

    private INextNoteCalculator mNoteCalculator;

    private GraffitiData mGraffitiData;
    private GraffitiData.GraffitiLayerData mDrawingLayer;

    private GraffitiBean.GraffitiLayerBean mDrawObject;

    private IOnDataChangedCallback mOnDataChangedCallback;

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
     * 初始化，GraffitiData 带有配置参数，做相应配置
     *
     * @param initData
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
        if (initData.isShowMode()) {
            post(new Runnable() {
                @Override
                public void run() {
                    notifyDataChanged();
                }
            });
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
            return super.onTouchEvent(event);
        }

        if (mGraffitiData.isShowMode()) {
            Log.e(TAG, "player mode, just show notes");
            return super.onTouchEvent(event);
        }

        if (getMeasuredHeight() == 0 || getMeasuredWidth() == 0) {
            Log.e(TAG, "width or height is 0, getMeasuredWidth() -> " + getMeasuredWidth() + " getMeasuredHeight() -> " + getMeasuredHeight());
            return super.onTouchEvent(event);
        }

        if (getCurrentDrawObject() == null) {
            Log.e(TAG, "no GraffitiLayerBean set, nothing to draw.");
            return super.onTouchEvent(event);
        }

        final float pointX = event.getX();
        final float pointY = event.getY();

        // Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mDrawingLayer != null) {
                    throw new RuntimeException("How could mDrawingLayer not be null !");
                }
                mDrawingLayer = mGraffitiData.getDrawingLayer(getCurrentDrawObject());
                mDrawingLayer.installView(getMeasuredWidth(), getMeasuredHeight());
                if (mDrawingLayer.addNote(mNoteCalculator.next(mDrawingLayer, mDrawingLayer.getLast(), pointX, pointY))) {
                    notifyDataChanged(mDrawingLayer, true);
                }
                return true;
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
                if (mOnDataChangedCallback != null) {
                    mOnDataChangedCallback.onDataChanged(GraffitiView.this, null);
                }
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

        if (notifyListener && mOnDataChangedCallback != null) {
            mOnDataChangedCallback.onDataChanged(this, layerData.getGraffitiLayerBean());
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
     * Setting {@link IOnDataChangedCallback}
     *
     * @param callback
     */
    public void setOnDataChangedCallback(IOnDataChangedCallback callback) {
        mOnDataChangedCallback = callback;
    }

    /**
     * Callbacks when internal data changed.
     */
    public interface IOnDataChangedCallback {

        /**
         * @param graffitiView  GraffitiView itself
         * @param drawingObject Current drawing object set by  {@link GraffitiView#setDrawObject(GraffitiBean.GraffitiLayerBean)}.
         *                      May be null if flush change like clear or something.
         */
        void onDataChanged(GraffitiView graffitiView, GraffitiBean.GraffitiLayerBean drawingObject);
    }


    /**
     * Layer view
     */
    private static class GraffitiLayerView extends View {

        final String TAG = GraffitiLayerView.class.getSimpleName() + this;

        private Bitmap mGiftIcon;
        private int mBlankCanvas = -1;

        public GraffitiLayerView(Context context, GraffitiData.GraffitiLayerData data) {
            super(context);
            setTag(data);
            mGiftIcon = BitmapFactory.decodeResource(getResources(), data.getNoteDrawableRes());

            // installView animator
            getLayerData().installAnimator(new Runnable() {
                @Override
                public void run() {
                    notifyDataChanged();
                }
            });
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
                        if (getLayerData().isShowMode()) {
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
            if (mGiftIcon != null && !mGiftIcon.isRecycled()) {
                try {
                    mGiftIcon.recycle();
                } catch (Error e) {
                    e.printStackTrace();
                }
            }
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
            onDrawNote(canvas, note, mGiftIcon, note.getCalculateRectF());
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
        private List<GraffitiData.GraffitiLayerData> mLayers = new ArrayList<>();

        private GraffitiBean mSource;

        /**
         * Constructor from {@link GraffitiBean}, which should be installed later checked by {@link #isShowMode()}
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
         * Whether or not we are in SHOW-MODE or note.
         * <p>
         * For some details with SHOW-MODE see {@link GraffitiData}
         *
         * @return
         */
        public boolean isShowMode() {
            return mSource != null;
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

        public List<GraffitiData.GraffitiLayerData> getLayers() {
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

            private List<GraffitiData.GraffitiLayerData.GraffitiNoteData> mNotes = new ArrayList<>();

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
             * Cooperate with {@link GraffitiData#isShowMode()}.
             *
             * @return
             */
            public boolean isShowMode() {
                return mLayerBean.getNotes() != null && mLayerBean.getNotes().size() > 0;
            }


            /**
             * Install notes
             */
            public void installNotes() {
                if (isShowMode()) {
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

            public List<GraffitiData.GraffitiLayerData.GraffitiNoteData> getNotes() {
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

            public int getNoteDrawableRes() {
                return mLayerBean.getNoteDrawableRes();
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
            public boolean addNote(List<GraffitiData.GraffitiLayerData.GraffitiNoteData> notes) {
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
                        GraffitiData.GraffitiLayerData.GraffitiNoteData note = new GraffitiData.GraffitiLayerData.GraffitiNoteData(layer, lastX + gapX - layer.getNoteWidth(), lastY + gapY - layer.getNoteHeight());
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
}
