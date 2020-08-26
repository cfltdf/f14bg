package com.f14.innovation.checker.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.checker.InnoConditionChecker
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #017-历法 检查器

 * @author F14eagle
 */
class InnoCustom017Checker(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility) : InnoConditionChecker(gameMode, player, initParam, resultParam, ability) {

    @Throws(BoardGameException::class)
    override fun check(): Boolean {
        // 如果计分区的卡牌数量比手牌多就执行
        val player = this.targetPlayer
        return player.scores.size() > player.hands.size()
    }

}
