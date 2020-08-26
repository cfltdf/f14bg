package com.f14.TTA.listener

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTACardDeck
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.BgUtils

class ChooseTakeHandListener(param: RoundParam, private val deck: TTACardDeck) : TTAInterruptListener(param.gameMode, param.player) {
    init {
        this.addListeningPlayer(trigPlayer)
    }

    private var card: TTACard? = null

    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["card"] = card
        return param
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        res.private("multiSelection", false)
        res.private("cardIds", BgUtils.card2String(deck.cards))
        return res
    }

    override fun doAction(action: BgAction.GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val cardIds = action.getAsString("cardIds")
        val confirm = action.getAsBoolean("confirm")
        if (confirm) {
            val cards = deck.getCards(cardIds)
            val card = cards.singleOrNull() ?: throw BoardGameException("你只能选择1张卡牌!")
            player.checkTakeCard(card)
            this.card = card
        }
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_DISCARD_MILITARY
}
