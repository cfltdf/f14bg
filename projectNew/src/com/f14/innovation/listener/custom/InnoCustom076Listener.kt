package com.f14.innovation.listener.custom

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.listener.InnoInterruptListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import com.f14.innovation.utils.InnoUtils
import java.util.*

/**
 * #076-大众传媒 监听器

 * @author F14eagle
 */
class InnoCustom076Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoInterruptListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val level = action.getAsInt("level")
        InnoUtils.checkLevel(level)
        // 归还所有玩家计分区中指定时期的牌
        for (p in gameMode.game.players) {
            val deck = p.scores.getCardDeck(level)
            if (!deck.empty) {
                val cards = ArrayList(deck.cards)
                cards.map { gameMode.game.playerRemoveScoreCard(p, it) }.forEach { gameMode.game.playerReturnCard(player, it) }
            }
        }
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_076

}
