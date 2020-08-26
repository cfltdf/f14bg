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
 * 展开的行动

 * @author F14eagle
 */
class InnoSplayExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.targetPlayer
        player.splay(this.initParam!!.color!!, this.initParam.splayDirection)
        this.game.sendAnimationResponse(InnoAnimParamFactory.createSplayCardParam(player, this.initParam.color!!))
        this.game.sendPlayerCardStackResponse(player, this.initParam.color!!, null)
        this.game.sendPlayerIconsInfoResponse(player, null)
        this.game.report.playerSplay(player, this.initParam.color!!, this.initParam.splayDirection)

        gameMode.executeAchieveChecker(InnoAchieveTrigType.STACK_CHANGE, player)
        gameMode.executeAchieveChecker(InnoAchieveTrigType.SPLAY_DIRECTION_CHANGE, player)
    }

}
