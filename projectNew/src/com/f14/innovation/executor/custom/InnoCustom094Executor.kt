package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import java.util.*

/**
 * #094-遗传学 执行器

 * @author F14eagle
 */
class InnoCustom094Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 抓1张[10]融合,然后将该牌下面的所有牌计分
        val player = this.targetPlayer
        var resultParam = gameMode.game.playerDrawCardAction(player, 10, 1, true)
        gameMode.game.playerMeldCard(player, resultParam)
        if (!resultParam.cards.empty) {
            val color = resultParam.cards.cards[0].color!!
            val stack = player.getCardStack(color)
            val cards = ArrayList(stack!!.cards)
            // 跳过第一张牌
            for (i in 1 until cards.size) {
                val card = cards[i]
                resultParam = gameMode.game.playerRemoveStackCard(player, card)
                gameMode.game.playerAddScoreCard(player, resultParam, true)
            }
        }
    }

}
