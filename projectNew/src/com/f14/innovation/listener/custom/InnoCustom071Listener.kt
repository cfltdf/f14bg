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
 * #071-照明 监听器

 * @author F14eagle
 */
class InnoCustom071Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 垫底了X张不同时期的牌,就摸X张[7]计分!
        cards.map { gameMode.game.playerRemoveHandCard(player, it) }.forEach { gameMode.game.playerTuckCard(player, it, true) }
        gameMode.game.playerDrawAndScoreCard(player, 7, InnoUtils.getDifferentLevelCardsNum(cards))
    }

}
