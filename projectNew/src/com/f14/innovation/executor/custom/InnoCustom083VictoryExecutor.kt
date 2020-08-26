package com.f14.innovation.executor.custom

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.executor.InnoVictoryExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #083-经验主义 判断胜利条件的执行器

 * @author F14eagle
 */
class InnoCustom083VictoryExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoVictoryExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    override // 如果版图中有20个以上的"灯泡"标志,就获得胜利
    val victoryPlayer: InnoPlayer?
        get() {
            val player = this.targetPlayer
            if (player.getIconCount(InnoIcon.LAMP) >= 20) {
                return player
            }
            return null
        }

}
