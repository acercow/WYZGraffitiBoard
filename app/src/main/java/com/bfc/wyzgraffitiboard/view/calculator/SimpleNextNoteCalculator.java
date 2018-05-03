package com.bfc.wyzgraffitiboard.view.calculator;

import android.util.Log;

import com.bfc.wyzgraffitiboard.view.data.GraffitiLayerDataObject;
import com.bfc.wyzgraffitiboard.view.data.GraffitiNoteDataObject;

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
    private List<GraffitiNoteDataObject> mPool = new ArrayList<>();

    public SimpleNextNoteCalculator() {

    }

    @Override
    public List<GraffitiNoteDataObject> next(GraffitiLayerDataObject layer, GraffitiNoteDataObject relative, float x, float y) {
        if (relative == null) {
            GraffitiNoteDataObject note = new GraffitiNoteDataObject(layer, x, y);
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
                    GraffitiNoteDataObject note = new GraffitiNoteDataObject(layer, lastX + gapX - layer.getNoteWidth(), lastY + gapY - layer.getNoteHeight());
                    Log.e(TAG, "add note -> " + note);
                    mPool.add(note);
                }
                return mPool;
            }
        }
        return null;
    }
}
