package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #089-复合材料 执行器

 * @author F14eagle
 */
class InnoCustom089Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 选择计分区最高等级的一张牌,转移到我的计分区
        val deck = player.scores.maxLevelCardDeck
        val card = deck!!.drawRandom()!!
        val resultParam = gameMode.game.playerRemoveScoreCard(player, card)
        gameMode.game.playerAddScoreCard(this.mainPlayer, resultParam)
    }

}
