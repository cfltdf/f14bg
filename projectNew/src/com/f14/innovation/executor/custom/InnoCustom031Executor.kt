package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */

class InnoCustom031Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 我要求你用所有手牌交换我手牌中所有最高时期的牌
        val targetPlayer = this.targetPlayer
        val mainPlayer = this.mainPlayer
        val playerHands = targetPlayer.hands.getCards().toTypedArray()
        val maxHands = mainPlayer.hands.maxLevelCards.toTypedArray()
        gameMode.game.run {
            playerHands.forEach { playerAddHandCard(mainPlayer, playerRemoveHandCard(targetPlayer, it)) }
            maxHands.forEach { playerAddHandCard(targetPlayer, playerRemoveHandCard(mainPlayer, it)) }
        }
    }

}
