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
class Custom108Listener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam, cards: List<TSCard>) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {
    private val selectedCards: MutableList<TSCard> = ArrayList()
    private val drawnCards = TSCardDeck()

    init {
        this.drawnCards.addCards(cards)
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) = Unit

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        res["cards"] = selectedCards
        return res
    }

    override fun createStartListenCommand(player: TSPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 将抽出的牌的信息发送给用户
        res.public("cardIds", BgUtils.card2String(drawnCards.cards))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val cardIds = action.getAsString("cardIds").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择要弃掉的牌!")
        // 将选择的牌加入弃牌堆
        selectedCards.addAll(this.drawnCards.getCards(cardIds))
        // 设置玩家行动完成
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) = Unit

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_108
}
