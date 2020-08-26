package com.f14.TS.consts

/**
 * TS的一些常量

 * @author F14eagle
 */
object TSConsts {
    /**
     * 中国牌的编号
     */
    const val CHINA_CARD_NO = 6
    /**
     * #103-背叛者的编号
     */
    const val DEFACTOR_CARD_NO = 103
    /**
     * #32-联合国干涉的编号
     */
    const val UNI_CARD_NO = 32
    /**
     * 双重间谍
     */
    const val DOUBLE_AGENT_NO = 302
    /**

     */
    const val FIRST_LIGHTNING_NO = 116
    /**
     * 最大回合数
     */
    const val MAX_ROUND = 10

    /**
     * 取得指定回合的手牌数量

     * @param round

     * @return
     */
    fun getRoundHandsNum(round: Int): Int {
        return if (round < 4) {
            8
        } else {
            9
        }
    }

    /**
     * 取得指定回合的默认轮数

     * @param round

     * @return
     */
    fun getRoundTurnNum(round: Int): Int {
        return getRoundHandsNum(round) - 2
    }

}
