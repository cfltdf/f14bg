package com.f14.TTA.executor

import com.f14.TTA.component.card.ActionCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.factory.TTAExecutorFactory
import com.f14.bg.exception.BoardGameException


/**
 * 打出行动牌的处理器
 * @author 吹风奈奈
 */
class TTAPlayActionCardExecutor(param: RoundParam, override val card: ActionCard) : TTAPlayCardExecutor(param, card) {

    override fun check() {
        super.check()
        // 新拿进来的行动牌需要在下回合才能打
        param.checkNewlyCard(card)
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        if (!this.checked) {
            this.check()
        }
        val executor = TTAExecutorFactory.createActionCardExecutor(param, card)
        executor.execute()
        if (executor.completed) {
            // 扣除行动点
            param.useActionPoint(actionType, actionCost)

            param.afterPlayActionCard(card)
            super.execute()
        }
    }
}
