package com.f14.bg

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.bg.exception.BoardGameException
import com.f14.f14bgdb.F14bgdb
import com.f14.f14bgdb.service.BgInstanceManager
import org.apache.log4j.Logger

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */
abstract class GameEndPhase<GM : GameMode<*, *, *>> {
    /**
     * 创建游戏结果对象
     * @return
     */
    protected abstract fun createVPResult(gameMode: GM): VPResult

    @Throws(BoardGameException::class)
    fun execute(gameMode: GM) {
        if (gameMode.game.room.isMatchMode){
            gameMode.game.players.forEach { it.name = it.user.name + it.name }
        }
        gameMode.report.end()
        val result = this.createVPResult(gameMode)
        // 将结果进行排名
        result.sort()
        // 保存游戏结果
        F14bgdb.getBean<BgInstanceManager>("bgInstanceManager").saveGameResult(result)
        // 记录游戏得分情况
        gameMode.report.result(result)
        // 保存游戏战报(实在太大还是不保存了...)
        // bm.saveGameReport(o, mode.getReport().toJSONString());
        // mode.getReport().print();
        // 发送游戏结果到客户端
        this.sendGameResult(gameMode, result)
        this.sendGameReport(gameMode)
    }

    /**
     * 发送完整战报
     * @param gameMode
     */
    protected fun sendGameReport(gameMode: GM) {
        gameMode.game.room.setReport()
        gameMode.game.room.sendReport()
    }

    /**
     * 将游戏结果发送到客户端
     * @param result
     */
    protected fun sendGameResult(gameMode: GM, result: VPResult) = CmdFactory.createGameResponse(CmdConst.GAME_CODE_VP_BOARD, -1)
            .public("vps", result.toMap())
            .send(gameMode.game)

    companion object {
        protected val log = Logger.getLogger(GameEndPhase::class.java)!!
    }

}
