package com.f14.innovation.executor

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.anim.InnoAnimParamFactory
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 退回牌的行动

 * @author F14eagle
 */
class InnoReturnCardExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        for (card in this.resultCards) {
            // 将入参中的牌归还到牌堆
            gameMode.drawDecks.addCard(card)
            val from = this.resultParam.getAnimVar(card)
            this.game.sendAnimationResponse(InnoAnimParamFactory.createReturnCardParam(card, from, this.resultParam.animType!!))
            this.game.sendDrawDeckInfo(null)
            this.game.report.playerReturnCard(player, card.level)
        }
    }

}
