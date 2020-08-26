package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.Region
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam


/**
 * #94-切尔诺贝利的监听器

 * @author F14eagle
 */
class Custom94Listener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {
    private var region: Region? = null

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        res["region"] = region
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val region = action.getAsString("region")
        val reg: Region
        try {
            reg = Region.valueOf(region)
        } catch (e: Exception) {
            throw BoardGameException("请选择区域!")
        }

        this.region = reg
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_94
}
