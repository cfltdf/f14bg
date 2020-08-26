package com.f14.TS.component

import com.f14.TS.component.ability.TSAbilityGroup

class TSZeroCard : TSCard() {

    /**
     * 能力组合
     */
    var abilityGroup1: TSAbilityGroup? = null
    var abilityGroup2: TSAbilityGroup? = null
    var abilityGroup3: TSAbilityGroup? = null
    var abilityGroup4: TSAbilityGroup? = null
    /**
     * 描述描述
     */
    lateinit var desc1: String
    lateinit var desc2: String
    lateinit var desc3: String
    lateinit var desc4: String
    /**
     * 描述效果
     */
    lateinit var effect: String
    lateinit var effect1: String
    lateinit var effect2: String
    lateinit var effect3: String
    lateinit var effect4: String
    /**
     * 序列
     */
    var zeroGroup: Int = 0
    /**
     * 结果
     */
    var result: Int = 0


    override fun clone(): TSZeroCard {
        return super.clone() as TSZeroCard
    }

    override fun toMap(): Map<String, Any> = super.toMap() + mapOf("result" to result, "descr" to descr + effect)

    fun doResult(result: Int) {
        when (result) {
            1 -> {
                this.descr = this.desc1
                this.abilityGroup = this.abilityGroup1
                this.effect = this.effect1
            }
            2, 3 -> {
                this.descr = this.desc2
                this.abilityGroup = this.abilityGroup2
                this.effect = this.effect2
            }
            4, 5 -> {
                this.descr = this.desc3
                this.abilityGroup = this.abilityGroup3
                this.effect = this.effect3
            }
            6 -> {
                this.descr = this.desc4
                this.abilityGroup = this.abilityGroup4
                this.effect = this.effect4
            }
        }
        this.result = result
    }
}
