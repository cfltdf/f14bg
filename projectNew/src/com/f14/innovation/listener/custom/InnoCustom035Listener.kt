package com.f14.innovation.listener.custom

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoCommonConfirmListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam


/**
 * #035-翻译 监听器

 * @author F14eagle
 */
class InnoCustom035Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoCommonConfirmListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = this.targetPlayer
        if (player.scores.isEmpty) {
            throw BoardGameException("你的计分区没有牌,不能融合!")
        }
        // 创建一个实际执行效果的监听器
        val al = InnoCustom035EffectListener(gameMode, this.targetPlayer, this.initParam, this.resultParam, this.ability, this.abilityGroup)
        // 将玩家的计分区的牌设为待选牌
        al.specificCards.addCards(player.scores.getCards())
        next = al
        this.setPlayerResponsed(player)
    }

}
