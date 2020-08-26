package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.BgUtils

class TTAChooseEventListener(gameMode: TTAGameMode, player: TTAPlayer, val events: List<TTACard>) : TTAInterruptListener(gameMode, player) {
    var selected: TTACard? = null

    init {
        this.addListeningPlayer(trigPlayer)
    }

    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["selected"] = selected
        return param
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 将军事牌作为参数传递到客户端
        res.private("multiSelection", false)
        res.private("cardIds", BgUtils.card2String(events))
        return res
    }

    override fun getMsg(player: TTAPlayer): String {
        return "请选择要发生的事件!"
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_DISCARD_MILITARY

    override fun doAction(action: BgAction.GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val cardIds = action.getAsString("cardIds")
        selected = events.singleOrNull { c -> c.id == cardIds }
        if (selected == null) {
            throw BoardGameException("必须选择要发生的事件!")
        }
        this.setPlayerResponsed(player)
    }

}
