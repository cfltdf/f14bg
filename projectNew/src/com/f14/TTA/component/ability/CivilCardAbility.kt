package com.f14.TTA.component.ability

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.WonderCard
import com.f14.TTA.consts.*
import com.google.gson.annotations.SerializedName
import kotlin.math.min

/**
 * 内政牌的能力
 * @author F14eagle
 */
class CivilCardAbility : CardAbility() {
    lateinit var abilityType: CivilAbilityType
    /**
     * 能力调整方式
     */
    var adjustType = AdjustType.BY_NUM
    /**
     * 能力调整的目标
     */
    var adjustTarget = AdjustTarget.NONE
    /**
     * 调整时参照的能力值
     */
    var byProperty: CivilizationProperty? = null
    /**
     * 全局能力中是否会影响到自己
     */
    var effectSelf: Boolean = false
    /**
     * 一次可建造奇迹的步骤数
     */
    var buildStep: Int = 0
    /**
     * 因子乘数
     */
    var amount = 1
    /**
     * 非启动式的能力
     */
    @SerializedName("flag")
    var isFlag = false
    /**
     * 卡片消失后保留
     */
    @SerializedName("keepAlive")
    var isKeepAlive = false
    /**
     * 该能力最多能应用的个体数量
     */
    var limit = Integer.MAX_VALUE
    /**
     * 该能力在每个个体上的限制
     */
    var limitUnit = Integer.MAX_VALUE

    /**
     * 从指定范围卡牌中取得符合能力条件的基数数量
     * @return
     */
    fun getAvailableNumber(range: List<TTACard>, player: TTAPlayer): Int {
        val cards: Collection<TTACard> = when (this.adjustTarget) {
            AdjustTarget.ALL, AdjustTarget.HAND -> this.getAvailableCards(range)
            AdjustTarget.BEST -> listOfNotNull(this.getBestCard(range))
            AdjustTarget.BEST_ALLOVER -> this.getBestAllOverCards(this.getAvailableCards(range), player.getFieldCards(this))
            else -> return 0
        }
        var res = when (this.adjustType) {
            AdjustType.BY_NUM // 计算个体数量
            -> cards.sumBy(TTACard::availableCount)
            AdjustType.BY_PROPERTY // 计算个体的属性
            -> cards.sumBy { c -> min(c.property.getProperty(byProperty!!), limitUnit) }
            AdjustType.BY_NUM_PROPERTY // 计算个体的属性数量
            -> cards.sumBy { c -> min(c.property.getProperty(byProperty!!), limitUnit) * c.availableCount }
            AdjustType.BY_LEVEL // 计算个体的等级
            -> cards.sumBy(TTACard::level)
            AdjustType.BY_NUM_LEVEL // 计算个体的等级数量
            -> cards.sumBy { it.level * it.availableCount }
            AdjustType.BY_GROUP_NUM -> // 计算组合的数量
                this.wcs.map { cards.filter(it::test).sumBy(TTACard::availableCount) }.min() ?: 0
            AdjustType.BY_CARD_AVAILABLE // 计算有效牌数量
            -> cards.count { it.availableCount > 0 }
            AdjustType.BY_SUBTYPE_AVAILABLE// 计算有效子种类数量
            -> cards.filter { it.availableCount > 0 }.distinctBy(TTACard::cardSubType).size
            AdjustType.BY_ASHOKA // 阿育王——不同颜色卡牌数
            -> return if (cards.distinctBy(TTACard::cardType).count() >= amount) 1 else 0
            AdjustType.BY_TECHNOLOGY_NUM //科技牌的数量
            -> cards.count()
            AdjustType.BY_BLUE_TOKEN // 根据蓝点数量
            -> cards.filterIsInstance<WonderCard>().sumBy(WonderCard::blues)
            else -> 0
        }
        // 乘因子
        res *= this.amount
        // 最多能应用的总数
        res = min(res, limit)
        return res
    }

    /**
     * 按照能力类型,从指定玩家取得符合能力条件的基数数量
     * @param player 指定玩家
     * @return
     */
    fun getAvailableNumber(player: TTAPlayer): Int {
        var res = when (adjustTarget) {
            AdjustTarget.NONE // 无
            -> 1
            AdjustTarget.ALL, // 对象是全部
            AdjustTarget.BEST, // 最高等级的
            AdjustTarget.BEST_ALLOVER -> return getAvailableNumber(player.allPlayedCard, player)
            AdjustTarget.AGE -> if (this.test(player.ageDummyCard)) 1 else 0
            AdjustTarget.MILITARY -> if (player.testRank(CivilizationProperty.MILITARY, this.effectSelf, this.limitUnit)) 1 else 0
            AdjustTarget.UNDERWAR -> if (player.isWarTarget) 1 else 0
            AdjustTarget.PA_BLOOD_AND_THREAT -> if (player.params.getBoolean(ActiveAbilityType.PA_BLOOD_AND_THREAT)) 0 else 1
            AdjustTarget.NANA -> (player.culturePoint - player.defenceMilitary) / 7
            AdjustTarget.PLAYERNUM -> player.realPlayerNumber
            AdjustTarget.UNUSED_WORKER -> player.tokenPool.unusedWorkers
            AdjustTarget.CURRENT_AGE -> player.ageDummyCard.level
            AdjustTarget.HAND // 手牌
            -> return getAvailableNumber(player.allHands, player)
            else -> 0
        }
        // 乘因子
        res *= this.amount
        // 最多能应用的总数
        res = min(res, limit)
        return res
    }

    private fun getBestAllOverCards(cards: List<TTACard>, otherCards: List<TTACard>): List<TTACard> {
        val maxLevel = (cards + otherCards).filter { it.availableCount > 0 }.map(TTACard::level).max() ?: 0
        return cards.filter { it.level == maxLevel }
    }

    /**
     * 按照能力类型,从指定玩家取得符合能力条件的属性数值
     * @param player
     * @param property
     * @return
     */
    fun getAvailableProperty(player: TTAPlayer, property: CivilizationProperty) = this.getAvailableNumber(player) * this.property.getProperty(property)

    /**
     * 取得符合技能条件中最高等级的一张牌,并且是有基数的
     * @param cards
     * @return
     */
    fun getBestCard(cards: List<TTACard>): TTACard? {
        return cards.filter { this.test(it) && it.availableCount > 0 }.maxBy(TTACard::level)
    }

}
