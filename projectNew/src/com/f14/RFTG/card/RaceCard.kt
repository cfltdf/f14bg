package com.f14.RFTG.card

import com.f14.RFTG.consts.*
import com.f14.bg.component.Card
import java.util.*

class RaceCard : Card() {

    var enName: String? = null
    var cost = 0
    var vp = 0
    var startWorld = -1
    var startHandNum = 0
    var military = 0

    var good: RaceCard? = null
    var type: CardType? = null

    var worldTypes: MutableList<WorldType> = ArrayList()
    var productionType: ProductionType? = null
    var goodType: GoodType? = null
    var specialProduction = false

    var symbols: MutableList<Symbol> = ArrayList()
    var startWorldType: StartWorldType? = null

    var exploreAbility: ExploreAbility? = null
    var developAbility: DevelopAbility? = null
    var settleAbility: SettleAbility? = null
    var tradeAbility: TradeAbility? = null
    var consumeAbility: ConsumeAbility? = null
    var produceAbility: ProduceAbility? = null
    var bonusAbilities: Array<BonusAbility> = emptyArray()
    var specialAbilities: Array<SpecialAbility> = emptyArray()

    val abilities: List<Ability>
        get() = listOfNotNull(
                exploreAbility,
                developAbility,
                settleAbility,
                tradeAbility,
                consumeAbility,
                produceAbility) +
                bonusAbilities +
                specialAbilities

    override fun clone() = super.clone() as RaceCard

    /**
     * 取得指定类型的能力
     * @param <A>
     * @param clazz
     * @return
     */

    fun <A> getAbilitiesByType(clazz: Class<A>): List<A> = abilities.filterIsInstance(clazz)

    /**
     * 判断卡牌是否拥有指定的skill
     * @param skill
     * @return
     */
    fun <A : Ability> getAbilityBySkill(clazz: Class<A>, skill: Skill): A? = this.abilities.filterIsInstance(clazz).firstOrNull { it.skill == skill }

    /**
     * 取得指定阶段中的主动使用的能力(第一个)
     * @param <A>
     * @param clazz
     * @return
    </A> */

    fun <A : Ability> getActiveAbilityByType(clazz: Class<A>): A? = this.getAbilitiesByType(clazz).firstOrNull { it.isActive }

    /**
     * 取得指定阶段中卡牌的可使用次数,暂时取第一个能力的使用次数
     * @param <A>
     * @param clazz
     * @return
    </A> */
    fun getUseNumByType(clazz: Class<out Ability>) = this.getActiveAbilityByType(clazz)?.maxNum ?: 0

    /**
     * 判断该卡牌是否拥有指定阶段的能力
     * @param <A>
     * @param clazz
     * @return
    </A> */
    fun hasAbility(clazz: Class<out Ability>) = this.getAbilitiesByType(clazz).isNotEmpty()

    /**
     * 判断卡牌是否拥有指定的skill
     * @param skill
     * @return
     */
    fun hasSkill(skill: Skill) = this.abilities.any { it.skill == skill }

    /**
     * 判断指定阶段中是否拥有skill
     * @param <A>
     * @param clazz
     * @param skill
     * @return
     */
    fun <A : Ability> hasSkillByType(clazz: Class<A>, skill: Skill) = this.getAbilitiesByType(clazz).any { it.skill != null && it.skill == skill }

    /**
     * 判断指定阶段中是否有主动使用的能力
     * @param <A>
     * @param clazz
     * @return
     */
    fun isAbilitiesActive(clazz: Class<out Ability>) = this.getAbilitiesByType(clazz).any(Ability::isActive)

    fun getChinese(): String = "[$name]"
}
