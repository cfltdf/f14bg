package com.f14.innovation.checker

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 检查手牌中符合条件的牌的数量的校验器

 * @author F14eagle
 */
class InnoHandNumChecker(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility) : InnoHasCardChecker(gameMode, player, initParam, resultParam, ability) {

    @Throws(BoardGameException::class)
    override fun check(): Boolean {
        // 检查玩家所有符合条件的卡牌的数量
        val num = this.resourceCards.count(this.ability::test)
        // 如果数量不匹配则返回false
        if (num != this.initParam!!.num) {
            return false
        }
        return true
    }

    override val resourceCards: Collection<InnoCard>
        get() = this.targetPlayer.hands.getCards()

}
