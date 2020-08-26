package com.f14.innovation.component

import com.f14.bg.component.CardDeck


open class InnoCardDeck : CardDeck<InnoCard>() {

    /**
     * 按照index取得卡牌
     * @param index
     * @return
     */
    fun getCardByIndex(index: Int): InnoCard? {
        return this.cards.firstOrNull { it.cardIndex == index }
    }

}
