package com.f14.TTA.component.card

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.ActiveAbility
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.consts.PactSide
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * 条约牌
 * @author F14eagle
 */
class PactCard : MilitaryCard(), IOvertimeCard {
    @SerializedName("asymetric")
    var isAsymetric: Boolean = false
    @Transient
    override var owner: TTAPlayer? = null
    @Transient
    override var target: TTAPlayer? = null
    @Transient
    override var a: TTAPlayer? = null
    @Transient
    override var b: TTAPlayer? = null

    @SerializedName("propertyMapA")
    var propertyA = TTAProperty()
    var abilitiesA: MutableList<CivilCardAbility> = ArrayList(0)
    var activeAbilityA: ActiveAbility? = null

    @SerializedName("propertyMapB")
    var propertyB = TTAProperty()
    var abilitiesB: MutableList<CivilCardAbility> = ArrayList(0)
    var activeAbilityB: ActiveAbility? = null

    val alian: TTAPlayer?
        get() = when (side) {
            PactSide.A -> b
            PactSide.B -> a
            else -> null
        }

    @Transient
    var side: PactSide? = null

    override var property: TTAProperty
        get() = when (side) {
            PactSide.A -> propertyA
            PactSide.B -> propertyB
            else -> super.property
        }
        set(value) {
            super.property = value
        }

    override var abilities: MutableList<CivilCardAbility>
        get() = when (side) {
            PactSide.A -> abilitiesA
            PactSide.B -> abilitiesB
            else -> super.abilities
        }
        set(value) {
            super.abilities = value
        }

    override var activeAbility: ActiveAbility?
        get() = when (side) {
            PactSide.A -> activeAbilityA
            PactSide.B -> activeAbilityB
            else -> super.activeAbility
        }
        set(value) {
            super.activeAbility = value
        }

    override fun clone() = super.clone() as PactCard

    override val reportString: String
        get() = when (side) {
            PactSide.A -> "[${this.ageString}${this.name}(A)]"
            PactSide.B -> "[${this.ageString}${this.name}(B)]"
            else -> super.reportString
        }

    override fun toMap(): Map<String, Any> = super.toMap() + mapOf("owner" to this.owner!!.position, "a" to this.a!!.position, "b" to this.b!!.position)
}
