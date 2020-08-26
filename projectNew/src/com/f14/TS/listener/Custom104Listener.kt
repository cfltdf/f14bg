package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.component.TSCardDeck
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.BgUtils


/**
 * #104-剑桥五杰的监听器

 * @author F14eagle
 */
class Custom104Listener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    private var drawnCards = TSCardDeck()

    private var selectedCard: TSCard? = null

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 检查美国玩家手中的计分牌,苏联玩家只能从这些计分牌区域中选择国家
        val player = gameMode.game.usaPlayer
        this.drawnCards.addCards(player.scoreCards)
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        res["card"] = selectedCard
        return res
    }

    override fun createStartListenCommand(player: TSPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 将抽出的牌的信息发送给用户
        res.public("cardIds", BgUtils.card2String(this.drawnCards.cards))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val cardId = action.getAsString("cardId").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择计分牌!")

        this.selectedCard = this.drawnCards.getCard(cardId)
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }


    override fun getMsg(player: TSPlayer): String {
        return "请选择一张计分牌,你可以在该区域添加一点影响力!"
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_104

}
