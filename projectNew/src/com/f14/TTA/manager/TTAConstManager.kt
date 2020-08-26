package com.f14.TTA.manager

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.consts.ActionAbilityType
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTACmdString
import kotlin.math.max

/**
 * TTA一些参数的管理器

 * @author F14eagle
 */
object TTAConstManager {
    /**
     * 扩张人口的费用参照标准
     */
    private val costs: List<CostValue>
    /**
     * 幸福度需求的参照标准
     */
    private val needHappiness: List<CostValue>
    /**
     * 食物供应的参照标准
     */
    private val foodSupply: List<CostValue>
    /**
     * 资源腐败的参照标准
     */
    private val resourceCorruption: List<CostValue>
    /**
     * 资源腐败的参照标准(新版)
     */
    private val resourceCorruptionV2: List<CostValue>
    /**
     * 行动所消耗的行动点数
     */
    private val actionCost: Map<String, Int>
    /**
     * 行动牌所允许的行动类型
     */
    private val actionAllowed: Map<ActionAbilityType, Set<String>>
    /**
     * 过时代失去的黄点
     */
    val darkAgeToken: TTAProperty
    /**
     * 时代总数
     */
    val ageCount: Int

    /**
     * 初始化
     */
    init {
        // 设置扩张人口的费用
        costs = listOf(
                CostValue(1, 4, 7),
                CostValue(5, 8, 5),
                CostValue(9, 12, 4),
                CostValue(13, 16, 3),
                CostValue(17, 99, 2)
        )

        // 设置人口需要的幸福度
        needHappiness = listOf(
                CostValue(0, 0, 8),
                CostValue(1, 2, 7),
                CostValue(3, 4, 6),
                CostValue(5, 6, 5),
                CostValue(7, 8, 4),
                CostValue(9, 10, 3),
                CostValue(11, 12, 2),
                CostValue(13, 16, 1),
                CostValue(17, 99, 0)
        )

        // 设置粮食供应的标准
        foodSupply = listOf(
                CostValue(0, 0, 6),
                CostValue(1, 4, 4),
                CostValue(5, 8, 3),
                CostValue(9, 12, 2),
                CostValue(13, 16, 1),
                CostValue(17, 99, 0)
        )

        // 设置资源腐败的标准
        resourceCorruption = listOf(
                CostValue(0, 0, 6),
                CostValue(1, 4, 4),
                CostValue(5, 8, 2),
                CostValue(9, 99, 0)
        )

        // 设置资源腐败的标准(新版)
        resourceCorruptionV2 = listOf(
                CostValue(0, 0, 6),
                CostValue(1, 5, 4),
                CostValue(6, 10, 2),
                CostValue(11, 99, 0)
        )

        // 设置行动点数
        actionCost = mapOf(
                TTACmdString.ACTION_BUILD to 1,
                TTACmdString.ACTION_POPULATION to 1,
                TTACmdString.ACTION_DESTORY to 1,
                TTACmdString.ACTION_UPGRADE to 1,
                TTACmdString.ACTION_PLAY_CARD to 1,
                TTACmdString.REQUEST_COPY_TAC to 2
        )

        // 设置需要响应的黄牌所适用的指令类型
        actionAllowed = mapOf(
                ActionAbilityType.BUILD_OR_UPGRADE to setOf(
                        TTACmdString.ACTION_BUILD_UPGRADE,
                        TTACmdString.REQUEST_BUILD,
                        TTACmdString.ACTION_BUILD,
                        TTACmdString.REQUEST_UPGRADE,
                        TTACmdString.REQUEST_UPGRADE_TO,
                        TTACmdString.ACTION_UPGRADE
                ),
                ActionAbilityType.BUILD to setOf(
                        TTACmdString.REQUEST_BUILD,
                        TTACmdString.ACTION_BUILD
                ),
                ActionAbilityType.UPGRADE to setOf(
                        TTACmdString.REQUEST_UPGRADE,
                        TTACmdString.REQUEST_UPGRADE_TO,
                        TTACmdString.ACTION_UPGRADE
                ),
                ActionAbilityType.PLAY_CARD to setOf(
                        TTACmdString.ACTION_PLAY_CARD,
                        TTACmdString.ACTION_CHANGE_GOVERMENT
                ),
                ActionAbilityType.TAKE_CARD to setOf(
                        TTACmdString.ACTION_TAKE_CARD
                ),
                ActionAbilityType.PA_LA_PORXADA to setOf(
                        TTACmdString.ACTION_EXCHANGE
                ),
                ActionAbilityType.SCORE_BETWEEN to setOf(
                        TTACmdString.ACTION_SCORE_BETWEEN
                )
        )

        darkAgeToken = TTAProperty()
        darkAgeToken.addProperty(CivilizationProperty.YELLOW_TOKEN, -2)

        ageCount = 4
    }

