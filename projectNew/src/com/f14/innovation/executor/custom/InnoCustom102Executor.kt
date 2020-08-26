package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.listener.InnoProcessAbilityListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #102-机器人学 执行器

 * @author F14eagle
 */
class InnoCustom102Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 拿绿色置顶牌计分,抓一张[10]融合,并执行上面的非要求效果
        val player = this.targetPlayer
        val card = player.getTopCard(InnoColor.GREEN)
        if (card != null) {
            val resultParam = gameMode.game.playerRemoveTopCard(player, InnoColor.GREEN)
            gameMode.game.playerAddScoreCard(player, resultParam, true)
        }

        val resultParam = gameMode.game.playerDrawCardAction(player, 10, 1, true)
        gameMode.game.playerMeldCard(player, resultParam)

        // 执行该牌上的效果
        val al = InnoProcessAbilityListener(gameMode, player, this.initParam!!, resultParam, this.ability, this.abilityGroup)
        this.commandList.insertInterrupteListener(al)
    }

}
