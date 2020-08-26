package com.f14.TS

import com.f14.TS.action.TSEffect
import com.f14.TS.component.TSCard
import com.f14.TS.component.TSCardDeck
import com.f14.TS.component.TSCountry
import com.f14.TS.component.TSProperties
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.consts.CardType
import com.f14.TS.consts.EffectType
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TSProperty
import com.f14.TS.manager.EffectManager
import com.f14.bg.exception.BoardGameException
import com.f14.bg.hall.GameRoom
import com.f14.bg.hall.User
import com.f14.bg.player.Player
import kotlin.math.max
import kotlin.math.min

class TSPlayer(user: User, room: GameRoom) : Player(user, room) {

    var superPower: SuperPower = SuperPower.NONE

    var hands = TSCardDeck()

    var properties = TSProperties()
        private set

    private var effectManager = EffectManager()

    init {
        this.init()
    }

    /**
     * 添加手牌

     * @param cards
     */
    fun addCards(cards: List<TSCard>) {
        this.hands.addCards(cards)
    }

    /**
     * 为玩家添加效果

     * @param card

     * @param effect
     */
    fun addEffect(card: TSCard, effect: TSEffect) {
        this.effectManager.addEffect(card, effect)
    }

    /**
     * 调整玩家本回合已经进行太空竞赛的次数
     */
    fun addSpaceRaceTimes(num: Int) {
        this.params.setRoundParameter("spaceRaceTimes", this.spaceRaceTimes + num)
    }

    /**
     * 判断玩家是否需要强制出计分牌,玩家在回合结束时不能保留计分牌

     * @param currentTurn

     * @return
     */
    fun forcePlayScoreCards(currentTurn: Int): Boolean {
        val sc = this.scoreCardsCount
        if (sc > 0) {
            if (this.actionRoundNumber - currentTurn < sc) {
                return true
            }
        }
        return false
    }

    /**
     * 取得每回合可以进行的行动轮数

     * @return
     */
    /**
     * 设置每回合可以进行的行动轮数

     * @param num
     */
    // 只要有这个效果在,永远可以执行8个行动轮
    var actionRoundNumber: Int
        get() {
            return if (this.hasEffect(EffectType.SR_PRIVILEGE_4)) {
                8
            } else {
                this.params.getInteger("actionRound")
            }
        }
        set(num) = this.params.setRoundParameter("actionRound", num)

    /**
     * 取得当前回合玩家剩余的太空竞赛次数

     * @return
     */
    // 总数-已用次数,最小为0
    val availableSpaceRaceTimes: Int
        get() = max(0, this.totalSpaceRaceTimes - this.spaceRaceTimes)

    /**
     * 得到手牌

     * @param cardId

     * @return

     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun getCard(cardId: String): TSCard {
        return this.hands.getCard(cardId)
    }

    /**
     * 按照指定的cardNo取得卡牌对象

     * @param cardNo

     * @return
     */

    fun getCardByCardNo(cardNo: Int): TSCard? {
        return this.hands.cards.firstOrNull { it.tsCardNo == cardNo }
    }

    /**
     * 按照效果类型取得对应的卡牌对象

     * @param effectType

     * @return
     */

    fun getCardByEffectType(effectType: EffectType): TSCard {
        return this.effectManager.getCardByEffectType(effectType)!!
    }

    /**
     * 按照效果类型取得效果对象

     * @param effectType

     * @return
     */

    fun getEffects(effectType: EffectType): Collection<TSEffect> {
        return this.effectManager.getEffects(effectType)
    }

    /**
     * 取得玩家不能放置影响力的区域

     * @return
     */
    /**
     * 设置玩家不能放置影响力的区域
     * @param condition
     */
    var forbiddenCondition: TSCountryCondition?
        get() = this.params.getParameter("forbidden")
        set(condition) = this.params.setGameParameter("forbidden", condition!!)

    /**
     * 取得玩家必须要出的牌

     * @return
     */

    val forcePlayCard: TSCard?
        get() {
            if (this.hasEffect(EffectType.EFFECT_49)) {
                return this.getCardByEffectType(EffectType.EFFECT_49)
            }
            return null
        }

    /**
     * 取得玩家手牌中OP点数最高的牌

     * @return
     */
    val maxOpValue: Int
        get() {
            return hands.cards.map(this::getOp).max() ?: 0
        }

