package com.bfc.wyzgraffitiboard.calculator;

import com.bfc.wyzgraffitiboard.data.GraffitiLayerData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fishyu on 2018/4/28.
 */

public class SimpleNextNoteCalculator implements INextNoteCalculator {

    /**
     * 优化内存，用于返回 数据
     */
    private List<GraffitiLayerData.GraffitiNote> mPool = new ArrayList<>();

    public SimpleNextNoteCalculator() {

    }

    @Override
    public List<GraffitiLayerData.GraffitiNote> next(GraffitiLayerData layer, GraffitiLayerData.GraffitiNote relative, float x, float y) {
        if (relative == null) {
            GraffitiLayerData.GraffitiNote note = new GraffitiLayerData.GraffitiNote(x, y);
            mPool.clear();
            mPool.add(note);
            return mPool;
        } else {
            float lastX = relative.mX;
            float lastY = relative.mY;

            float distance = (float) Math.sqrt(Math.pow(x - lastX, 2) + Math.pow(y - lastY, 2));
            if (distance > 120) {
                mPool.clear();
                float ratio = distance / 120F;
                float gapX = (x - lastX) / ratio;
                float gapY = (y - lastY) / ratio;
                for (int i = 1; i <= ratio; i++) {
                    GraffitiLayerData.GraffitiNote note = new GraffitiLayerData.GraffitiNote(lastX + gapX - layer.getWidth(), lastY + gapY - layer.getHeight());
                    mPool.add(note);
                }
                return mPool;
            }
        }
        return null;
    }
}
