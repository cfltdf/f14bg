package com.f14.PuertoRico.component

import com.f14.PuertoRico.consts.GoodType
import java.util.*

/**
 * 港口
 * @author F14eagle
 */
class ShipPort {

    var ships: MutableMap<Int, Ship> = LinkedHashMap()

    /**
     * 添加货船
     * @param ship
     */
    fun add(ship: Ship) {
        this.ships[ship.maxSize] = ship
    }

    /**
     * 判断港口中是否有货船可以装运指定的货物
     * @param goodType
     * @return
     */
    fun canShip(goodType: GoodType): Boolean {
        // 判断是否有装有指定货物的货船
        return when (val ship = this.getShipByGoodType(goodType)) {
            null -> // 如果没有,则判断是否有可以装货的船
                this.ships.values.any { it.canShip(goodType) }
            else -> // 如果有,则判断该船是否已经满了,如果满了则不能进行装货
                !ship.isFull
        }

    }

    /**
     * 判断港口中是否可以往指定的货船上装运指定的货物
     * @param
     * @param ship
     * @return
     */
    fun canShip(goodType: GoodType, ship: Ship): Boolean {
        if (!ship.canShip(goodType)) return false
        // 判断是否存在装有指定货物的其他货船
        val s = this.getShipByGoodType(goodType)
        return s == null || s == ship
    }

    /**
     * 移除所有货船
     */
    fun clear() {
        this.ships.clear()
    }

    /**
     * 取得对应容量的货船
     * @param maxSize
     * @return
     */
    operator fun get(maxSize: Int) = this.ships[maxSize]!!

    /**
     * 取得所有可以装运指定货物的船
     * @param goodType
     * @return
     */
    fun getAvailableShips(goodType: GoodType): List<Ship> {
        // 判断是否有装有指定货物的货船
        return when (val ship = this.getShipByGoodType(goodType)) {
            null -> // 如果没有,则判断是否有可以装货的船
                this.ships.values.filter { it.canShip(goodType) }
            else -> // 如果有,则判断该船是否已经满了,如果满了则不能进行装货
                listOf(ship).filterNot(Ship::isFull)
        }
    }

    /**
     * 按照货物类型取得货船
     * @param goodType
     * @return
     */
    fun getShipByGoodType(goodType: GoodType) = this.ships.values.firstOrNull { it.goodType == goodType }
}
