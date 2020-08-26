package com.f14.RFTG.card

import com.google.gson.annotations.SerializedName

/**
 * 生产阶段的能力
 * @author F14eagle
 */
class ProduceAbility : Ability() {
    var drawNum = 0
    var drawAfterProduced = 0
    var onStartDrawNum = 0
    var discardNum = 0
    @SerializedName("canTargetSelf")
    var isCanTargetSelf = true
}
