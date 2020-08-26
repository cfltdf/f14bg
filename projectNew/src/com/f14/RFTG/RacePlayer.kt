package com.f14.RFTG

import com.f14.RFTG.card.Ability
import com.f14.RFTG.card.Goal
import com.f14.RFTG.card.RaceCard
import com.f14.RFTG.card.SettleAbility
import com.f14.RFTG.consts.GameState
import com.f14.RFTG.consts.RaceActionType
import com.f14.RFTG.consts.Skill
import com.f14.bg.exception.BoardGameException
import com.f14.bg.hall.GameRoom
import com.f14.bg.hall.User
import com.f14.bg.player.Player
import com.f14.utils.StringUtils
import java.util.*

class RacePlayer(user: User, room: GameRoom) : Player(user, room) {
    var vp: Int = 0

    var state: GameState? = null
    var roundDiscardNum: Int = 0

    val goals: MutableList<Goal> = ArrayList()
    private var index: Int = 0

    var actionTypes: MutableList<RaceActionType> = ArrayList(0)

    val hands: MutableList<RaceCard> = LinkedList()

    val builtCards: MutableList<RaceCard> = LinkedList()

    var startWorld: RaceCard? = null
        set(card) {
            if (card != null) {
                if (card.startWorld < 0) {
                    throw BoardGameException("选择的牌不能作为起始星球!")
                }
                this.index = card.startWorld
                this.addBuiltCard(card)
            }
            field = card
        }

    /**
     * 添加建造完成的牌
     * @param card
     */
    fun addBuiltCard(card: RaceCard) {
        this.builtCards.add(card)
    }

    /**
     * 添加建造完成的牌
     */
    fun addBuiltCards(cards: List<RaceCard>) {
        this.builtCards.addAll(cards)
    }

    /**
     * 将指定的牌加入到手牌
     * @param card
     */
    fun addCard(card: RaceCard) {
        this.hands.add(card)
    }

    /**
     * 将指定的牌加入到手牌
     * @param cards
     */
    fun addCards(cards: List<RaceCard>) {
        this.hands.addAll(cards)
    }

    /**
     * 得到目标
     * @param goal
     */
    fun addGoal(goal: Goal) {
        this.goals.add(goal)
    }

    /**
     * 弃掉指定牌上的货物,如果没有牌或者货物,则抛出异常
     * @param cardIds
     * @return
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun discardGoods(cardIds: String): List<RaceCard> {
        val cards = this.getBuiltCards(cardIds)
        cards.any { it.good == null } && throw BoardGameException("该星球没有货物!")
        val goods = cards.mapNotNull(RaceCard::good)
        cards.forEach { it.good = null }
        return goods
    }

    /**
     * 弃掉指定的已打出的牌,如果没有找到则抛出异常 如果输入的是空字符串则返回空列表
     * @param cardIds
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun discardPlayedCards(cardIds: String): List<RaceCard> {
        val res = this.getBuiltCards(cardIds)
        this.builtCards.removeAll(res)
        return res
    }

    /**
     * 按照技能取得能力对象
     * @param skill
     * @return
     */
    fun <A : Ability> getAbilityBySkill(clazz: Class<A>, skill: Skill): A? {
        return this.builtCards.mapNotNull { it.getAbilityBySkill(clazz, skill) }.firstOrNull()
    }

    /**
     * 取得玩家所有建成的卡牌中拥有指定阶段能力的卡牌
     * @param <A>
     * @param clazz
     * @return
    </A> */
    fun <A : Ability> getActiveCardsByAbilityType(clazz: Class<A>): List<RaceCard> {
        return builtCards.filter { it.isAbilitiesActive(clazz) }
    }

    /**
     * 取得基本的战力
     * @return
     */
    // 检查玩家在扩张阶段能提供军事力的特殊能力
    // 每个星球提供军事力
    val baseMilitary: Int
        get() = builtCards.sumBy(RaceCard::military) + builtCards.mapNotNull(RaceCard::settleAbility)
                .filter { it.skill === Skill.WORLD_TO_MILITARY }
                .sumBy { builtCards.count(it::test) }

