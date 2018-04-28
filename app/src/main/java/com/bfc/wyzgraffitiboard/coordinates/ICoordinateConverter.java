package com.bfc.wyzgraffitiboard.coordinates;

/**
 * Created by fishyu on 2018/4/28.
 * <p>
 * 坐标系统
 * <p>
 * 将 抽象比例坐标 转换到 实际坐标
 */

public interface ICoordinateConverter {


    /**
     * 转 宽度
     *
     * @param width
     * @return
     */
    int width(float width);


    /**
     * 转 高度
     *
     * @param height
     * @return
     */
    int height(float height);


    /**
     * 转 坐标
     *
     * @param value
     * @return
     */
    int coordinate(float value);


}
