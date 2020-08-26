package com.f14.TTA.consts

/**
 * 回合中的阶段

 * @author F14eagle
 */
enum class RoundStep {
    /**
     * 无（初始化）
     */
    NONE,

    /**
     * 政治行动阶段
     */
    POLITICAL,

    /**
     * 普通阶段
     */
    NORMAL,

    /**
     * 体面退出游戏
     */
    RESIGNED
}
