package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoChooseScoreListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #053-统计学 监听器

 * @author F14eagle
 */
class InnoCustom053Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseScoreListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun afterProcessChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.afterProcessChooseCard(player, cards)
        if (player.hands.size() == 1) {
            // 如果执行后玩家手牌只有1张,则再执行一次
            next = InnoCustom053Listener(gameMode, player, this.initParam, this.resultParam, this.ability, this.abilityGroup)
        }
    }

    override fun canChooseCard(player: InnoPlayer, card: InnoCard): Boolean {
        // 只能选择等级最高的牌
        val maxLevel = player.scores.maxLevel
        return card.level >= maxLevel && super.canChooseCard(player, card)
    }

}
