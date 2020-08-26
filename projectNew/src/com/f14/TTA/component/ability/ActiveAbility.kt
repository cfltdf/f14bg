package com.f14.TTA.component.ability

import com.f14.TTA.TTAPlayer
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.ActiveAbilityType
import com.f14.TTA.consts.RoundStep
import com.f14.bg.common.LifeCycle
import com.google.gson.annotations.SerializedName

/**
 * 可激活的能力
 * @author F14eagle
 */
class ActiveAbility : CardAbility() {
    lateinit var abilityType: ActiveAbilityType
    lateinit var activeStep: RoundStep // 作用阶段
    var endPhase: Boolean = false // 结束当前阶段
    var sacrifice: Boolean = false // 使用后牺牲本卡牌
    var lifeCycle: LifeCycle? = null
    @SerializedName("useActionPoint")
    var isUseActionPoint: Boolean = false
    lateinit var actionType: ActionType
    var actionCost: Int = 0
    var limit = 1

    /**
     * 检查玩家是否可以使用该能力
     * @param activeStep
     * @param player
     * @return
     */
    fun checkCanActive(activeStep: RoundStep, player: TTAPlayer) = this.activeStep == activeStep && (this.lifeCycle == null || player.params.getInteger(this) < limit) && (!this.isUseActionPoint || player.getAvailableActionPoint(this.actionType) >= this.actionCost)
}
