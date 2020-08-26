package com.f14.innovation.component

import com.f14.bg.component.Convertable
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.consts.InnoConsts

import java.util.*

class InnoCardGroup : Convertable {

    private var cards = InnoCardDeck()

    private var cardDecks: MutableMap<Int, InnoCardDeck> = LinkedHashMap()

    /**
     * 添加卡牌
     * @param card
     */
    fun addCard(card: InnoCard) {
        this.getCardDeck(card.level).addCard(card)
        this.cards.addCard(card)
    }

    /**
     * 添加卡牌
     * @param cards
     */
    fun addCards(cards: Collection<InnoCard>) = cards.forEach(this::addCard)

    fun clear() {
        this.cards.clear()
        this.cardDecks.clear()
    }

    /**
     * 摸指定等级的牌,如果没有这个等级的牌,则摸高一等级的
     * @param level
     * @return
     */
    fun draw(level: Int): InnoCard? = (level..InnoConsts.MAX_LEVEL).map(this::getCardDeck).firstOrNull { !it.empty }?.draw()?.also { this.cards.removeCard(it) }

    /**
     * 摸指定等级指定数量的牌
     * @param level
     * @param num
     * @return
     */
    fun draw(level: Int, num: Int): List<InnoCard> = (1..num).mapNotNull { this.draw(level) }

    /**
     * 取得指定卡牌
     * @param cardId
     * @return
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun getCard(cardId: String): InnoCard = this.cards.getCard(cardId)

    /**
     * 取得指定等级对应的牌堆
     * @param level
     * @return
     */
    fun getCardDeck(level: Int): InnoCardDeck = this.cardDecks.computeIfAbsent(level) { InnoCardDeck() }

    /**
     * 取得所有卡牌
     * @return
     */
    fun getCards() = this.cards.cards

    /**
     * 取得指定卡牌
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getCards(cardIds: String) = this.cards.getCards(cardIds)

    /**
     * 取得牌堆中最高的等级
     * @return 如果没有牌则返回-1
     */
    val maxLevel: Int
        get() = (InnoConsts.MAX_LEVEL downTo 1).firstOrNull { !this.getCardDeck(it).empty } ?: 0

    /**
     * 取得牌堆中最高等级的牌堆
     * @return
     */
    val maxLevelCardDeck: InnoCardDeck?
        get() = (InnoConsts.MAX_LEVEL downTo 1).map(this::getCardDeck).firstOrNull { !it.empty }

    /**
     * 取得牌堆中最高所有最高等级的牌
     * @return
     */
    val maxLevelCards: List<InnoCard>
        get() = this.maxLevelCardDeck?.cards ?: emptyList()

    /**
     * 取得牌堆中最低的等级
     * @return 如果没有牌则返回-1
     */
    val minLevel: Int
        get() = (1..InnoConsts.MAX_LEVEL).firstOrNull { !this.getCardDeck(it).empty } ?: -1

    /**
     * 取得牌堆中最低等级的牌堆
     * @return
     */
    val minLevelCardDeck: InnoCardDeck?
        get() = (1..InnoConsts.MAX_LEVEL).map(this::getCardDeck).firstOrNull { !it.empty }

    /**
     * 判断是否存在指定等级的牌
     * @param level
     * @return
     */
    fun hasCard(level: Int) = !this.getCardDeck(level).empty

    val isEmpty: Boolean
        get() = this.getCards().isEmpty()

    /**
     * 移除卡牌
     * @param card
     */
    fun removeCard(card: InnoCard) {
        this.cards.removeCard(card)
        this.getCardDeck(card.level).removeCard(card)
    }

    /**
     * 洗牌
     */
    fun reshuffle() {
        this.cardDecks.values.forEach(InnoCardDeck::reshuffle)
    }

    fun size() = this.getCards().size

    override fun toMap(): Map<String, Any> = (1..InnoConsts.MAX_LEVEL).map { it.toString() to this.getCardDeck(it).size }.toMap()
}
