package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import com.f14.innovation.utils.InnoUtils

/**
 * #100-微型化 执行器

 * @author F14eagle
 */
class InnoCustom100Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 若你归还了一张[10],则你的计分区中每有一种不同时期的牌,便抓1张[10]加入手牌!
        val player = this.targetPlayer
        if (this.resultCards.isNotEmpty()) {
            if (this.resultCards[0].level == 10) {
                val num = InnoUtils.getDifferentLevelCardsNum(player.scores.getCards())
                if (num > 0) {
                    gameMode.game.playerDrawCard(player, 10, num)
                }
            }
        }
    }

}
