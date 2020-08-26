package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoChooseStackListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #016-一神论 监听器

 * @author F14eagle
 */
class InnoCustom016Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseStackListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun canChooseCard(player: InnoPlayer, card: InnoCard): Boolean {
        // 只能选择触发玩家版图中没有该颜色的置顶牌
        return !this.mainPlayer.hasCardStack(card.color!!) && super.canChooseCard(player, card)
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 将该牌转移到触发玩家的计分区中
        cards.map { gameMode.game.playerRemoveTopCard(player, it.color!!) }.forEach { gameMode.game.playerAddScoreCard(this.mainPlayer, it) }
    }

}
