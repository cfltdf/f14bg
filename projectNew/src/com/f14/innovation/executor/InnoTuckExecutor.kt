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
 * 追加的行动

 * @author F14eagle
 */
class InnoTuckExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.targetPlayer
        for (card in this.resultCards) {
            player.tuck(card)
            val from = this.resultParam.getAnimVar(card)
            this.game.sendAnimationResponse(InnoAnimParamFactory.createMeldCardParam(player, card, from))
            this.game.sendPlayerCardStackResponse(player, card.color!!, null)
            this.game.sendPlayerIconsInfoResponse(player, null)
            this.game.report.playerTuck(player, card)

            gameMode.executeAchieveChecker(InnoAchieveTrigType.STACK_CHANGE, player)
        }
        if (this.initParam != null && this.initParam.isCheckAchieve) {
            // 如果要检查成就,则添加玩家的回合垫底牌数
            player.addRoundTuckCount(this.resultCards.size)
            gameMode.achieveManager.executeAchieveChecker(InnoAchieveTrigType.TUCK, player)
        }
    }

}
