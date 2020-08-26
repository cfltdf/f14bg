package com.f14.TTA.executor

import com.f14.TTA.component.card.GovermentCard
import com.f14.TTA.component.param.CostParam
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilAbilityType
import com.f14.bg.exception.BoardGameException


/**
 * 更变政府的处理器
 * @author 吹风奈奈
 */
open class TTAChangeGovermentExecutor(param: RoundParam, override val card: GovermentCard, protected var revolution: Int) : TTAPlayCardExecutor(param, card) {
    var costModify: Int = 0
    protected var cp: CostParam = CostParam()

    @Throws(BoardGameException::class)
    override fun check() {
        when (revolution) {
            0 -> {
                if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_PEACE_REVOLUTION)) {
                    revolution = 2
                } else {
                    actionType = param.checkCanRevolution(gameMode.isVersion2)
                }
                // 革命使用的是次要的科技点数
                cp = param.getResearchCost(card, true, costModify)
            }
            1 -> {
                // 和平演变使用的是主要的科技点数
                cp = param.getResearchCost(card, false, costModify)
            }
        }
        param.checkResearchCost(card, cp)
        super.check()
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        if (!checked) {
            this.check()
        }

        val oldCard = player.government
        // 研发科技时支付科技点
        gameMode.game.playerResearchCost(player, cp.cost)
        gameMode.game.playerAddCard(player, card)
        when (revolution) {
            0 -> {
                // 革命时触发的能力
                val a = player.abilityManager.getAbility(CivilAbilityType.PA_MILITARY_REVOLUTION)
                if (a != null) {
                    gameMode.report.playerActiveCardCache(player, CivilAbilityType.PA_MILITARY_REVOLUTION)
                    gameMode.game.playerAddPoint(player, a.property)
                }
                // 革命将使用掉所有的行动点
                actionCost = player.getAvailableActionPoint(actionType)
            }
            2 -> {
                // 革命时触发的能力
                val a = player.abilityManager.getAbility(CivilAbilityType.PA_PEACE_REVOLUTION)
                if (a != null) {
                    gameMode.report.playerActiveCardCache(player, CivilAbilityType.PA_PEACE_REVOLUTION)
                    gameMode.game.playerAddPoint(player, a.property)
                }
            }
        // 通知客户端关闭请求窗口
        // 扣除行动点
        }
        gameMode.game.executeTemplateResource(player, cp)
        // 通知客户端关闭请求窗口
        gameMode.game.playerRequestEnd(player)
        // 扣除行动点
        param.useActionPoint(actionType, actionCost)
        param.afterRrevolution(revolution)
        gameMode.game.playerReattachCard(player, player.government!!, oldCard!!)
        super.execute()

    }

    override fun sendReport() {
        gameMode.report.playerCostAction(player, this.actionType, this.actionCost, revolution == 0)
        if (cached) {
            gameMode.report.playerChangeGovermentCache(player, revolution != 1, card)
        } else {
            gameMode.report.playerChangeGoverment(player, revolution != 1, card)
        }
    }
}
