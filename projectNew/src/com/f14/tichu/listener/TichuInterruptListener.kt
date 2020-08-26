package com.f14.tichu.listener

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.tichu.TichuGameMode
import com.f14.tichu.TichuPlayer


/**
 * Tichu的中断监听器(所有玩家同时执行)
 * @author F14eagle
 */
abstract class TichuInterruptListener(gameMode: TichuGameMode, protected var trigPlayer: TichuPlayer) : TichuActionListener(gameMode) {

    override fun createInterruptParam() = super.createInterruptParam().also {
        it["validCode"] = this.validCode
        it["player"] = this.trigPlayer
    }

    override fun createStartListenCommand(player: TichuPlayer): BgResponse {
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
    protected open fun getMsg(player: TichuPlayer): String {
        return ""
    }

    /**
     * 刷新玩家的当前提示信息

     * @param player

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun refreshMsg(player: TichuPlayer) {
        val res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_REFRESH_MSG, player.position)
        res.public("msg", this.getMsg(player))
        gameMode.game.sendResponse(player, res)
    }

    override fun setListenerInfo(res: BgResponse): BgResponse {
        return super.setListenerInfo(res)
                // 设置触发玩家的位置参数
                .public("trigPlayerPosition", this.trigPlayer.position)
    }

}
