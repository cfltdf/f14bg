package com.f14.innovation.executor

import com.f14.bg.anim.AnimType
import com.f14.bg.anim.AnimVar
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.anim.InnoAnimParamFactory
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoAnimPosition
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 展示手牌的行动

 * @author F14eagle
 */
class InnoRevealHandExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.targetPlayer
        for (card in this.resultCards) {
            // 发送一个展示的动画效果...
            val from = AnimVar.createAnimVar(InnoAnimPosition.PLAYER_HANDS, player.position, card.id)
            this.game.sendAnimationResponse(InnoAnimParamFactory.createDrawCardParam(player, card, from, AnimType.REVEAL))
            this.game.report.playerRevealHand(player, card)
        }
    }

}
