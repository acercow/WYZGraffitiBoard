package com.sina.weibo;

/**
 * @author shichao5
 * @date 2018/5/2
 * @describ 涂鸦绘制状态监听
 */

public interface DrawStatusListener {
    void onStatusChange(DrawStatus drawStatus, int giftNum, int coinNum);

    /**
     * 未开始绘制（显示引导蒙层）
     * 开始绘制（未达到10个礼物）
     * 绘制基本完成（达到10个礼物）
     */
    public enum DrawStatus {
        DEFAULT, START, FINISH
    }
}


