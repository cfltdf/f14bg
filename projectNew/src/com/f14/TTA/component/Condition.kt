package com.f14.TTA.component

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.CardSubType
import com.f14.TTA.consts.CardType
import com.f14.bg.component.ICondition

/**
 * 用于匹配对象属性的条件对象

 * @author F14eagle
 */
class Condition : ICondition<TTACard?> {
    var actionType: ActionType? = null
    var cardType: CardType? = null
    var cardSubType: CardSubType? = null
    var level: Int? = null
    var cardNo: String? = null
    var isTechnologyCard: Boolean? = null

    /**
     * 判断指定的牌是否符合该条件对象
     * @param card
     * @return
     */
    override infix fun test(card: TTACard?) = when {
        card == null -> false
        this.cardNo?.equals(card.cardNo) == false -> false
        this.cardSubType?.equals(card.cardSubType) == false -> false
        this.cardType?.equals(card.cardType) == false -> false
        this.actionType?.equals(card.actionType) == false -> false
        this.isTechnologyCard?.equals(card.isTechnologyCard) == false -> false
        this.level?.equals(card.level) == false -> false
        else -> true
    }
}
