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
 * #077-抗生素 监听器

 * @author F14eagle
 */
class InnoCustom077Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 你可以选择并归还至多{maxNum}张手牌,你归还
        // 的手牌中每有一种不同的时期,便抓两张[8]!
        for (card in cards) {
            gameMode.game.playerReturnCard(player, card)
        }
        val num = InnoUtils.getDifferentLevelCardsNum(cards)
        gameMode.game.playerDrawCard(player, 8, num * 2)
    }

}
