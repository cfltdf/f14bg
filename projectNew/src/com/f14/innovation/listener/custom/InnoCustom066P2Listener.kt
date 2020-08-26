package com.f14.innovation.listener.custom

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoChooseSpecificCardListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import com.f14.innovation.utils.InnoUtils


/**
 * #066-公共卫生 实际执行效果的监听器(选择最低时期的1张牌给对方)

 * @author F14eagle
 */
class InnoCustom066P2Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseSpecificCardListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun cannotChooseCard(player: InnoPlayer, card: InnoCard): Boolean {
        // 只能选择等级最低的牌
        val level = InnoUtils.getMinLevel(this.specificCards.cards)
        return card.level > level || super.cannotChooseCard(player, card)
    }

    override fun canEndResponse(player: InnoPlayer): Boolean {
        // 转移1张后结束..
        return this.selectedCards.size >= 1 || super.canEndResponse(player)
    }

    override fun canPass(action: GameAction): Boolean {
        return false
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, card: InnoCard) {
        // 将手牌转移给对方
        val resultParam = gameMode.game.playerRemoveHandCard(player, card)
        gameMode.game.playerAddHandCard(this.currentPlayer, resultParam)
    }

}
