package com.f14.innovation.consts

/**
 * 成就牌的触发事件类型

 * @author F14eagle
 */
enum class InnoAchieveTrigType {
    /**
     * 卡牌堆变化时
     */
    STACK_CHANGE,
    /**
     * 展开方向变化时
     */
    SPLAY_DIRECTION_CHANGE,
    /**
     * 垫底时
     */
    TUCK,
    /**
     * 计分时
     */
    SCORE

}