    /**
     * 取得行动牌是否可用于当前行动中
     * @param aat 行动牌类型
     * @param cmd 行动指令
     * @return
     */
    fun getActionAllowed(aat: ActionAbilityType, cmd: String): Boolean {
        return actionAllowed.containsKey(aat) && actionAllowed.getValue(aat).contains(cmd)
    }

    /**
     * 取得行动所消耗的行动点数
     * @param player
     * @param action
     * @return
     */
    fun getActionCost(player: TTAPlayer, action: String): Int {
        return actionCost[action] ?: 0
    }

    /**
     * 取得当前工人需要消耗的粮食数量
     * @param availableWorkers
     * @return
     */
    fun getFoodSupply(availableWorkers: Int): Int {
        return foodSupply.firstOrNull { it test availableWorkers }?.cost ?: 0
    }

    /**
     * 取得当前需要的幸福度
     * @param availableWorkers
     * @return
     */
    fun getNeedHappiness(availableWorkers: Int): Int {
        return needHappiness.firstOrNull { it test availableWorkers }?.cost ?: 0
    }

    @JvmOverloads
    fun getPopulationCost(player: TTAPlayer, costModify: Int = 0): Int {
        val availableWorkers = player.tokenPool.availableWorkers
        var res = costs.filter { it test availableWorkers }.sumBy(CostValue::cost) +
                player.abilityManager.getAbilitiesByType(CivilAbilityType.PA_POPULATION_COST)
                        .sumBy { it.property.getProperty(CivilizationProperty.FOOD) }
        // 计算扩张人口时调整费用的能力
        res = max(0, res + costModify)
        return res
    }

    fun getResignWarCulture(isVersion2: Boolean) = if (isVersion2) 7 else 0

    /**
     * 取得玩家当前需要腐败的资源数量
     * @param availableBlues
     * @return
     */
    fun getResourceCorruption(availableBlues: Int, v2: Boolean): Int {
        return (if (v2) resourceCorruptionV2 else resourceCorruption).firstOrNull { it.test(availableBlues) }?.cost ?: 0
    }


    fun initPlayerPoints(points: TTAProperty, noLimit: Boolean) {
        points.setMinValue(CivilizationProperty.CULTURE, 0)
        points.setMaxValue(CivilizationProperty.CULTURE, Integer.MAX_VALUE)
        points.setOverflow(CivilizationProperty.CULTURE, false)

        points.setMinValue(CivilizationProperty.SCIENCE, 0)
        points.setMaxValue(CivilizationProperty.SCIENCE, if (noLimit) Integer.MAX_VALUE else 40)
        points.setOverflow(CivilizationProperty.SCIENCE, false)
    }

    fun initPlayerProperties(properties: TTAProperty, noLimit: Boolean) {
        properties.setMinValue(CivilizationProperty.CULTURE, 0)
        properties.setMaxValue(CivilizationProperty.CULTURE, if (noLimit) Integer.MAX_VALUE else 30)
        properties.setOverflow(CivilizationProperty.CULTURE, true)

        properties.setMinValue(CivilizationProperty.SCIENCE, 0)
        properties.setMaxValue(CivilizationProperty.SCIENCE, if (noLimit) Integer.MAX_VALUE else 30)
        properties.setOverflow(CivilizationProperty.SCIENCE, true)

        properties.setMinValue(CivilizationProperty.MILITARY, 0)
        properties.setMaxValue(CivilizationProperty.MILITARY, if (noLimit) Integer.MAX_VALUE else 60)
        properties.setOverflow(CivilizationProperty.MILITARY, true)

        properties.setMinValue(CivilizationProperty.HAPPINESS, 0)
        properties.setMaxValue(CivilizationProperty.HAPPINESS, 8)
        properties.setOverflow(CivilizationProperty.HAPPINESS, true)
    }

    /**
     * @param aat
     * @return
     */
    fun isActionAlternate(aat: ActionAbilityType): Boolean {
        return actionAllowed.containsKey(aat)
    }

    /**
     * 判断玩家是否会引起暴动
     * @param player
     * @return
     */
    fun isUprising(player: TTAPlayer): Boolean {
        // 当玩家空闲的工人数量小于暴怒的人口,则发生暴动
        return player.tokenPool.unhappyWorkers > player.tokenPool.unusedWorkers
    }

    /**
     * 费用区间的对应关系
     * @author F14eagle
     */
    internal class CostValue(val min: Int, val max: Int, val cost: Int) {

        infix fun test(value: Int): Boolean {
            return value in min..max
        }
    }
}