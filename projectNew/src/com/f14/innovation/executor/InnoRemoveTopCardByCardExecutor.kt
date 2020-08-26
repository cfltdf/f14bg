package com.f14.innovation.executor

import com.f14.bg.anim.AnimVar
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoAchieveTrigType
import com.f14.innovation.consts.InnoAnimPosition
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 按照指定的牌移除置顶牌的行动

 * @author F14eagle
 */
class InnoRemoveTopCardByCardExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.targetPlayer
        for (c in this.resultCards) {
            val card = player.removeTopCard(c.color!!)!!
            // 设置from参数
            val from = AnimVar.createAnimVar(InnoAnimPosition.PLAYER_STACKS, player.position, card.color!!)
            this.resultParam.putAnimVar(card, from)
            // 刷新该牌堆的信息
            this.game.sendPlayerCardStackResponse(player, card.color!!, null)
            this.game.report.playerRemoveStackCard(player, card)

            gameMode.executeAchieveChecker(InnoAchieveTrigType.STACK_CHANGE, player)
        }
        this.game.sendPlayerIconsInfoResponse(player, null)
    }

}
