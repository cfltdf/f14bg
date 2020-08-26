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
 * #078-摩天大楼 执行器

 * @author F14eagle
 */
class InnoCustom078Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 若有任何卡牌因此法而转移,则该玩家将被转移卡牌下面的一张
        // 牌计分,并将版图中该颜色所有剩余的卡牌归还
        val player = this.targetPlayer
        if (this.resultCards.isNotEmpty()) {
            val color = this.resultCards[0].color!!
            var card = player.getTopCard(color)
            if (card != null) {
                val resultParam = gameMode.game.playerRemoveTopCard(player, color)
                gameMode.game.playerAddScoreCard(player, resultParam, true)
            }
            card = player.getTopCard(color)
            while (card != null) {
                val resultParam = gameMode.game.playerRemoveTopCard(player, color)
                gameMode.game.playerReturnCard(player, resultParam)
                card = player.getTopCard(color)
            }
        }
    }

}
