package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoChooseStackListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoParamFactory
import com.f14.innovation.param.InnoResultParam

/**
 * #069-出版 监听器

 * @author F14eagle
 */
class InnoCustom069Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseStackListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun canChooseCard(player: InnoPlayer, card: InnoCard): Boolean {
        // 选择的牌堆必须多余1张牌
        val stack = player.getCardStack(card.color!!)
        return stack!!.size > 1 && super.canChooseCard(player, card)
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 创建一个实际执行效果的监听器
        val initParam = InnoParamFactory.createInitParam()
        initParam.color = cards[0].color
        initParam.isCanPass = true
        initParam.msg = this.initParam!!.msg
        next = InnoCustom069P1Listener(gameMode, player, initParam, this.resultParam, this.ability, this.abilityGroup)
    }

}
