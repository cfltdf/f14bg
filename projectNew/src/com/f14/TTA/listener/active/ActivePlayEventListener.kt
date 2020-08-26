package com.f14.TTA.listener.active

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTAActiveCardListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils


/**
 * 直接打出Event卡牌的监听器(哥伦布直接殖民一类的能力)

 * @author F14eagle
 */
class ActivePlayEventListener(param: RoundParam, card: TTACard) : TTAActiveCardListener(param, card) {


    private var cardId: String? = null


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["cardId"] = cardId
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val confirm = action.getAsBoolean("confirm")
        val player = action.getPlayer<TTAPlayer>()
        if (confirm) {
            // 检查选择的卡牌是否可以应用到该能力
            val cardId = action.getAsString("cardId")
            val card = player.getCard(cardId)
            CheckUtils.check(!activeCard.activeAbility!!.test(card), "该能力不能在这张牌上使用!")

            this.cardId = cardId
        }
        this.setPlayerResponsed(player)
    }


    override val actionString: String
        get() = TTACmdString.ACTION_PLAY_CARD


    override fun getMsg(player: TTAPlayer): String {
        return "请选择一张的事件牌!"
    }
}
