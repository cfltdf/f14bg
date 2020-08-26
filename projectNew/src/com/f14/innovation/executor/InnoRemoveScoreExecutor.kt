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
 * 移除分数的行动

 * @author F14eagle
 */
class InnoRemoveScoreExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.targetPlayer
        for (card in this.resultCards) {
            // 入参中的牌从分数中移除
            player.scores.removeCard(card)
            // 设置from参数
            val from = AnimVar.createAnimVar(InnoAnimPosition.PLAYER_SCORES, player.position, card.level)
            this.resultParam.putAnimVar(card, from)
            // 发送玩家分数的信息
            this.game.sendPlayerRemoveScoreResponse(player, card, null)
            this.game.report.playerRemoveScore(player, card.level)
        }
    }

}
