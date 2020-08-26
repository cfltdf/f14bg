package com.f14.innovation.param

import com.f14.bg.anim.AnimType
import com.f14.bg.anim.AnimVar
import com.f14.bg.consts.ConditionResult
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.InnoCardDeck
import java.util.*

class InnoResultParam(param: InnoResultParam? = null) {
    var nextAbilityGroups: MutableMap<ConditionResult, InnoPlayer> = HashMap()
    var cards = InnoCardDeck()
    var animVars: MutableMap<InnoCard, AnimVar> = HashMap()
    var animType: AnimType? = null
    var conditionResult: ConditionResult? = null

    init {
        if (param != null) {
            this.cards.addCards(param.cards.cards)
            this.animVars.putAll(param.animVars)
            this.animType = param.animType
            this.conditionResult = param.conditionResult
        }
    }

    fun addCard(card: InnoCard) {
        this.cards.addCard(card)
    }

    fun addCards(cards: List<InnoCard>) {
        this.cards.addCards(cards)
    }

    fun getAnimVar(card: InnoCard): AnimVar {
        return this.animVars[card]!!
    }

    fun putAnimVar(card: InnoCard, animvar: AnimVar) {
        this.animVars[card] = animvar
    }

    fun reset() {
        this.cards.clear()
        this.animVars.clear()
        this.animType = null
        this.conditionResult = null
        this.nextAbilityGroups.clear()
    }

}
