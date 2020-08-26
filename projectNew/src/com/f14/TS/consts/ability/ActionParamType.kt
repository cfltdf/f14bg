package com.f14.TS.consts.ability

/**
 * 行动参数类型

 * @author F14eagle
 */
enum class ActionParamType {
    /**
     * 调整DEFCON
     */
    ADJUST_DEFCON,
    /**
     * 设置DEFCON
     */
    SET_DEFCON,
    /**
     * 调整VP
     */
    ADJUST_VP,
    /**
     * 设置影响力
     */
    SET_INFLUENCE,
    /**
     * 调整影响力
     */
    ADJUST_INFLUENCE,
    /**
     * 太空竞赛前进
     */
    ADVANCE_SPACE_RACE,
    /**
     * 调整军事行动
     */
    ADJUST_MILITARY_ACTION,
    /**
     * 战争
     */
    WAR,
    /**
     * 随机弃牌
     */
    RANDOM_DISCARD_CARD,
    /**
     * 弃牌
     */
    DISCARD_CARD,
    /**
     * 清除影响力
     */
    CLEAR_INFLUENCE,
    /**
     * 弃掉牌堆的牌
     */
    DISCARD_DECK_CARD,
    /**
     * 更变国家稳定值
     */
    ADD_STABILIZATION_BONUS,
    /**
     * 视为战场国
     */
    TREAT_AS_BATTLEFIELD
}
