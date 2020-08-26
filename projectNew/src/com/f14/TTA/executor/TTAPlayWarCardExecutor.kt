package com.f14.TTA.executor

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.card.PactCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.listener.ChoosePlayerWarListener
import com.f14.bg.exception.BoardGameException

/**
 * 打出战争(侵略)牌的处理器
 * @author 吹风奈奈
 */
class TTAPlayWarCardExecutor(param: RoundParam, private val card: AttackCard) : TTAPoliticalCardExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        // 检查玩家是否有行动力打该牌
        player.checkUseCard(card)
        // 创建选择玩家的监听器
        val l = ChoosePlayerWarListener(param, card)
        val res = gameMode.insertListener(l)
        val targetPlayer = res.get<TTAPlayer>("target")
        if (targetPlayer != null) {
            // 则减去玩家的行动点数
            val actionCost = res.getInteger("actionCost")
            if (actionCost > 0) {
                param.useActionPoint(card.actionCost.actionType, actionCost)
            }
            // 将牌从玩家手中移除
            gameMode.game.playerRemoveHand(player, card)
            gameMode.report.playerActiveCard(player, targetPlayer, card, card.actionCost.actionType, actionCost)

            gameMode.game.checkUnitedNation(CivilAbilityType.PA_SCORE_USE_CARD, 1)

            // 废弃所有待删除列表中的卡牌
            val pactIds = res.get<List<String>>("pactIds")!!
            player.allPlayedCard.filter { it.id in pactIds }.forEach { gameMode.game.removeOvertimeCard(it as PactCard) }

            // 检查目标是否拥有被攻击时得分的能力
            targetPlayer.abilityManager.getAbilitiesByType(CivilAbilityType.PA_SCORE_UNDERWAR).forEach { gameMode.game.playerAddPoint(if (it.effectSelf) targetPlayer else player, it.property) }

            if (gameMode.olympicsPosition != -1) {
                val player = gameMode.game.getPlayer(gameMode.olympicsPosition)
                if (player != null && !player.resigned) {
                    gameMode.game.playerAddCulturePoint(player, 5)
                    gameMode.report.playerAddCulturePoint(player, 5)
                }
            }

            // 完成出牌后触发的方法
            param.afterPlayCard(card)
            gameMode.report.printCache(player)
            gameMode.report.printCache(targetPlayer)

            card.owner = player
            card.target = targetPlayer
            when (card.cardType) {
                CardType.AGGRESSION -> TTAWarCardExecutor(param, card).execute()
                CardType.WAR -> gameMode.game.playerDeclareWar(player, targetPlayer, card)
                else -> return
            }
            this.finish()
        }
    }

}
