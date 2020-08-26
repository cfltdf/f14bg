package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.BgUtils
import com.f14.utils.CommonUtil

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class DiscardMilitaryLimitListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer) : TTAInterruptListener(gameMode, trigPlayer) {
    private var discards: List<TTACard>? = null

    init {
        this.addListeningPlayer(trigPlayer)
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["discards"] = discards
        return param
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 将军事牌作为参数传递到客户端
        res.private("multiSelection", true)
        res.private("cardIds", BgUtils.card2String(player.militaryHands.cards))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val cardIds = action.getAsString("cardIds")
        discards = player.militaryHands.getCards(cardIds)
        this.setPlayerResponsed(player)
    }

    override fun getMsg(player: TTAPlayer): String {
        val num = player.militaryHands.size - player.militaryHandLimit
        var msg = "你需要弃掉{0}张军事牌!"
        msg = CommonUtil.getMsg(msg, num)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_DISCARD_MILITARY

}
