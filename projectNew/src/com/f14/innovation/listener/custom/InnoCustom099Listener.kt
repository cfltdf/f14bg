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
 * #099-干细胞 监听器

 * @author F14eagle
 */
class InnoCustom099Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoCommonConfirmListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        val handEmpty = player.hands.isEmpty
        // 如果手牌为空,则不需要回应
        return !handEmpty && super.beforeListeningCheck(player)
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = this.targetPlayer
        // 所有手牌计分
        val handCards = ArrayList(player.hands.getCards())
        handCards.map { gameMode.game.playerRemoveHandCard(player, it) }.forEach { gameMode.game.playerAddScoreCard(player, it, true) }
        this.setPlayerResponsed(player)
    }

}
