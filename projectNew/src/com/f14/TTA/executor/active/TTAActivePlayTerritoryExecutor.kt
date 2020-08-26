package com.f14.TTA.executor.active

import com.f14.TTA.component.card.EventCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.executor.event.TTAInstantAbilityExecutor
import com.f14.TTA.listener.active.ActivePlayEventListener
import com.f14.bg.exception.BoardGameException

/**
 * 直接获得殖民地的处理器
 * @author 吹风奈奈
 */
class TTAActivePlayTerritoryExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val l = ActivePlayEventListener(param, card)
        l.addListeningPlayer(param.player)
        val res = gameMode.insertListener(l)
        val cardId = res.getString("cardId")
        if (cardId.isNotEmpty()) {
            val card = player.militaryHands.getCard(cardId)
            // 玩家打出殖民地
            gameMode.game.playerRemoveHand(player, card)
            gameMode.game.playerAddCardDirect(player, card)
            gameMode.report.playerAddCardCache(player, card)

            // 处理殖民地的INSTANT类型的事件能力
            for (ability in (card as EventCard).eventAbilities) {
                val executor = TTAInstantAbilityExecutor(param, ability)
                executor.trigPlayer = player
                executor.execute()
            }
            this.actived = true
        }
    }

}
