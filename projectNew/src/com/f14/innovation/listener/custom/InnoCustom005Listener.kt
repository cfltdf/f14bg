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
 * #005-弓箭 监听器

 * @author F14eagle
 */
class InnoCustom005Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun canChooseCard(player: InnoPlayer, card: InnoCard): Boolean {
        if (!super.canChooseCard(player, card)) {
            return false
        }
        // 只能选择最高等级的手牌
        return player.isHighestLevelInHand(card)
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 将牌转移到触发玩家的手牌中
        cards.map { gameMode.game.playerRemoveHandCard(player, it) }.forEach { gameMode.game.playerAddHandCard(this.mainPlayer, it) }
    }

}
