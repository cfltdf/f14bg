package com.f14.TTA.executor

import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTAMode
import com.f14.TTA.listener.ProductionChoiceListener
import com.f14.TTA.listener.SaladineListener
import com.f14.TTA.manager.TTAConstManager
import com.f14.bg.exception.BoardGameException


/**
 * 回合结束的处理器

 * @author 吹风奈奈
 */
class TTAEndTurnExecutor(param: RoundParam) : TTAActionExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        player.abilityManager.getAbility(CivilAbilityType.PA_SALADINE)?.let { ability ->
            val l = SaladineListener(gameMode, player, ability)
            val res = gameMode.insertListener(l)
            val property = res.get<TTAProperty>("property")!!
            val c = player.abilityManager.getAbilityCard(CivilAbilityType.PA_SALADINE)!!
            player.params.setGameParameter(CivilAbilityType.PA_SALADINE, property)
            gameMode.game.playerRefreshProperty(player)
            gameMode.report.playerAddSelect(player, property)
            gameMode.report.playerActiveCard(player, c)
        }
        player.abilityManager.getAbility(CivilAbilityType.PA_MARTINE)?.let { ability ->
            if (player.tokenPool.unhappyWorkers == 0) {
                val num = 2 + player.getProperty(CivilizationProperty.HAPPINESS) - TTAConstManager.getNeedHappiness(player.tokenPool.availableWorkers)
                gameMode.game.playerAddCulturePoint(player, num)
                gameMode.report.playerAddCulturePoint(player, num)
            }
        }
        for (ca in player.abilityManager.getAbilitiesByType(CivilAbilityType.EXTRA_SCORE_PHASE)) {
            if (!player.params.getBoolean(ca)) {
                val num = ca.getAvailableNumber(player)
                if (num > 0) {
                    val culture = ca.property.getProperty(CivilizationProperty.CULTURE)
                    val science = ca.property.getProperty(CivilizationProperty.SCIENCE)
                    if (culture > 0 && science > 0) {
                        val l = ProductionChoiceListener(gameMode, player, ca)
                        val param = gameMode.insertListener(l)
                        val property = param.get<TTAProperty>("property")!!
                        gameMode.game.playerAddPoint(player, property)
                    } else {
                        val res = gameMode.game.playerAddPoint(player, ca.property, num)
                        gameMode.report.playerAddPoint(player, res)
                    }
                    gameMode.report.playerActiveCardCache(player, ca.abilityType)
                }
                player.params.setRoundParameter(ca, true)
            }
        }
        gameMode.game.playerRoundScore(player)
        if (gameMode.game.config.mode != TTAMode.SIMPLE) {
            // 非简单模式下,都需要玩家摸军事牌
            gameMode.game.playerDrawMilitaryCard(player)
        }
        // 重置玩家的行动点数
        gameMode.game.playerResetActionPoint(player)
        gameMode.report.playerRoundEnd(player)

    }

}
