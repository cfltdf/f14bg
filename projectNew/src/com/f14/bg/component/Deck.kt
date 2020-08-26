package com.f14.bg.component

import com.f14.utils.CollectionUtils
import org.apache.log4j.Logger
import java.util.*

/**
 * 牌堆

 * @param <C>
 * @author F14eagle
 */
open class Deck<C : Any> {
    protected val log = Logger.getLogger(this.javaClass)!!
    var defaultCards: List<C> = emptyList()
    protected var autoReshuffle = false
    val cards: MutableList<C> = LinkedList()
    val discards: MutableList<C> = LinkedList()

    /**
     * 当牌堆摸完时是否自动重洗
     * @param autoReshuffle
     */
    constructor(autoReshuffle: Boolean = false) {
        this.autoReshuffle = autoReshuffle
    }

    constructor(defaultCards: List<C>, autoReshuffle: Boolean = false) {
        this.defaultCards = defaultCards
        this.autoReshuffle = autoReshuffle
        this.init()
    }

    /**
     * 添加卡牌
     * @param card
     */
    fun addCard(card: C) {
        this.cards.add(card)
    }

    /**
     * 添加卡牌
     * @param cards
     */
    fun addCards(cards: Collection<C>) {
        this.cards.addAll(cards)
    }

    /**
     * 清除牌堆和弃牌堆
     */
    fun clear() {
        cards.clear()
        discards.clear()
    }

    /**
     * 弃牌,将牌添加到弃牌堆
     * @param card
     */
    fun discard(card: C) {
        discards.add(card)
    }

    /**
     * 弃牌,将牌添加到弃牌堆
     * @param cards
     */
    fun discard(cards: Collection<C>) {
        this.discards.addAll(cards)
    }

    /**
     * 摸牌,返回牌堆的第一张牌并从牌堆中移除该牌; 如果牌堆中没有牌,则返回null
     * @return
     */

    open fun draw(): C? {
        if (cards.isEmpty() && this.autoReshuffle) {
            // 如果牌堆为空并且会自动洗牌,则重洗牌堆
            this.reshuffle()
        }
        return if (cards.isEmpty()) null else cards.removeAt(0)
    }

    /**
     * 摸牌,摸牌堆最上面的num张牌
     * @param num
     * @return
     */
    fun draw(num: Int) = (1..num).mapNotNull { this.draw() }

    /**
     * 随机摸牌,摸牌前会调用shuffle方法
     * @return
     */
    fun drawRandom(): C? {
        this.shuffle()
        return this.draw()
    }

    /**
     * 初始化牌堆
     */
    fun init() {
        cards.clear()
        discards.clear()
        cards.addAll(defaultCards)
    }

    /**
     * 判断牌堆和弃牌堆是否都为空
     * @return
     */
    val empty: Boolean
    get() = this.cards.isEmpty() && this.discards.isEmpty()

    /**
     * 移除卡牌
     * @param card
     * @return
     */
    fun removeCard(card: C?): Boolean {
        return this.cards.remove(card)
    }

    /**
     * 移除卡牌
     * @param cards
     * @return
     */
    fun removeCards(cards: Collection<C>): Boolean {
        return this.cards.removeAll(cards)
    }

    /**
     * 重置牌堆
     */
    fun reset() {
        cards.clear()
        discards.clear()
        cards.addAll(defaultCards)
        this.shuffle()
    }

    /**
     * 重新洗牌,将弃牌堆加入到牌堆中后重新洗牌
     */
    fun reshuffle() {
        cards.addAll(discards)
        discards.clear()
        this.shuffle()
    }

    /**
     * 洗牌
     */
    fun shuffle() {
        CollectionUtils.shuffle(cards)
    }

    /**
     * 取得牌堆中牌的数量
     * @return
     */
    val size: Int
        get() = this.cards.size
}
