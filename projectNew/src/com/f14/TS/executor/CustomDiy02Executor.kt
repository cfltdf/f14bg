package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSConsts
import com.f14.TS.consts.TrigType
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException
import com.f14.utils.CollectionUtils

/**
 * Created by 吹风奈奈 on 2017/8/8.
 */
class CustomDiy02Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {
    override fun execute() {
        val player = this.initiativePlayer!!
        // 从对方手上随机抽出一张牌
        val opposite = gameMode.game.getOppositePlayer(player.superPower)
        if (!opposite.hands.empty) {
            val drawnCard = CollectionUtils.randomDraw(opposite.hands.cards)
                    ?: throw BoardGameException("No such card!")
            gameMode.report.playerRandowDrawCard(player, drawnCard)
            if (drawnCard.superPower == opposite.superPower) {
                // 如果这张牌是对方的,则对方本回合不能使用这张卡
                gameMode.report.playerCannotUseTurn(opposite, drawnCard)
                opposite.params.setRoundParameter(TSConsts.DOUBLE_AGENT_NO, drawnCard.tsCardNo)
            } else {
                // 否则事件直接发生
                gameMode.report.playerPlayCard(player, drawnCard, TrigType.EVENT)
                gameMode.game.playerRemoveHand(opposite, drawnCard)
                // 执行触发事件
                gameMode.game.activeCardEvent(player, drawnCard)
            }
        }
    }
}