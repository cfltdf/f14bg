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
 * #086-专业化 执行器

 * @author F14eagle
 */
class InnoCustom086Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 拿取所有其他玩家版图中该颜色的置顶牌到手牌
        val player = this.targetPlayer
        if (this.resultCards.isNotEmpty()) {
            // 只要取第一张牌的颜色即可
            val color = this.resultCards[0].color!!
            for (p in gameMode.game.players) {
                if (p !== player && !gameMode.game.isTeammates(p, player)) {
                    val card = p.getTopCard(color)
                    if (card != null) {
                        val resultParam = gameMode.game.playerRemoveTopCard(p, color)
                        gameMode.game.playerAddHandCard(player, resultParam)
                    }
                }
            }
        }
    }

}
