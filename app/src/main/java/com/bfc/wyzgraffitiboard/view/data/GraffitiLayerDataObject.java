package com.bfc.wyzgraffitiboard.view.data;

import android.util.Log;

import com.bfc.wyzgraffitiboard.bean.GraffitiLayerBean;
import com.bfc.wyzgraffitiboard.view.animation.AbstractBaseAnimator;
import com.bfc.wyzgraffitiboard.view.coordinates.ICoordinateConverter;
import com.bfc.wyzgraffitiboard.view.coordinates.SimpleCoordinateConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * <p>
 * Percentage level
 */

public class GraffitiLayerDataObject {

    static final String TAG = GraffitiLayerDataObject.class.getSimpleName();

    private GraffitiLayerBean mLayerBean;

    private float mCanvasWidth; //画布宽度
    private float mCanvasHeight; //画布高度

    private float mNoteWidth;
    private float mNoteHeight;

    private float mNoteDistance;

    private List<GraffitiNoteDataObject> mNotes = new ArrayList<>();

    public static final int MASK_REDRAW = 0x00000011;

    public static final int FLAG_REDRAW_ALL = 0x1 << 0;
    public static final int FLAG_REDRAW_ONCE = 0x1 << 1;

    private int mFlag = 0;

    AbstractBaseAnimator mAnimator; // 是否有动画
    ICoordinateConverter mCoordinateConverter;

    public GraffitiLayerDataObject(GraffitiLayerBean layerBean) {
        if (layerBean == null) {
            throw new IllegalArgumentException("Must support a valid GraffitiLayerBean");
        }
        mLayerBean = layerBean;
    }

    /**
     * Must called when view size changed
     *
     * @param viewWidth
     * @param viewHeight
     */
    public void initialize(float viewWidth, float viewHeight) {
        if (viewWidth <= 0 || viewHeight <= 0) {
            throw new IllegalArgumentException("viewWidth or viewHeight must > 0, viewWidth -> " + viewWidth + " viewHeight -> " + viewHeight);
        }

        if (viewWidth == mCanvasWidth && viewHeight == mCanvasHeight) {
            //init already
            return;
        }

        mCoordinateConverter = new SimpleCoordinateConverter(mLayerBean.getPercentageCanvasWidth(), mLayerBean.getPercentageCanvasHeight(), viewWidth, viewHeight);

        mCanvasWidth = viewWidth;
        mCanvasHeight = viewHeight;

        mNoteWidth = mCoordinateConverter.convertWidthPercentageToPixel(mLayerBean.getPercentageNoteWidth());
        mNoteHeight = mCoordinateConverter.convertHeightPercentageToPixel(mLayerBean.getPercentageNoteHeight());
        mNoteDistance = mCoordinateConverter.convertHeightPercentageToPixel(mLayerBean.getPercentageNoteDistance());

        Log.e(TAG, "initialized... \n" + this.toString());

    }

    /**
     * Total points
     *
     * @return
     */
    public int getCount() {
        return mNotes == null ? 0 : mNotes.size();
    }

    public List<GraffitiNoteDataObject> getNotes() {
        return mNotes;
    }

    /**
     * Getting last note
     *
     * @return
     */
    public GraffitiNoteDataObject getLast() {
        if (getCount() > 0) {
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

    public boolean isMergeAble() {
        return !isHasAnimation();
    }

    public boolean isHasAnimation() {
        return mLayerBean.getAnimation() > 0;
    }

    public void addNote(GraffitiNoteDataObject note) {
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
    public boolean addNote(List<GraffitiNoteDataObject> notes) {
        if (notes == null || notes.size() < 0) {
            return false;
        }
        mNotes.addAll(notes);
        return true;
    }


    public void installAnimator(AbstractBaseAnimator animator) {
        mAnimator = animator;
        setFlag(MASK_REDRAW, FLAG_REDRAW_ALL | ~FLAG_REDRAW_ONCE);
    }

    public void uninstallAnimator() {
        mAnimator = null;
        setFlag(MASK_REDRAW, FLAG_REDRAW_ALL | FLAG_REDRAW_ONCE);
    }


    public boolean isForceDrawAll() {
        return (mFlag & FLAG_REDRAW_ALL) == 1;
    }

    /**
     * Force draw all
     * <p>
     * See {@link #FLAG_REDRAW_ONCE}, {@link #FLAG_REDRAW_ALL}
     *
     * @param mask
     * @param flag
     */
    public void setFlag(int mask, int flag) {
        mFlag = (mFlag & ~mask) | flag;
    }

    /**
     * Finish draw all
     */
    public void finishForceDrawAll(boolean forceFinish) {
        if ((mFlag & FLAG_REDRAW_ONCE) == 1 || forceFinish) {
            setFlag(MASK_REDRAW, ~FLAG_REDRAW_ALL | ~FLAG_REDRAW_ONCE);
        }
    }

    @Override
    public String toString() {
        return "[" +
                "mCanvasWidth:" + mCanvasWidth +
                ",mCanvasHeight:" + mCanvasHeight +
                ",mNoteWidth:" + mNoteWidth +
                ",mNoteHeight:" + mNoteHeight +
                ",mNoteDistance:" + mNoteDistance +
                "]";
    }

}
