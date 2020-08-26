package com.f14.TTA.listener.event

import com.f14.TTA.TTAConfig
import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.TTAReport
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.GovermentCard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.executor.*
import com.f14.TTA.listener.TTAEventListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ActionStep
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class DevOfCivListener(gameMode: TTAGameMode, ability: EventAbility, trigPlayer: TTAPlayer) : TTAEventListener(gameMode, ability, trigPlayer) {

    private val choices = HashMap<TTAPlayer, TTAActionExecutor?>()

    @Throws(BoardGameException::class)
    private fun build(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player.position)
        // 省1资源建造1个新的城市建筑
        val cardId = action.getAsString("cardId")
        val card = player.getPlayedCard(cardId) as TechCard
        CheckUtils.check(card.cardType !in arrayOf(CardType.BUILDING, CardType.PRODUCTION), "只能建造农场、矿山或城市建筑!")
        val executor = TTABuildCivilExecutor(param, card)
        executor.actionCost = 0
        executor.costModify = -1
        executor.check()
        choices[player] = executor
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        for (player in this.listeningPlayers) {
            val executor = choices.getOrDefault(player, null)
            param[player.position] = executor
        }
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val confirm = action.getAsBoolean("confirm")
        if (confirm) {
            val subact = action.getAsString("subact")
            if (TTACmdString.ACTION_POPULATION == subact) {
                this.increasePop(action)
            } else if (TTACmdString.ACTION_PLAY_CARD == subact) {
                if (this.playTechCard(action)) {
                    return
                }
            } else if (TTACmdString.ACTION_DEV_OF_CIV == subact) {
                this.build(action)
            } else if ("cancel" == subact) {
                // Do nothing
            } else {
                throw BoardGameException("不能执行此操作!")
            }
        }
        // 成功或取消后,设置玩家完成回应
        this.setPlayerResponsed(player)
    }


    override val actionString: String
        get() = TTACmdString.ACTION_DEV_OF_CIV


    override fun getMsg(player: TTAPlayer): String {
        return "你可以: 省1科技打出1张科技牌 / 省1粮食扩张人口 / 省1资源建造1个新的农场、矿山或城市建筑"
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_DESTORY

    @Throws(BoardGameException::class)
    private fun increasePop(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player.position)
        val executor = TTAIncreasePopExecutor(param)
        executor.actionCost = 0
        executor.costModify = -1
        executor.check()
        choices[player] = executor
    }

    @Throws(BoardGameException::class)
    private fun playTechCard(action: GameAction): Boolean {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<RoundParam>(player.position)
        // 省1科技打出1张科技牌
        val cardId = action.getAsString("cardId")
        val card = player.getCard(cardId) as TechCard
        CheckUtils.check(!card.isTechnologyCard, "只能打出科技牌!")
        if (card.cardType == CardType.GOVERMENT) {
            // 政体可以此时革命或和平演变
            val step = ChangeGovernmentStep(this, card as GovermentCard)
            this.addActionStep(player, step)
            return true
        }
        val executor = TTAPlayTechCardExecutor(param, card)
        executor.actionCost = 0
        executor.costModify = -1
        executor.check()
        choices[player] = executor
        return false
    }

    internal inner class ChangeGovernmentStep(listener: DevOfCivListener, private val card: GovermentCard) : ActionStep<TTAPlayer, TTAConfig, TTAReport>(listener) {
        private var fullfilled = false

        @Throws(BoardGameException::class)
        private fun changeGovernment(action: GameAction) {
            val player = action.getPlayer<TTAPlayer>()
            val param = this@DevOfCivListener.getParam<RoundParam>(player.position)
            val sel = action.getAsInt("sel")
            // 是否以革命的方式改变政府
            if (sel < 0) {
                return
            }
            val executor = TTAChangeGovermentExecutor(param, card, sel)
            executor.actionCost = 0
            executor.costModify = -1
            executor.check()
            choices[player] = executor
            this.fullfilled = true
        }

        @Throws(BoardGameException::class)
        override fun doAction(action: GameAction) {
            val confirm = action.getAsBoolean("confirm")
            if (confirm) {
                val subact = action.getAsString("subact")
                if (TTACmdString.REQUEST_SELECT == subact) {
                    this.changeGovernment(action)
                } else {
                    // Do nothing
                }
            }
        }

        override val actionCode: Int
            get() = TTAGameCmd.GAME_CODE_CHANGE_GOVERMENT


        override val stepCode: String
            get() = TTACmdString.ACTION_CHANGE_GOVERMENT

        @Throws(BoardGameException::class)
        override fun onStepOver(player: TTAPlayer) {
            super.onStepOver(player)
            gameMode.game.playerRequestEnd(player)
            if (this.fullfilled) {
                listener.setPlayerResponsed(player)
            }
        }

        @Throws(BoardGameException::class)
        override fun onStepStart(player: TTAPlayer) {
            super.onStepStart(player)
            val param = HashMap<String, Any>()
            param["subact"] = TTACmdString.ACTION_CHANGE_GOVERMENT
            param["sel"] = "革命,和平演变"
            gameMode.game.sendPlayerActionRequestResponse(player, TTACmdString.REQUEST_SELECT, "请选择要更换政府的方式", card, param)
        }
    }
}
