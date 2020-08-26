package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.SuperPower
import com.f14.TS.factory.InitParamFactory
import com.f14.TS.listener.Custom45Listener
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.TS.utils.TSRoll
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

/**
 * #45-高峰会议 的执行器

 * @author F14eagle
 */
class Custom45Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val ussr = gameMode.game.ussrPlayer
        val usa = gameMode.game.usaPlayer

        // 双方玩家掷骰,并加上每个支配或掌控的区域数为修正
        val russr = TSRoll.roll()
        val mussr = gameMode.scoreManager.getDominationNumber(SuperPower.USSR)
        val rusa = TSRoll.roll()
        val musa = gameMode.scoreManager.getDominationNumber(SuperPower.USA)

        // 输出日志
        gameMode.report.playerRoll(ussr, russr, mussr)
        gameMode.report.playerRoll(usa, rusa, musa)

        val tussr = russr + mussr
        val tusa = rusa + musa

        // 最终点数大的玩家可以得到2VP,并且可以任意移动DEFCON 1格
        val winner: TSPlayer
        winner = when {
            tussr > tusa -> {
                gameMode.game.adjustVp(ussr, 2)
                ussr
            }
            tusa > tussr -> {
                gameMode.game.adjustVp(usa, 2)
                usa
            }
            else -> return
        }
        val ip = InitParamFactory.createActionInitParam(winner, card, this.initParam.trigType)
        ip.msg = this.initParam.msg
        ip.isCanPass = (this.initParam.isCanPass)
        val l = Custom45Listener(winner, gameMode, ip)
        val res = gameMode.insertListener(l)
        val confirmString = res.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val value = res.getInteger("value")
            gameMode.game.adjustDefcon(value)
        }
    }

}
