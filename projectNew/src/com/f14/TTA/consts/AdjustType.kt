package com.f14.TTA.consts

/**
 * 属性调整类型

 * @author F14eagle
 */
enum class AdjustType {

    /**
     * 无调整
     */
    NONE,

    /**
     * 按等级调整
     */
    BY_LEVEL,

    /**
     * 按数量调整
     */
    BY_NUM,

    /**
     * 按组合的数量调整
     */
    BY_GROUP_NUM,

    /**
     * 按属性调整
     */
    BY_PROPERTY,

    /**
     * 按数量的属性调整
     */
    BY_NUM_PROPERTY,

    /**
     * 按数量的等级调整
     */
    BY_NUM_LEVEL,

    /**
     * 按照科技的数量调整
     */
    BY_TECHNOLOGY_NUM,

    /**
     * 按卡牌种类有效与否调整
     */
    BY_CARD_AVAILABLE,

    /**
     * 按子种类有效与否调整
     */
    BY_SUBTYPE_AVAILABLE,

    /**
     * 阿育王
     */
    BY_ASHOKA,

    /**
     * 卡牌上的蓝点
     */
    BY_BLUE_TOKEN,
}
