package com.f14.bg.component

import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils

/**
 * 卡牌类用的牌堆
 * @param <C>
 * @author F14eagle
 */
open class CardDeck<C : Card> : Deck<C> {

    constructor() : super()

    constructor(autoReshuffle: Boolean) : super(autoReshuffle)

    constructor(defaultCards: List<C>) : super(defaultCards)

    constructor(defaultCards: List<C>, autoReshuffle: Boolean) : super(defaultCards, autoReshuffle)

    /**
     * 取得指定id的卡牌
     * @param cardId
     * @return @throws
     */
    @Throws(BoardGameException::class)
    fun getCard(cardId: String) = this.cards.firstOrNull { it.id == cardId }
            ?: throw BoardGameException("没有找到指定的对象!")

    /**
     * 取得指定id的卡牌
     * @param cardIds
     * @return @throws
     */
    @Throws(BoardGameException::class)
    fun getCards(cardIds: String?) = cardIds?.let(BgUtils::string2Array)?.map(this::getCard)
            ?: emptyList()

    /**
     * 从弃牌堆中取得指定id的卡牌
     * @param cardId
     * @return @throws
     */
    @Throws(BoardGameException::class)
    fun getDiscardCard(cardId: String) = this.discards.firstOrNull { it.id == cardId }
            ?: throw BoardGameException("没有找到指定的对象!")

    /**
     * 从牌堆中抽出指定id的卡牌
     * @param cardId
     * @return @throws
     */
    @Throws(BoardGameException::class)
    fun takeCard(cardId: String): C {
        val card = this.getCard(cardId)
        this.cards.remove(card)
        return card
    }

    /**
     * 从弃牌堆中抽出指定id的卡牌
     * @param cardId
     * @return @throws
     */
    @Throws(BoardGameException::class)
    fun takeDiscardCard(cardId: String): C {
        val card = this.getDiscardCard(cardId)
        this.discards.remove(card)
        return card
    }
}
