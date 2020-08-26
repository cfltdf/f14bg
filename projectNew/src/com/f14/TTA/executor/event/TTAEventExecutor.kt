package com.f14.TTA.executor.event

import com.f14.TTA.component.card.EventCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.EventType
import com.f14.TTA.executor.ChooseEventExecutor
import com.f14.TTA.factory.TTAExecutorFactory
import com.f14.bg.exception.BoardGameException

/**
 * 普通的事件牌的处理器

 * @author 吹风奈奈
 */
class TTAEventExecutor(param: RoundParam, card: EventCard) : TTAEventCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        if (card !is EventCard) return
        for (ea in card.eventAbilities) {
            TTAExecutorFactory.createEventAbilityExecutor(param, ea).execute()
        }
        player.roundTempParam.get<List<TTACard>>(EventType.CHOOSE_EVENT)?.let { events ->
            val executor = ChooseEventExecutor(param, events)
            executor.execute()
            player.roundTempParam[EventType.CHOOSE_EVENT] = null
        }
        for (p in gameMode.game.getPlayersByOrder(player)) {
            if (!p.resigned) {
                val res = gameMode.game.playerAddCulturePoint(p, p.getScoreCulturePoint(card.scoreAbilities))
                gameMode.report.playerAddCulturePoint(p, res)
                gameMode.report.printCache(p)
            }
        }
        if (card.level == 3) {
            gameMode.game.checkUnitedNation(CivilAbilityType.PA_SCORE_EVENT, 0)
        }
    }
}
