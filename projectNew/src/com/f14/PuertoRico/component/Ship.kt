package com.f14.PuertoRico.component

import com.f14.PuertoRico.consts.GoodType
import com.f14.PuertoRico.game.PRPlayer
import kotlin.math.min


class Ship(val maxSize: Int) {
    var goodType: GoodType? = null
    var size: Int = 0

    /**
     * 判断是否可以装货
     * @param goodType
     * @return
     */
    fun canShip(goodType: GoodType) =// 如果船是空的,或者是同类货物并且船未满,才能装货
            this.size == 0 || this.goodType == goodType && this.size < this.maxSize

    /**
     * 清除货物
     * @return 返回清除掉的货物数量
     */
    fun clear(): Int {
        val res = this.size
        this.size = 0
        this.goodType = null
        return res
    }

    /**
     * 玩家执行装货行动,返回装货数量
     * @param player
     * @param goodType
     * @return
     */
    fun doShip(player: PRPlayer, goodType: GoodType): Int {
        var res = 0
        if (this.canShip(goodType)) {
            val shipSize = this.maxSize - this.size
            val goodNum = player.resources.getAvailableNum(goodType)
            val realNum = min(shipSize, goodNum)
            player.resources.takePart(goodType, realNum)
            this.goodType = goodType
            this.size += realNum
            res = realNum
        }
        return res
    }

    /**
     * 判断货船是否已经装满
     * @return
     */
    val isFull: Boolean
        get() = this.size >= this.maxSize

}
