package com.f14.bg.listener

import com.f14.bg.BoardGameConfig
import com.f14.bg.GameMode
import com.f14.bg.exception.BoardGameException
import com.f14.bg.player.Player
import com.f14.bg.report.BgReport
import org.apache.log4j.Logger

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

class ListenerHandler<P : Player, C : BoardGameConfig, R : BgReport<P>>(val gameMode: GameMode<P, C, R>, var listener: ActionListener<P, C, R>) {

    /**
     * 中止线程
     */
    fun done() {
        this.listener.close()
    }


    /**
     * 开始监听
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    suspend fun run() {
        for (action in gameMode.receiver) {
            try {
                listener.execute(action)
            } catch (e: BoardGameException) {
                // 当发生异常时,将该异常信息发送到指令的所属玩家
                log.warn(e, e)
                action.getPlayer<P>().sendException(gameMode.game.room.id, e)
            } catch (e: Exception) {
                log.error("游戏过程中发生系统错误: " + e.message, e)
                action.getPlayer<P>().sendException(gameMode.game.room.id, e)
            }
            if (listener.closed) break
        }
    }

    companion object {
        private var log = Logger.getLogger(ListenerHandler::class.java)!!
    }
}
