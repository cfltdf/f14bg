package com.f14.TTA.listener

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.TTA.TTAConfig
import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.TTAReport
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ListenerType
import com.f14.bg.listener.OrderActionListener

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

abstract class TTAOrderInterruptListener(var gameMode: TTAGameMode, protected var trigPlayer: TTAPlayer) : OrderActionListener<TTAPlayer, TTAConfig, TTAReport>(gameMode, ListenerType.INTERRUPT) {

    override fun createStartListenCommand(player: TTAPlayer): BgResponse = super.createStartListenCommand(player).public("msg", this.getMsg(player)).public("actionString", this.actionString) // 设置行动字符串

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
    protected open fun getMsg(player: TTAPlayer): String = ""


    override val playersByOrder: List<TTAPlayer>
        get() = gameMode.game.getPlayersByOrder(this.trigPlayer)

    /**
     * 刷新玩家的当前提示信息
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun refreshMsg(player: TTAPlayer) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_REFRESH_MSG, player.position).public("msg", this.getMsg(player)).send(gameMode, player)
    }

    override fun setListenerInfo(res: BgResponse): BgResponse = // 设置触发玩家的位置参数
            super.setListenerInfo(res).public("trigPlayerPosition", this.trigPlayer.position)

}
