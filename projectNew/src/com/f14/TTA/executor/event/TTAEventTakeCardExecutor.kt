package com.f14.TTA.executor.event

import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.RoundStep
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.event.TakeCardListener
import com.f14.bg.exception.BoardGameException
import java.util.*

/**
 * 从卡牌列获取卡牌的事件牌能力处理器

 * @author 吹风奈奈
 */
class TTAEventTakeCardExecutor(param: RoundParam, ability: EventAbility) : TTAEventAbilityExecutor(param, ability) {

    @Throws(BoardGameException::class)
    override fun execute() {
        var amount = ability.amount
        val p = gameMode.getPlayersByChooser(ability.chooser).first()
        val newcards = ArrayList<TTACard>(0)
        val cb = gameMode.cardBoard
        do {
            val l = TakeCardListener(gameMode, ability, player, amount)
            l.addListeningPlayer(p)
            val res = gameMode.insertListener(l)
            val card = res.get<TTACard>("card") ?: break
            val index = res.getInteger("index")
            val actionCost = res.getInteger("actionCost")
            // 拿取卡牌并添加到新拿卡牌列表
            cb.takeCard(index)
            newcards.add(card)
            gameMode.game.playerTakeCard(p, card, index)
            gameMode.report.playerTakeCard(p, actionCost, index, card)
            amount -= actionCost
        } while (amount > 0)
        if (newcards.size > 0) {
            if (p === param.player && !gameMode.isVersion2) {
                param.newcards.addAll(newcards)
            }
            p.roundTempParam[RoundStep.POLITICAL] = TTACmdString.POLITICAL_PASS
            // 行动完成后,需要重新补牌,不需要进行弃牌的步骤
            param.regroupCardRow(false)
            gameMode.game.sendBaseInfo(null)
        } else {
            gameMode.report.action(p, "拒绝了国际条约!")
        }
    }
}
