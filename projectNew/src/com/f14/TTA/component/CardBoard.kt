package com.f14.TTA.component

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.TTAResourceManager
import com.f14.TTA.component.card.MilitaryCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.TacticsCard
import com.f14.TTA.component.card.WonderCard
import com.f14.TTA.consts.*
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CollectionUtils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max

/**
 * TTA用的公用卡牌面板

 * @author F14eagle
 */
class CardBoard(private val gameMode: TTAGameMode) {
    /**
     * 领袖和奇迹表
     */
    val leaders: MutableMap<Int, List<TTACard>> = HashMap()
    val wonders: MutableMap<Int, List<TTACard>> = HashMap()
    /**

     */
    var currentEventRelation: MutableMap<TTACard, TTAPlayer?> = HashMap()
    var futureEventRelation: MutableMap<TTACard, TTAPlayer?> = HashMap()
    var removedCurrentEventRelation: MutableMap<TTACard, TTAPlayer?> = HashMap()

    var tichuCard: TTACard? = null
    /**
     * 摸牌区
     */
    var cardRow: Array<TTACard?> = arrayOfNulls(TTAConsts.CARD_ROW_SIZE)
    /**
     * 当前白色牌组
     */
    private val currentWhiteDeck: TTACardDeck?
        get() = this.whiteDecks[this.gameMode.currentAge]
    /**
     * 当前黑色牌组
     */
    private val currentBlackDeck: TTACardDeck?
        get() = this.blackDecks[this.gameMode.currentAge]
    /**
     * 当前事件牌组
     */
    var pastEvents: MutableList<TTACard>
        private set
    /**
     * 当前事件牌组
     */
    var currentEvents: MutableList<TTACard>
        private set
    /**
     * 将来事件牌组
     */
    var futureEvents: MutableList<TTACard>
        private set
    /**
     * 所有世纪的白牌容器
     */
    private val whiteDecks = HashMap<Int, TTACardDeck>()
    /**
     * 所有世纪的黑牌容器
     */
    private val blackDecks = HashMap<Int, TTACardDeck>()
    /**
     * 废弃奇迹的牌组容器
     */
    private lateinit var flipWonders: Map<Int, TTACardDeck>
    /**
     * 新版公共阵型区
     */
    var publicTacticsDeck: TTACardDeck
        private set
    /**
     *
     */
    val extraCards: MutableList<TTACard>
    /**
     * 新版叠加牌信息
     */
    val attachedCards: MutableMap<TTACard, TTACard> = HashMap()

    init {
        // 摸牌区总共13格
        // 装载所有游戏中用到的卡牌
        this.loadDecks()
        // 初始化事件牌堆
        this.currentEvents = ArrayList()
        this.futureEvents = ArrayList()
        this.pastEvents = ArrayList()
        this.extraCards = ArrayList()

        // 清空公共阵型区
        this.publicTacticsDeck = TTACardDeck()

//        // 将起始世纪的牌堆设置为当前牌堆
//        this.currentWhiteDeck = this.whiteDecks[this.gameMode.currentAge]
//        this.currentBlackDeck = this.blackDecks[this.gameMode.currentAge]

        // 抽取默认的摸牌区白牌
        cardRow.indices.forEach { i -> cardRow[i] = this.currentWhiteDeck!!.draw() }
        // 抽取默认的当前事件牌
        val cards = this.currentBlackDeck!!.draw(this.eventCardNumber)
        this.currentEvents.addAll(cards)
        cards.forEach { c -> this.currentEventRelation[c] = null }
        CollectionUtils.shuffle(this.currentEvents)
    }

    fun drawUnusedAcientEvent(num: Int): List<TTACard> {
        return this.blackDecks[0]?.draw(num) ?: emptyList()
    }

    /**
     * 添加新的事件卡,并返回当前触发的事件卡
     * @param player
     * @param card
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun addEvent(player: TTAPlayer, card: TTACard): EventResult {
//        CheckUtils.check(card.cardType != CardType.EVENT, "事件牌堆只能添加事件卡!")
        // 新事件加入到未来事件牌堆
        this.futureEvents.add(card)
        this.futureEventRelation[card] = player
        // 从当前事件牌堆中抽取触发的事件
        val res = this.currentEvents.last() as MilitaryCard
        this.currentEvents.remove(res)
        this.lastEvent = res
        val p = this.currentEventRelation[res]
        val result = EventResult(res, p, false)
        this.removedCurrentEventRelation[res] = p
        // 如果当前事件牌堆中没有牌了,则用未来事件牌堆代替当前事件牌堆
        if (this.currentEvents.size <= 0) {
            this.futureToCurrentEvent()
            result.futureToCurrent = true
        }
        return result
    }

    /**
     * 测试用代码,插入下一个事件
     * @param card
     * @throws BoardGameException
     */
    fun addNextEvent(card: TTACard) {
        CheckUtils.check(card.cardType != CardType.EVENT, "事件牌堆只能添加事件卡!")
        this.currentEvents.add(card)
    }

