package com.f14.PuertoRico.component

import com.f14.PuertoRico.consts.GoodType
import java.util.*

/**
 * 交易所

 * @author F14eagle
 */
class TradeHouse(var maxGoodNum: Int) {
    var goods: MutableList<GoodType> = ArrayList()

    /**
     * 添加货物
     * @param goodType
     */
    fun add(goodType: GoodType) {
        this.goods.add(goodType)
    }

    /**
     * 清空交易所
     */
    fun clear() {
        this.goods.clear()
    }

    /**
     * 判断交易所中是否存在指定的货物
     * @param goodType
     * @return
     */
    fun contain(goodType: GoodType): Boolean {
        return this.goods.contains(goodType)
    }

    /**
     * 取得交易货物的基本价格
     * @param goodType
     * @return
     */
    fun getBaseCost(goodType: GoodType?): Int {
        return when (goodType) {
            null -> 0
            GoodType.CORN -> 0
            GoodType.INDIGO -> 1
            GoodType.SUGAR -> 2
            GoodType.TOBACCO -> 3
            GoodType.COFFEE -> 4
        }
    }

    /**
     * 判断交易所是否已经满了
     * @return
     */
    val isFull: Boolean
        get() = goods.size >= this.maxGoodNum

    /**
     * 出售货物
     * @param goodType
     * @return
     */
    fun sell(goodType: GoodType): Int {
        this.add(goodType)
        return this.getBaseCost(goodType)
    }
}
