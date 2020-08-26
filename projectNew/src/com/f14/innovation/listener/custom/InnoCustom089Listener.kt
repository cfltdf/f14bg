package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoChooseHandListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import java.util.*

/**
 * #089-复合材料 监听器

 * @author F14eagle
 */
class InnoCustom089Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 我要求你选择并保留{num}张手牌,并将其余的手牌转移至我的手牌!
        val hands = ArrayList(player.hands.getCards())
        hands.filterNot(cards::contains).map { gameMode.game.playerRemoveHandCard(player, it) }.forEach { gameMode.game.playerAddHandCard(this.mainPlayer, it) }
        // 选择计分区最高等级的一张牌,转移到我的计分区
        if (!player.scores.isEmpty) {
            val deck = player.scores.maxLevelCardDeck
            val card = deck!!.drawRandom()!!
            val resultParam = gameMode.game.playerRemoveScoreCard(player, card)
            gameMode.game.playerAddScoreCard(this.mainPlayer, resultParam)
        }
    }

}
