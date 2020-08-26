package com.f14.TTA.executor.action

import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTARequestSelectCardListener
import com.f14.bg.exception.BoardGameException

/**
 * 提示移除卡牌的行动牌处理器

 * @author 吹风奈奈
 */
class TTAActionTakeCardExecutor(param: RoundParam, card: ActionCard) : TTAActionCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val listener = TTARequestSelectCardListener(gameMode, player, card, TTACmdString.ACTION_TAKE_CARD, "请选择要移除的牌!")
        val res = gameMode.insertListener(listener)
        val subact = res.getString("subact")
        if (TTACmdString.ACTION_TAKE_CARD == subact) {
            val takeCard = res.get<TTACard>("card")!!
            val index = gameMode.cardBoard.getCardIndex(takeCard.id)
            gameMode.game.playerRemoveBoardCard(player, takeCard, index)
            this.completed = true
        }
    }

}
