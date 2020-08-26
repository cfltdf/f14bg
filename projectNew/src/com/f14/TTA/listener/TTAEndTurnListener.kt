package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.consts.RoundStep
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam


/**
 * 回合结束的监听器

 * @author 吹风奈奈
 */
class TTAEndTurnListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer) : TTAInterruptListener(gameMode, trigPlayer) {
    private var confirm = true

    init {
        this.addListeningPlayer(trigPlayer)
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["confirm"] = this.confirm
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val confirm = action.getAsBoolean("confirm")
        val player = action.getPlayer<TTAPlayer>()
        this.confirm = confirm
        this.setPlayerResponsed(player)
    }


    override fun getMsg(player: TTAPlayer): String {
        val msg = mutableListOf("你是否确定要结束行动?")
        if (player.availableCivilAction > 0) {
            msg.add("你还有未使用的内政行动点")
        }
        if (player.isUprising) {
            msg.add("你的人民生活在水深火热中")
        }
        if (player.isWarTarget) {
            msg.add("你的文明正受到战火的侵扰")
        }
        for (c in player.getActiveCards(RoundStep.NORMAL)) {
            msg.add("你还能够使用${c.reportString}")
        }
        return msg.reversed().joinToString(",")
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_BUILD

    fun needResponse(): Boolean {
        val p = this.trigPlayer
        return p.availableCivilAction > 0 || p.isUprising || p.isWarTarget || !p.getActiveCards(RoundStep.NORMAL).isEmpty()
    }

}
