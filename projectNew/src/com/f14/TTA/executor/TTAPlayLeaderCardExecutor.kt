package com.f14.TTA.executor

import com.f14.TTA.component.card.LeaderCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilAbilityType
import com.f14.bg.exception.BoardGameException


/**
 * 打出领袖牌的处理器

 * @author 吹风奈奈
 */
class TTAPlayLeaderCardExecutor(param: RoundParam, override val card: LeaderCard) : TTAPlayCardExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun execute() {
        if (!this.cached) {
            this.check()
        }
        // 扣除行动点
        param.useActionPoint(actionType, actionCost)
        player.leader?.let {
            // 新版替换领袖返还1白
            if (gameMode.game.isVersion2) {
                gameMode.game.playerAddCivilAction(player, 1)
                // 泰姬陵
                player.params.setRoundParameter(CivilAbilityType.PA_NEW_TAJ, true)
            }
            gameMode.game.report.playerRemoveCardCache(player, it)
        }
        gameMode.game.playerAddCard(player, card)

        super.execute()

        param.checkWillCard()
    }
}
