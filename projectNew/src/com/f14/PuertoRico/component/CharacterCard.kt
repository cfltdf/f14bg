package com.f14.PuertoRico.component

import com.f14.PuertoRico.consts.Character
import com.f14.bg.component.Card

/**
 * 角色卡
 * @author F14eagle
 */
class CharacterCard : Card() {
    var doubloon = 0
    lateinit var character: Character
    var isCanUse = false


    override fun clone(): CharacterCard {
        return super.clone() as CharacterCard
    }

    override fun toMap(): Map<String, Any> = super.toMap() + mapOf("canUse" to isCanUse, "doubloon" to doubloon)
}
