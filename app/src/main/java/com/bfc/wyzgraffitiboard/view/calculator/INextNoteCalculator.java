package com.bfc.wyzgraffitiboard.view.calculator;

import com.bfc.wyzgraffitiboard.view.data.GraffitiLayerDataObject;
import com.bfc.wyzgraffitiboard.view.data.GraffitiNoteDataObject;

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
    List<GraffitiNoteDataObject> next(GraffitiLayerDataObject layer, GraffitiNoteDataObject relative, float x, float y);


}
