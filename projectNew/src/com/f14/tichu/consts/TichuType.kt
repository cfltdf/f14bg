package com.f14.tichu.consts


/**
 * 大小地主
 * @author F14eagle
 */
enum class TichuType {
    /**
     * 大地主
     */
    BIG_TICHU,
    /**
     * 小地主
     */
    SMALL_TICHU;


    companion object {
        fun getChinese(tichuType: TichuType) = when (tichuType) {
            BIG_TICHU -> "大地主"
            SMALL_TICHU -> "小地主"
        }
    }
}
