package com.f14.TTA.consts

/**
 * 文明的属性
 * @author F14eagle
 */
enum class CivilizationProperty {
    /**
     * 食物
     */
    FOOD,
    /**
     * 资源
     */
    RESOURCE,
    /**
     * 科技
     */
    SCIENCE,
    /**
     * 文化
     */
    CULTURE,
    /**
     * 军事力
     */
    MILITARY,
    /**
     * 幸福度
     */
    HAPPINESS,
    /**
     * 笑脸
     */
    HAPPY_FACE,
    /**
     * 哭脸
     */
    UNHAPPY_FACE,
    /**
     * 政治行动力
     */
    CIVIL_ACTION,
    /**
     * 军事行动力
     */
    MILITARY_ACTION,
    /**
     * 文明点数
     */
    CULTURE_POINT,
    /**
     * 科技点数
     */
    SCIENCE_POINT,
    /**
     * 内政手牌数
     */
    CIVIL_HANDS,
    /**
     * 军事手牌数
     */
    MILITARY_HANDS,
    /**
     * 殖民奖励点数
     */
    COLONIZING_BONUS,
    /**
     * 黄色指示物
     */
    YELLOW_TOKEN,
    /**
     * 蓝色指示物
     */
    BLUE_TOKEN,
    /**
     * 一次建造奇迹的多步
     */
    BUILD_STEP,
    /**
     * 不满的工人数
     */
    DISCONTENT_WORKER,
    /**
     * 额外生产资源
     */
    EXTRA_RESOURCE,
    /**
     * 额外生产食物
     */
    EXTRA_FOOD,
    /**
     * 多摸军事牌
     */
    MILITARY_DRAW,
    /**
     * 不能用于侵略的军力
     */
    DEFENSIVE_MILITARY,
    /**
     * 建筑上限
     */
    BUILDING_LIMIT;

    val propertyName: String
        get() = when (this) {
            FOOD -> "食物"
            RESOURCE -> "资源"
            SCIENCE -> "科技点数"
            CULTURE -> "文明点数"
            MILITARY -> "军力"
            CIVIL_ACTION -> "内政行动点数"
            else -> ""
        }
}
