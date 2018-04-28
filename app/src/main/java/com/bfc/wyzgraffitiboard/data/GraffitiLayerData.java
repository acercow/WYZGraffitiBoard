package com.bfc.wyzgraffitiboard.data;

import com.bfc.wyzgraffitiboard.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/4/28.
 */

public class GraffitiLayerData {

    public static final boolean mOptimizeAnimation = true;

    public String id; //礼物id

    public int mType; // 0手绘 1图片 2其他

    public boolean mAnimation; // 是否有动画

    public int mCount; //总数

    public String mExtra;


    public float mWidth = 100.0f; //画布宽度
    public float mHegith = 100.0f; //画布高度

    public int mIconRes = R.drawable.shield_icon;


    public List<GraffitiNote> mNotes = new ArrayList<>();


    /**
     * Total points
     *
     * @return
     */
    public int getCount() {
        return mNotes == null ? 0 : mNotes.size();
    }


    public List<GraffitiNote> getNotes() {
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
    public GraffitiNote getLast() {
        if (getCount() > 0) {
            return mNotes.get(mNotes.size() - 1);
        }
        return null;
    }


    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHegith;
    }


    public boolean mergeAble() {
        return mAnimation && mOptimizeAnimation;
    }

    public static class GraffitiNote {

        public float mX;
        public float mY;

        /**
         * 是否渲染
         */
        public boolean mDrawn = false;


        public GraffitiNote(float x, float y) {
            mX = x;
            mY = y;
            mDrawn = false;
        }

    }


    public void addNote(GraffitiNote note) {
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
    public boolean addNote(List<GraffitiNote> notes) {
        if (notes == null || notes.size() < 0) {
            return false;
        }
        mNotes.addAll(notes);
        return true;
    }


    public static GraffitiLayerData generateDefault() {
        return new GraffitiLayerData();
    }


}
