package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */

class InnoCustom029Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 我要求你转移所有有"城堡"的置顶牌到我的计分区
        val targetPlayer = this.targetPlayer
        val mainPlayer = this.mainPlayer
        targetPlayer.topCards.filter { it.containsIcons(InnoIcon.CASTLE) }.forEach {
            gameMode.game.run { playerAddScoreCard(mainPlayer, playerRemoveTopCard(targetPlayer, it.color!!)) }
        }
    }

}