    /**
     * 将牌放入对应时代的弃牌堆,只操作军事牌
     * @param cards
     * @return
     */
    fun discardCards(cards: List<TTACard>) {
        cards.filter { it.actionType == ActionType.MILITARY }.mapNotNull { blackDecks[it.level]?.to(it) }.forEach { it.first.discard(it.second) }
    }

    /**
     * 取得指定等级的废弃奇迹牌
     * @param level
     * @return
     */
    fun drawFlipWonder(level: Int) = this.flipWonders.getValue(level).draw() as WonderCard

    /**
     * 摸取军事牌
     * @param num
     * @return
     */
    fun drawMilitaryCard(num: Int): List<TTACard> {
        return this.currentBlackDeck?.draw(num) ?: emptyList()
    }

    /**
     * 将未来事件转换成当前事件
     */
    private fun futureToCurrentEvent() {
        // 需要将事件随机打乱,但是要按照时代先后排序
        // 创建按时代分类的卡牌容器
        val map = this.futureEvents.groupBy(TTACard::level)
        // 添加到当前事件牌堆
        for (i in 0 until TTAConsts.AGE_COUNT) {
            val cards = map[i]?.toMutableList()
            if (cards != null) {
                CollectionUtils.shuffle(cards)
                this.currentEvents.addAll(cards)
            }
        }
        this.currentEvents.reverse()
        // 清空未来事件牌堆
        this.futureEvents.clear()
        this.currentEventRelation = this.futureEventRelation
        this.futureEventRelation = HashMap()
        this.removedCurrentEventRelation = HashMap()
    }

    /**
     * 按照卡牌序列的位置取得基本价格
     * @param index
     * @return
     */
    private fun getBaseCost(index: Int) = COSTS[index]

    /**
     * 在拿牌列取得指定cardId的卡牌,如果不存在则抛出异常
     * @param cardId
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getCard(cardId: String) = this.cardRow[this.getCardIndex(cardId)]!!

    /**
     * 按照cardId取得卡牌序列,如果不存在则抛出异常
     * @param cardId
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getCardIndex(cardId: String): Int {
        return cardRow.withIndex().firstOrNull { (_, c) -> c?.id == cardId }?.index
                ?: throw BoardGameException("没有找到指定的卡牌!")
    }

    /**
     * 取得文明牌序列
     * @return
     */
    fun getCardRow(): Map<Int, List<Pair<TTACard, Int>>> = this.cardRow.mapIndexedNotNull { i, c -> c?.to(i) }.groupBy { (_, i) -> getBaseCost(i) }

    /**
     * 取得文明牌序列的id数组
     * @return
     */
    val cardRowIds: Array<String?>
        get() = this.cardRow.map { it?.id }.toTypedArray()

    /**
     * 取得当前白牌堆剩余数量
     * @return
     */
    val civilRemain: Int
        get() = this.currentWhiteDeck?.size ?: 0

    /**
     * 取得cardId指定卡牌的价格,如果不存在则抛出异常
     * @param cardId
     * @param player
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getCost(cardId: String, player: TTAPlayer): Int {
        val index = this.getCardIndex(cardId)
        var cost = this.getBaseCost(index)
        val card = this.getCard(cardId)

        // 如果该卡牌是奇迹,且无减费能力,则拿取的费用需要加上玩家已有奇迹的数量
        if (card.cardType == CardType.WONDER) {
            if (!player.abilityManager.hasAbilitiy(CivilAbilityType.PA_NO_WONDER_EXTRA_CA)) cost += player.completedWonderNumber
            if (player.params.getBoolean(CivilAbilityType.PA_NEW_TAJ)) {
                cost += card.abilities.filter { it.abilityType == CivilAbilityType.PA_NEW_TAJ }.sumBy { it.property.getProperty(CivilizationProperty.CIVIL_ACTION) }
            }
        }
        if (player.tokenPool.unhappyWorkers == 0) {
            cost += card.abilities.filter { it.abilityType == CivilAbilityType.PA_MARTINE }.sumBy { it.property.getProperty(CivilizationProperty.CIVIL_ACTION) }
        }
        // 计算所有调整拿牌费用的能力
        cost += player.abilityManager.getAbilitiesByType(CivilAbilityType.PA_TAKE_CARD_COST).filter { it.test(card) }.filter { it.limit < cost }.sumBy { it.property.getProperty(CivilizationProperty.CIVIL_ACTION) }
        cost = max(0, cost)
        return cost
    }

    /**
     * 取得事件卡的数量(游戏玩家人数+2)
     * @return
     */
    private val eventCardNumber: Int
        get() = this.gameMode.game.currentPlayerNumber + 2


