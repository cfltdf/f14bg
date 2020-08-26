package com.f14.innovation.checker

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 检查是否存在符合条件的卡牌的校验器

 * @author F14eagle
 */
abstract class InnoHasCardChecker(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility) : InnoConditionChecker(gameMode, player, initParam, resultParam, ability) {

    @Throws(BoardGameException::class)
    override fun check(): Boolean {
        // 检查玩家所有符合条件的卡牌的数量
        // 如果没有合适的卡牌则返回false
        return this.resourceCards.any(this.ability::test)
    }

    /**
     * 取得需要判断的卡牌

     * @return
     */
    protected abstract val resourceCards: Collection<InnoCard>

}
