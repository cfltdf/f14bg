package com.f14.TS.consts


/**
 * 获胜方式

 * @author F14eagle
 */
enum class TSVictoryType {
    /**
     * VP到达20,获胜
     */
    VP,
    /**
     * DEFCON降为1,获胜
     */
    DEFCON,
    /**
     * 对方保留计分牌
     */
    SCORE_CARD,
    /**
     * 特殊胜利条件
     */
    SPECIAL,
    /**
     * VP获胜
     */
    VP_VICTORY,
    /**
     * 对方投降
     */
    RESIGN;


    companion object {

        fun getChinese(vt: TSVictoryType): String {
            return when (vt) {
                VP -> "VP达到20"
                DEFCON -> "对手DEFCON降为1"
                SCORE_CARD -> "对手保留计分牌"
                SPECIAL -> "特殊胜利条件"
                VP_VICTORY -> "VP高于对手"
                RESIGN -> "对方投降"
            }
        }
    }
}