    var lastEvent: TTACard?
        get() = pastEvents.firstOrNull() as MilitaryCard?
        set(card) {
            card?.let { pastEvents.add(0, it) }
        }

    /**
     * 取得当前军事牌堆剩余数量
     * @return
     */
    val militaryRemain: Int
        get() = this.currentBlackDeck?.size ?: 0

    /**
     * 取得下一个当前事件
     * @return
     */
    val nextCurrentEventCard: MilitaryCard?
        get() = currentEvents.lastOrNull() as MilitaryCard?

    /**
     * 取得下一个未来事件
     * @return
     */
    val nextFutureEventCard: MilitaryCard?
        get() = this.futureEvents.lastOrNull() as MilitaryCard?

    /**
     * 取得需要移除的卡牌数量
     * @return
     */
    private val removeCardNumber: Int
        get() = when (gameMode.game.realPlayerNumber) {
            2 -> 3
            3 -> 2
            4 -> 1
            else -> 0
        }

    /**
     * 获取指定CardNo临时卡牌(测试用)
     * @return
     */
    fun getTempCardByNo(cardNo: String): TTACard {
        val rm = this.gameMode.game.getResourceManager<TTAResourceManager>()
        val cards = rm.getCardByNo(cardNo)
        return cards!![0]
    }

    /**
     * 装载所有的牌组
     */
    private fun loadDecks() {
        val config = this.gameMode.game.config
        val rm = this.gameMode.game.getResourceManager<TTAResourceManager>()
        // 装载游戏配置中所有使用到的牌组
        for (i in 0 until config.ageCount) {
            // 白色内政牌堆
            val whiteCards = ArrayList<TTACard>()

            // 加入一般科技牌和黄色行动牌
            whiteCards.addAll(rm.getTechCards(config, i))
            whiteCards.addAll(rm.getActionCards(config, i))

            // 创建领袖和奇迹表
            val leaderCards = rm.getLeaderCards(config, i).toMutableList()
            val wonderCards = rm.getWonderCards(config, i).toMutableList()
            CollectionUtils.shuffle(leaderCards)
            CollectionUtils.shuffle(wonderCards)
            val selectedLeaders = leaderCards.take(6)
            val selectedWonders = wonderCards.take(4)
            leaders[i] = selectedLeaders
            wonders[i] = selectedWonders

            // 加入领袖和奇迹
            whiteCards.addAll(BgUtils.cloneList(selectedLeaders))
            whiteCards.addAll(BgUtils.cloneList(selectedWonders))

            val whiteDeck = TTACardDeck(whiteCards)
            whiteDeck.shuffle()
            whiteDecks[i] = whiteDeck

            // 黑色军事牌堆
            val blackCards = ArrayList(rm.getMilitaryCards(config, i))

            // 按照游戏配置过滤牌组
            if (config.mode == TTAMode.PEACE) {
                // 和平模式中,将过滤所有的战争和侵略牌
                blackCards.removeIf { card -> card.cardType in arrayOf(CardType.WAR, CardType.AGGRESSION) }
            }
            if (gameMode.game.isTeamMatch || gameMode.game.isTichuMode || config.playerNumber == 2) {
                // 如果是2人局或组队模式, 则移除所有的条约
                blackCards.removeIf { c -> c.cardType == CardType.PACT }
            }

            if (gameMode.game.isVersion2 && gameMode.game.isTeamMatch && i == 3) {
                val holywar = rm.getCardByNo("217.0") ?: return
                blackCards.addAll(holywar)
                val warcards = blackCards.filter { it.cardType == CardType.WAR }.take(2)
                blackCards.removeAll(warcards)
            }

            val blackDeck = TTACardDeck(blackCards, true)
            blackDeck.shuffle()
            blackDecks[i] = blackDeck
        }

        // 装载所有废弃的奇迹
        this.flipWonders = rm.getFlipWonders().groupBy(WonderCard::level).mapValues { TTACardDeck(it.value) }

        // 装载地主牌
        if (gameMode.game.isTichuMode) {
            this.tichuCard = CollectionUtils.randomDraw(rm.getTichuCard()) ?: throw BoardGameException("No such card!")
        }
    }

