package com.bfc.wyzgraffitiboard.view.coordinates;

import android.graphics.RectF;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * 坐标系统
 * <p>
 * 概念：
 * 1，Percentage 比例坐标 用于传输的抽象坐标，用比例值来描述
 * 2，Pixel 像素坐标 用于计算机显示的坐标，用实际像素来描述
 */

public interface ICoordinateConverter {

    float convertWidthPixelToPercentage(float widthPixel);

    float convertWidthPercentageToPixel(float widthPercentage);

    float convertHeightPixelToPercentage(float heightPixel);

    float convertHeightPercentageToPixel(float heightPercentage);

    /**
     * Convert Percentage to Pixel
     *
     * @param from
     * @param to
     * @return
     */
    RectF convertPercentageToPixel(RectF from, RectF to);


    /**
     * Convert Pixel to Percentage
     *
     * @param from
     * @param to
     * @return
     */
    RectF convertPixelToPercentage(RectF from, RectF to);


}
