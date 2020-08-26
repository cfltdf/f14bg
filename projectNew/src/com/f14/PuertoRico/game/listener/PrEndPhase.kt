package com.f14.PuertoRico.game.listener

import com.f14.PuertoRico.component.PRTile
import com.f14.PuertoRico.consts.BonusType
import com.f14.PuertoRico.consts.BuildingType
import com.f14.PuertoRico.game.PRGameMode
import com.f14.PuertoRico.game.PRPlayer
import com.f14.bg.GameEndPhase
import com.f14.bg.VPCounter
import com.f14.bg.VPResult
import org.apache.log4j.Logger
import kotlin.math.min

class PrEndPhase : GameEndPhase<PRGameMode>() {
    override fun createVPResult(gameMode: PRGameMode): VPResult {
        val result = VPResult(gameMode.game)
        gameMode.game.players.forEach { player ->
            log.debug("玩家 [${player.user.name}] 的分数:")
            val vpc = VPCounter(player)
            vpc.addVp("VP(筹码)", player.vp)
            vpc.addVp("VP(建筑)", this.getBuiltVP(player))
            // 计算有额外VP功能的牌的得分
            player.buildings.forEach {
                val vp = this.getBonus(player, it)
                if (vp != 0) vpc.addVp(it.name, vp)
            }
            val totalVP = vpc.totalVP
            log.debug("总计 : $totalVP")
            vpc.addSecondaryVp("金钱+货物 ", player.doubloon + player.resources.totalNum)
            result += vpc
        }
        return result
    }

    /**
     * 取得指定卡牌能得到的额外VP
     * @param player
     * @param building
     * @return
     */
    private fun getBonus(player: PRPlayer, building: PRTile): Int {
        return if (building.colonistNum > 0) {
            // 如果没有分配移民,则不能得到额外VP
            when (building.bonusType) {
                BonusType.COLONIST -> player.totalColonist / 3
                BonusType.VP -> player.vp / 4
                BonusType.PLANTATION -> min(4, player.fields.size - 9)
                BonusType.FACTORY -> player.buildings.sumBy {
                    when (it.buildingType) {
                        BuildingType.SMALL_FACTORY -> 1
                        BuildingType.LARGE_FACTORY -> 2
                        else -> 0
                    }
                }
                BonusType.BUILDING -> player.buildings.sumBy {
                    when (it.buildingType) {
                        BuildingType.BUILDING, BuildingType.LARGE_BUILDING -> 1
                        else -> 0
                    }
                }
                BonusType.GROUP_PLANTATION -> player.fields
                        // 每3个一组的种植园可以得1分,如果有2组得3分,3组得6分,4组得10分
                        .groupBy(PRTile::cardNo).values.map(List<PRTile>::size).sumBy { it / 3 }.downTo(1).sum()
                else -> 0
            }
        } else 0
    }

    /**
     * 取得玩家所有建筑的VP
     * @param player
     * @return
     */
    private fun getBuiltVP(player: PRPlayer) = player.buildings.sumBy(PRTile::vp)


    companion object {
        private val log = Logger.getLogger(PrEndPhase::class.java)!!
    }
}
