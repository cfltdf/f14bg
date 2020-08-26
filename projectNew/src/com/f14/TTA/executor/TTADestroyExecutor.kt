package com.f14.TTA.executor

import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.manager.TTAConstManager
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils


/**
 * 摧毁的处理器
 * @author 吹风奈奈
 */
class TTADestroyExecutor(param: RoundParam, private var card: TechCard) : TTAActionExecutor(param) {
    private var actionType: ActionType = if (card.cardType == CardType.UNIT) ActionType.MILITARY else ActionType.CIVIL
    private var actionCost: Int = 0

    init {
        // 部队卡的行动类型为军事,其他为内政
        actionCost = TTAConstManager.getActionCost(player, TTACmdString.ACTION_DESTORY)
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        player.checkActionPoint(actionType, actionCost)
        when (card.cardType) {
            CardType.BUILDING, CardType.PRODUCTION, CardType.UNIT -> {
                CheckUtils.check(card.workers <= 0, "该卡牌上没有工人!")
                // 玩家消耗行动点,摧毁建筑
                param.useActionPoint(actionType, actionCost)
                gameMode.game.playerDestroy(player, card, 1)
                gameMode.report.playerDestory(player, actionType, actionCost, card, 1)
            }
            else -> throw BoardGameException("你不能在这张卡牌上进行摧毁行动!")
        }
    }

}