    /**
     * 新世纪到来
     */
    fun newAge() {
        // 移除所有玩家过时的牌
        this.gameMode.game.removePastCards()
        this.gameMode.addAge()
        this.gameMode.game.report.newAge(this.gameMode.currentAge)
        // 如果当前世纪超出了游戏设置的世纪,则不再有牌堆补牌,并且游戏结束
        if (this.gameMode.currentAge >= this.gameMode.game.config.ageCount) this.gameMode.gameOver = true
    }

    /**
     * 新版公开阵型牌
     * @return
     */
    fun publicTactics(card: TacticsCard?): Boolean {
        return if (card == null || card in this.publicTacticsDeck.cards) {
            false
        } else {
            publicTacticsDeck.addCard(card)
            true
        }
    }

    /**
     * 玩家回合开始时重整摸牌区
     */
    fun regroupCardRow(doDiscard: Boolean): Map<TTACard, IntArray> {
        val res = HashMap<TTACard, IntArray>()
        if (doDiscard) {
            // 如果需要,则先移除需要摸牌区中的牌
            val num = this.removeCardNumber
            repeat(num) { i ->
                val card = this.cardRow[i] ?: return@repeat
                res[card] = intArrayOf(i, -1)
                this.cardRow[i] = null
            }
        }

        // 将所有卡牌左移,并补满摸牌区
        val tmp = this.cardRow.filterNotNull().toMutableList()
        // 如果存在当前牌堆,则进行补牌动作
        this.currentWhiteDeck?.let { deck ->
            var drawNum = TTAConsts.CARD_ROW_SIZE - tmp.size
            var drawnCards = deck.draw(drawNum)
            tmp.addAll(drawnCards)
            if (drawnCards.size < drawNum || deck.size == 0) {
                if (gameMode.currentAge <= gameMode.game.config.ageCount) {
                    // 如果当前文明牌不够补满或者正好补满摸牌区,则表示时代的结束,此时将进行时代结算,并使用新时代的牌堆
                    this.newAge()

                    val newDeck = this.currentWhiteDeck
                    if (newDeck != null) {
                        // 如果存在新的牌堆,则继续从新的牌堆中补牌
                        drawNum = TTAConsts.CARD_ROW_SIZE - tmp.size
                        drawnCards = newDeck.draw(drawNum)
                        tmp.addAll(drawnCards)
                    } else {
                        tmp.removeIf { it.activeAbility?.abilityType in arrayOf(ActiveAbilityType.PA_HUBATIAN, ActiveAbilityType.PA_NARODNI) }
                    }
                }
            }
            drawnCards.forEach { res[it] = intArrayOf(-1, tmp.indexOf(it)) }
        }
        // 补满摸牌区
        Arrays.fill(this.cardRow, null)
        tmp.indices.forEach { this.cardRow[it] = tmp[it] }
        return res
    }

    fun removeLastEvent() {
        pastEvents.removeAt(0)
    }

    /**
     * 拿走卡牌
     * @param index
     */
    fun takeCard(index: Int) {
        this.cardRow[index] = null
    }

    /**
     * 拿取指定cardId的卡牌,如果不存在则抛出异常
     * @param cardId
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun takeCard(cardId: String): TTACard {
        val index = this.getCardIndex(cardId)
        val card = this.cardRow[index] ?: throw  BoardGameException("找不到目标卡!")
        takeCard(index)
        return card
    }

    /**
     * 将指定牌放到牌堆顶
     * @return
     */
    fun topCard(cardName: String): Boolean {
        return listOfNotNull(currentWhiteDeck, currentBlackDeck).any { deck ->
            deck.cards.firstOrNull { c -> c.name == cardName || c.cardNo == cardName }?.let {
                deck.removeCard(it)
                deck.cards.add(0, it)
                return@any true
            } ?: false
        }
    }

    /**
     * 将指定牌放到牌堆顶
     * @return
     */
    fun topCardByNo(cardNo: String): Boolean {
        return listOfNotNull(currentWhiteDeck, currentBlackDeck).any { deck ->
            deck.cards.firstOrNull { c -> c.cardNo == cardNo }?.let {
                deck.removeCard(it)
                deck.cards.add(0, it)
                return@any true
            } ?: false
        }
    }

    inner class EventResult(var card: MilitaryCard, var player: TTAPlayer?, var futureToCurrent: Boolean = false)

    companion object {
        /**
         * 表达式中可能出现的参数名称
         */
        private val COSTS = intArrayOf(1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3)
    }
}


