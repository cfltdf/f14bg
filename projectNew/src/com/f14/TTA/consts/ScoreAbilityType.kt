package com.f14.TTA.consts

/**
 * 得分能力的类型

 * @author F14eagle
 */
enum class ScoreAbilityType {

    /**
     * 固定数值
     */
    CONSTANT,

    /**
     * 有效牌种类数
     */
    BY_COUNT,

    /**
     * 有效牌种类数平方
     */
    BY_COUNT_SQUARE,

    /**
     * 有效牌总等级
     */
    BY_LEVEL,

    /**
     * 有效数量
     */
    BY_NUM,

    /**
     * 有效数量总等级
     */
    BY_NUM_LEVEL,

    /**
     * 有效数量按属性得分
     */
    BY_NUM_PROPERTY,

    /**
     * 有效数量按属性得分,加额外调整值
     */
    BY_NUM_PROPERTY_ADJUSTED,

    /**
     * 按子种类数量
     */
    BY_SUBTYPE_AVAILABLE,

    /**
     * 按玩家的属性得分
     */
    BY_PLAYER_PROPERTY,

    /**
     * 按玩家的属性排名得分
     */
    BY_RANK,

    /**
     * 按食物生产力得分
     */
    FOOD_PRODUCTION,

    /**
     * 按资源生产力得分
     */
    RESOURCE_PRODUCTION,

    /**
     * 按照工人数得分
     */
    BY_WORKER,

    /**
     * 按照满意工人数得分(新版人口影响)
     */
    BY_CONTENT_WORKER,

    /**
     * 按照剩余属性点得分
     */
    BY_AVAILABLE_ACTION,

    /**
     * 新版农业影响
     */
    IMPACT_OF_AGRICULTURE_V2,

    /**
     * 新版工业影响
     */
    IMPACT_OF_INDUSTRY_V2,

    /**
     * 新版平衡影响
     */
    IMPACT_OF_BALANCE_V2,

    /**
     * 帝国大厦
     */
    BY_EMPIRE,

    /**
     * 新版联合国
     */
    UNION_NATION,

    /**
     * 所有玩家的卡牌数
     */
    BY_NUM_FIELD,
}
