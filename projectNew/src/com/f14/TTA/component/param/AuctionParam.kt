package com.f14.TTA.component.param

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.component.card.BonusCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.CivilizationProperty
import com.f14.bg.component.Convertable
import java.util.*

/**
 * 拍卖参数

 * @author F14eagle
 */
class AuctionParam(internal var player: TTAPlayer, private val isColony: Boolean) : Convertable {
    var military = 0
    private var totalValue = 0
    var pass = false
    var inputing = false

    var units: MutableMap<TechCard, Int> = HashMap()

    var blues: MutableMap<CivilCardAbility, Int> = HashMap()

    var bonusCards: MutableMap<TTACard, Boolean> = HashMap()
    val colonyBonus = player.getProperty(CivilizationProperty.COLONIZING_BONUS)

    init {
        this.military = player.getProperty(CivilizationProperty.MILITARY)
    }

    /**
     * 取得总数

     * @return
     */
    fun checkTotalValue(): Int {
        return if (isColony) {
            this.totalColonyValue
        } else {
            this.totalMilitaryValue
        }
    }

    /**
     * 清除拍卖参数
     */
    fun clear() {
        this.units.clear()
        this.bonusCards.clear()
    }

    /**
     * 取得使用加值卡的信息

     * @return
     */
    internal // 只返回选中的加值卡
    val bonusCardInfo: List<Map<String, Any>>
        get() {
            val res = ArrayList<Map<String, Any>>()
            for (card in this.bonusCards.keys) {
                val o = HashMap<String, Any>()
                o["cardId"] = card.id
                o["selected"] = this.bonusCards[card]!!
                res.add(o)
            }
            return res
        }

    /**
     * 取得所有选中的加值卡

     * @return
     */

    val selectedBonusCards: List<TTACard>
        get() {
            val res = ArrayList<TTACard>()
            for (card in this.bonusCards.keys) {
                val selected = this.bonusCards[card]
                if (selected != null && selected) {
                    res.add(card)
                }
            }
            return res
        }

    /**
     * 取得殖民点数总值
     * @return
     */
    private val totalColonyValue: Int
        get() {
            // 附加加值卡的数值
            // 检查加值卡增加能力
            // 附加部队的基本军事力
            // 如果玩家拥有战术牌,并且没有忽略战术牌的能力,则计算部队组成的军队提供的军事力
            var res = this.colonyBonus
            var bonusCardNum = 0
            for (card in this.bonusCards.filterValues { it }.keys) {
                when {
                    card.cardType == CardType.DEFENSE_BONUS -> res += (card as BonusCard).colo
                    player.abilityManager.hasAbilitiy(CivilAbilityType.PA_MEIRIN) -> res += card.level
                    player.abilityManager.hasAbilitiy(CivilAbilityType.PA_CARD_AS_BONUSCARD) -> {
                        val ca = player.abilityManager.getAbility(CivilAbilityType.PA_CARD_AS_BONUSCARD)
                        res += ca!!.property.getProperty(CivilizationProperty.COLONIZING_BONUS)
                    }
                }
                bonusCardNum += 1
            }
            res += player.abilityManager.getAbilitiesByType(CivilAbilityType.PA_ENHANCE_BONUS_CARD).sumBy {
                bonusCardNum * it.property.getProperty(CivilizationProperty.COLONIZING_BONUS)
            }
            res += this.units.entries.sumBy { (u, n) -> u.property.getProperty(CivilizationProperty.MILITARY) * n }
            res += this.blues.entries.sumBy { (u, n) -> u.property.getProperty(CivilizationProperty.MILITARY) * n }
            if (this.player.tactics != null && !player.abilityManager.hasAbilitiy(CivilAbilityType.PA_IGNORE_TACTICS)) {
                val result = this.player.getTacticsResult(units)
                res += result.totalMilitaryBonus + player.abilityManager.getAbilitiesByType(CivilAbilityType.PA_TATIC_BONUS).sumBy {
                    val totalNum = result.mainArmyNum + result.secondaryArmyNum
                    (totalNum + minOf(totalNum, result.airForceNum)) * it.getAvailableNumber(listOfNotNull(this.player.tactics), player)
                }
            }
            return res
        }

