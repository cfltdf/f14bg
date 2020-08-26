package com.f14.innovation.checker

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 检查是否存在计分牌

 * @author F14eagle
 */
class InnoHasScoreCardChecker(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility) : InnoHasCardChecker(gameMode, player, initParam, resultParam, ability) {

    override val resourceCards: Collection<InnoCard>
        get() = this.targetPlayer.scores.getCards()

}
