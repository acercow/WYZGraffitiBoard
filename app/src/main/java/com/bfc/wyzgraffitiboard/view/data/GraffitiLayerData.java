package com.bfc.wyzgraffitiboard.view.data;

import android.util.Log;

import com.bfc.wyzgraffitiboard.bean.GraffitiLayerBean;
import com.bfc.wyzgraffitiboard.bean.GraffitiNoteBean;
import com.bfc.wyzgraffitiboard.view.animation.AbstractBaseAnimator;
import com.bfc.wyzgraffitiboard.view.animation.AnimatorFactory;
import com.bfc.wyzgraffitiboard.view.coordinates.ICoordinateConverter;
import com.bfc.wyzgraffitiboard.view.coordinates.SimpleCoordinateConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * <p>
 */

public class GraffitiLayerData {

    static final String TAG = GraffitiLayerData.class.getSimpleName();

    private GraffitiLayerBean mLayerBean;

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

    AbstractBaseAnimator mAnimator; // 是否有动画
    ICoordinateConverter mCoordinateConverter;

    public GraffitiLayerData(GraffitiLayerBean layerBean) {
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
     * @return any {@link GraffitiNoteData} has been added from {@link GraffitiLayerBean}
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
            for (GraffitiNoteBean noteBean : mLayerBean.getNotes()) {
                GraffitiNoteData noteData = new GraffitiNoteData(this, noteBean);
                mNotes.add(noteData);
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
    public GraffitiNoteData getLast() {
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
     * Getting internal {@link GraffitiLayerBean}
     *
     * @return
     */
    public GraffitiLayerBean getGraffitiLayerBean() {
        return mLayerBean;
    }

    @Deprecated
    public boolean isMergeAble(GraffitiLayerData layerData) {
        return !isHasAnimation() && mLayerBean.equals(layerData.getGraffitiLayerBean());
    }

    public boolean isHasAnimation() {
        return mLayerBean.getAnimation() > 0;
    }

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

}
