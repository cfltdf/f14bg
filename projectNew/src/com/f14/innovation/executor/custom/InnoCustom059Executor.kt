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
 * #059-工业化 执行器

 * @author F14eagle
 */
class InnoCustom059Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 每有2个工厂符号,就抓一张[6]垫底
        val player = this.targetPlayer
        val num = player.getIconCount(InnoIcon.FACTORY) / 2
        if (num > 0) {
            gameMode.game.playerDrawAndTuckCard(player, 6, num)
            this.setPlayerActived(player)
        }
    }

}
