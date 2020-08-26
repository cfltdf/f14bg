package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoConsts
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */

class InnoCustom035Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val targetPlayer = this.targetPlayer
        // 如果你版图中每张置顶牌都包含"皇冠",则获得特殊成就"世界"
        if (targetPlayer.topCards.run { isNotEmpty() && all { it.containsIcons(InnoIcon.CROWN) } }) {
            gameMode.achieveManager.specialAchieveCards.getCardByIndex(InnoConsts.SPECIAL_ACHIEVE_WORLD)?.let {
                gameMode.game.playerAddSpecialAchieveCard(player, it)
                this.setPlayerActived(player)
            }
        }
    }
}