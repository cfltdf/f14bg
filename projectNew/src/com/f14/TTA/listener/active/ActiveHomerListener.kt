package com.f14.TTA.listener.active

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAActiveCardListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class ActiveHomerListener(param: RoundParam, card: TTACard) : TTAActiveCardListener(param, card) {
    private var card: TTACard? = null

    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["card"] = card
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val confirm = action.getAsBoolean("confirm")
        val player = action.getPlayer<TTAPlayer>()
        if (confirm) {
            val cardId = action.getAsString("cardId")
            val card = player.getPlayedCard(cardId)
            CheckUtils.check(!activeCard.activeAbility!!.test(card), "不能选择这张牌!")

            this.card = card
        } else {
        }
        this.setPlayerResponsed(player)
    }


    override val actionString: String
        get() = TTACmdString.ACTION_CHOOSE_WONDER


    override fun getMsg(player: TTAPlayer): String {
        return "你可以选择一个建成的奇迹，把${this.activeCard.name}放在那张奇迹下方,请选择!"
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_FLIP_WONDER
}