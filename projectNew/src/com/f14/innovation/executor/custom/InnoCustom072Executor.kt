package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #072-电学 执行器

 * @author F14eagle
 */
class InnoCustom072Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 将所有不含工厂符号的置顶牌全部归还,归还X张,就抓X张[8]
        val player = this.targetPlayer
        var num = 0
        for (color in InnoColor.values()) {
            val card = player.getTopCard(color)
            if (card != null && !card.containsIcons(InnoIcon.FACTORY)) {
                val resultParam = gameMode.game.playerRemoveTopCard(player, color)
                gameMode.game.playerReturnCard(player, resultParam)
                num += 1
            }
        }
        if (num > 0) {
            gameMode.game.playerDrawCard(player, 8, num)
            this.setPlayerActived(player)
        }
    }

}
