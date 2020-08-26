package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam


/**
 * #45-高峰会议的监听器

 * @author F14eagle
 */
class Custom45Listener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {
    private var value: Int = 0

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        res["value"] = value
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val adjustType = action.getAsString("adjustType").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择调整方式!")
        val value: Int
        value = when (adjustType) {
            "increase" -> 1
            "decrease" -> -1
            else -> throw BoardGameException("无效的调整方式!")
        }
        this.value = value
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_45
}
