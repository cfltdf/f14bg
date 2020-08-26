package com.f14.TS.listener

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ListenerType


/**
 * TS的中断监听器(所有玩家同时执行)

 * @author F14eagle
 */
abstract class TSInterruptListener(gameMode: TSGameMode, protected var trigPlayer: TSPlayer) : TSActionListener(gameMode, ListenerType.INTERRUPT) {

    override fun createStartListenCommand(player: TSPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        res.public("msg", this.getMsg(player))
        // 设置行动字符串
        res.public("actionString", this.actionString)
        return res
    }

    /**
     * 取得选择行动字符串

     * @return
     */
    protected open val actionString: String
        get() = ""

    /**
     * 取得提示文本

     * @param player

     * @return
     */
    protected open fun getMsg(player: TSPlayer): String {
        return ""
    }

    /**
     * 刷新玩家的当前提示信息

     * @param player

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun refreshMsg(player: TSPlayer) {
        val res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_REFRESH_MSG, player.position)
        res.public("msg", this.getMsg(player))
        gameMode.game.sendResponse(player, res)
    }

    override fun setListenerInfo(res: BgResponse) = super.setListenerInfo(res)
            // 设置触发玩家的位置参数
            .public("trigPlayerPosition", this.trigPlayer.position)

}
