package com.f14.TTA.listener

import com.f14.F14bg.network.CmdFactory
import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.PactCard
import com.f14.TTA.consts.PactSide
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam


/**
 * TTA玩家签订条约的监听器

 * @author F14eagle
 */
class TTAPactListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, private var targetPlayer: TTAPlayer, private var pact: PactCard) : TTAOrderInterruptListener(gameMode, trigPlayer) {
    private var step: ChooseStep = ChooseStep.CHOOSE_SIDE
    private var accepted = false

    init {
        this.addListeningPlayer(trigPlayer)
        this.addListeningPlayer(targetPlayer)
    }

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 如果是触发玩家,并且该条约是平等条约,则不需要玩家选择
        if (player === this.trigPlayer && !this.pact.isAsymetric) {
            // 将触发玩家自动设置为A方
            val param = this.getParam<ChooseParam>(player)
            param.pactSide = PactSide.A
            return false
        }
        return true
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        this.step = ChooseStep.CHOOSE_SIDE
        // 为所有监听的玩家创建选择参数
        for (player in this.listeningPlayers) {
            val param = ChooseParam()
            this.setParam(player, param)
        }
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        if (this.accepted) {
            val cp = this.getParam<ChooseParam>(trigPlayer)
            param["pactSide"] = cp.pactSide
        }
        return param
    }

    /**
     * 创建玩家所选条约方的指令
     * @return
     */
    private fun createPlayerPactSideInfo(): BgResponse {
        val res = CmdFactory.createGameResponse(this.validCode, -1)
        res.public("subact", "loadPactSide")
        val param = this.getParam<ChooseParam>(this.trigPlayer)
        if (param.pactSide != null) {
            when (param.pactSide) {
                PactSide.A -> {
                    res.public("A", trigPlayer.position)
                    res.public("B", targetPlayer.position)
                }
                PactSide.B -> {
                    res.public("B", trigPlayer.position)
                    res.public("A", targetPlayer.position)
                }
            }
        }
        return res
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 设置选择步骤参数
        res.public("step", this.step.toString())
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        val player = action.getPlayer<TTAPlayer>()
        if (player === trigPlayer && this.step == ChooseStep.CHOOSE_SIDE) {
            // 触发玩家的选择,只能选择条约方
            val pstr = action.getAsString("pactSide").takeUnless(String?::isNullOrEmpty)
                    ?: throw BoardGameException("请选择条约方!")
            val pactSide: PactSide
            try {
                pactSide = PactSide.valueOf(pstr)
            } catch (e: Exception) {
                log.error("条约方类型转换时出错!", e)
                throw BoardGameException("请选择条约方!")
            }

            // 设置玩家的选择参数
            val param = this.getParam<ChooseParam>(player)
            param.pactSide = pactSide
            gameMode.report.playerChoosePactSide(player, pact, pactSide.toString())
            // 设置玩家为已回应
            this.setPlayerResponsed(player)
        } else if (player === targetPlayer && this.step == ChooseStep.CHOOSE_ACCEPT) {
            // 目标玩家的选择,只能选择是否接受条约
            val confirm = action.getAsBoolean("confirm")
            this.accepted = confirm
            gameMode.report.playerAcceptPact(player, pact, confirm)
            this.setPlayerResponsed(player)
        } else {
            throw BoardGameException("你还不能执行该行动!")
        }
    }


    override fun getMsg(player: TTAPlayer): String {
        return if (this.step == ChooseStep.CHOOSE_SIDE) {
            "请选择要成为条约的A方还是B方!"
        } else {
            "是否接受该条约?"
        }
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_PACT

    @Throws(BoardGameException::class)
    override fun onPlayerResponsed(player: TTAPlayer) {
        super.onPlayerResponsed(player)
        if (this.step == ChooseStep.CHOOSE_SIDE && player === this.trigPlayer) {
            // 如果触发玩家选择完了行动,则
            // 设置条约步骤为选择是否接受
            this.step = ChooseStep.CHOOSE_ACCEPT
            // 发送选择结果
            this.createPlayerPactSideInfo().send(gameMode)
        }
    }

    override fun sendPlayerListeningInfo(receiver: TTAPlayer?) {
        super.sendPlayerListeningInfo(receiver)
        // 如果是选择是否接受条约的步骤,则发送选择条约的信息
        if (this.step == ChooseStep.CHOOSE_ACCEPT) {
            val res = this.createPlayerPactSideInfo()
            gameMode.game.sendResponse(receiver, res)
        }
    }

    override fun setListenerInfo(res: BgResponse) = super.setListenerInfo(res).public("cardId", this.pact.id)

    /**
     * 签订条约的步骤

     * @author F14eagle
     */
    internal enum class ChooseStep {
        /**
         * 选择条约方
         */
        CHOOSE_SIDE,
        /**
         * 选择是否接受条约
         */
        CHOOSE_ACCEPT
    }

    /**
     * 玩家的选择参数

     * @author F14eagle
     */
    internal inner class ChooseParam {
        var pactSide: PactSide? = null
    }
}
