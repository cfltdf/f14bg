package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #054-蒸汽机 执行器

 * @author F14eagle
 */
class InnoCustom054Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 将黄色置底牌计分
        val player = this.targetPlayer
        val card = player.getBottomCard(InnoColor.YELLOW)
        if (card != null) {
            val resultParam = gameMode.game.playerRemoveStackCard(player, card)
            // 需要检查成就
            gameMode.game.playerAddScoreCard(player, resultParam, true)
        }
    }

}