    /**
     * 取得军事点数总值
     * @return
     */
    private val totalMilitaryValue: Int
        get() {
            // 检查玩家是否有加强防御卡的能力
            // 加强倍数
            // 附加加值卡的数值
            // 检查加值卡增加能力
            // 附加部队的基本军事力
            // 如果玩家拥有战术牌,并且没有忽略战术牌的能力,则计算部队组成的军队提供的军事力
            var res = this.military
            val multi = 1 + player.abilityManager.getAbilitiesByType(CivilAbilityType.PA_ENHANCE_DEFENSE_CARD).sumBy(CivilCardAbility::amount)
            var bonusCardNum = 0
            for (card in this.bonusCards.keys) {
                if (this.bonusCards[card]!!) {
                    res += when {
                        card.cardType == CardType.DEFENSE_BONUS -> (card as BonusCard).defense * multi
                        player.abilityManager.hasAbilitiy(CivilAbilityType.PA_MEIRIN) -> card.level * 2 * multi
                        else -> 1
                    }
                    bonusCardNum += 1
                }
            }

            res += player.abilityManager.getAbilitiesByType(CivilAbilityType.PA_ENHANCE_BONUS_CARD).sumBy {
                bonusCardNum * it.property.getProperty(CivilizationProperty.MILITARY)
            }
            res += this.units.entries.sumBy { (u, n) -> u.property.getProperty(CivilizationProperty.MILITARY) * n }
            res += this.blues.entries.sumBy { (u, n) -> u.property.getProperty(CivilizationProperty.MILITARY) * n }
            if (this.player.tactics != null && !player.abilityManager.hasAbilitiy(CivilAbilityType.PA_IGNORE_TACTICS)) {
                val result = this.player.getTacticsResult(units)
                res += result.totalMilitaryBonus
                if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_MEIRIN)) {
                    res += (result.mainArmyNum + result.secondaryArmyNum) * this.player.tactics!!.level
                }
            }
            return res
        }


    fun getTotalValue(): Int {
        return if (isColony) {
            this.totalValue
        } else {
            this.checkTotalValue()
        }
    }

    /**
     * 取得牺牲部队的信息
     * @return
     */
    internal // 只返回部队的cardId和牺牲的数量
    val unitsInfo: List<Map<String, Any>>
        get() {
            val res = ArrayList<Map<String, Any>>()
            this.units.mapTo(res) { (u, num) -> mapOf("cardId" to u.id, "num" to num) }
            this.blues.mapKeys { player.abilityManager.getAbilityCard(it.key.abilityType)!! }.mapTo(res) { (a, num) -> mapOf("cardId" to a.id, "num" to num) }
            return res
        }

    /**
     * 检查是否拥有部队
     * @return
     */
    fun hasUnit(): Boolean {
        return units.values.any { it > 0 }
    }

    fun setBlueNum(a: CivilCardAbility, num: Int) {
        this.blues[a] = num
        this.totalValue = this.checkTotalValue()
    }

    /**
     * 设置加值卡是否选中
     * @param card
     * @param selected
     */
    fun setBonusCard(card: TTACard, selected: Boolean) {
        this.bonusCards[card] = selected
        this.totalValue = this.checkTotalValue()
    }

    /**
     * 设置unit的数量
     * @param unit
     * @param num
     */
    fun setUnitNum(unit: TechCard, num: Int) {
        this.units[unit] = num
        this.totalValue = this.checkTotalValue()
    }


    override fun toMap(): Map<String, Any> = mapOf("pass" to this.pass, "totalValue" to this.getTotalValue(), "units" to this.unitsInfo, "bonusCards" to this.bonusCardInfo)

    fun setTotalValue(num: Int) {
        totalValue = num
    }
}