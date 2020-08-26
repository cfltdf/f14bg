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

/**
 * #015-驯养 监听器

 * @author F14eagle
 */
class InnoCustom015Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun checkChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.checkChooseCard(player, cards)
        // 只能选择手牌中最低时期的牌
        for (card in cards)
            if (player.hands.getCards().any { card !== it && card.level > it.level })
                throw BoardGameException("你只能选择最低时期的手牌!")
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 融合选择的手牌
        for (card in cards) {
            gameMode.game.playerMeldHandCard(player, card)
        }
    }

}
