package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.BgUtils


/**
 * #98-阿尔德里希·阿姆斯的监听器

 * @author F14eagle
 */
class Custom98Listener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {
    private var selectedCard: TSCard? = null

    override fun cannotPass(action: GameAction): Boolean {
        // 如果美国玩家没有手牌,则可以放弃
        return !gameMode.game.usaPlayer.hands.empty && super.cannotPass(action)
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
        // 设置美国玩家的手牌信息
        val usa = gameMode.game.usaPlayer
        res.public("cardIds", BgUtils.card2String(usa.hands.cards))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val cardId = action.getAsString("cardId").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择要弃掉的牌!")
        // 从美国玩家手中移除选中的牌
        val usa = gameMode.game.usaPlayer
        this.selectedCard = usa.getCard(cardId)
        // 设置玩家行动完成
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_98
}
