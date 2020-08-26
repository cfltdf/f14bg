package com.f14.TTA.executor.war

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.EventType
import com.f14.TTA.executor.TTAActionExecutor
import com.f14.bg.common.ParamSet


/**
 * 战争(侵略)结果的处理器

 * @author 吹风奈奈
 */
abstract class TTAWarResultExecutor(param: RoundParam, protected var card: AttackCard, protected var winner: TTAPlayer, protected var loser: TTAPlayer, protected var advantage: Int) : TTAActionExecutor(param) {


    protected fun createWarParam(): ParamSet {
        val warParam = ParamSet()
        warParam["advantage"] = advantage
        return warParam
    }

    protected fun processWinnerEffect(warParam: ParamSet) {
        val property = card.winnerEffect.getRealProperty(warParam)
        when (card.winnerEffect.eventType) {
            EventType.SCORE // 得到资源/食物/科技/文明
            -> {
                var multi = 1
                repeat(winner.abilityManager.getAbilitiesByType(CivilAbilityType.PA_DOUBLE_REWARD)
                        // 战争或侵略收益加倍
                        .filter { it.test(card) }.size) { multi *= 2 }
                val res = gameMode.game.playerAddPoint(winner, property, multi)
                gameMode.report.playerAddPoint(winner, res)
            }
            EventType.TOKEN // 得到蓝色/黄色标志物
            -> gameMode.game.playerAddToken(winner, property)
            else -> Unit
        }
        // 输出战报信息
        gameMode.report.printCache(winner)
    }
}
