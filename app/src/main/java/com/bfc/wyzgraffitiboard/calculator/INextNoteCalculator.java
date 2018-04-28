package com.bfc.wyzgraffitiboard.calculator;

import com.bfc.wyzgraffitiboard.data.GraffitiLayerData;

import java.util.List;

/**
 * Created by fishyu on 2018/4/28.
 */

public interface INextNoteCalculator {


    /**
     * 根据当前点 计算接下来的点
     *
     * @param layer
     * @param relative
     * @return
     */
    List<GraffitiLayerData.GraffitiNote> next(GraffitiLayerData layer, GraffitiLayerData.GraffitiNote relative, float x, float y);


}
