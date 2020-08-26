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
 * #108-我们在伊朗有人的监听器

 * @author F14eagle
 */
class TakeCardListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam, cards: Collection<TSCard>) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    private var selectedCard: TSCard? = null

    private var drawnCards = TSCardDeck()

    init {
        this.drawnCards.addCards(cards)
    }

    override fun cannotPass(action: GameAction): Boolean {
        return false
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
        res.public("msg", "请选择要获取的牌!")
        res.public("cardIds", BgUtils.card2String(drawnCards.cards))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val cardId = action.getAsString("cardId").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择要获取的牌!")
        // 从美国玩家手中移除选中的牌
        this.selectedCard = drawnCards.getCard(cardId)
        // 设置玩家行动完成
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_TAKE_CARD
}
