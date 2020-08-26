package com.f14.PuertoRico.game.listener

import com.f14.PuertoRico.consts.GameCmdConst
import com.f14.PuertoRico.game.PRGameMode
import com.f14.PuertoRico.game.PRPlayer
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException


class ChooseCharacterListener(gameMode: PRGameMode) : PRActionListener(gameMode) {

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<PRPlayer>()
        val cardId = action.getAsString("cardId")
        gameMode.game.chooseCharacter(player, cardId)
        // 设置玩家已回应
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = GameCmdConst.GAME_CODE_CHOOSE_CHARACTER

    override fun initListeningPlayers() {
        // 只允许当前玩家选择行动
        val player = gameMode.game.roundPlayer
        for (p in gameMode.game.players) {
            if (player === p) {
                this.setNeedPlayerResponse(p.position, true)
            } else {
                this.setNeedPlayerResponse(p.position, false)
            }
        }
    }

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        // 发送玩家选择的行动信息
        gameMode.game.sendPlayerActionInfo()
    }

}
