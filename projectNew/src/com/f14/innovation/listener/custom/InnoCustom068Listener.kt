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
 * #068-冷藏 监听器

 * @author F14eagle
 */
class InnoCustom068Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    init {
        this.calculateNumValue()
    }

    /**
     * 计算实际允许选择的手牌数量
     */
    private fun calculateNumValue() {
        // 必须归还一半的手牌,向下取整
        val i = this.targetPlayer.hands.size()
        this.initParam!!.num = i / 2
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 将选择的牌归还
        cards.map { gameMode.game.playerRemoveHandCard(player, it) }.forEach { gameMode.game.playerReturnCard(player, it) }
    }

}
