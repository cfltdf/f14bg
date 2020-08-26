package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #048-测量 执行器

 * @author F14eagle
 */
class InnoCustom048Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 抓一张X时期的牌,X等于该颜色所展开的卡牌数量!
        val player = this.targetPlayer
        if (this.resultCards.isNotEmpty()) {
            val stack = player.getCardStack(this.resultCards[0].color!!)
            val num = stack!!.size
            if (num > 0) {
                gameMode.game.playerDrawCard(player, num, 1)
            }
        }
    }

}
