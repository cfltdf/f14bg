package com.f14.innovation.executor

import com.f14.bg.anim.AnimVar
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoAnimPosition
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 移除手牌的行动

 * @author F14eagle
 */
class InnoRemoveHandExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.targetPlayer
        for (card in this.resultCards) {
            // 入参中的牌从手牌中移除
            player.hands.removeCard(card)
            // 设置from参数
            val from = AnimVar.createAnimVar(InnoAnimPosition.PLAYER_HANDS, player.position, card.level)
            this.resultParam.putAnimVar(card, from)
            // 发送移除手牌的信息
            this.game.sendPlayerRemoveHandResponse(player, card, null)
            this.game.report.playerRemoveHand(player, card.level)
        }
    }

}
