package com.f14.tichu

import com.f14.bg.hall.GameRoom
import com.f14.bg.hall.User
import com.f14.bg.player.Player
import com.f14.tichu.componet.TichuCard
import com.f14.tichu.componet.TichuCardCheck
import com.f14.tichu.componet.TichuCardDeck
import com.f14.tichu.componet.TichuCardGroup
import com.f14.tichu.consts.AbilityType
import com.f14.tichu.consts.TichuType
import com.f14.tichu.utils.CombinationUtil

class TichuPlayer(user: User, room: GameRoom) : Player(user, room) {
    var groupIndex: Int = 0
    var rank = 0
    var tichuType: TichuType? = null
    var lastGroup: TichuCardGroup? = null
    var score = 0
    var pass = false
    var tichuScore = 0
    var tichuButton = true
    var bombButton = true
    var showHand = false

    /**
     * 取得手牌
     * @return
     */
    var hands = TichuCardDeck()

    /**
     * 取得收获的牌
     * @return
     */
    var roots = TichuCardDeck()
        private set

    /**
     * 判断玩家是否可以叫tichu
     * @return
     */
    val canCallTichu: Boolean
        get() = this.tichuType == null

    /**
     * 取得手牌中的分数
     * @return
     */
    val handScore: Int
        get() = hands.cards.sumBy(TichuCard::score)

    /**
     * 取得玩家的总分
     * @return
     */
    val totalScore: Int
        get() = this.score + this.tichuScore

    /**
     * 检查玩家是否有炸弹
     * @return
     */
    val hasBomb: Boolean
        get() = TichuCardCheck(this, this.hands.cards).bombs.isNotEmpty()

    /**
     * 判断玩家是否还有手牌
     * @return
     */
    val hasCard: Boolean
        get() = !this.hands.empty

    /**
     * 检查玩家是否拥有指定能力的卡牌
     * @param abilityType
     * @return
     */
    fun hasCard(abilityType: AbilityType): Boolean {
        return CombinationUtil.hasCard(this.hands.cards, abilityType)
    }

    /**
     * 检查玩家是否拥有指定点数的卡牌
     * @param point
     * @return
     */
    fun hasCard(point: Double): Boolean {
        return CombinationUtil.hasCard(this.hands.cards, point)
    }

    override fun reset() {
        super.reset()
        this.hands.clear()
        this.roots.clear()
        this.rank = 0
        this.tichuType = null
        this.lastGroup = null
        this.score = 0
        this.pass = false
        this.tichuScore = 0
        this.tichuButton = true
        this.bombButton = true
        this.showHand = false
    }

    override fun toMap() = super.toMap() + mapOf(
            "rank" to rank,
            "score" to score,
            "handSize" to hands.size,
            "tichuType" to (tichuType?.let(TichuType.Companion::getChinese) ?: ""),
            "tichuScore" to tichuScore
    )
}
