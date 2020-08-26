package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import java.util.*

class TTARequestSelectListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, showCard: TTACard, actionString: String, private var sels: String, msg: String) : TTAActionRequestListener(gameMode, trigPlayer, showCard, actionString, msg) {
    private var sel: Int = 0


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["sel"] = sel
        return param
    }


    override fun createParam(): MutableMap<String, Any> {
        val param = HashMap<String, Any>()
        param["sel"] = sels
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val sel = action.getAsInt("sel")
        val player = action.getPlayer<TTAPlayer>()
        this.sel = sel
        this.setPlayerResponsed(player)

    }

}
