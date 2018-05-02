package com.bfc.wyzgraffitiboard.coordinates;

import android.graphics.RectF;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * 坐标系统
 * <p>
 * 将 抽象比例坐标 转换到 实际坐标
 */

public interface ICoordinateConverter {


    /**
     * Convert value to computer pixel level
     *
     * @param from
     * @param to
     * @return
     */
    RectF convert(RectF from, RectF to);

}