    /**
     * 按照id取得已经打出的牌,如果没有找到则抛出异常
     * @param id
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getBuiltCard(id: String) = this.builtCards.firstOrNull { it.id == id } ?: throw BoardGameException("没有找到指定的牌!")


    /**
     * 按照cardIds取得已打出的牌,如果没有找到则抛出异常 如果输入的是空字符串则返回空列表
     * @param cardIds 各个id间用","隔开
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getBuiltCards(cardIds: String) = StringUtils.string2List(cardIds).map(this::getBuiltCard)

    /**
     * 取得所有拥有货物的星球
     * @return
     */
    val builtCardsWithGood: List<RaceCard>
        get() = this.builtCards.filter { it.good != null }

    /**
     * 取得所有拥有货物并且适用于指定能力的星球
     * @param ability
     * @return
     */

    fun getBuiltCardsWithGood(ability: Ability) = this.builtCards.filter { it.good != null && ability.test(it) }

    /**
     * 按照id取得手牌,如果没有找到则抛出异常
     * @param id
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getCard(id: String) = this.hands.firstOrNull { it.id == id } ?: throw BoardGameException("没有找到指定的牌!")

    /**
     * 按照cardIds取得的手牌,如果没有找到则抛出异常 如果输入的是空字符串则返回空列表
     * @param cardIds 各个id间用","隔开
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getCards(cardIds: String) = StringUtils.string2List(cardIds).map(this::getCard)

    /**
     * 取得玩家所有建成的卡牌中拥有指定阶段能力的卡牌
     * @param <A>
     * @param clazz
     * @return
    </A> */
    inline fun <reified A : Ability> getCardsByAbilityType(clazz: Class<A>) = this.builtCards.filter { it.hasAbility(clazz) }

    /**
     * 取得所有的货物
     * @return
     */
    val goods: List<RaceCard>
        get() = this.builtCards.mapNotNull(RaceCard::good)

    /**
     * 取得手牌数量
     * @return
     */
    val handSize: Int
        get() = this.hands.size

    /**
     * 按照星球的种类取得战力值
     * @param card
     * @return
     */
    fun getMilitary(card: RaceCard) = baseMilitary +
            // 加上特殊能力值
            this.builtCards.mapNotNull(RaceCard::settleAbility)
                    .filter { it.military != 0 && it.test(card) }
                    .sumBy(SettleAbility::military)


    /**
     * 判断是否已经建造过指定的牌
     * @param cardNo
     * @return
     */
    fun hasBuiltCard(cardNo: String) = this.builtCards.any { it.cardNo == cardNo }

    /**
     * 判断手牌中是否有指定id的牌
     * @param id
     * @return
     */
    fun hasCard(id: String) = this.hands.any { it.id == id }

    /**
     * 判断玩家的星球上是否有货物
     * @return
     */
    fun hasGood() = this.goods.isNotEmpty()

    /**
     * 判断玩家是否拥有指定的技能
     * @param skill
     * @return
     */
    fun hasSkill(skill: Skill) = this.builtCards.any { it.hasSkill(skill) }

    /**
     * 判断玩家是否选择了指定的行动
     * @param actionType
     * @return
     */
    fun isActionSelected(vararg actionType: RaceActionType) = actionType.any(this.actionTypes::contains)

    /**
     * 打出指定id的牌,如果手牌中没有该牌,则抛出异常
     * @param id
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playCard(id: String): RaceCard {
        val card = this.getCard(id)
        this.hands.remove(card)
        return card
    }

    /**
     * 打出指定的手牌,如果没有找到则抛出异常 如果输入的是空字符串则返回空列表
     * @param cardIds
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playCards(cardIds: String): List<RaceCard> {
        val res = this.getCards(cardIds)
        this.hands.removeAll(res)
        return res
    }

    /**
     * 移除目标
     * @param goal
     */
    fun removeGoal(goal: Goal) {
        this.goals.remove(goal)
    }

    /**
     * 重置玩家的游戏信息
     */
    override fun reset() {
        super.reset()
        this.vp = 0
        this.roundDiscardNum = 0
        this.actionTypes.clear()
        this.builtCards.clear()
        this.goals.clear()
        this.hands.clear()
        this.index = 0
        this.startWorld = null
        this.state = null
    }

}
