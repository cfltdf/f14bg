package com.f14.RFTG.component

import com.f14.RFTG.card.Goal
import com.f14.RFTG.consts.GoalType
import com.f14.bg.component.Deck

import java.util.*

/**
 * 目标管理

 * @author F14eagle
 */
class GoalManager {
    private val defaultMostNum = 2
    private val defaultFirstNum = 4


    private val allGoals: MutableList<Goal> = ArrayList()
    private val goals: MutableMap<String, Goal> = LinkedHashMap()
    private val mostDeckDefault = Deck<Goal>()
    private val firstDeckDefault = Deck<Goal>()

    /**
     * 添加目标

     * @param goal
     */
    fun addGoal(goal: Goal) {
        this.goals[goal.id] = goal
    }

    /**
     * 将目标添加到默认牌堆中
     * @param goals
     */
    fun addGoalsToDefaultDeck(goals: List<Goal>) {
        val group = goals.groupBy(Goal::goalType)
        group[GoalType.MOST]?.let { mostDeckDefault.defaultCards = it }
        group[GoalType.FIRST]?.let { firstDeckDefault.defaultCards = it }
    }

    /**
     * 取得所有还需要检查的目标,包括所有的MOST目标以及剩余的FIRST目标

     * @return
     */

    // 将所有的MOST目标添加到结果
    val checkGoals: Collection<Goal>
        get() {
            val res = HashSet<Goal>()
            res.addAll(this.goals.values)
            this.allGoals.filterTo(res) { it.goalType == GoalType.MOST }
            return res
        }

    /**
     * 取得目标

     * @param id

     * @return
     */
    fun getGoal(id: String): Goal {
        return this.goals[id]!!
    }

    /**
     * 取得当前剩余的目标

     * @return
     */

    fun getGoals(): Collection<Goal> {
        return this.goals.values
    }

    /**
     * 按照goalType取得目标

     * @return
     */

    fun getGoals(goalType: GoalType): Collection<Goal> {
        return allGoals.filter { it.goalType == goalType }.toSet()
    }

    /**
     * 初始化目标
     */
    fun initGoals() {
        this.mostDeckDefault.reset()
        this.firstDeckDefault.reset()
        for (g in this.mostDeckDefault.draw(defaultMostNum)) {
            this.addGoal(g)
        }
        for (g in this.firstDeckDefault.draw(defaultFirstNum)) {
            this.addGoal(g)
        }
        allGoals.clear()
        allGoals.addAll(this.goals.values)
    }

    /**
     * 移除目标

     * @param goal
     */
    fun removeGoal(goal: Goal) {
        this.goals.remove(goal.id)
    }
}
