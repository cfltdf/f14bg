package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.consts.TrigType
import com.f14.TS.factory.InitParamFactory
import com.f14.TS.listener.Custom49Listener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class Custom49Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = this.initiativePlayer!!
        val me = initParam.card
        if (player.hands.empty) {
            gameMode.report.action(player, "没有手牌")
        } else {
            val l = Custom49Listener(trigPlayer, gameMode, initParam)
            val param = gameMode.insertListener(l)
            val confirmString = param.getString("confirmString")
            if (ConfirmString.CONFIRM == confirmString) {
                val card = param.get<TSCard>("card")!!
                // 从玩家手上移除该牌
                gameMode.game.playerRemoveHand(player, card)
                gameMode.report.playerRemoveCard(player, card)
                gameMode.game.playerGetCard(player, me!!)

                val opposite = gameMode.game.getOppositePlayer(player.superPower)
                if (card.superPower == player.superPower) {
                    // 如果这张牌是自己的,则由对方使用该牌的OP点数
                    gameMode.report.playerPlayCard(opposite, card, TrigType.ACTION)
                    // 使用OP进行行动
                    val type = TrigType.ACTION
                    val initParam = InitParamFactory.createOpActionParam(gameMode, opposite, card, type)
                    val executor = TSOpActionExecutor(opposite, gameMode, initParam)
                    executor.execute()
                } else {
                    // 否则事件由对方直接发生
                    gameMode.report.playerPlayCard(opposite, card, TrigType.EVENT)
                    // 执行触发事件
                    gameMode.game.activeCardEvent(opposite, card)
                }
            }
        }
    }
}
