package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoConsts
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */

class InnoCustom020Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val targetPlayer = this.targetPlayer
        // 如果玩家是场上唯一拥有5种颜色置顶牌的玩家,则得到特殊成就,帝国
        if (gameMode.game.players.singleOrNull(InnoPlayer::hasAllColorStack) === targetPlayer) {
            val ac = gameMode.achieveManager.specialAchieveCards.getCardByIndex(InnoConsts.SPECIAL_ACHIEVE_EMPIRE)
                    ?: return
            gameMode.game.playerAddSpecialAchieveCard(targetPlayer, ac)
            this.setPlayerActived(targetPlayer)
        }
    }

}