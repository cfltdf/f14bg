package com.f14.innovation.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.consts.InnoGameCmd


/**
 * Innovation选择起始牌的监听器
 * @author F14eagle
 */
class InnoSetupListener(gameMode: InnoGameMode) : InnoActionListener(gameMode) {
//
//    @Throws(BoardGameException::class)
//    override fun beforeStartListen() {
//        super.beforeStartListen()
//        // 为所有玩家创建参数
//        for (player in gameMode.game.players) {
//            val param = SetupParam()
//            this.setParam(player, param)
//        }
//    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val subact = action.getAsString("subact")
        if ("MELD_CARD" == subact) {
            this.setupCard(action)
        } else {
            throw BoardGameException("无效的指令!")
        }
    }

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_SETUP_CARD

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        super.onAllPlayerResponsed()
        // 设置初始牌
        for (player in gameMode.game.players) {
            val param = this.getParam<SetupParam>(player)
            gameMode.game.playerMeldHandCard(player, param.card)
        }
    }

    /**
     * 设置起始卡牌
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun setupCard(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val cardId = action.getAsString("cardId")
        val card = player.hands.getCard(cardId)
        val param = SetupParam(card)
        this.setParam(player, param)
        this.setPlayerResponsed(player)
    }

    internal inner class SetupParam(val card: InnoCard)
}
