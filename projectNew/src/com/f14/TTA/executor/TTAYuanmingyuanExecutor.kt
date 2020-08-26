package com.f14.TTA.executor

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.component.card.WonderCard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException


/**
 * 圆明园的处理器
 * @author 吹风奈奈
 */
class TTAYuanmingyuanExecutor(param: RoundParam, internal var targetPlayer: TTAPlayer, val card: WonderCard, val ability: CivilCardAbility) : TTAActionExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        val wonder = gameMode.cardBoard.drawFlipWonder(card.level)
        wonder.cardNo = card.cardNo
        gameMode.game.playerRemoveCardDirect(targetPlayer, card)
        gameMode.report.playerRemoveCardCache(targetPlayer, card)
        gameMode.game.playerAddCardDirect(targetPlayer, wonder)
        gameMode.report.playerAddCardCache(targetPlayer, wonder)
        gameMode.game.playerReattachCard(targetPlayer, wonder, card)
        gameMode.report.playerActiveCard(targetPlayer, card)
    }

}
