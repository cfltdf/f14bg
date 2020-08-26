package com.f14.TTA.executor

import com.f14.TTA.component.card.TacticsCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.CardType
import com.f14.bg.exception.BoardGameException


/**
 * 打出阵型牌的处理器
 * @author 吹风奈奈
 */
class TTAPlayTacticsCardExecutor(param: RoundParam, override val card: TacticsCard) : TTAPlayCardExecutor(param, card) {
    init {
        this.actionType = ActionType.MILITARY
    }

    override fun check() {
        super.check()
        param.checkChangeTactic()
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        if (!checked) {
            this.check()
        }
        // 扣除行动点
        param.useActionPoint(actionType, actionCost)
        gameMode.game.playerAddCard(player, card)

        super.execute()

        player.params.setRoundParameter(CardType.TACTICS, true)
    }
}