    /**
     * 取得军事行动力,包括效果影响的

     * @return
     */
    // 计算额外加值后的实际军事行动力
    // 移除额外加值
    val militaryActionWithEffect: Int
        get() {
            val res: Int
            val effects = this.getEffects(EffectType.ADDITIONAL_MA_POINT)
            var bonus = effects.sumBy(TSEffect::num)
            if (this.hasEffect(EffectType.SR_PRIVILEGE_N1)) {
                bonus += 2
            }
            if (bonus != 0) {
                this.properties.addPropertyBonus(TSProperty.MILITARY_ACTION, bonus)
                res = this.getProperty(TSProperty.MILITARY_ACTION)
                this.properties.clearAllBonus()
            } else {
                res = this.getProperty(TSProperty.MILITARY_ACTION)
            }
            return res
        }

    /**
     * 取得玩家使用该牌时实际的OP值

     * @param card

     * @return
     */
    fun getOp(card: TSCard): Int {
        return this.getOp(card, null)
    }

    /**
     * 取得玩家在指定的国家使用该牌时实际的OP值
     * @param card
     * @param countries
     * @return
     */
    fun getOp(card: TSCard, countries: Collection<TSCountry>?): Int {
        var res = card.op
        // 取得OP的调整值
        res += this.effectManager.getEffects(EffectType.ADJUST_OP).sumBy(TSEffect::num)
        // res调整值调整后的op最小为1,最大为4
        res = min(4, res)
        res = max(1, res)
        // 检查所有可以提供额外OP的能力,是否可以用于指定的这些国家
        if (countries?.isEmpty() == false) res += this.effectManager.getEffects(EffectType.ADDITIONAL_OP).filter { it.countryCondGroup!!.test(countries) }.sumBy(TSEffect::num)
        return res
    }

    /**
     * 取得玩家属性
     * @param property
     * @return
     */
    fun getProperty(property: TSProperty) = this.properties.getProperty(property)


    override val reportString: String
        get() = "[${superPower.chinese}(${this.name})]"

    /**
     * 取得玩家手牌中的所有计分牌
     * @return
     */
    val scoreCards: Collection<TSCard>
        get() = this.hands.cards.filter { it.cardType == CardType.SCORING }

    /**
     * 取得玩家手牌中的计分牌数量
     * @return
     */
    val scoreCardsCount: Int
        get() = this.scoreCards.size

    /**
     * 玩家本回合已经进行太空竞赛的次数
     */
    var spaceRaceTimes: Int
        get() = this.params.getInteger("spaceRaceTimes")
        set(num) = this.params.setRoundParameter("spaceRaceTimes", num)

    /**
     * 取得玩家每个回合允许的太空竞赛次数
     */
    val totalSpaceRaceTimes: Int
    // 如果拥有特权,则可以进行2次
    // 否则只能进行1次
        get() = if (this.hasEffect(EffectType.SR_PRIVILEGE_1)) 2 else 1

    /**
     * 是否拥有指定卡牌的能力
     * @param card
     * @return
     */
    fun hasCardEffect(card: TSCard) = this.effectManager.hasCardEffect(card)

    /**
     * 判断玩家是否拥有指定的效果
     * @param effectType
     * @return
     */
    fun hasEffect(effectType: EffectType) = this.effectManager.hasEffect(effectType)

    /**
     * 判断玩家手牌中是否有计分牌
     * @return
     */
    fun hasScoreCard() = this.hands.cards.any { it.cardType == CardType.SCORING }

    /**
     * 初始化
     */
    private fun init() {
        this.properties.setMinValue(TSProperty.SPACE_RACE, 0)
        this.properties.setMaxValue(TSProperty.SPACE_RACE, 8)

        this.properties.setMinValue(TSProperty.MILITARY_ACTION, 0)
        this.properties.setMaxValue(TSProperty.MILITARY_ACTION, 5)
    }

    /**
     * 从玩家移除效果
     * @param card
     */
    fun removeEffect(card: TSCard) {
        this.effectManager.removeEffects(card)
    }

    override fun reset() {
        super.reset()
        this.hands.clear()
        this.properties.clear()
        this.superPower = SuperPower.NONE
        this.effectManager.clear()
        this.init()
    }

    /**
     * 移除手牌
     * @param cardId
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun takeCard(cardId: String) = this.hands.takeCard(cardId)

}
