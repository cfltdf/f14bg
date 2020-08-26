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
 * #050-煤 执行器

 * @author F14eagle
 */
class InnoCustom050Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 你可以选择你的1张置顶牌计分.若你如此做,再将该牌下面的一张牌计分!
        val player = this.targetPlayer
        if (this.resultCards.isNotEmpty()) {
            val card = player.getTopCard(this.resultCards[0].color!!)
            if (card != null) {
                val resultParam = gameMode.game.playerRemoveTopCard(player, card.color!!)
                gameMode.game.playerAddScoreCard(player, resultParam)
            }
        }
    }

}
