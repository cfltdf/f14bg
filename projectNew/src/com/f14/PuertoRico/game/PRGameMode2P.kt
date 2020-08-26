package com.f14.PuertoRico.game

import com.f14.PuertoRico.component.PRTile
import com.f14.PuertoRico.consts.BuildingType
import com.f14.PuertoRico.consts.GoodType
import com.f14.PuertoRico.consts.Part
import com.f14.bg.exception.BoardGameException

class PRGameMode2P(game: PuertoRico) : PRGameMode(game) {

    /**
     * 重整2人游戏时用到的建筑物数量
     */
    private fun regroupBuildings() {
        var tile: PRTile
        // 将所有紫色建筑移除1个,小工厂移除2个,大工厂移除1个
        for (cardNo in buildingPool.cardNos) {
            tile = buildingPool.getCard(cardNo)!!
            val takeNum = when (tile.buildingType) {
                BuildingType.SMALL_FACTORY -> 2
                BuildingType.LARGE_FACTORY, BuildingType.BUILDING -> 1
                else -> 0
            }
            repeat(takeNum) { this.buildingPool.takeCard<PRTile>(cardNo) }
        }
    }

    /**
     * 重整2人游戏时用到的板块数量
     */
    private fun regroupTiles() {
        // 将每种种植园移除3个
        this.plantationsDeck.defaultCards = this.plantationsDeck.defaultCards.groupBy(PRTile::cardNo).values.flatMap { it.drop(3) }
        this.plantationsDeck.init()

        // 将采石场移除3个
        this.quarriesDesk.defaultCards = this.quarriesDesk.defaultCards.groupBy(PRTile::cardNo).values.flatMap { it.drop(32) }
        this.quarriesDesk.init()
    }

    @Throws(BoardGameException::class)
    override fun round() {
        // 双人游戏时,需要每人选择3次角色
        repeat(3) { super.round() }
    }

    override fun setupGame() {
        // 重整2人游戏时用到的版块数量
        this.regroupTiles()
        // 重整2人游戏时用到的建筑数量
        this.regroupBuildings()
        super.setupGame()
    }

    override fun initPartPool() {
        // 初始化配件数量
        partPool.setPart(Part.QUARRY, 5)
        partPool.setPart(GoodType.CORN, 8)
        partPool.setPart(GoodType.INDIGO, 9)
        partPool.setPart(GoodType.SUGAR, 9)
        partPool.setPart(GoodType.TOBACCO, 7)
        partPool.setPart(GoodType.COFFEE, 7)
    }
}
