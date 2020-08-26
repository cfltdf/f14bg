package com.f14.innovation.listener.custom

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.listener.InnoCommonConfirmListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam


/**
 * #064-罐头制造 监听器

 * @author F14eagle
 */
class InnoCustom064Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoCommonConfirmListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        // 抓一张[6]垫底;如果这样做,就将置顶牌中所有不含工厂标志的牌计分
        val player = this.targetPlayer
        gameMode.game.playerDrawAndTuckCard(player, 6, 1)

        for (color in InnoColor.values()) {
            val card = player.getTopCard(color)
            if (card != null && !card.containsIcons(InnoIcon.FACTORY)) {
                val resultParam = gameMode.game.playerRemoveTopCard(player, color)
                // 需要检查成就
                gameMode.game.playerAddScoreCard(player, resultParam, true)
            }
        }
        this.setPlayerResponsed(player)
    }

}
