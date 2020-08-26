package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.listener.InnoChoosePlayerListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam


/**
 * #025-造路 监听器

 * @author F14eagle
 */
class InnoCustom025Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChoosePlayerListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        // 如果自己没有红色置顶牌,则跳过执行
        return player.hasCardStack(InnoColor.RED) && super.beforeListeningCheck(player)
    }

    @Throws(BoardGameException::class)
    override fun processChoosePlayer(player: InnoPlayer, choosePlayer: InnoPlayer) {
        // 将自己的红色置顶牌转移到目标玩家的版图
        // 将目标玩家的绿色置顶牌转移到自己的版图
        if (player.hasCardStack(InnoColor.RED)) {
            run {
                val resultParam = gameMode.game.playerRemoveTopCard(player, InnoColor.RED)
                gameMode.game.playerMeldCard(choosePlayer, resultParam)
            }

            if (choosePlayer.hasCardStack(InnoColor.GREEN)) {
                val resultParam = gameMode.game.playerRemoveTopCard(choosePlayer, InnoColor.GREEN)
                gameMode.game.playerMeldCard(player, resultParam)
            }
        }
    }

}
