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


/**
 * #035-翻译 实际执行效果的监听器

 * @author F14eagle
 */
class InnoCustom035EffectListener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseSpecificCardListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun canPass(action: GameAction): Boolean {
        return false
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, card: InnoCard) {
        // 从计分区拿牌融合
        val resultParam = gameMode.game.playerRemoveScoreCard(player, card)
        gameMode.game.playerMeldCard(player, resultParam)
    }

}
