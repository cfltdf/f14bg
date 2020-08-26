package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * Created by 吹风奈奈 on 2017/7/18.
 */
class InnoCustom011Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 每独有X种颜色的置顶牌,就抓X张1计分
        val i = InnoColor.values().count { c -> gameMode.game.players.all { (it == targetPlayer) == it.hasCardStack(c) } }
        if (i > 0) {
            gameMode.game.playerDrawAndScoreCard(player, 1, i)
            // 因为可能没有计分,所有手动设置玩家是否触发了效果
            this.setPlayerActived(player)
        }
    }

}
