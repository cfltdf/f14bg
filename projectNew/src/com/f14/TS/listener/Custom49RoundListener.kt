package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam


/**
 * 导弹嫉妒的执行监听器

 * @author F14eagle
 */
class Custom49RoundListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {


    private var subact: String? = null

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        res["subact"] = subact
        return res
    }

    override fun createStartListenCommand(player: TSPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        res.public("spaceRaceChance", player.availableSpaceRaceTimes)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {

    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {
        when (val subact = action.getAsString("subact")) {
            TSCmdString.ACTION_USE_OP -> // 执行OP
                this.subact = subact
            TSCmdString.ACTION_SPACE_RACE -> // 太空竞赛
                this.subact = subact
            else -> throw BoardGameException("无效的行动指令!")
        }
        this.setPlayerResponsed(action.getPlayer<TSPlayer>())
    }

    override fun getMsg(player: TSPlayer): String {
        return "你必须以行动方式打出#49-导弹嫉妒!"
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_49_ROUND
}
