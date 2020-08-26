package com.f14.TS.consts


/**
 * 阶段

 * @author F14eagle
 */
enum class TSPhase {
    /**
     * 冷战早期
     */
    EARLY,
    /**
     * 冷战中期
     */
    MID,
    /**
     * 冷战后期
     */
    LATE,
    /**
     * 第零回合
     */
    ZERO;


    companion object {

        fun getChineseDesc(phase: TSPhase?): String {
            if (phase != null) {
                return when (phase) {
                    EARLY -> "冷战早期"
                    MID -> "冷战中期"
                    LATE -> "冷战后期"
                    ZERO -> "第零回合"
                }
            }
            return ""
        }
    }
}
