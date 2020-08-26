package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.component.TSZeroCard
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSGameCmd
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException


class TSZeroCardListener(gameMode: TSGameMode, private var card: TSZeroCard) : TSActionListener(gameMode) {

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        for (player in gameMode.game.players) {
            val param = ZeroTurnParam(player)
            this.setParam(player, param)
        }
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        if (TSCmdString.RESIGN == action.getAsString("subact")) {
            gameMode.game.playerResigen(player)
            return
        }
        val param = this.getParam<ZeroTurnParam>(player)
        if (param.card != null) {
            throw BoardGameException("你已经选择过国策了!")
        }
        val cardId = action.getAsString("cardId").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择要作为国策的卡牌!")
        param.card = player.getCard(cardId)
        // 发送头条选择状态
        this.sendZeroTurnParam(null)
        this.sendInputStateParam(player)
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_ZERO_TURN

    /**
     * 判断是否所有的玩家都选择好了头条

     * @param gameMode

     * @return
     */
    private fun isAllZeroTurnSelected(): Boolean {
        return gameMode.game.players.map { this.getParam<ZeroTurnParam>(it) }.none { it.card == null }
    }

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        var ussrModify = 0
        var usaModify = 0
        var cancelEffect = false
        for (player in gameMode.game.players) {
            val param = this.getParam<ZeroTurnParam>(player)
            val card = param.card!!
            if (card.ignoreAfterEvent) {
                cancelEffect = true
            }
            gameMode.report.action(player, "选" + card.reportString + "作为国策")
            if (card.removeAfterEvent) {
                gameMode.game.playerRemoveHand(player, card)
            }
            when (player.superPower) {
                SuperPower.USSR -> ussrModify = card.op
                SuperPower.USA -> usaModify = card.op
                else -> {
                }
            }
        }
        gameMode.game.addZeroCard(card, ussrModify, usaModify, cancelEffect)
    }

    override fun onPlayerStartListen(player: TSPlayer) {
        super.onPlayerStartListen(player)
        this.sendInputStateParam(player)
    }

    /**
     * 向指定玩家发送输入选择状态的信息

     * @param gameMode

     * @param receiver
     */
    private fun sendInputStateParam(receiver: TSPlayer?) {
        val res = this.createSubactResponse(receiver, "inputState")
        // 只有没选择过头条的玩家才能进行选择
        val param = this.getParam<ZeroTurnParam>(receiver!!)
        res.public("selecting", param.card == null)
        gameMode.game.sendResponse(receiver, res)
    }

    override fun sendPlayerListeningInfo(receiver: TSPlayer?) {
        this.sendZeroTurnParam(receiver)

    }

    /**
     * 向指定玩家发送头条选择状态的信息

     * @param gameMode

     * @param receiver
     */
    private fun sendZeroTurnParam(receiver: TSPlayer?) {
        val res = this.createSubactResponse(receiver, "headLineParam")
        val isAllSelected = this.isAllZeroTurnSelected()
        res.public("isAllSelected", isAllSelected)
        res.public("cardId", card.id)
        res.public("effect1", card.effect1)
        res.public("effect2", card.effect2)
        res.public("effect3", card.effect3)
        res.public("effect4", card.effect4)
        val ussr = this.getParam<ZeroTurnParam>(gameMode.game.ussrPlayer)
        val usa = this.getParam<ZeroTurnParam>(gameMode.game.usaPlayer)
        if (isAllSelected) {
            // 如果已经全部选择完成,则设置选择的卡牌
            res.public("ussrCardId", ussr.card!!.id)
            res.public("usaCardId", usa.card!!.id)
        } else {
            // 如果没有选择完成,则设置是否选择卡牌
            res.public("isUssrSelected", ussr.card != null)
            res.public("isUsaSelected", usa.card != null)
        }
        gameMode.game.sendResponse(receiver, res)
    }

    /**
     * 头条参数

     * @author F14eagle
     */
    inner class ZeroTurnParam internal constructor(var player: TSPlayer) {
        var card: TSCard? = null
    }

}
