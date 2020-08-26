package com.f14.innovation.executor.custom

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.executor.InnoVictoryExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import com.f14.innovation.utils.InnoUtils

/**
 * #103-生物工程 判断胜利条件的执行器

 * @author F14eagle
 */
class InnoCustom103VictoryExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoVictoryExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    override // 如果有玩家的叶子少于3个,则拥有最多叶子的玩家获胜
    // 则拥有最多叶子的玩家独赢
    val victoryPlayer: InnoPlayer?
        get() {
            if (this.isConditionTrue) {
                val players = InnoUtils.getMostIconPlayers(gameMode.game.players, InnoIcon.LEAF)
                if (players.size == 1) {
                    return players[0]
                }
            }
            return null
        }

    /**
     * 判断是否有玩家的叶子少于3个

     * @return
     */
    private val isConditionTrue: Boolean
        get() {
            return gameMode.game.players.any { it.getIconCount(InnoIcon.LEAF) < 3 }
        }

}
