package com.f14.RFTG.card

import com.f14.RFTG.RacePlayer
import com.f14.RFTG.consts.GameState
import com.f14.RFTG.consts.GoalClass
import com.f14.RFTG.consts.GoalType
import com.f14.RFTG.consts.ProductionType
import com.f14.bg.component.Card
import com.google.gson.annotations.SerializedName
import java.util.*

class Goal : Card() {
    var goalType: GoalType = GoalType.MOST
    var vp: Int = 0
    @SerializedName("phasesPattern")
    var phases = emptyArray<GameState>()
    var subGoals = emptyArray<SubGoal>()
    var goalNum: Int = 0
    var goalClass: GoalClass = GoalClass.NORMAL

    @Transient
    var currentGoalValue: GoalValue? = null

    override fun clone() = super.clone() as Goal

    /**
     * 取得玩家所有能力的目标指数
     * @param player
     * @return
     */
    private fun getAbilityGoalNum(player: RacePlayer): Int {
        val abilities = HashSet<Class<out Ability>>()
        for (card in player.builtCards) {
            abilities.addAll(card.abilities.map { it.javaClass })
            // 军事力也算做扩张阶段的能力
            if (card.military != 0) abilities.add(SettleAbility::class.java)
            // 生产星球算作生产阶段的能力
            if (card.productionType == ProductionType.PRODUCTION) abilities.add(ProduceAbility::class.java)
        }
        // 移除bonus能力和特殊能力
        abilities.remove(BonusAbility::class.java)
        abilities.remove(SpecialAbility::class.java)
        // 如果拥有所有6个阶段的能力,则返回1
        return if (abilities.size == 6) 1 else -1
    }

    /**
     * 取得符合目标的玩家
     * @param players
     * @return
     */

    fun getGoalPlayers(players: List<RacePlayer>): List<GoalValue> {
        val values = players.map { it to this.test(it) }.toMap().filterValues { it >= this.goalNum }
        return when (this.goalType) {
            GoalType.FIRST -> // 如果是FIRST目标,则返回所有达成目标的玩家
                values
            GoalType.MOST -> { // 如果是MOST目标,则返回最大目标指数的玩家
                val maxValue = values.values.max() ?: 0
                values.filterValues { it == maxValue }
            }
        }.map { (p, v) -> GoalValue(p, v) }
    }


    /**
     * 取得玩家各个能力的数量
     * @param player
     * @return
     */
    private fun getPlayerAbilities(player: RacePlayer): AbilityCount {
        val count = AbilityCount()
        for (card in player.builtCards) {
            // 统计所有能力的数量
            for (cls in card.abilities) count.add(cls.javaClass)
            // 军事力也算做扩张阶段的能力
            if (card.military != 0) count.add(SettleAbility::class.java)
            // 生产星球算作生产阶段的能力
            if (card.productionType == ProductionType.PRODUCTION) count.add(ProduceAbility::class.java)
        }
        return count
    }

    /**
     * 检查玩家是否达成该目标
     * @param player
     * @return 返回目标指数
     */
    fun test(player: RacePlayer) = when (this.goalClass) {
        GoalClass.NORMAL -> // 普通的目标
            this.subGoals.map { it.test(player) }.filter { it > 0 }.sum()
        GoalClass.SINGLE -> // 单个目标目标
            this.subGoals.count { it.test(player) > 0 }
        GoalClass.MILITARY -> // 达到指定的军事力
            player.baseMilitary
        GoalClass.ABILITY_ALL -> // 拥有所有阶段能力,包括交易阶段
            this.getAbilityGoalNum(player)
        GoalClass.DISCARD -> // 弃牌
            player.roundDiscardNum
        GoalClass.VP -> // 得到指定的VP
            player.vp
        GoalClass.GOOD -> // 拥有指定的货物
            player.goods.size
        GoalClass.ABILITY -> // 拥有指定数量的能力
            this.subGoals.mapNotNull { GameState.getPhaseClass(it.gameState) }.sumBy(this.getPlayerAbilities(player)::get)
    }

    /**
     * 能力计数器
     * @author F14eagle
     */
    private inner class AbilityCount {
        var map: MutableMap<Class<*>, Int> = HashMap()

        /**
         * 添加能力
         * @param ability
         */
        fun add(ability: Class<*>) = map.set(ability, get(ability) + 1)

        /**
         * 取得能力的数量
         * @param ability
         * @return
         */
        fun get(ability: Class<*>) = map[ability] ?: 0
    }

}
