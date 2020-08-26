package com.f14.innovation.listener.custom

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.listener.InnoChooseSpecificCardListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import com.f14.innovation.utils.InnoUtils
import java.util.*

/**
 * #082-社会主义 监听器(垫底所有手牌,拿取其他玩家的手牌)

 * @author F14eagle
 */
class InnoCustom082P1Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseSpecificCardListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun canPass(action: GameAction): Boolean {
        return false
    }

    @Throws(BoardGameException::class)
    override fun onProcessChooseCardOver(player: InnoPlayer) {
        super.onProcessChooseCardOver(player)
        // 如果选择牌中有紫色的牌,则拿取所有其他玩家手牌中最低时期的牌
        if (InnoUtils.hasColor(this.selectedCards.cards, InnoColor.PURPLE)) {
            for (p in gameMode.game.players) {
                if (p !== player && !p.hands.isEmpty) {
                    val deck = p.hands.minLevelCardDeck
                    val cards = ArrayList(deck!!.cards)
                    cards.map { gameMode.game.playerRemoveHandCard(p, it) }.forEach { gameMode.game.playerAddHandCard(player, it) }
                }
            }
        }
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, card: InnoCard) {
        super.processChooseCard(player, card)
        val resultParam = gameMode.game.playerRemoveHandCard(player, card)
        gameMode.game.playerTuckCard(player, resultParam, true)
    }

}
