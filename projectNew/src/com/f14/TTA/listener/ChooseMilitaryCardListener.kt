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
import com.f14.bg.utils.CheckUtils

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class ChooseMilitaryCardListener(
        gamMode: TTAGameMode,
        trigPlayer: TTAPlayer,
        private var cards: List<TTACard>,
        private var index: Int
) : TTAInterruptListener(gamMode, trigPlayer) {
    private var cardId: String? = null

    init {
        this.addListeningPlayer(trigPlayer)
    }


    override fun createInterruptParam(): InterruptParam {
        return super.createInterruptParam()
                .apply { this["cardId"] = cardId }
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        return super.createStartListenCommand(player) // 将军事牌作为参数传递到客户端
                .private("multiSelection", false)
                .private("cardIds", BgUtils.card2String(cards))

    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        cardId = action.getAsString("cardIds")
        CheckUtils.check(cardId.isNullOrEmpty(), "请选择1张牌!")
        this.setPlayerResponsed(player)
    }


    override fun getMsg(player: TTAPlayer) = "请选择第 $index 张军事牌!"

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_DISCARD_MILITARY
}
