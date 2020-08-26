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
import com.f14.innovation.utils.InnoUtils

/**
 * #070-炸药 监听器

 * @author F14eagle
 */
class InnoCustom070Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun cannotChooseCard(player: InnoPlayer, cards: List<InnoCard>): Boolean {
        // 必须选择最高时期的三张牌
        val max = InnoUtils.getMaxLevel(player.hands.getCards(), 3)
        return if (cards.any { it.level < max }) true else super.cannotChooseCard(player, cards)
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 我要求你转移手牌中时期最高的{num}张牌至我的手牌,如果转移后你没有手牌,则抓一张[7]!
        cards.map { gameMode.game.playerRemoveHandCard(player, it) }.forEach { gameMode.game.playerAddHandCard(this.mainPlayer, it) }
        if (player.hands.isEmpty) {
            gameMode.game.playerDrawCard(player, 7, 1)
        }
    }

}
