package com.f14.innovation.executor.custom

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.executor.InnoVictoryExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #088-协同合作 判断胜利条件的执行器

 * @author F14eagle
 */
class InnoCustom088VictoryExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoVictoryExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    override // 如果拥有10张或以上的绿色牌,就获得胜利
    val victoryPlayer: InnoPlayer?
        get() {
            val player = this.targetPlayer
            val stack = player.getCardStack(InnoColor.GREEN)
            if (stack != null && stack.size >= 10) {
                return player
            }
            return null
        }

}
