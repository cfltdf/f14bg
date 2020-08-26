package com.f14.innovation.executor

import com.f14.bg.anim.AnimType
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.anim.InnoAnimParamFactory
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 加入手牌的行动

 * @author F14eagle
 */
class InnoAddHandExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.targetPlayer
        for (card in this.resultCards) {
            // 将入参中的牌加入手牌
            player.addHand(card)
            val from = this.resultParam.getAnimVar(card)
            this.game.sendAnimationResponse(InnoAnimParamFactory.createDrawCardParam(player, card, from, this.resultParam.animType!!))
            this.game.sendPlayerAddHandResponse(player, card, null)
            if (this.resultParam.animType === AnimType.REVEAL) {
                this.game.report.playerAddHand(player, card)
            } else {
                this.game.report.playerAddHand(player, card.level)
            }
        }
    }

}
