package com.f14.innovation.listener.custom

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoChooseSpecificCardListener
import com.f14.innovation.listener.InnoProcessAbilityListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #104-自助服务 监听器

 * @author F14eagle
 */
class InnoCustom104Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseSpecificCardListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun canEndResponse(player: InnoPlayer): Boolean {
        // 转移1张后结束..
        return this.selectedCards.size >= 1 || super.canEndResponse(player)
    }

    override fun canPass(action: GameAction): Boolean {
        return false
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, card: InnoCard) {
        super.processChooseCard(player, card)
        // 执行选择卡牌上面的效果
        val resultParam = InnoResultParam()
        resultParam.addCard(card)
        next = InnoProcessAbilityListener(gameMode, player, this.initParam, resultParam, this.ability, this.abilityGroup)
    }

}
