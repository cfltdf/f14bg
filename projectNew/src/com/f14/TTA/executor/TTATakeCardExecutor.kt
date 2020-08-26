package com.f14.TTA.executor

import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.RoundStep
import com.f14.bg.exception.BoardGameException


/**
 * 从卡牌列拿走(移除)牌的处理器

 * @author 吹风奈奈
 */
class TTATakeCardExecutor(param: RoundParam, private var cardId: String) : TTAActionExecutor(param) {
    private var actionType: ActionType = ActionType.CIVIL
    private var actionCost: Int = 0
    var costModify = 0
    var modifyLimit = 0

    @Throws(BoardGameException::class)
    override fun execute() {
        val cb = gameMode.cardBoard
        val index = cb.getCardIndex(cardId)
        actionCost = cb.getCost(cardId, player)
        if (actionCost > modifyLimit) actionCost += costModify
        actionCost = maxOf(0, actionCost)
        // 检查玩家是否有足够的内政行动点
        player.checkActionPoint(actionType, actionCost)
        val card = cb.getCard(cardId)
        // 检查玩家是否可以拿牌
        player.checkTakeCard(card)
        // 拿取卡牌并添加到新拿卡牌列表

        gameMode.game.playerTakeCard(player, card, index)
        gameMode.game.sendPlayerActivableCards(RoundStep.NORMAL, player)
        param.newcards.add(card)
        param.useActionPoint(actionType, actionCost)
        param.afterTakeCard(card)
        gameMode.report.playerTakeCard(player, actionCost, index, card)
    }

}
