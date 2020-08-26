package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.CardType
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.consts.TrigType
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException


/**
 * 出计分牌的监听器

 * @author F14eagle
 */
class TSScoreCardListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    override fun cannotPass(action: GameAction): Boolean {
        val player = action.getPlayer<TSPlayer>()
        // 如果玩家没有计分牌,则允许跳过
        return player.hasScoreCard()
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {

    }

    /**
     * 打出记分牌事件

     * @param gameMode

     * @param action

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doScoreEvent(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val cardId = action.getAsString("cardId").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择计分牌!")
        val card = player.getCard(cardId)
        if (card.cardType != CardType.SCORING) {
            throw BoardGameException("选择的牌不是计分牌!")
        }
        // 输出战报信息
        val type = TrigType.EVENT
        gameMode.game.playerPlayCard(player, card)
        gameMode.report.playerPlayCard(player, card, type)
        // 触发前置事件
        gameMode.game.onPlayerAction(player, type, card)
        gameMode.game.activeCardEvent(player, card)

        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {
        val subact = action.getAsString("subact")
        if ("SCORE" == subact) {
            // 打出记分牌
            this.doScoreEvent(action)
        } else {
            throw BoardGameException("无效的行动指令!")
        }
    }


    override val actionString: String
        get() = TSCmdString.ACTION_SELECT_CARD

    private val actionInitParam: ActionInitParam
        get() = super.initParam as ActionInitParam


    override fun getMsg(player: TSPlayer): String {
        return "你必须打出计分牌!"
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_PLAY_SCORE_CARD

}
