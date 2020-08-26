package com.f14.RFTG.card

import com.f14.RFTG.consts.*
import com.f14.bg.component.ICondition


/**
 * 能力触发条件
 * @author F14eagle
 */
class Condition: ICondition<RaceCard> {
    var id: String? = null
    var cardNo: String? = null
    var cost: Int? = null
    var type: CardType? = null
    var worldType: WorldType? = null
    var productionType: ProductionType? = null
    var goodType: GoodType? = null
    var symbol: Symbol? = null

    /**
     * 测试卡牌的属性是否符合该条件
     * @param card
     * @return
     */
    override fun test(card: RaceCard) = when {
        id != null && id != card.id -> false
        cardNo != null && cardNo != card.cardNo -> false
        cost != null && cost != card.cost -> false
        type != null && type != card.type -> false
        worldType != null && !card.worldTypes.contains(worldType!!) -> false
        productionType != null && productionType != card.productionType -> false
        goodType != null && goodType != card.goodType -> false
        symbol != null && !card.symbols.contains(symbol!!) -> false
        else -> true
    }

}
