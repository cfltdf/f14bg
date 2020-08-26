package com.f14.tichu.consts


/**
 * 卡牌类型,花色

 * @author F14eagle
 */
enum class CardType {
    JADE, SWORD, PAGODA, STAR;


    companion object {

        fun getIndex(cardType: CardType?): Int {
            return when (cardType) {
                null -> 0
                JADE -> 1
                SWORD -> 2
                PAGODA -> 3
                STAR -> 4
                else -> 0
            }
        }
    }
}
