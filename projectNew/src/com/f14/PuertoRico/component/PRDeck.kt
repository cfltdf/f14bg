package com.f14.PuertoRico.component

import com.f14.bg.component.Card
import com.f14.bg.component.Deck
import com.f14.bg.exception.BoardGameException

open class PRDeck<C : Card>(defaultCard: List<C> = emptyList()) : Deck<C>(defaultCard, true) {

    /**
     * 取得指定id的卡牌
     * @param cardId
     * @return @throws
     */
    @Throws(BoardGameException::class)
    fun getCard(cardId: String) = this.cards.firstOrNull { it.id == cardId } ?: throw BoardGameException("没有找到指定的对象!")

    /**
     * 取得指定id的卡牌
     * @param cardIds
     * @return @throws
     */
    @Throws(BoardGameException::class)
    fun getCards(cardIds: String) = cardIds.split(",".toRegex()).dropLastWhile(String::isEmpty).map(this::getCard)

}
