package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.listener.InnoProcessAbilityListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #105-软件 执行器

 * @author F14eagle
 */
class InnoCustom105Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 抓2张[10]融合,并执行第2张牌上面的非要求效果
        val player = this.targetPlayer
        var resultParam = gameMode.game.playerDrawCardAction(player, 10, 2, true)
        gameMode.game.playerMeldCard(player, resultParam)

        if (resultParam.cards.size == 2) {
            // 执行该牌上的效果
            val card = resultParam.cards.cards[1]
            resultParam = InnoResultParam()
            resultParam.addCard(card)
            val al = InnoProcessAbilityListener(gameMode, player, this.initParam!!, resultParam, this.ability, this.abilityGroup)
            this.commandList.insertInterrupteListener(al)
        }

    }

}
