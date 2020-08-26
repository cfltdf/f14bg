package com.f14.TTA.executor

import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.listener.GetAndPlayCardListener
import com.f14.bg.exception.BoardGameException


/**
 * 获取并打出牌的处理器

 * @author 吹风奈奈
 */
class TTAGetAndPlayExecutor(param: RoundParam, var ability: CivilCardAbility) : TTAActionExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val l = GetAndPlayCardListener(gameMode, player, ability)
        l.addListeningPlayer(player)
        val res = gameMode.insertListener(l)
        val cardId = res.get<String>("cardId")
        if (cardId != null) {
            val cb = gameMode.cardBoard
            val index = cb.getCardIndex(cardId)
            val card = cb.getCard(cardId) as TechCard
            val executor = TTAPlayTechCardExecutor(param, card)
            executor.actionCost = 0
            executor.costModify = ability.property.getProperty(CivilizationProperty.SCIENCE)
            executor.cached = true
            executor.check()

            cb.takeCard(cardId)
            gameMode.game.playerTakeCard(player, card, index)
            executor.execute()
            gameMode.report.playerTakeCard(player, 0, index, card)
        }
    }

}
