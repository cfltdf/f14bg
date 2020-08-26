package com.f14.RFTG.listener

import com.f14.RFTG.RacePlayer
import com.f14.RFTG.card.Ability
import com.f14.RFTG.mode.RaceGameMode
import com.f14.RFTG.network.CmdConst
import com.f14.RFTG.network.CmdFactory
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils

/**
 * 游戏开始时弃牌的监听器

 * @author F14eagle
 */
class StartingDiscardListener(gameMode: RaceGameMode) : RaceActionListener(gameMode) {

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<RacePlayer>()
        val cardIds = action.getAsString("cardIds")
        val discards = player.getCards(cardIds)
        if (player.handSize - discards.size != player.startWorld!!.startHandNum) {
            throw BoardGameException("弃牌数量错误,你需要弃 " + (player.handSize - player.startWorld!!.startHandNum) + " 张牌!")
        }
        // 将弃牌信息发送到客户端
        gameMode.game.discardCard(player, cardIds)
        // 将该玩家的是否回应设置为已回应
        this.setPlayerResponsed(action.getPlayer<RacePlayer>().position)
    }


    override val ability: Class<out Ability>?
        get() = null

    override val validCode: Int
        get() = CmdConst.GAME_CODE_STARTING_DISCARD

    @Throws(BoardGameException::class)
    override fun onStartListen() {
        // 将所有玩家需要丢弃卡牌的数量返回到客户端
        gameMode.game.players.fold(CmdFactory.createGameResponse(this.validCode, -1)) { acc, p ->
            gameMode.report.playerPlayCards(p, BgUtils.toList(p.startWorld!!))
            gameMode.report.printCache(p)
            acc.public(p.position.toString(), p.startWorld!!.startHandNum)
        }.send(gameMode)
//        val res = CmdFactory.createGameResponse(this.validCode, -1)
//        for (o in gameMode.game.players) {
//            res.public(o.position.toString(), o.startWorld!!.startHandNum)
//            gameMode.report.playerPlayCards(o, BgUtils.toList(o.startWorld!!))
//            gameMode.report.printCache(o)
//        }
//        gameMode.game.sendResponse(res)
    }

}
