package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction
import com.f14.bg.action.BgResponse
import com.f14.bg.utils.BgUtils

class ViewCardListener(gameMode: TTAGameMode, player: TTAPlayer, val cards: MutableList<TTACard>) : TTAInterruptListener(gameMode, player) {
    init {
        this.addListeningPlayer(trigPlayer)
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_DISCARD_MILITARY

    override fun doAction(action: BgAction.GameAction) {
        this.setPlayerResponsed(action.getPlayer<TTAPlayer>())
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)

        // 将军事牌作为参数传递到客户端
        res.private("cardIds", BgUtils.card2String(cards))
        return res
    }

}
