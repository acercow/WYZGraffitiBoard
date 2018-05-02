package com.bfc.wyzgraffitiboard.data;

import com.bfc.wyzgraffitiboard.R;
import com.bfc.wyzgraffitiboard.animation.AbstractBaseAnimator;
import com.bfc.wyzgraffitiboard.animation.AnimatorFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * 我们通过相对比例来存储理解所有的信息，在绘制的时候，通过 坐标转换 系统来得到具体的值
 */

public class GraffitiLayerData {

    public static final int MASK_REDRAW = 0x00000011;

    public static final int FLAG_REDRAW_ALL = 0x1 << 0;
    public static final int FLAG_REDRAW_ONCE = 0x1 << 1;


    public float mWidth = 100.0f; //画布宽度
    public float mHeight = 100.0f; //画布高度

    private int mFlag = 0;

    private String id; //礼物id

    public int mType; // 0手绘 1图片 2其他

    public int mCount; //总数

    public String mExtra;

    public int mIconRes = R.drawable.shield_icon;

    public List<GraffitiNoteData> mNotes = new ArrayList<>();

    private int mAnimation = AnimatorFactory.SCALE;

    public AbstractBaseAnimator mAnimator; // 是否有动画

    public GraffitiLayerData() {

    }

    /**
     * Total points
     *
     * @return
     */
    public int getCount() {
        return mNotes == null ? 0 : mNotes.size();
    }


    public List<GraffitiNoteData> getNotes() {
        return mNotes;
    }

    private boolean changed() {
        return true;
    }

    /**
     * Getting last note
     *
     * @return
     */
    public GraffitiNoteData getLast() {
        if (getCount() > 0) {
            return mNotes.get(mNotes.size() - 1);
        }
        return null;
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
    }

    public float getNoteWidth() {
        return 10;
    }

    public float getNoteHeight() {
        return 10;
    }

    public boolean mergeAble() {
        return !hasAnimation();
    }

    public boolean hasAnimation() {
        return mAnimation > 0;
    }

    public void installAnimator(AbstractBaseAnimator animator) {
        mAnimator = animator;
        setFlag(MASK_REDRAW, FLAG_REDRAW_ALL | ~FLAG_REDRAW_ONCE);
    }

    public void uninstallAnimator() {
        mAnimator = null;
        setFlag(MASK_REDRAW, FLAG_REDRAW_ALL | FLAG_REDRAW_ONCE);
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


    public static GraffitiLayerData generateDefault() {
        return new GraffitiLayerData();
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


}
