package com.f14.loveletter

import com.f14.bg.component.Card


class LLCard : Card(), Comparable<LLCard> {
    var point = 0.0

    override fun compareTo(other: LLCard) = this.point.compareTo(other.point)

    override fun clone() = super.clone() as LLCard

    override fun toString() = this.name

}
