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


/**
 * 选择失去殖民地的事件

 * @author F14eagle
 */
class ChooseColonyListener(gameMode: TTAGameMode, eventAbility: EventAbility, trigPlayer: TTAPlayer) : TTAEventListener(gameMode, eventAbility, trigPlayer) {

    private var card: TTACard? = null

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 如果玩家可以结束回合,则无需玩家回应
        return this.cannotPass(player)
    }

    /**
     * 判断玩家是否可以结束操作

     * @param player

     * @return
     */
    private fun cannotPass(player: TTAPlayer): Boolean {
        var count = 0
        // 如果玩家拥有事件能力适用的牌,则不能结束
        for (c in player.buildings.cards) {
            if (this.eventAbility.test(c)) {
                count += 1
                if (count == 1) {
                    this.card = c
                }
            }
        }
        return if (count > 1) {
            this.card = null
            true
        } else {
            false
        }
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        if (card != null) {
            param["card"] = card
        }
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val confirm = action.getAsBoolean("confirm")
        if (confirm) {
            val cardId = action.getAsString("cardId")
            val card = player.buildings.getCard(cardId)
            CheckUtils.check(!this.eventAbility.test(card), "该事件不能选择这张牌!")
            this.card = card
            this.setPlayerResponsed(player)
        } else {
            // 判断玩家是否可以结束
            CheckUtils.check(this.cannotPass(player), this.getMsg(player))
            this.setPlayerResponsed(player)
        }
    }


    override val actionString: String
        get() = TTACmdString.ACTION_CHOOSE_COLONY


    override fun getMsg(player: TTAPlayer): String {
        return "你失去1个殖民地,请选择!"
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_COLONY
}
