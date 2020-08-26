package com.f14.TTA.component.card

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility

/**
 * 攻击牌 (侵略和战争)
 * @author F14eagle
 */
class AttackCard : MilitaryCard(), IOvertimeCard {
    lateinit var winnerEffect: EventAbility
    lateinit var loserEffect: EventAbility
    lateinit var actionCost: ActionCost
    @Transient
    override var owner: TTAPlayer? = null
    @Transient
    override var target: TTAPlayer? = null

    override var a: TTAPlayer?
        get() = this.owner
        set(player) = Unit

    override var b: TTAPlayer?
        get() = this.target
        set(player) = Unit

    override fun toMap(): Map<String, Any> = super.toMap() + mapOf("owner" to this.owner!!.position, "a" to this.a!!.position, "b" to this.b!!.position)
}
