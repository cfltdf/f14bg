package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException


/**
 * 回合结束时弃牌的监听器

 * @author F14eagle
 */
class TSRoundDiscardListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    override fun cannotPass(action: GameAction): Boolean {
        return false
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val cardId = action.getAsString("cardId").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择要弃掉的牌!")
        val card = player.getCard(cardId)

        gameMode.game.playerRemoveHand(player, card)
        gameMode.report.playerDiscardCard(player, card)
        gameMode.game.discardCard(card)

        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }


    override val actionString: String
        get() = TSCmdString.ACTION_SELECT_CARD

    override fun getMsg(player: TSPlayer): String {
        return "你可以弃掉一张手牌!"
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_ROUND_DISCARD

}
