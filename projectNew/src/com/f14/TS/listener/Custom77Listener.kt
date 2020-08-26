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
 * #77-“不要问你的祖国能为你做什么……”的监听器

 * @author F14eagle
 */
class Custom77Listener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    private var cards: List<TSCard>? = null

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        res["cards"] = cards
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val cardIds = action.getAsString("cardIds").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择要弃掉的牌!")
        cards = player.hands.getCards(cardIds)
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }

    override val actionString: String
        get() = TSCmdString.ACTION_SELECT_CARD_MULTI

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_77

}
