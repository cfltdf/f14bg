package com.f14.innovation.listener

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 选择牌堆展开的监听器

 * @author F14eagle
 */
class InnoChooseSplayListener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseStackListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun canChooseCard(player: InnoPlayer, card: InnoCard): Boolean {
        // 检查玩家是否可以展开该牌堆
        return player.canSplayStack(card.color!!, this.initParam!!.splayDirection) && super.canChooseCard(player, card)
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 将选中的牌堆按照指定的方向展开
        val color = cards[0].color
        gameMode.game.playerSplayStack(player, color!!, this.initParam!!.splayDirection)
    }

}
