package com.sina.weibo.view.graffitiview.calculator;

import android.util.Log;

import com.sina.weibo.view.graffitiview.data.GraffitiLayerData;
import com.sina.weibo.view.graffitiview.data.GraffitiNoteData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/4/28.
 */

public class SimpleNextNoteCalculator implements INextNoteCalculator {

    static final String TAG = SimpleNextNoteCalculator.class.getSimpleName();

    /**
     * 优化内存，用于返回 数据
     */
    private List<GraffitiNoteData> mPool = new ArrayList<>();

    public SimpleNextNoteCalculator() {

    }

    @Override
    public List<GraffitiNoteData> next(GraffitiLayerData layer, GraffitiNoteData relative, float x, float y) {
        if (relative == null) {
            GraffitiNoteData note = new GraffitiNoteData(layer, x, y);
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
                    GraffitiNoteData note = new GraffitiNoteData(layer, lastX + gapX * i, lastY + gapY * i);
                    Log.e(TAG, "add note -> " + note);
                    mPool.add(note);
                }
                return mPool;
            }
        }
        return null;
    }
}
