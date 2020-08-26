package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.consts.RoundStep
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

class FirstRoundListener(gameMode: TTAGameMode) : TTAOrderListener(gameMode) {

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 首轮直接进行普通阶段
        res.public("currentStep", RoundStep.NORMAL)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        val player = action.getPlayer<TTAPlayer>()
        when (action.getAsString("subact")) {
            TTACmdString.ACTION_TAKE_CARD -> this.takeCard(action)
            TTACmdString.ACTION_PASS -> this.setPlayerResponsed(player)
            else -> throw BoardGameException("第一回合不能进行其他的操作!")
        }
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_FIRST_ROUND

    @Throws(BoardGameException::class)
    override fun onStartListen() {
        super.onStartListen()
        // 为所有玩家创建使用多少行动点的参数
        var point = if (gameMode.game.config.isBalanced22) 0 else 1
        for (p in gameMode.game.getPlayersByOrder(gameMode.game.startPlayer!!)) {
            val param = PointParam()
            // 玩家可使用行动点的数量等于其顺位
            param.available = point++
            this.setParam(p.position, param)
        }
    }

    /**
     * 玩家从卡牌序列中拿牌
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun takeCard(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val cardId = action.getAsString("cardId")
        val cb = gameMode.cardBoard
        val cost = cb.getCost(cardId, player)
        val index = cb.getCardIndex(cardId)
        // 第一回合时,每个玩家只能使用自己顺位数值的行动点
        val param = this.getParam<PointParam>(player.position)
        CheckUtils.check(cost > param.available, "内政行动点不够,你还能使用 " + param.available + " 个内政行动点!")
        val card = cb.getCard(cardId)
        // 检查玩家是否可以拿牌
        player.checkTakeCard(card)
        gameMode.game.playerTakeCard(player, card, index)
        gameMode.report.playerTakeCard(player, cost, index, card)
        param.available -= cost
        if (param.available <= 0) {
            this.setPlayerResponsed(player)
        }
    }

    /**
     * 使用行动点的参数

     * @author F14eagle
     */
    internal inner class PointParam {
        var available = 0
    }

}
