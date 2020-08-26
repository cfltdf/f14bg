package com.f14.TTA.executor.action

import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.card.GovermentCard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CardSubType
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.executor.TTAChangeGovermentExecutor
import com.f14.TTA.executor.TTAPlayTechCardExecutor
import com.f14.TTA.listener.TTARequestSelectCardListener
import com.f14.TTA.listener.TTARequestSelectListener
import com.f14.bg.exception.BoardGameException

/**
 * 提示打出牌的行动牌处理器
 * @author 吹风奈奈
 */
class TTAActionRequestPlayCardExecutor(param: RoundParam, card: ActionCard) : TTAActionCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val listener = TTARequestSelectCardListener(gameMode, player, card, TTACmdString.ACTION_PLAY_CARD, "请选择要打出的手牌!")
        listener.condition = card.actionAbility
        val res = gameMode.insertListener(listener)
        val subact = res.getString("subact")
        if (TTACmdString.ACTION_PLAY_CARD == subact) {
            val playCard = res.get<TechCard>("card")!!
            param.checkActionCardEnhance(card)
            when (playCard.cardSubType) {
                CardSubType.GOVERMENT -> requestChangeGovement(playCard as GovermentCard)
                else -> {
                    val executor = TTAPlayTechCardExecutor(param, playCard)
                    executor.actionCost = 0
                    executor.cached = true
                    executor.check()
                    executor.execute()
                    gameMode.game.playerAddPoint(player, card.actionAbility.property)
                    this.completed = true
                }
            }
        }
    }

    @Throws(BoardGameException::class)
    private fun requestChangeGovement(playCard: GovermentCard) {
        val listener = TTARequestSelectListener(gameMode, player, playCard, TTACmdString.REQUEST_SELECT, "革命,和平演变", "请选择要更换政府的方式")
        val res = gameMode.insertListener(listener)
        val revolution = res.getInteger("sel")
        if (revolution < 0) {
            return
        }
        val executor = TTAChangeGovermentExecutor(param, playCard, revolution)
        executor.cached = true
        executor.execute()
        gameMode.game.playerAddPoint(player, card.actionAbility.property)
        param.afterPlayActionCard(card)
        gameMode.report.playerPlayCard(player, executor.actionType, 0, card)
    }

}
