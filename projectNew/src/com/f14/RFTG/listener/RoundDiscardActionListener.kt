package com.f14.RFTG.listener

import com.f14.RFTG.RacePlayer
import com.f14.RFTG.card.Ability
import com.f14.RFTG.mode.RaceGameMode
import com.f14.RFTG.network.CmdConst
import com.f14.RFTG.network.CmdFactory
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import java.util.*

/**
 * 回合结束时弃牌的监听器

 * @author F14eagle
 */
class RoundDiscardActionListener(gameMode: RaceGameMode) : RaceActionListener(gameMode) {

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<RacePlayer>()
        val discardIds = action.getAsString("cardIds")
        val discards = player.getCards(discardIds)
        if (player.handSize - discards.size != gameMode.getHandsLimit(player)) {
            throw BoardGameException("弃牌数量错误,你需要弃 " + (player.handSize - gameMode.getHandsLimit(player)) + " 张牌!")
        }
        player.roundDiscardNum = discards.size
        // 将弃牌信息发送到客户端
        gameMode.game.discardCard(player, discardIds)
        // 将该玩家的是否回应设置为已回应
        this.setPlayerResponsed(action.getPlayer<RacePlayer>().position)
    }


    override val ability: Class<out Ability>?
        get() = null

    override val validCode: Int
        get() = CmdConst.GAME_CODE_ROUND_DISCARD

    override fun initListeningPlayers() {
        // 检查所有玩家的手牌是否超过上限,只需要给超过上限的玩家发送弃牌指令
        for (p in gameMode.game.players) {
            if (p.handSize > gameMode.getHandsLimit(p)) {
                this.setNeedPlayerResponse(p.position, true)
            } else {
                this.setNeedPlayerResponse(p.position, false)
            }
        }
    }

    @Throws(BoardGameException::class)
    override fun onStartListen() {
        val res = ArrayList<BgResponse>()
        val r = CmdFactory.createGameResponse(validCode, -1)
        gameMode.game.players.forEach { r.public(it.position.toString(), it.handSize - gameMode.getHandsLimit(it)) }
        res.add(r)
        gameMode.game.sendResponse(res)
    }

}
