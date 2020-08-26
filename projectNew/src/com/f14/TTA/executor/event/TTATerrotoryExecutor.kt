package com.f14.TTA.executor.event

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.EventCard
import com.f14.TTA.component.card.LeaderCard
import com.f14.TTA.component.param.AuctionParam
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.listener.ChooseArmyTerritoryListener
import com.f14.bg.exception.BoardGameException

/**
 * 殖民地处理器

 * @author 吹风奈奈
 */
class TTATerrotoryExecutor(param: RoundParam, card: EventCard) : TTAEventCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        if (card !is EventCard) return
        var topPlayer: TTAPlayer? = null
        var totalValue = 0
        gameMode.cardBoard.removeLastEvent()
        gameMode.game.sendRemovePastEventResponse(card, null)
        gameMode.game.sendBaseInfo(null)
        for (p in gameMode.game.players) {
            if (!p.resigned && p.checkAbility(CivilAbilityType.PA_ZHUDI_ABILITY)) {
                topPlayer = p
                gameMode.report.playerActiveCardCache(topPlayer, CivilAbilityType.PA_ZHUDI_ABILITY)
                p.params.setGameParameter(CivilAbilityType.PA_ZHUDI_ABILITY, true)
            }
        }
        if (topPlayer == null) {
            val l = ChooseArmyTerritoryListener(gameMode, player, card)
            val res = gameMode.insertListener(l)
            // 结算拍卖结果
            topPlayer = res.get<TTAPlayer>("topPlayer")
            if (topPlayer != null) {
                // 如果存在出价的玩家,则他牺牲和用掉对应的兵力,并得到该殖民地
                val ap = res.get<AuctionParam>(topPlayer.position)!!
                if (ap.colonyBonus > 0) {
                    gameMode.report.playerUseColoBonus(topPlayer, ap.colonyBonus)
                }
                // 战报输出殖民所牺牲的部队
                gameMode.game.playerSacrificeUnit(topPlayer, ap.units)
                for ((a, n) in ap.blues) {
                    val c = player.abilityManager.getAbilityCard(a.abilityType)
                    if (c is LeaderCard) {
                        val num = -(n + 1) * n / 2
                        gameMode.game.playerAddResource(player, num)
                        gameMode.report.playerAddResource(player, num)
                        gameMode.report.playerActiveCard(player, c)
                    }
                }
                // 战报输出殖民所用殖民奖励牌
                gameMode.game.playerPlayBonusCard(topPlayer, ap.selectedBonusCards)
                totalValue = ap.getTotalValue()
            }
        }
        if (topPlayer != null) {
            // 战报输出拍卖结果信息
            gameMode.game.playerAddCardDirect(topPlayer, card)
            // 处理殖民地的INSTANT类型的事件能力
            for (ability in card.eventAbilities) {
                val executor = TTAInstantAbilityExecutor(param, ability)
                executor.trigPlayer = topPlayer
                executor.execute()
            }
            gameMode.report.playerGetColony(topPlayer, card, totalValue)
            // 处理获得殖民地之后的能力
            for (ca in topPlayer.abilityManager.getAbilitiesByType(CivilAbilityType.PA_INCPOP_AFTER_COLO)) {
                gameMode.game.playerIncreasePopulation(topPlayer, ca.amount)
                gameMode.report.playerIncreasePopulationCache(topPlayer, ca.amount)
                gameMode.report.playerActiveCard(topPlayer, CivilAbilityType.PA_INCPOP_AFTER_COLO)
            }
        } else {
            gameMode.cardBoard.lastEvent = card
            gameMode.game.sendBaseInfo(null)
            gameMode.game.sendAddPastEventResponse(card, null)
        }
    }
}
