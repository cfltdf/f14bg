package com.f14.RFTG.card

import com.google.gson.annotations.SerializedName

/**
 * 消费阶段的能力

 * @author F14eagle
 */
class ConsumeAbility : Ability() {
    var vp = 0
    var drawNum = 0
    var goodNum = 0
    var onStartDrawNum = 0
    var discardNum = 0
    @SerializedName("tradeWithSkill")
    var isTradeWithSkill = false
    var onStartVp = 0

}
