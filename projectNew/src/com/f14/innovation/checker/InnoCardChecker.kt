package com.f14.innovation.checker

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 检查卡牌的校验器

 * @author F14eagle
 */
class InnoCardChecker(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility) : InnoConditionChecker(gameMode, player, initParam, resultParam, ability) {

    @Throws(BoardGameException::class)
    override fun check(): Boolean {
        // 检查resultParam中的cards
        return this.resultParam.cards.empty || this.ability.test(this.resultParam.cards.cards)
    }

}
