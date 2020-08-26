package com.f14.TTA.listener.event

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAEventListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class TakeCardListener(gameMode: TTAGameMode, ability: EventAbility, trigPlayer: TTAPlayer, private var amount: Int) : TTAEventListener(gameMode, ability, trigPlayer) {
    private var card: TTACard? = null
    private var index: Int = 0
    private var actionCost: Int = 0

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
    }


    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        res["card"] = card
        res["index"] = index
        res["actionCost"] = actionCost
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val confirm = action.getAsBoolean("confirm")
        val player = action.getPlayer<TTAPlayer>()
        if (confirm) {
            val cb = gameMode.cardBoard
            // 玩家从卡牌序列中拿牌
            val cardId = action.getAsString("cardId")
            val actionCost = cb.getCost(cardId, player)
            // 检查玩家是否有足够的内政行动点
            CheckUtils.check(actionCost > amount, "内政行动点不够,你还能使用 $amount 个内政行动点!")
            val card = cb.getCard(cardId)
            // 检查玩家是否可以拿牌
            player.checkTakeCard(card)
            this.index = cb.getCardIndex(cardId)
            this.card = card
            this.actionCost = actionCost
        }
        this.setPlayerResponsed(player)
    }


    override val actionString: String
        get() = TTACmdString.ACTION_TAKE_CARD

    override fun getMsg(player: TTAPlayer): String {
        var msg = "你还有{0}个内政行动点用来拿牌,请选择!"
        msg = CommonUtil.getMsg(msg, amount)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_TAKE_CARD

}
