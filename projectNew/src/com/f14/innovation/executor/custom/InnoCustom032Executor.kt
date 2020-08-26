package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.listener.InnoReturnAllHandListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import com.f14.innovation.utils.InnoUtils

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */

class InnoCustom032Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 你版图中每有3个城堡,就抓一张4展示;其中只要有任意一张红色,就
        // 连同手牌全部归还;否则全部加入手牌
        val targetPlayer = this.targetPlayer
        val i = player.getIconCount(InnoIcon.CASTLE)
        val num = i / 3
        if (num > 0) {
            val cards = gameMode.game.playerDrawCard(targetPlayer, 4, num, true)
            if (InnoUtils.hasColor(cards, InnoColor.RED)) {
                // 创建归还所有手牌的监听器
                val al = InnoReturnAllHandListener(gameMode, targetPlayer, initParam!!, resultParam, ability, abilityGroup)
                this.commandList.insertInterrupteListener(al)
            }
            this.setPlayerActived(targetPlayer)
        }
    }

}
