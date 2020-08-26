package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam


/**
 * #49-导弹嫉妒的监听器

 * @author F14eagle
 */
class Custom49Listener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {
    private var selectedCard: TSCard? = null

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        res["card"] = selectedCard
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val cardId = action.getAsString("cardId").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择要交换的卡牌!")
        val card = player.getCard(cardId)
        if (player.getOp(card) < player.maxOpValue) {
            throw BoardGameException("你只能选择手牌中OP点数最大的牌!")
        }
        this.selectedCard = card
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }


    override val actionString: String
        get() = TSCmdString.ACTION_SELECT_CARD

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_49

}
