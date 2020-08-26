package com.f14.TTA.manager

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.CardAbility
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.CivilizationProperty
import java.util.*

/**
 * 玩家临时资源管理对象

 * @author F14eagle
 */
class TTATemplateResourceManager(private var player: TTAPlayer) {
    /**
     * 临时资源
     */
    private var templateResources: MutableMap<CardAbility, TTAProperty> = HashMap()
    /**
     * 能力给予的临时资源
     */
    private var alternateTemplateResources: MutableMap<CivilCardAbility, TTAProperty> = HashMap()

    /**
     * 添加玩家的回合临时资源
     * @param ability
     */
    fun addAlternateTemplateResource(ability: CivilCardAbility) {
        val restResource = TTAProperty()
        restResource.addProperties(ability.property)
        this.alternateTemplateResources[ability] = restResource
    }

    /**
     * 添加玩家的回合临时资源
     * @param ability
     */
    fun addTemplateResource(ability: CardAbility) {
        val restResource = TTAProperty()
        if (ability is CivilCardAbility) {
            if (ability.byProperty != null) {
                if (player.testRank(ability.byProperty!!, true, 2)) {
                    restResource.addProperties(ability.property, ability.amount)
                    this.templateResources[ability] = restResource
                    return
                }
            }
        }
        restResource.addProperties(ability.property)
        this.templateResources[ability] = restResource
    }

    fun clear() {
        this.templateResources.clear()
        this.alternateTemplateResources.clear()
    }

    /**
     * 取得临时资源剩余数量
     * @param ability
     * @return
     */
    fun getAlternateTempRes(ability: CivilCardAbility): TTAProperty {
        return this.alternateTemplateResources[ability]!!
    }

    /**
     * 取得所有临时资源能力的集合
     * @return
     */
    val alternateTempResAbility: Collection<CivilCardAbility>
        get() = this.alternateTemplateResources.keys

    /**
     * 取得临时资源剩余数量
     * @param ability
     * @return
     */
    fun getTempRes(ability: CardAbility): TTAProperty? {
        return this.templateResources[ability]
    }

    /**
     * 取得所有临时资源能力的集合
     * @return
     */
    val tempResAbility: Collection<CardAbility>
        get() = this.templateResources.keys

    /**
     * 添加玩家的回合临时资源
     * @param ability
     */
    fun removeAlternateTemplateResource(ability: CardAbility) {
        this.alternateTemplateResources.remove(ability)
    }

    /**
     * 移除玩家的回合临时资源
     * @param ability
     */
    fun removeTemplateResource(ability: CardAbility) {
        this.templateResources.remove(ability)
    }

    /**
     * 重置玩家的回合临时资源
     */
    fun resetTemplateResource() {
        this.clear()
        player.abilityManager.getAbilitiesByType(CivilAbilityType.PA_TEMPLATE_RESOURCE).forEach(this::addTemplateResource)
        player.abilityManager.allFlaggedAbilities.forEach(this::addAlternateTemplateResource)
        player.abilityManager.getAbility(CivilAbilityType.PA_BONDICA)?.let { ability ->
            if (player.testRank(CivilizationProperty.MILITARY, true, 2)) {
                player.params.setRoundParameter(CivilAbilityType.PA_BONDICA, true)
                this.addTemplateResource(ability)
            }
        }
    }

    /**
     * 使用玩家的回合临时资源
     * @param ability
     * @param usedResources
     */
    fun useAlternateTemplateResource(ability: CardAbility, usedResources: TTAProperty): TTAProperty {
        val res = this.alternateTemplateResources.remove(ability)
        res?.removeProperties(usedResources)
        return usedResources
    }

    /**
     * 使用玩家的回合临时资源
     * @param ability
     * @param usedResources
     */
    fun useTemplateResource(ability: CardAbility, usedResources: TTAProperty): TTAProperty {
        val res = this.templateResources[ability]
        res?.removeProperties(usedResources)
        return usedResources
    }
}
