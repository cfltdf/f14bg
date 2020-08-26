package com.f14.TTA.component.ability

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.*
import java.util.*

/**
 * 卡牌的得文化分能力
 * @author F14eagle
 */
class ScoreAbility : CardAbility() {
    lateinit var scoreAbilityType: ScoreAbilityType
    var aboveNum: Int = 0
    var byProperty: CivilizationProperty? = null
    var rankValue: Map<Int, String> = HashMap()
    var maxValue = Integer.MAX_VALUE

    /**
     * 取得玩家可以从该卡牌得到的分数
     * @param player
     * @return
     */
    fun getScoreCulturePoint(player: TTAPlayer): Int {
        val cards = player.getPlayedCard(this)
                // 不计算红十字会虚拟牌
                .filterNot { it.activeAbility?.abilityType == ActiveAbilityType.PA_RED_CROSS }
        val cp = this.property.getProperty(CivilizationProperty.CULTURE)
        var res = when (this.scoreAbilityType) {
            ScoreAbilityType.CONSTANT -> 1
            ScoreAbilityType.BY_COUNT -> cards.size
            ScoreAbilityType.BY_COUNT_SQUARE -> cards.size * cards.size
            ScoreAbilityType.BY_LEVEL -> cards.sumBy(TTACard::level)
            ScoreAbilityType.BY_NUM -> cards.sumBy(TTACard::availableCount)
            ScoreAbilityType.BY_NUM_LEVEL -> cards.sumBy { it.level * it.availableCount }
            ScoreAbilityType.BY_NUM_PROPERTY_ADJUSTED -> cards.sumBy { it.availableCount * it.property.getProperty(this.byProperty!!) } +
                    // 统计玩家因指定卡牌而增加的能力
                    player.abilityManager.getAbilitiesByType(CivilAbilityType.ADJUST_PROPERTY).sumBy { it.getAvailableNumber(cards, player) * it.property.getProperty(this.byProperty!!) }
            ScoreAbilityType.BY_SUBTYPE_AVAILABLE -> cards.filter { it.availableCount > 0 }.mapNotNull(TTACard::cardSubType).toSet().size
            ScoreAbilityType.BY_PLAYER_PROPERTY -> player.getProperty(this.byProperty!!)
            ScoreAbilityType.BY_RANK -> {
                val rank = player.getRank(this.byProperty!!, false)
                this.rankValue.getValue(player.realPlayerNumber).split(",".toRegex())[rank - 1].toInt()
            }
            ScoreAbilityType.FOOD_PRODUCTION -> player.foodProduction
            ScoreAbilityType.RESOURCE_PRODUCTION -> player.resourceProduction
            ScoreAbilityType.BY_CONTENT_WORKER -> maxOf(0, player.workers - player.tokenPool.unhappyWorkers - this.aboveNum)
            ScoreAbilityType.BY_WORKER -> maxOf(0, player.workers - this.aboveNum)
            ScoreAbilityType.IMPACT_OF_AGRICULTURE_V2 ->
                // 食物生产力
                // 生产大于消费时额外得4分
                player.foodProduction.let { if (it > player.consumption) it + 4 else it }
            ScoreAbilityType.IMPACT_OF_INDUSTRY_V2 ->
                // 仅计算矿山产值
                player.productionFromMine.totalValue
            ScoreAbilityType.IMPACT_OF_BALANCE_V2 ->
                // 粮矿灯分4项产值取最小值x2
                arrayOf(player.foodProduction, player.resourceProduction, player.getProperty(CivilizationProperty.CULTURE), player.getProperty(CivilizationProperty.SCIENCE)).min()
                        ?: 0
            ScoreAbilityType.BY_EMPIRE -> this.rankValue.getValue(player.realPlayerNumber).split(",".toRegex())[0].toInt() * arrayOf(CivilizationProperty.FOOD, CivilizationProperty.RESOURCE, CivilizationProperty.SCIENCE, CivilizationProperty.CULTURE, CivilizationProperty.MILITARY).count(player::testAboveAll)
            ScoreAbilityType.UNION_NATION -> player.allPlayedCard.groupBy(TTACard::cardType).filterKeys { it in arrayOf(CardType.BUILDING, CardType.SPECIAL, CardType.WONDER) }.map { it.value.count() }.max()
                    ?: 0
            ScoreAbilityType.BY_NUM_FIELD -> player.getFieldCards(this).count()

            else -> 0
        }
        // 检查最大值限制
        res = minOf(this.maxValue, res * cp)
        return res
    }
}
