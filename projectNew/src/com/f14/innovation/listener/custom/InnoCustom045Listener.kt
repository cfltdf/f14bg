package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.listener.InnoChooseHandListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #045-透视法 监听器

 * @author F14eagle
 */
class InnoCustom045Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    init {
        this.calculateNumValue()
    }

    /**
     * 计算实际允许选择的手牌数量
     */
    private fun calculateNumValue() {
        // 每有2个灯泡就能选1张手牌
        val i = this.targetPlayer.getIconCount(InnoIcon.LAMP)
        this.initParam!!.maxNum = i / 2
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 将选择的牌计分
        cards
                // 需要检查成就
                .map { gameMode.game.playerRemoveHandCard(player, it) }.forEach { gameMode.game.playerAddScoreCard(player, it, true) }
    }

}
