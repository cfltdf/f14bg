package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.ChoiceInitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam


/**
 * 选择国家并执行相应行动的监听器

 * @author F14eagle
 */
class TSChoiceRecoupListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ChoiceInitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        for (player in gameMode.game.players) {
            val param = ChoiceParam(player)
            this.setParam(player, param)
        }
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        // 将玩家实际用掉的点数设置在返回参数中
        val player = this.listeningPlayer
        val param = this.getParam<ChoiceParam>(player)
        res["choice"] = param.choice
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val choice = action.getAsInt("choice")
        // 暂时只能接受1或2选项
        if (choice != 1 && choice != 2) {
            throw BoardGameException("请选择行动!")
        }
        val param = this.getParam<ChoiceParam>(player)
        param.choice = choice
        // 结束选择
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }

    val choiceInitParam: ChoiceInitParam
        get() = super.initParam as ChoiceInitParam

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_CHOICE

    /**
     * 发送选项参数

     * @param gameMode

     * @param p
     */
    private fun sendChoiceParamInfo(p: TSPlayer) {
        val res = this.createSubactResponse(p, "choiceParam")
        res.public("choice1", "重新投掷")
        res.public("choice2", "不用重投")
        gameMode.game.sendResponse(p, res)
    }

    override fun sendStartListenCommand(player: TSPlayer, receiver: TSPlayer?) {
        super.sendStartListenCommand(player, receiver)
        // 只会向指定自己发送该监听信息
        this.sendChoiceParamInfo(player)
    }

    // @Override
    // protected void onInterrupteListenerOver(TSGameMode mode,
    // InterruptParam param) throws BoardGameException {
    // super.onInterrupteListenerOver(mode, param);
    // //如果中断处理完成,则设置所有玩家已回应
    // if(!this.isInterruped()){
    // this.setAllPlayerResponsed(mode);
    // }
    // }

    /**
     * 选择参数

     * @author F14eagle
     */
    internal inner class ChoiceParam(var player: TSPlayer) {
        var choice: Int = 0
    }

}
