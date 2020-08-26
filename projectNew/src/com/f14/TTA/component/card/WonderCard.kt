package com.f14.TTA.component.card

import com.f14.TTA.component.ability.ScoreAbility
import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.math.min

/**
 * 奇迹牌
 * @author F14eagle
 */
class WonderCard : CivilCard() {
    var costResources: IntArray = IntArray(0)
    var currentStep: Int = 0
    @SerializedName("costBlues")
    var isCostBlues: Boolean = false
    var scoreAbilities: MutableList<ScoreAbility> = ArrayList(0)

    /**
     * 建造奇迹的step个步骤,返回奇迹是否建造完成
     * @param step
     * @return
     */
    fun buildStep(step: Int): Boolean {
        this.addBlues(step)
        this.currentStep += step
        return this.isComplete
    }


    override fun clone(): WonderCard {
        return super.clone() as WonderCard
    }

    /**
     * 判断奇迹是否建造完成
     * @return
     */
    val isComplete: Boolean
        get() = this.currentStep >= this.costResources.size

    /**
     * 取得建造所需要的资源
     * @param step 建造的步骤数
     * @return
     */
    fun stepCostResource(step: Int): Int {
        val count = min(this.costResources.size - this.currentStep, step)
        return this.costResources.asSequence().drop(this.currentStep).take(count).sum()
    }

}
