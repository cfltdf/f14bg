package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.listener.InnoReturnAllHandListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import com.f14.innovation.utils.InnoUtils

/**
 * #051-物理学 执行器

 * @author F14eagle
 */
class InnoCustom051Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 抓3张[6]展示,其中只要有2张或更多是同一颜色,就
        // 连同手牌全部归还;否则全部加入手牌
        val player = this.targetPlayer
        val cards = gameMode.game.playerDrawCard(player, 6, 3, true)
        if (InnoUtils.hasSameColor(cards)) {
            // 创建归还所有手牌的监听器
            val al = InnoReturnAllHandListener(gameMode, player, this.initParam!!, this.resultParam, null, null)
            this.commandList.insertInterrupteListener(al)
        }
    }

}
