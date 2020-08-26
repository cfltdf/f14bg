package com.f14.tichu.componet

import com.f14.bg.component.Card
import com.f14.tichu.consts.AbilityType
import com.f14.tichu.consts.CardType


class TichuCard : Card(), Comparable<TichuCard> {
    var cardType: CardType? = null
    var abilityType: AbilityType? = null
    var point: Double = 0.0
    var score: Int = 0

    override fun compareTo(other: TichuCard): Int {
        return when {
            this.point > other.point -> 1
            this.point < other.point -> -1
            else -> {
                val a = CardType.getIndex(this.cardType)
                val b = CardType.getIndex(other.cardType)
                a.compareTo(b)
            }
        }
    }


    override fun toString() = this.name

}
