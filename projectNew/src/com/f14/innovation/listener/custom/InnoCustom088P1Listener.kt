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
 * #088-协同合作 监听器

 * @author F14eagle
 */
class InnoCustom088P1Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseSpecificCardListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun canPass(action: GameAction): Boolean {
        return false
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, card: InnoCard) {
        super.processChooseCard(player, card)
        var resultParam = gameMode.game.playerRemoveHandCard(player, card)
        gameMode.game.playerMeldCard(player, resultParam)
        // 将待选列表中的其他牌给当前执行玩家融合
        for (c in this.specificCards.cards) {
            resultParam = gameMode.game.playerRemoveHandCard(player, c)
            gameMode.game.playerMeldCard(this.currentPlayer, resultParam)
        }
        this.specificCards.clear()
    }

}
