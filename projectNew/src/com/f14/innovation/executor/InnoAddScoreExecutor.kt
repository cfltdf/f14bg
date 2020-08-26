package com.f14.innovation.executor

import com.f14.bg.anim.AnimType
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
 * 加入分数的行动

 * @author F14eagle
 */
class InnoAddScoreExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.targetPlayer
        for (card in this.resultCards) {
            // 将入参中的牌加入分数
            player.addScore(card)
            val from = this.resultParam.getAnimVar(card)
            this.game.sendAnimationResponse(InnoAnimParamFactory.createAddScoreParam(player, card, from, this.resultParam.animType!!))
            this.game.sendPlayerAddScoreResponse(player, card, null)
            if (this.resultParam.animType === AnimType.REVEAL) {
                this.game.report.playerAddScore(player, card)
            } else {
                this.game.report.playerAddScore(player, card.level)
            }
        }
        if (this.initParam != null && this.initParam.isCheckAchieve) {
            // 如果要检查成就,则添加玩家的回合计分牌数
            player.addRoundScoreCount(this.resultCards.size)
            gameMode.achieveManager.executeAchieveChecker(InnoAchieveTrigType.SCORE, player)
        }
    }

}
