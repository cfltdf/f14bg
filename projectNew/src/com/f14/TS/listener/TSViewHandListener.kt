package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils


/**
 * 查看对方手牌的监听器

 * @author F14eagle
 */
class TSViewHandListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        // 直接结束
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }

    val actionInitParam
        get() = initParam as ActionInitParam

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_VIEW_HAND

    /**
     * 发送手牌信息参数

     * @param gameMode

     * @param p
     */
    private fun sendHandParamInfo(p: TSPlayer) {
        val res = this.createSubactResponse(p, "handParam")
        val target = gameMode.game.getPlayer(this.actionInitParam.targetPower)!!
        res.public("cardIds", BgUtils.card2String(target.hands.cards))
        gameMode.game.sendResponse(p, res)
    }

    override fun sendStartListenCommand(player: TSPlayer, receiver: TSPlayer?) {
        super.sendStartListenCommand(player, receiver)
        // 只会向指定自己发送该监听信息
        this.sendHandParamInfo(player)
    }

}
