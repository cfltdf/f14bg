package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCardStack
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoSplayDirection
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */

class InnoCustom033Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 你每有一种向左展开的颜色,就抓一张[4]
        val player = this.targetPlayer
        val num = InnoColor.values().count { player.getCardStack(it)?.takeUnless(InnoCardStack::empty)?.splayDirection == InnoSplayDirection.LEFT }
        if (num > 0) {
            gameMode.game.playerDrawCard(player, 4, num)
            this.setPlayerActived(player)
        }
    }

}
