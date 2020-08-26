package com.f14.bg.component


import java.util.*

open class CardPool : Convertable {

    protected var map: MutableMap<String, MutableList<Card>> = HashMap()

    /**
     * 添加card
     * @param card
     */
    open fun addCard(card: Card) {
        map.computeIfAbsent(card.cardNo) { ArrayList() }.add(card)
    }

    /**
     * 添加cards
     */
    fun <C : Card> addCards(cards: List<C>) = cards.forEach(this::addCard)

    /**
     * 按照cardNo取得卡牌
     * @param <C>
     * @param cardNo
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    fun <C : Card> getCard(cardNo: String) = this.getDeck(cardNo)?.firstOrNull() as? C?

    /**
     * 取得所有cardNo的集合
     * @return
     */
    val cardNos: Set<String>
        get() = this.map.keys

    /**
     * 按照cardNo取得牌堆
     * @param cardNo
     */
    fun getDeck(cardNo: String) = map[cardNo]

    /**
     * 取得指定牌堆的大小
     * @param cardNo
     * @return
     */
    fun getDeckSize(cardNo: String) = this.getDeck(cardNo)?.size ?: 0

    /**
     * 按照cardNo取得卡牌并从牌堆中移除
     * @param <C>
     * @param cardNo
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    fun <C : Card> takeCard(cardNo: String) = this.getDeck(cardNo)?.let{ deck ->
        deck.firstOrNull()?.also { deck.remove(it) }
    } as C?

    override fun toMap(): Map<String, Any> = this.map.mapValues { it.value.size }
}
