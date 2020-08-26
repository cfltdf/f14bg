package com.f14.TTA.executor

import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.CostParam
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CardType
import com.f14.bg.exception.BoardGameException


/**
 * 打出科技牌的处理器
 * @author 吹风奈奈
 */
class TTAPlayTechCardExecutor(param: RoundParam, override val card: TechCard) : TTAPlayCardExecutor(param, card) {
    var costModify = 0
    private var cp: CostParam = CostParam()

    @Throws(BoardGameException::class)
    override fun check() {
        super.check()
        // 这些都是非政府科技牌,付出需要的科技直接打出就行了
        cp = param.getResearchCost(card, costModify)
        param.checkResearchCost(card, cp)
        if (gameMode.isVersion2 && this.card.cardType == CardType.SPECIAL) {
            if (player.allPlayedCard.any { it.cardSubType == this.card.cardSubType && it.level >= this.card.level }) {
                throw BoardGameException("不能打出相同或更高等级的特殊科技牌!")
            }
        }
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        if (!this.checked) {
            this.check()
        }
        // 扣除行动点
        param.useActionPoint(actionType, actionCost)
        // 研发科技时支付科技点
        gameMode.game.playerResearchCost(player, cp.cost)
        gameMode.game.playerAddCard(player, card)
        gameMode.game.executeTemplateResource(player, cp)
        super.execute()
    }
}
