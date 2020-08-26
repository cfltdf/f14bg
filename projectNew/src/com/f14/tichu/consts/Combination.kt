package com.f14.tichu.consts

/**
 * 牌的组合方式

 * @author F14eagle
 */
enum class Combination {
    /**
     * 单张
     */
    SINGLE,
    /**
     * 一对
     */
    PAIR,
    /**
     * 姐妹对
     */
    GROUP_PAIRS,
    /**
     * 三张
     */
    TRIO,
    /**
     * FULLHOUSE
     */
    FULLHOUSE,
    /**
     * 顺子
     */
    STRAIGHT,
    /**
     * 炸弹
     */
    BOMBS;

    companion object {
        fun getChinese(combination: Combination) = when (combination) {
            SINGLE -> "单张"
            PAIR -> "一对"
            GROUP_PAIRS -> "姐妹对"
            TRIO -> "三条"
            FULLHOUSE -> "葫芦"
            STRAIGHT -> "顺子"
            BOMBS -> "炸弹"
        }
    }
}
