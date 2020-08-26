package com.f14.TTA.manager

import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.component.card.PactCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.CivilAbilityType

import java.util.*

/**
 * TTA玩家的能力管理器

 * @author F14eagle
 */
class TTAAbilityManager {
    /**
     * 可激活的卡牌
     */
    val activeCards: MutableSet<TTACard> = LinkedHashSet()
    /**
     * 分组的abilities容器
     */
    private val abilities: MutableMap<CivilAbilityType, MutableList<CivilCardAbility>> = HashMap()
    /**
     * 能力对应的卡牌信息
     */
    private val cardRelationMap: MutableMap<CivilCardAbility, TTACard> = HashMap()
    /**
     * 非启动式的能力
     */
    private val flaggedAbilities: MutableSet<CivilCardAbility> = LinkedHashSet()

    /**
     * 添加能力
     * @param ability
     * @param card
     */
    fun addAbility(ability: CivilCardAbility, card: TTACard) {
        val abilities = this.getAbilitiesByType(ability.abilityType)
        abilities.add(ability)
        this.cardRelationMap[ability] = card
        if (ability.isFlag) {
            flaggedAbilities.add(ability)
        }
    }

    /**
     * 添加可激活的卡牌
     * @param card
     */
    fun addActiveCard(card: TTACard) {
        this.activeCards.add(card)
    }

    /**
     * 添加指定卡牌的所有能力
     * @param card
     */
    fun addCardAbilities(card: TTACard) {
        for (ca in card.abilities) {
            this.addAbility(ca, card)
        }
    }

    /**
     * 清除所有能力
     */
    fun clear() {
        this.abilities.clear()
        this.activeCards.clear()
        this.flaggedAbilities.clear()
        this.cardRelationMap.clear()
    }

    /**
     * 按照类型取得相应的能力
     * @param type
     * @return
     */
    fun getAbilitiesByType(type: CivilAbilityType): MutableList<CivilCardAbility> {
        return this.abilities.computeIfAbsent(type) { ArrayList() }
    }

    /**
     * 按照类型取得相应的能力及所属卡牌关系
     * @param type
     * @return
     */
    fun getAbilitiesWithRelation(type: CivilAbilityType): Map<CivilCardAbility, TTACard> {
        val res = HashMap<CivilCardAbility, TTACard>()
        for (ca in this.getAbilitiesByType(type)) {
            res[ca] = cardRelationMap[ca]!!
        }
        return res
    }

    /**
     * 取得指定技能列表中的第一个技能
     * @param type
     * @return
     */
    fun getAbility(type: CivilAbilityType): CivilCardAbility? {

        return this.getAbilitiesByType(type).firstOrNull()
    }

    /**
     * 取得指定技能列表中的第一个技能的卡
     * @param type
     * @return
     */
    fun getAbilityCard(type: CivilAbilityType): TTACard? {
        return this.getAbilitiesWithRelation(type).values.firstOrNull()
    }

    /**
     * 取得可激活的卡牌列表
     * @return
     */
    fun getActiveCards(): Collection<TTACard> {
        return activeCards
    }


    val allFlaggedAbilities: Collection<CivilCardAbility>
        get() = flaggedAbilities

    /**
     * 按照类型取得相应的能力及所属卡牌关系(只返回条约)
     * @param type
     * @return
     */
    fun getPactAbilitiesWithRelation(type: CivilAbilityType): Map<CivilCardAbility, PactCard> {
        val res = HashMap<CivilCardAbility, PactCard>()
        for (ca in this.getAbilitiesByType(type)) {
            val c = cardRelationMap[ca]!!
            if (c.cardType == CardType.PACT) {
                res[ca] = c as PactCard
            }
        }
        return res
    }

    /**
     * 判断玩家是否拥有指定类型的技能
     * @param type
     * @return
     */
    fun hasAbilitiy(type: CivilAbilityType): Boolean {
        return this.getAbilitiesByType(type).isNotEmpty()
    }

    /**
     * 移除能力
     * @param ability
     * @return
     */
    private fun removeAbility(ability: CivilCardAbility): Boolean {
        if (ability.isFlag) {
            flaggedAbilities.remove(ability)
        }
        this.cardRelationMap.remove(ability)
        val abilities = this.getAbilitiesByType(ability.abilityType)
        return abilities.remove(ability)
    }

    /**
     * 移除可激活的卡牌
     * @param card
     */
    fun removeActiveCard(card: TTACard) {
        this.activeCards.remove(card)
    }

    /**
     * 移除指定卡牌的所有能力
     * @param card
     */
    fun removeCardAbilities(card: TTACard) {
        card.abilities.filterNot(CivilCardAbility::isKeepAlive).forEach { this.removeAbility(it) }
    }
}
