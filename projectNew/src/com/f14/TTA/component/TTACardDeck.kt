package com.f14.TTA.component

import com.f14.TTA.component.card.TTACard
import com.f14.bg.component.CardDeck

/**
 * TTA牌堆

 * @author F14eagle
 */
class TTACardDeck : CardDeck<TTACard> {

    constructor() : super()

    constructor(autoReshuffle: Boolean) : super(autoReshuffle)

    constructor(defaultCards: List<TTACard>) : super(defaultCards)

    constructor(defaultCards: List<TTACard>, autoReshuffle: Boolean) : super(defaultCards, autoReshuffle)

    /**
     * 将牌堆排序
     */
    fun sortCards() {
        this.cards.sort()
    }

}
