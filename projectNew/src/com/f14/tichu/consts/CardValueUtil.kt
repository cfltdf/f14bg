package com.f14.tichu.consts

object CardValueUtil {

    /**
     * 取得牌值的描述
     * @param point
     * @return
     */
    fun getCardValue(point: Double) = when (val i = point.toInt()) {
        11 -> "J"
        12 -> "Q"
        13 -> "K"
        14 -> "A"
        else -> i.toString()
    }
}
