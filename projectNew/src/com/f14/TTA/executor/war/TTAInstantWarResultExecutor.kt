package com.f14.TTA.executor.war

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.EventType
import com.f14.bg.common.ParamSet
import com.f14.bg.exception.BoardGameException

/**
 * 立即执行的战争(侵略)结果处理器

 * @author 吹风奈奈
 */
class TTAInstantWarResultExecutor(param: RoundParam, card: AttackCard, winner: TTAPlayer, loser: TTAPlayer, advantage: Int) : TTAWarResultExecutor(param, card, winner, loser, advantage) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val warParam = ParamSet()
        warParam["advantage"] = advantage
        when (card.loserEffect.eventType) {
            EventType.LOSE_LEADER -> { // 失去当前的领袖
                val leader = loser.leader
                if (leader != null) {
                    // 记录对象等级
                    warParam["level"] = leader.level
                    gameMode.game.playerRemoveCard(loser, leader)
                    param.checkWillCard(loser)
                }
            }
            EventType.LOSE_UNCOMPLETE_WONDER -> { // 失去建造中的奇迹
                val wonder = loser.uncompletedWonder
                if (wonder != null) {
                    // 记录对象等级
                    warParam["level"] = wonder.level
                    gameMode.game.playerRemoveUncompleteWonder(loser)
                }
            }
            EventType.SCORE -> { // 得分
                val property = gameMode.game.playerAddPoint(loser, card.loserEffect.getRealProperty(warParam))
                gameMode.report.printCache(loser)
                property.multi(-1)
                // 记录实际的得分情况
                warParam["property"] = property
            }
            EventType.TOKEN -> { // 失去资源库中的标志物
                val property = gameMode.game.playerAddToken(loser, card.loserEffect.getRealProperty(warParam))
                gameMode.report.printCache(loser)
                property.multi(-1)
                // 记录实际的得分情况
                warParam["property"] = property
            }
            EventType.PARTITION -> { // 瓜分
                var property = TTAProperty()
                property.setProperty(CivilizationProperty.YELLOW_TOKEN, -winner.government!!.level)
                property.setProperty(CivilizationProperty.BLUE_TOKEN, -loser.government!!.level)
                property = gameMode.game.playerAddToken(loser, property)
                gameMode.report.printCache(loser)
                property.multi(-1)
                warParam["property"] = property
            }
            else -> {
            }
        }
        this.processWinnerEffect(warParam)
    }

}
