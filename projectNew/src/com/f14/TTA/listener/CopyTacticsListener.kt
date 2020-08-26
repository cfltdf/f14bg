package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.BgUtils
import com.f14.bg.utils.CheckUtils

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class CopyTacticsListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer) : TTAInterruptListener(gameMode, trigPlayer) {

    private var card: TTACard? = null

    init {
        this.addListeningPlayer(trigPlayer)
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["card"] = card
        return param
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 传递当前公共阵型库
        res.private("multiSelection", false)
        res.private("cardIds", BgUtils.card2String(gameMode.cardBoard.publicTacticsDeck.cards))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val confirm = action.getAsBoolean("confirm")
        val player = action.getPlayer<TTAPlayer>()
        if (confirm) {
            val cardIds = action.getAsString("cardIds")
            val cards = gameMode.cardBoard.publicTacticsDeck.getCards(cardIds)
            CheckUtils.check(cards.size != 1, "没有找到指定阵型!")
            val card = cards[0]
            CheckUtils.check(card === player.tactics, "你正在使用这个阵型!")
            this.card = card
        }
        this.setPlayerResponsed(player)
    }


    override fun getMsg(player: TTAPlayer): String {
        return "请选择需要学习的阵型"
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_COPY_TACTICS
}
