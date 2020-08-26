package com.f14.tichu.componet

import com.f14.bg.component.CardDeck

class TichuCardDeck : CardDeck<TichuCard>() {

    /**
     * 将牌堆排序
     */
    fun sort() {
        this.cards.sort()
    }

}
