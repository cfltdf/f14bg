package com.f14.innovation.component

import com.f14.bg.component.Card
import com.f14.bg.consts.ConditionResult
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.component.ability.InnoAchieveAbility
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.consts.InnoSplayDirection
import java.util.*
import kotlin.collections.HashMap

class InnoCard : Card() {
    /**
     * 等级
     */
    var level: Int = 0
    /**
     * 颜色
     */
    var color: InnoColor? = null
    /**
     * 卡牌的英文名,暂时只用来作为起始玩家排序用
     */
    var englishName: String? = null
    /**
     * 附带能力的主要符号
     */
    var mainIcon: InnoIcon? = null
    var special: Boolean = false
    /**
     * 所有拥有的符号,长度为4
     */
    var icons: Array<InnoIcon> = emptyArray()

    val containIcons by lazy { this.icons.distinct() }

    /**
     * 各种展开方式的符号容器
     */
    val splayIcons by lazy {
        mapOf(InnoSplayDirection.LEFT to icons.asSequence().drop(3), InnoSplayDirection.RIGHT to icons.asSequence().take(2), InnoSplayDirection.UP to icons.asSequence().drop(1))
    }

    var abilityGroups: MutableList<InnoAbilityGroup> = ArrayList()

    /**
     * 执行要求类指令后可能需要执行的方法
     */
    var dogmaResultAbilities: MutableMap<ConditionResult, InnoAbilityGroup> = HashMap()

    /**
     * 成就牌的效果
     */
    var achieveAbility: InnoAchieveAbility? = null

    /**
     * 检查是否包含指定的符号
     * @param icons
     * @return
     */
    fun containsIcons(vararg icons: InnoIcon) = icons.any(this.containIcons::contains)


    fun getDogmaResultAbilitiyGroup(conditionResult: ConditionResult) = this.dogmaResultAbilities[conditionResult]

    /**
     * 取得展开方式后的符号
     * @param splayDirection
     * @return
     */
    fun getSplayIcons(splayDirection: InnoSplayDirection) = this.splayIcons[splayDirection]

    fun topIcons() = this.icons.asSequence()

}
