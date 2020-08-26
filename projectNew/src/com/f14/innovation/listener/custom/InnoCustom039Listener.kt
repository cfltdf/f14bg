package com.f14.innovation.listener.custom

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.listener.InnoChooseHandListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #039-宗教改革 监听器

 * @author F14eagle
 */
class InnoCustom039Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    init {
        this.calculateNumValue()
    }

    /**
     * 计算实际允许选择的手牌数量
     */
    private fun calculateNumValue() {
        // 你版图中每有2个叶子,你就可以选择一张手牌垫底!
        val i = this.targetPlayer.getIconCount(InnoIcon.LEAF)
        this.initParam!!.maxNum = i / 2
    }

}
