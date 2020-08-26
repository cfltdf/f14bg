package com.f14.PuertoRico.component

import com.f14.PuertoRico.consts.GoodType


class PRTileDeck(defaultCard: List<PRTile> = emptyList()) : PRDeck<PRTile>(defaultCard) {

    /**
     * 按照goodType取得板块
     * @param goodType
     * @return
     */
    fun takeTileByGoodType(goodType: GoodType): PRTile? {
        return this.cards.firstOrNull { it.goodType == goodType }?.also { this.cards.remove(it) }
    }
}
