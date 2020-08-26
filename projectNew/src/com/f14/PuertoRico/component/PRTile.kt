package com.f14.PuertoRico.component

import com.f14.PuertoRico.consts.*
import com.f14.bg.component.Card


class PRTile : Card(), Comparable<PRTile> {
    var vp: Int = 0
    var cost: Int = 0
    var level: Int = 0
    var colonistMax: Int = 0
    var colonistNum: Int = 0
    var ability: Ability? = null
    var goodType: GoodType? = null
    lateinit var part: Part
    lateinit var buildingType: BuildingType
    var bonusType: BonusType? = null

    val size: Int // 大建筑占2格
        get() = if (buildingType == BuildingType.LARGE_BUILDING) 2 else 1

    val emptyNum: Int
        get() = colonistMax - colonistNum

    override fun clone() = super.clone() as PRTile

    override fun compareTo(other: PRTile): Int {
        // 先比等级
        when {
            this.level > other.level -> return 1
            this.level < other.level -> return -1
        // 再比建筑类型,工厂类比普通建筑小
            (this.buildingType == BuildingType.SMALL_FACTORY || this.buildingType == BuildingType.LARGE_FACTORY) && (other.buildingType == BuildingType.BUILDING || other.buildingType == BuildingType.LARGE_BUILDING) -> return -1
            (other.buildingType == BuildingType.SMALL_FACTORY || other.buildingType == BuildingType.LARGE_FACTORY) && (this.buildingType == BuildingType.BUILDING || this.buildingType == BuildingType.LARGE_BUILDING) -> return 1
        // 再比价格
            this.cost > other.cost -> return 1
            this.cost < other.cost -> return -1
        // 最后比cardNo
            else -> {
                val no1 = this.cardNo.toDouble()
                val no2 = other.cardNo.toDouble()
                return no1.compareTo(no2)
            }
        }
    }
}
