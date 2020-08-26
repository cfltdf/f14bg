package com.f14.TTA

import com.f14.TTA.component.card.EventCard
import com.f14.TTA.component.card.WonderCard
import com.f14.TTA.consts.ActiveAbilityType
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.CivilizationProperty
import com.f14.bg.GameEndPhase
import com.f14.bg.VPCounter
import com.f14.bg.VPResult
import org.apache.log4j.Logger
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

class TTAEndPhase : GameEndPhase<TTAGameMode>() {

    override fun createVPResult(gameMode: TTAGameMode): VPResult {
        gameMode.game.setCurrentPlayer(gameMode.game.startPlayer!!)
        val result = VPResult(gameMode.game)
        // 记录组队的得分
        val teamScore = HashMap<Int, Int>()
        val resignedPlayerCount = gameMode.resignedPlayerNumber
        // 终盘结算的事件牌
        // 完整模式下,需要检查所有剩余的事件牌堆,计算其中的得分事件
        val events = (gameMode.cardBoard.currentEvents + gameMode.cardBoard.futureEvents).filter { it.cardType == CardType.EVENT && it.level == 3 }.map { it as EventCard }
        // 计算各自的得分
        for (player in gameMode.game.players) {
            log.debug("玩家 [" + player.user.name + "] 的分数:")
            val vpc = VPCounter(player)
            result += (vpc)
            if (player.resigned) {
                vpc.addDisplayVp("体面退出游戏", (gameMode.getResignedPlayerPosition(player) - resignedPlayerCount) * 100)
            } else {
                vpc.addDisplayVp("文明点数", player.culturePoint)
                if (gameMode.game.realPlayerNumber > (if (gameMode.game.isTeamMatch) 2 else 1)) {
                    // 圣家堂得分
                    val wc = player.uncompletedWonder
                    if (wc?.activeAbility != null) {
                        if (wc.activeAbility!!.abilityType == ActiveAbilityType.PA_SAGRADA_FAMILIA) {
                            val step = wc.currentStep
                            vpc.addDisplayVp(wc.name, step * wc.activeAbility!!.property.getProperty(CivilizationProperty.CULTURE))
                        }
                    }
                    // 联合国得分
                    if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_SCORE_EVENT)) {
                        val ability = player.abilityManager.getAbility(CivilAbilityType.PA_SCORE_EVENT)!!
                        val card = player.abilityManager.getAbilityCard(CivilAbilityType.PA_SCORE_EVENT)!!
                        val num = events.size * ability.property.getProperty(CivilizationProperty.CULTURE)
                        vpc.addDisplayVp(card.name, num)
                    }
                    // 新版盖茨得分
                    val lc = player.leader
                    if (lc?.activeAbility != null) {
                        if (lc.activeAbility!!.abilityType == ActiveAbilityType.PA_NEW_GATES_ABILITY) {
                            val ca = player.abilityManager.getAbility(CivilAbilityType.PA_NEW_GATES_ABILITY)
                            vpc.addDisplayVp(lc.name, ca!!.getAvailableProperty(player, CivilizationProperty.CULTURE))
                        }
                    }
                    // 始皇陵得分
                    if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_SHIHUANG_TOMB)) {
                        val c = player.abilityManager.getAbilityCard(CivilAbilityType.PA_SHIHUANG_TOMB) as WonderCard
                        if (c.activeAbility!!.abilityType == ActiveAbilityType.PA_SHIHUANG_TOMB) {
                            vpc.addDisplayVp(c.name, c.blues * c.activeAbility!!.property.getProperty(CivilizationProperty.CULTURE))
                        }
                    }
                    // 计算得分
                    for (ec in events) {
                        vpc.addDisplayVp(ec.name, player.getScoreCulturePoint(ec.scoreAbilities))
                    }
                }
            }
            log.debug("总计 : " + vpc.totalDisplayVP)
            // 记录队伍的得分
            var score: Int = teamScore[player.team] ?: 0
            score += vpc.totalDisplayVP
            teamScore[player.team] = score
        }
        // 设置玩家的总得分
        for (vpc in result.vpCounters) {
            val total = teamScore[vpc.player.team] ?: 0
            val self = vpc.totalDisplayVP
            val mate = total - self
            if (gameMode.game.isTichuMode) {
                when {
                    vpc.player.team == 0 -> {
                        vpc.addVp("总分", self)
                        vpc.addVp("地主让分", -gameMode.tichuBid)
                    }
                    self > mate -> {
                        vpc.addVp("总分", self)
                        vpc.addDisplayVp("队友得分", mate)
                    }
                    else -> {
                        vpc.addDisplayVp("总分", self)
                        vpc.addVp("队友得分", mate)
                    }
                }
            } else {
                vpc.addVp("总分", self)
                if (gameMode.game.isTeamMatch) {
                    vpc.addVp("队友得分", mate)
                }
            }
        }
        return result
    }


    companion object {
        private val log = Logger.getLogger(TTAEndPhase::class.java)!!
    }
}