package com.f14.RFTG.card

import com.google.gson.annotations.SerializedName

/**
 * 消费阶段时交易的能力
 * @author F14eagle
 */
class TradeAbility : Ability() {
    var drawNum = 0
    @SerializedName("current")
    var isCurrent = false
    var worldCondition: Condition = Condition()

}
