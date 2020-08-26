package com.f14.TS.manager

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.TSResourceManager
import com.f14.TS.component.TSCard
import com.f14.TS.component.TSCardDeck
import com.f14.TS.component.TSReplaceCard
import com.f14.TS.component.TSZeroCard
import com.f14.TS.component.condition.TSCardConditionGroup
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TSConsts
import com.f14.TS.consts.TSPhase
import com.f14.bg.exception.BoardGameException
import org.apache.log4j.Logger
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class CardManager(private var gameMode: TSGameMode) {
    /**
     * 中国牌
     */
    lateinit var chinaCard: TSCard
        private set
    /**
     * 中国牌所有者
     */
    var chinaOwner: SuperPower = SuperPower.NONE
    /**
     * 中国牌是否可用
     */
    var chinaCanUse: Boolean = false

    private var cardDecks: MutableMap<TSPhase, TSCardDeck> = LinkedHashMap()
    lateinit var playingDeck: TSCardDeck
        private set
    lateinit var trashDeck: TSCardDeck
        private set

    private var cardNoCache: MutableMap<Int, TSCard> = HashMap()
    private var zeroCards: Map<Int, Collection<TSZeroCard>> = LinkedHashMap()
    private var replaceCards: Collection<TSReplaceCard> = ArrayList()
    /**
     * 背叛者牌
     */
    lateinit var defactorCard: TSCard
        private set

    init {
        this.init()
    }

    fun addCardToPlayingDeck(res: Collection<TSCard>, phase: TSPhase) {
        val d = this.getCardDeck(phase)
        d.addCards(res)
    }

    /**
     * 将卡牌对象加入到缓存中

     * @param card
     */
    private fun addToCache(card: TSCard) {
        this.cardNoCache[card.tsCardNo] = card
    }

    /**
     * 将指定的时期的牌组加入到牌堆并重洗

     * @param phase
     */
    fun addToPlayingDeck(phase: TSPhase) {
        val deck = this.getCardDeck(phase)
        this.playingDeck.addCards(deck.cards)
        // 该次洗牌不重洗弃牌堆中的牌
        this.playingDeck.shuffle()
    }

    /**
     * 改变中国牌的所属玩家和使用状态

     * @param player

     * @param canUse
     */
    fun changeChinaCardOwner(player: TSPlayer?, canUse: Boolean) {
        this.chinaOwner = player?.superPower ?: SuperPower.NONE
        this.chinaCanUse = canUse
    }

    /**
     * 按照cardNo取得卡牌对象

     * @param cardNo

     * @return
     */
    fun getCardByCardNo(cardNo: Int): TSCard? {
        return this.cardNoCache[cardNo]
    }

    /**
     * 取得指定阶段的牌组

     * @param phase

     * @return
     */
    private fun getCardDeck(phase: TSPhase): TSCardDeck {
        return this.cardDecks.computeIfAbsent(phase) { TSCardDeck() }
    }


    fun getReplaceCard(tsCardNo: Int): TSCard? {
        return this.replaceCards.firstOrNull { it.replaceCardNo == tsCardNo }
    }

    fun getZeroDeck(group: Int?): Collection<TSZeroCard>? {
        return this.zeroCards[group]
    }

    val zeroHandDeck: TSCardDeck
        get() = this.getCardDeck(TSPhase.ZERO)

    /**
     * 初始化
     */
    private fun init() {
        // 将所有的卡牌按阶段分类
        val res = this.gameMode.game.getResourceManager<TSResourceManager>()
        val cards = res.getCardsInstanceByConfig(this.gameMode.game.config)
        for (card in cards) {
            val deck = this.getCardDeck(card.phase)
            deck.addCard(card)
            // 检查并设置中国牌
            if (TSConsts.CHINA_CARD_NO == card.tsCardNo) {
                this.chinaCard = card
            }
            // 检查并设置背叛者
            if (TSConsts.DEFACTOR_CARD_NO == card.tsCardNo) {
                this.defactorCard = card
            }
            this.addToCache(card)

        }

        // 初始化当前牌堆,将早期牌组放入当前牌堆
        val d = this.getCardDeck(TSPhase.EARLY)
        // 该牌堆摸完时将自动重洗弃牌堆
        this.playingDeck = TSCardDeck(d.cards, true)
        // 将中国牌从牌堆中移除
        try {
            this.playingDeck.takeCard(this.chinaCard.id)
        } catch (e: BoardGameException) {
            log.error("没有找到中国牌!")
        }

        this.zeroCards = res.allZeroCards()
        this.replaceCards = res.getReplaceCards()
        // 初始化废牌堆
        this.trashDeck = TSCardDeck()
    }


    fun removeCards(con: TSCardConditionGroup): Collection<TSCard> {
        val res = this.removeCards(TSPhase.EARLY, con)
        res.addAll(this.removeCards(TSPhase.MID, con))
        res.addAll(this.removeCards(TSPhase.LATE, con))
        return res
    }


    private fun removeCards(phase: TSPhase, con: TSCardConditionGroup): MutableCollection<TSCard> {
        val res = ArrayList<TSCard>(0)
        val d = this.getCardDeck(phase)
        val it = d.cards.iterator()
        while (it.hasNext()) {
            val c = it.next()
            if (con.test(c)) {
                it.remove()
                res.add(c)
            }
        }
        return res
    }

    fun reset() {
        // 初始化当前牌堆,将早期牌组放入当前牌堆
        val d = this.getCardDeck(TSPhase.EARLY)
        // 该牌堆摸完时将自动重洗弃牌堆
        this.playingDeck = TSCardDeck(d.cards, true)
        // 将中国牌从牌堆中移除
        try {
            this.playingDeck.takeCard(this.chinaCard.id)
        } catch (e: BoardGameException) {
            log.error("没有找到中国牌!")
        }

    }

    companion object {
        private val log = Logger.getLogger(CardManager::class.java)!!
    }
}
