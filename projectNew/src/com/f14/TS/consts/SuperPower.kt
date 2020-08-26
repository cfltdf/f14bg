package com.f14.TS.consts


/**
 * 超级大国

 * @author F14eagle
 */
enum class SuperPower {
    /**
     * 美国
     */
    USA,
    /**
     * 苏联
     */
    USSR,
    /**
     * 无
     */
    NONE,
    /**
     * 当前回合玩家
     */
    CURRENT_PLAYER,
    /**
     * 出牌的玩家
     */
    PLAYED_CARD_PLAYER,
    /**
     * 出牌玩家的对手
     */
    OPPOSITE_PLAYER;

    /**
     * 取得对方的超级大国
     * @return
     */
    val oppositeSuperPower: SuperPower
        get() = when (this) {
            USSR -> USA
            USA -> USSR
            else -> this
        }

    val chinese: String
        get() = when (this) {
            USSR -> "苏联"
            USA -> "美国"
            else -> ""
        }

}
