package com.f14.TTA.executor

import com.f14.TTA.component.card.PactCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTARequestSelectCardListener
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils


/**
 * 请求中止条约处理器

 * @author 吹风奈奈
 */
class TTARequestBreakPactExecutor(param: RoundParam) : TTAPoliticalCardExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val listener = TTARequestSelectCardListener(gameMode, player, null, TTACmdString.ACTION_BREAK_PACT, "请选择要废除的条约!")
        val res = gameMode.insertListener(listener)
        if (TTACmdString.ACTION_BREAK_PACT == res.getString("subact")) {
            val card = res.get<TTACard>("card")!!
            CheckUtils.check(card.cardType != CardType.PACT, "你选择的不是条约牌!")
            TTABreakPactExecutor(param, card as PactCard).execute()
            this.finish()
        }
    }

}
