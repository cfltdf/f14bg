package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils


class GetAndPlayCardListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, private var ability: CivilCardAbility) : TTAInterruptListener(gameMode, trigPlayer) {

    var cardId: String? = null

    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["cardId"] = cardId
        return param
    }

    override fun beforeListeningCheck(player: TTAPlayer) = player.civilHands.size < player.civilHandLimit

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val confirm = action.getAsBoolean("confirm")
        val player = action.getPlayer<TTAPlayer>()
        if (confirm) {
            // 检查选择的卡牌是否可以应用到该能力
            val cardId = action.getAsString("cardId")
            val cb = gameMode.cardBoard
            val card = cb.getCard(cardId)
            CheckUtils.check(!ability.test(card), "该能力不能在这张牌上使用!")
            player.checkTakeCard(card)

            this.cardId = cardId
        }
        this.setPlayerResponsed(player)
    }


    override val actionString: String
        get() = TTACmdString.ACTION_TAKE_CARD


    override fun getMsg(player: TTAPlayer): String {
        return "你可以拿取一张印刷术并用2科技点数打出!"
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_TAKE_CARD
}
