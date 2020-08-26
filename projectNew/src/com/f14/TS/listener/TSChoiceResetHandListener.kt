package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSGameCmd
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam


/**
 * 选择国家并执行相应行动的监听器

 * @author F14eagle
 */
class TSChoiceResetHandListener(gameMode: TSGameMode, trigPlayer: TSPlayer) : TSInterruptListener(gameMode, trigPlayer) {
    private var choice: Int = 0

    init {
        this.addListeningPlayer(trigPlayer)
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["choice"] = this.choice
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val confirmString = action.getAsString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val player = action.getPlayer<TSPlayer>()
            val choice = action.getAsInt("choice")
            // 暂时只能接受1或2选项
            if (choice != 1 && choice != 2) {
                throw BoardGameException("请选择行动!")
            }
            this.choice = choice
            this.setPlayerResponsed(player)
        }
    }

    /**
     * 取得提示文本

     * @param player

     * @return
     */

    override fun getMsg(player: TSPlayer): String {
        return "你摸了3张计分牌，你可以选择重新发牌!"
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_CHOICE

    /**
     * 发送选项参数

     * @param gameMode

     * @param p
     */
    private fun sendChoiceParamInfo(p: TSPlayer) {
        val res = this.createSubactResponse(p, "choiceParam")
        // TSCard card = this.getCard();
        res.public("choice1", "重新发牌")
        res.public("choice2", "就这样吧")
        gameMode.game.sendResponse(p, res)
    }

    override fun sendStartListenCommand(player: TSPlayer, receiver: TSPlayer?) {
        super.sendStartListenCommand(player, receiver)
        // 只会向指定自己发送该监听信息
        this.sendChoiceParamInfo(player)
    }

}
