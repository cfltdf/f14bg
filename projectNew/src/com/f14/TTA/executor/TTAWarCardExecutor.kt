package com.f14.TTA.executor

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.card.WonderCard
import com.f14.TTA.component.param.AuctionParam
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.factory.TTAExecutorFactory
import com.f14.TTA.listener.ChooseArmyWarListener
import com.f14.bg.exception.BoardGameException
import kotlin.math.abs


/**
 * 战争(侵略)的处理器
 * @author 吹风奈奈
 */
class TTAWarCardExecutor(param: RoundParam, private var card: AttackCard) : TTAActionExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val l = ChooseArmyWarListener(gameMode, card)
        val res = gameMode.insertListener(l)
        val trigPlayer = card.owner!!
        val targetPlayer = card.target!!
        val trigParam = res.get<AuctionParam>(trigPlayer.position)!!
        val targetParam = res.get<AuctionParam>(targetPlayer.position)!!
        // 取得玩家部队的总军事点数
        val trigMilitary = trigParam.getTotalValue()
        val targetMilitary = targetParam.getTotalValue()
        // 判断胜负玩家
        val result = when {
            trigMilitary > targetMilitary -> 1
            trigMilitary < targetMilitary || !gameMode.isVersion2 -> -1 // 老版平局算防守方胜利
            else -> 0
        }
        // 牺牲掉对应的部队,消耗掉防御卡
        this.sacrifice(trigParam, trigPlayer, false)
        this.sacrifice(targetParam, targetPlayer, true)
        gameMode.report.printWarResult(trigPlayer, targetPlayer, card, trigMilitary, targetMilitary, result)

        if (result == 1 || card.cardType == CardType.WAR && result != 0) {
            // 结算胜负结果
            if (result == 1 && player.abilityManager.hasAbilitiy(CivilAbilityType.PA_YUANMINGYUAN)) {
                val ability = player.abilityManager.getAbility(CivilAbilityType.PA_YUANMINGYUAN)!!
                val wonder = player.abilityManager.getAbilityCard(CivilAbilityType.PA_YUANMINGYUAN) as WonderCard
                TTAYuanmingyuanExecutor(param, targetPlayer, wonder, ability).execute()
            }

            val advantage = abs(trigMilitary - targetMilitary)
            val winner = if (result == 1) trigPlayer else targetPlayer
            val loser = if (result == -1) trigPlayer else targetPlayer
            TTAExecutorFactory.createWarResultExecutor(param, card, winner, loser, advantage).execute()

        }
        gameMode.game.tryDiscardCards(card)
    }

    /**
     * 牺牲掉对应的部队,消耗掉防御卡
     * @param param
     * @param player
     */
    private fun sacrifice(param: AuctionParam, player: TTAPlayer, isTarget: Boolean) {
        if (param.hasUnit()) gameMode.game.playerSacrificeUnit(player, param.units)
        for ((a, n) in param.blues) {
            val c = player.abilityManager.getAbilityCard(a.abilityType)
            if (c is WonderCard) {
                c.addBlues(-n)
                player.tokenPool.addAvailableBlues(n)
                gameMode.game.sendPlayerCardToken(player, c, null)
                gameMode.game.sendPlayerBoardTokens(player, null)
                gameMode.report.playerActiveCard(player, c)
            } else if (isTarget && a.abilityType == CivilAbilityType.PA_SACRIFICE_UNIT) {
                val p = gameMode.game.playerAddPoint(player, a.property, n)
                gameMode.report.playerAddPoint(player, p)
            }
        }
        gameMode.game.playerPlayBonusCard(player, param.selectedBonusCards)
        gameMode.report.printCache(player)
    }

}
