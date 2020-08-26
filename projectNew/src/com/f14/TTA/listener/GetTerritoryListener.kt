package com.f14.TTA.listener

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils

class GetTerritoryListener(param: RoundParam, val ability: CivilCardAbility) : TTAInterruptListener(param.gameMode, param.player) {
    private var cardId: String? = null

    init {
        this.addListeningPlayer(param.player)
    }

    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["cardId"] = cardId
        return param
    }

    override fun getMsg(player: TTAPlayer): String {
        return "请选择一张的事件牌!"
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_ACTIVABLE_CARD

    override val actionString: String
        get() = TTACmdString.ACTION_PLAY_CARD

    override fun doAction(action: BgAction.GameAction) {
        val confirm = action.getAsBoolean("confirm")
        val player = action.getPlayer<TTAPlayer>()
        if (confirm) {
            // 检查选择的卡牌是否可以应用到该能力
            val cardId = action.getAsString("cardId")
            val card = player.getCard(cardId)
            CheckUtils.check(!ability.test(card), "该能力不能在这张牌上使用!")

            this.cardId = cardId
        }
        this.setPlayerResponsed(player)
    }

}
