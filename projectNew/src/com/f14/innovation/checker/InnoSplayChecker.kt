package com.f14.innovation.checker

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 检查牌堆展开方向的校验器

 * @author F14eagle
 */
class InnoSplayChecker(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility) : InnoConditionChecker(gameMode, player, initParam, resultParam, ability) {

    @Throws(BoardGameException::class)
    override fun check(): Boolean {
        // 检查指定颜色牌堆的展开方向
        val player = this.targetPlayer
        val stack = player.getCardStack(this.initParam!!.color!!)
        return stack != null && stack.splayDirection == this.initParam.splayDirection
    }

}
