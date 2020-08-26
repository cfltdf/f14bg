package com.f14.innovation

import com.f14.bg.exception.BoardGameException
import com.f14.bg.hall.GameRoom
import com.f14.bg.hall.User
import com.f14.bg.player.Player
import com.f14.innovation.component.*
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.consts.InnoSplayDirection
import java.util.*

class InnoPlayer(user: User, room: GameRoom) : Player(user, room) {
    var firstAction: Boolean = false
    val hands: InnoCardGroup = InnoCardGroup()
    val scores: InnoCardGroup = InnoCardGroup()
    private val cardStacks: MutableMap<InnoColor, InnoCardStack> = HashMap()
    val achieveCards: InnoCardDeck = InnoCardDeck()
    val iconCounter = InnoIconCounter()

    /**
     * 添加成就牌
     * @param card
     */
    fun addAchieveCard(card: InnoCard) = this.achieveCards.addCard(card)

    /**
     * 添加手牌
     * @param card
     */
    fun addHand(card: InnoCard) = this.hands.addCard(card)

    /**
     * 添加手牌
     * @param cards
     */
    fun addHands(cards: Collection<InnoCard>) = this.hands.addCards(cards)

    /**
     * 增加回合计分牌数
     * @param i
     */
    fun addRoundScoreCount(i: Int) {
        this.roundScoreCount += i
    }

    /**
     * 增加回合垫底牌数
     * @param i
     */
    fun addRoundTuckCount(i: Int) {
        this.roundTuckCount += i
    }

    /**
     * 添加分数
     * @param card
     */
    fun addScore(card: InnoCard) = this.scores.addCard(card)

    /**
     * 添加分数
     * @param cards
     */
    fun addScores(cards: Collection<InnoCard>) = this.scores.addCards(cards)

    /**
     * 判断玩家是否可以展开指定的牌堆
     * @param color
     * @return
     */
    fun canSplayStack(color: InnoColor, splayDirection: InnoSplayDirection): Boolean {
        val stack = this.getCardStack(color)
        // 至少要有1张牌,并且与需要展开的方向不同,才能展开
        return stack != null && stack.size > 1 && stack.splayDirection != splayDirection
    }

    /**
     * 移除玩家所有游戏中的卡牌
     */
    fun clearAllCards() {
        this.hands.clear()
        this.scores.clear()
        this.cardStacks.clear()
        this.iconCounter.clear()
    }

    /**
     * 清除回合垫底/计分牌数
     */
    fun clearRoundCount() {
        this.params.setRoundParameter(ROUND_TUCK_COUNT, 0)
        this.params.setRoundParameter(ROUND_SCORE_COUNT, 0)
    }

    /**
     * 取得指定颜色的置底牌
     * @param color
     * @return
     */
    fun getBottomCard(color: InnoColor) = this.getCardStack(color)?.bottomCard

    /**
     * 取得颜色对应的已打出牌堆
     * @param color
     * @return
     */
    fun getCardStack(color: InnoColor) = this.cardStacks[color]

    /**
     * 取得所有指定颜色的手牌
     * @param color
     * @return
     */
    fun getHandsByColor(color: InnoColor) = this.hands.getCards().filter { it.color == color }

    /**
     * 取得玩家指定符号的数量
     * @param icon
     * @return
     */
    fun getIconCount(icon: InnoIcon) = this.cardStacks.values.sumBy { it.getIconCount(icon) }

    /**
     * 取得玩家最高等级的置顶牌等级

     * @return
     */
    val maxLevel: Int
        get() = this.cardStacks.values.mapNotNull(InnoCardStack::topCard).map(InnoCard::level).max() ?: 0

    /**
     * 取得回合计分牌数
     * @return
     */
    var roundScoreCount: Int
        get() = this.params.getInteger(ROUND_SCORE_COUNT)
        set(i) = this.params.setRoundParameter(ROUND_SCORE_COUNT, i)

    /**
     * 取得回合垫底牌数
     * @return
     */
    var roundTuckCount: Int
        get() = this.params.getInteger(ROUND_TUCK_COUNT)
        set(i) = this.params.setRoundParameter(ROUND_TUCK_COUNT, i)

    /**
     * 取得玩家的总分数
     * @return
     */
    val score: Int
        get() = this.scores.getCards().sumBy(InnoCard::level)

