package com.f14.TTA.listener.event

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.executor.TTAActionExecutor
import com.f14.TTA.executor.TTAUpgradeExecutor
import com.f14.TTA.listener.TTAEventListener
import com.f14.bg.action.BgAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import java.util.*

class FreeMigrationListener(gameMode: TTAGameMode, ability: EventAbility, trigPlayer: TTAPlayer) : TTAEventListener(gameMode, ability, trigPlayer) {
    private val fromCards = HashMap<TTAPlayer, TechCard?>()

    private val choices = HashMap<TTAPlayer, TTAActionExecutor?>()

    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        for (player in this.listeningPlayers) {
            val executor = choices.getOrDefault(player, null)
            param[player.position] = executor
        }
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: BgAction.GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val fromCard = fromCards[player]
        val confirm = action.getAsBoolean("confirm")
        if (confirm) {
            val subact = action.getAsString("subact")
            if (TTACmdString.ACTION_UPGRADE == subact) {
                val cardId = action.getAsString("cardId")
                val card = player.getPlayedCard(cardId) as TechCard
                if (fromCard == null) {
                    fromCards[player] = card
                    this.refreshMsg(player)
                } else {
                    val param = this.getParam<RoundParam>(player.position)
                    val executor = TTAUpgradeExecutor(param, fromCard, card)
                    executor.actionCost = 0
                    executor.checkType = false
                    executor.check()
                    choices[player] = executor
                    this.setPlayerResponsed(player)
                }
            } else if ("cancel" == subact) {
                // Do nothing
            } else {
                throw BoardGameException("不能执行此操作!")
            }
        } else {
            if (fromCard == null) {
                this.setPlayerResponsed(player)
            } else {
                fromCards[player] = null
                this.refreshMsg(player)
            }
        }
    }


    override val actionString: String
        get() = TTACmdString.ACTION_UPGRADE


    override fun getMsg(player: TTAPlayer): String {
        val fromCard = fromCards[player]
        return if (fromCard == null) {
            "请选择要转移的农场、矿山、城市建筑或部队。"
        } else {
            "请选择要转移到的农场、矿山、城市建筑或部队。"
        }
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_DESTORY

}
