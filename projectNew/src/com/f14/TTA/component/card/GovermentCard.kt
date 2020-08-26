package com.f14.TTA.component.card

import com.f14.TTA.consts.Token

/**
 * 政府牌

 * @author F14eagle
 */
class GovermentCard : TechCard() {
    /**
     * 建筑上限
     */
    var buildingLimit: Int = 0
    /**
     * 革命科技需求
     */
    var secondaryCostScience: Int = 0

    /**
     * 调整红色指示物的数量
     * @param num
     */
    fun addReds(num: Int) = this.addToken(Token.RED, num)

    /**
     * 调整白色指示物的数量
     * @param num
     */
    fun addWhites(num: Int) = this.addToken(Token.WHITE, num)


    override fun clone() = super.clone() as GovermentCard

    /**
     * 红色指示物的数量
     */
    var reds: Int
        get() = this.tokens.getAvailableNum(Token.RED)
        set(num) = this.setToken(Token.RED, num)

    /**
     * 取得所有指示物的数量
     * @return
     */
    override fun getTokens() = mapOf(Token.YELLOW.toString() to this.tokens.getAvailableNum(Token.YELLOW), Token.RED.toString() to this.reds, Token.WHITE.toString() to this.whites)

    /**
     * 白色指示物的数量
     */
    var whites: Int
        get() = this.tokens.getAvailableNum(Token.WHITE)
        set(num) = this.setToken(Token.WHITE, num)
}
