package com.f14.bg.component

import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import java.util.*

/**
 * 牌组

 * @author F14eagle
 */
class CardList<C : Card> {
    private var cards: MutableList<C> = ArrayList()

    constructor()

    constructor(cards: MutableList<C>) {
        this.cards = cards
    }

    /**
     * 按照id取得牌,如果没有找到则抛出异常
     * @param id
     * @return
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun getCard(id: String) = this.cards.firstOrNull { it.id == id }
            ?: throw BoardGameException("没有找到指定的牌!")

    /**
     * 按照cardIds取得的手牌,如果没有找到则抛出异常 如果输入的是空字符串则返回空列表
     * @param cardIds 各个id间用","隔开
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getCards(cardIds: String) = BgUtils.string2Array(cardIds).map(this::getCard)

    /**
     * 判断牌组中是否有指定id的牌
     * @param id
     * @return
     */
    fun hasCard(id: String) = this.cards.any { it.id == id }

    /**
     * 打出指定id的牌,如果牌组中没有该牌,则抛出异常
     * @param id
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playCard(id: String): C {
        val card = this.getCard(id)
        this.cards.remove(card)
        return card
    }

    /**
     * 打出指定的手牌,如果没有找到则抛出异常 如果输入的是空字符串则返回空列表
     * @param cardIds
     * @return
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun playCards(cardIds: String): List<C> {
        val res = this.getCards(cardIds)
        this.cards.removeAll(res)
        return res
    }


    override fun toString() = cards.joinToString(",") { it.id }
}
