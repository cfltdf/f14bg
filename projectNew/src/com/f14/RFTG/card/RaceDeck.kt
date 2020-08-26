package com.f14.RFTG.card

import com.f14.bg.component.Deck
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils

/**
 * 银河竞逐用的牌堆
 * @author F14eagle
 */
class RaceDeck : Deck<RaceCard> {

    constructor() : super(true)

    constructor(defaultCards: List<RaceCard>) : super(defaultCards, true)

    /**
     * 取得指定id的卡牌
     * @param cardId
     * @return @throws
     */
    @Throws(BoardGameException::class)
    fun getCard(cardId: String): RaceCard = this.cards.firstOrNull { it.id == cardId }
            ?: throw BoardGameException("没有找到指定的对象!")

    /**
     * 取得指定id的卡牌
     * @param cardIds
     * @return @throws
     */
    @Throws(BoardGameException::class)
    fun getCards(cardIds: String) = BgUtils.string2Array(cardIds).map(this::getCard)

}
