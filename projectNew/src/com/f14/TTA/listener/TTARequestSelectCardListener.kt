package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.TTACmdString
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.component.ICondition
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import java.util.*

class TTARequestSelectCardListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, showCard: TTACard?, actionString: String, msg: String) : TTAActionRequestListener(gameMode, trigPlayer, showCard, actionString, msg) {

    var condition: ICondition<TTACard>? = null
    private var card: TTACard? = null
    private var subact: String? = null
    private var index = 0


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["card"] = card
        param["subact"] = subact
        return param
    }


    override fun createParam(): MutableMap<String, Any> {
        return HashMap()
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        subact = action.getAsString("subact")
        val player = action.getPlayer<TTAPlayer>()
        if ("cancel" != subact) {
            // 检查选择的卡牌是否可以应用到该能力
            val cardId = action.getAsString("cardId")
            val card = this.getCard(player, cardId)
            CheckUtils.check(condition != null && !condition!!.test(card), "该能力不能在这张牌上使用!")
            this.card = card
        }
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    private fun getCard(player: TTAPlayer, cardId: String): TTACard {
        return when (actionString) {
            TTACmdString.ACTION_TAKE_CARD -> gameMode.cardBoard.getCard(cardId)
            TTACmdString.ACTION_PLAY_CARD -> player.getCard(cardId)
            else -> player.getPlayedCard(cardId, true)
        }
    }
}
