package com.f14.innovation.executor.custom

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoVictoryExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import com.f14.innovation.utils.InnoUtils

/**
 * #097-人工智能 判断胜利条件的执行器

 * @author F14eagle
 */
class InnoCustom097VictoryExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoVictoryExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    override // 如果机器人学和软件是置顶牌
    // 则最低分的玩家独赢
    val victoryPlayer: InnoPlayer?
        get() {
            if (this.isTopCard(105) && this.isTopCard(102)) {
                val players = InnoUtils.getLowestScorePlayers(gameMode.game.players)
                if (players.size == 1) {
                    return players[0]
                }
            }
            return null
        }

    /**
     * 检查指定的牌是否是置顶牌

     * @param cardIndex

     * @return
     */
    private fun isTopCard(cardIndex: Int): Boolean {
        return gameMode.game.players.flatMap(InnoPlayer::topCards).any { it.cardIndex == cardIndex }
    }

}
