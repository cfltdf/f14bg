package com.f14.TTA.listener.event

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.WonderCard
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAEventListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil


/**
 * 选择失去奇迹的事件

 * @author F14eagle
 */
class FlipWonderListener(gameMode: TTAGameMode, eventAbility: EventAbility, trigPlayer: TTAPlayer) : TTAEventListener(gameMode, eventAbility, trigPlayer) {

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 如果玩家可以结束回合,则无需玩家回应
        return this.cannotPass(player)
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有玩家创建选择参数
        for (player in gameMode.game.players) {
            val param = ChoiceParam()
            this.setParam(player.position, param)
        }
    }

    /**
     * 判断玩家是否可以结束操作

     * @param player

     * @return
     */
    private fun cannotPass(player: TTAPlayer): Boolean {
        // 如果玩家拥有事件能力适用的牌,则不能结束
        var count = 0
        val param = this.getParam<ChoiceParam>(player)
        // 如果玩家拥有事件能力适用的牌,则不能结束
        for (c in player.buildings.cards) {
            if (this.eventAbility.test(c)) {
                count += 1
                if (count == 1) {
                    param.wonder = c as WonderCard
                }
            }
        }
        return if (count > 1) {
            param.wonder = null
            true
        } else {
            false
        }
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        for (p in this.listeningPlayers) {
            val cp = this.getParam<ChoiceParam>(p)
            param[p.position] = cp.wonder
        }
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<ChoiceParam>(player)
        val confirm = action.getAsBoolean("confirm")
        if (confirm) {
            val cardId = action.getAsString("cardId")
            val card = player.buildings.getCard(cardId)
            CheckUtils.check(!this.eventAbility.test(card), "该事件不能选择这张牌!")
            param.wonder = card as WonderCard
            // 设置玩家已回应
            this.setPlayerResponsed(player)
        } else {
            // 判断玩家是否可以结束
            CheckUtils.check(this.cannotPass(player), this.getMsg(player))
            this.setPlayerResponsed(player)
        }
    }


    override val actionString: String
        get() = TTACmdString.ACTION_CHOOSE_WONDER

    override fun getMsg(player: TTAPlayer): String {
        var msg = "你失去1个{0}的效果,效果变成+2文化指数,请选择!"
        msg = CommonUtil.getMsg(msg, this.eventAbility.descr)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_FLIP_WONDER

    internal inner class ChoiceParam {
        var wonder: WonderCard? = null
    }
}
