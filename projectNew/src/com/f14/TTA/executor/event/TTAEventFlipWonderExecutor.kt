package com.f14.TTA.executor.event

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.WonderCard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.listener.InterruptParam


/**
 * 反面奇迹的事件牌能力处理器

 * @author 吹风奈奈
 */
class TTAEventFlipWonderExecutor(param: RoundParam, ability: EventAbility) : TTAAlternateAbilityExecutor(param, ability) {

    override fun processPlayer(result: InterruptParam, p: TTAPlayer) {
        val card = result.get<WonderCard>(p.position) ?: return
        // 移除奇迹后,需要添加一个的同等级的废弃奇迹牌
        if (card.blues > 0) {
            player.tokenPool.addAvailableBlues(card.blues)
            card.addBlues(-card.blues)
        }
        val wonder = gameMode.cardBoard.drawFlipWonder(card.level)
        wonder.cardNo = card.cardNo
        gameMode.game.playerRemoveCardDirect(p, card)
        gameMode.report.playerRemoveCardCache(p, card)
        gameMode.game.playerAddCardDirect(p, wonder)
        gameMode.game.playerReattachCard(p, wonder, card)
        gameMode.report.playerAddCard(p, wonder)
    }

}
