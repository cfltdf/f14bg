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
 * #098-全球化 判断胜利条件的执行器

 * @author F14eagle
 */
class InnoCustom098VictoryExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoVictoryExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    override // 如果没有玩家的叶子比工厂多
    // 则最高分的玩家独赢
    val victoryPlayer: InnoPlayer?
        get() {
            if (gameMode.game.players.any { it.getIconCount(InnoIcon.LEAF) > it.getIconCount(InnoIcon.FACTORY) }) return null
            return InnoUtils.getHighestScorePlayers(gameMode.game.players).singleOrNull()
        }

}
