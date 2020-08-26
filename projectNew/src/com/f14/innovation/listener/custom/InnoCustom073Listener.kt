package com.f14.innovation.listener.custom

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoCommonConfirmListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import java.util.*

/**
 * #073-自行车 监听器

 * @author F14eagle
 */
class InnoCustom073Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoCommonConfirmListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        val handEmpty = player.hands.isEmpty
        val scoreEmpty = player.scores.isEmpty
        // 如果手牌和分数都为空,则不需要回应
        return !(handEmpty && scoreEmpty) && super.beforeListeningCheck(player)
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = this.targetPlayer
        // 交换所有手牌和计分牌
        val handEmpty = player.hands.isEmpty
        val scoreEmpty = player.scores.isEmpty
        val handCards = ArrayList(player.hands.getCards())
        val scoreCards = ArrayList(player.scores.getCards())
        // 允许空换!!
        if (!handEmpty) {
            handCards.map { gameMode.game.playerRemoveHandCard(player, it) }.forEach { gameMode.game.playerAddScoreCard(player, it) }
        }
        if (!scoreEmpty) {
            scoreCards.map { gameMode.game.playerRemoveScoreCard(player, it) }.forEach { gameMode.game.playerAddHandCard(player, it) }
        }
        this.setPlayerResponsed(player)
    }

}
