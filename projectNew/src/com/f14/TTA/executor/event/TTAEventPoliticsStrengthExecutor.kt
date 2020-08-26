package com.f14.TTA.executor.event

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.listener.InterruptParam

/**
 * 强权政治的弃牌能力处理器

 * @author 吹风奈奈
 */
class TTAEventPoliticsStrengthExecutor(param: RoundParam, ability: EventAbility) : TTAAlternateAbilityExecutor(param, ability) {

    override fun processPlayer(result: InterruptParam, p: TTAPlayer) {
        if (gameMode.gameOver) {
            // 进入Ⅳ时代后则为扣分
            val res = gameMode.game.playerAddCulturePoint(p, ability.amount)
            gameMode.report.playerAddCulturePoint(p, res)
        } else {
            // 结算弃牌效果
            val cards = result.get<List<TTACard>>(p.position)
            if (cards != null) {
                gameMode.game.playerDiscardHand(p, cards)
            }
        }
    }

}