    /**
     * 取得对应颜色牌堆的信息
     * @param color
     * @return
     */
    fun getStackInfo(color: InnoColor): Map<String, Any?> = mapOf(color.toString() to this.getCardStack(color)?.toMap())

    /**
     * 取得所有颜色牌堆的信息
     * @return
     */
    val stacksInfo: Map<String, Any?>
        get() = InnoColor.values().map { it.toString() to this.getCardStack(it)?.toMap() }.toMap()

    /**
     * 取得起始牌(其实是随便拿了一张牌)
     * @return
     */
    val startCard: InnoCard?
        get() = this.cardStacks.values.firstOrNull()?.topCard

    /**
     * 取得指定颜色的置顶牌
     * @param color
     * @return
     */
    fun getTopCard(color: InnoColor) = this.getCardStack(color)?.topCard

    /**
     * 取得所有置顶牌
     * @return
     */
    val topCards: List<InnoCard>
        get() = this.cardStacks.values.mapNotNull(InnoCardStack::topCard)

    /**
     * 取得指定颜色的置顶牌
     * @param colors
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getTopCards(vararg colors: InnoColor) = colors.map {
        this.getTopCard(it) ?: throw BoardGameException("没有找到对应颜色的置顶牌!")
    }

    /**
     * 判断玩家是否拥有所有颜色的牌堆
     * @return
     */
    fun hasAllColorStack() = InnoColor.values().all(this::hasCardStack)

    /**
     * 判断玩家是否有指定颜色的已打出牌堆
     * @param color
     * @return
     */
    fun hasCardStack(color: InnoColor) = this.getCardStack(color)?.empty == false

    /**
     * 判断这张牌是否是手牌中最高的等级
     * @param card
     * @return
     */
    fun isHighestLevelInHand(card: InnoCard) = this.hands.getCards().none { it.level > card.level }

    /**
     * 合并指定牌
     * @param card
     */
    fun meld(card: InnoCard) {
        val color = card.color!!
        var stack = this.getCardStack(color)
        if (stack == null) {
            stack = InnoCardStack(card)
            this.cardStacks[color] = stack
        } else {
            stack.meld(card)
        }
        this.refreshIconCounter()
    }

    /**
     * 刷新符号计数
     */
    private fun refreshIconCounter() {
        this.iconCounter.clear()
        this.cardStacks.values.map(InnoCardStack::iconCounter).forEach(this.iconCounter::addProperties)
    }

    /**
     * 移除牌堆中的牌
     */
    fun removeStackCard(card: InnoCard): Boolean {
        val color = card.color!!
        val stack = this.getCardStack(card.color!!) ?: return false
        val res = stack.removeStackCard(card)
        // 如果牌堆为空,则移除该牌堆
        if (stack.empty) this.cardStacks.remove(color)
        this.refreshIconCounter()
        return res
    }

    /**
     * 移除置顶牌
     * @param color
     */

    fun removeTopCard(color: InnoColor): InnoCard? {
        val stack = this.getCardStack(color) ?: return null
        val card = stack.removeTopCard()
        // 如果牌堆为空,则移除该牌堆
        if (stack.empty) this.cardStacks.remove(color)
        this.refreshIconCounter()
        return card
    }

    override fun reset() {
        super.reset()
        this.hands.clear()
        this.scores.clear()
        this.cardStacks.clear()
        this.achieveCards.clear()
        this.iconCounter.clear()
    }

    /**
     * 展开指定颜色的牌堆
     * @param color
     * @param splayDirection
     */
    fun splay(color: InnoColor, splayDirection: InnoSplayDirection) {
        this.getCardStack(color)?.splay(splayDirection)
        this.refreshIconCounter()
    }

    /**
     * 追加指定牌
     * @param card
     */
    fun tuck(card: InnoCard) {
        var stack = this.getCardStack(card.color!!)
        if (stack == null) {
            stack = InnoCardStack(card)
            this.cardStacks[card.color!!] = stack
        } else {
            stack.tuck(card)
        }
        this.refreshIconCounter()
    }

    companion object {
        /**
         * 参数 - 回合计分牌数
         */
        private const val ROUND_SCORE_COUNT = "ROUND_SCORE_COUNT"
        /**
         * 参数 - 回合垫底牌数
         */
        private const val ROUND_TUCK_COUNT = "ROUND_TUCK_COUNT"
    }

}
