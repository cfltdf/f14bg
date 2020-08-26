package com.f14.TS.consts


/**
 * 局势

 * @author F14eagle
 */
enum class TSSituation {
    /**
     * 无
     */
    NONE,
    /**
     * 在场
     */
    PRESENCE,
    /**
     * 支配
     */
    DOMINATION,
    /**
     * 控制
     */
    CONTROL;


    companion object {

        fun getDescr(situation: TSSituation): String? {
            return when (situation) {
                NONE -> "无"
                PRESENCE -> "在场"
                DOMINATION -> "支配"
                CONTROL -> "控制"
                else -> null
            }
        }
    }
}
