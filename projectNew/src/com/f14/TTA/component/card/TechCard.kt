package com.f14.TTA.component.card

/**
 * 科技牌

 * @author F14eagle
 */
open class TechCard : CivilCard() {
    /**
     * 造价
     */
    var costResource: Int = 0
    /**
     * 科技需求
     */
    var costScience: Int = 0
    /**
     * 2-4人局的数量
     */
    var qty2p: Int = 0
    var qty3p: Int = 0
    var qty4p: Int = 0

    override fun clone() = super.clone() as TechCard
}
