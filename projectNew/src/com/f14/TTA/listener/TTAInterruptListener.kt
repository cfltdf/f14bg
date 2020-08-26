package com.f14.TTA.listener

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.TTA.TTAConfig
import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.TTAReport
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ActionListener
import com.f14.bg.listener.ListenerType

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

abstract class TTAInterruptListener(val gameMode: TTAGameMode, protected var trigPlayer: TTAPlayer) : ActionListener<TTAPlayer, TTAConfig, TTAReport>(gameMode, ListenerType.INTERRUPT) {

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        res.private("msg", this.getMsg(player))
        // 设置行动字符串
        res.private("actionString", this.actionString)
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
    protected open fun getMsg(player: TTAPlayer) = ""

    /**
     * 刷新玩家的当前提示信息
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun refreshMsg(player: TTAPlayer) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_REFRESH_MSG, player.position).private("msg", this.getMsg(player)).send(gameMode.game, player)
    }

    override fun setListenerInfo(res: BgResponse) = // 设置触发玩家的位置参数
            super.setListenerInfo(res).public("trigPlayerPosition", this.trigPlayer.position)

}
