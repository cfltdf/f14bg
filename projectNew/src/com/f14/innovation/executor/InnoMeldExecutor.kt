package com.f14.innovation.executor

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.anim.InnoAnimParamFactory
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoAchieveTrigType
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 合并的行动

 * @author F14eagle
 */
class InnoMeldExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.targetPlayer
        for (card in this.resultCards) {
            player.meld(card)
            val from = this.resultParam.getAnimVar(card)
            this.game.sendAnimationResponse(InnoAnimParamFactory.createMeldCardParam(player, card, from))
            this.game.sendPlayerCardStackResponse(player, card.color!!, null)
            this.game.sendPlayerIconsInfoResponse(player, null)
            this.game.report.playerMeld(player, card)

            gameMode.executeAchieveChecker(InnoAchieveTrigType.STACK_CHANGE, player)
        }
    }

}
