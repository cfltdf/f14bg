package com.f14.TTA.consts

/**
 * 事件类型

 * @author F14eagle
 */
enum class EventType {

    /**
     * 直接得到资源/粮食/科技/分数
     */
    SCORE,

    /**
     * 选择资源或食物
     */
    FOOD_RESOURCE,

    /**
     * 生产粮食/资源
     */
    PRODUCE,

    /**
     * 失去所有的粮食/资源
     */
    LOSE_ALL,

    /**
     * 摸军事牌
     */
    DRAW_MILITARY,

    /**
     * 弃军事牌
     */
    DISCARD_MILITARY,

    /**
     * 建造
     */
    BUILD,

    /**
     * 摧毁
     */
    DESTORY,

    /**
     * 新版突袭
     */
    DESTORY_MAX_LEVEL,

    /**
     * 新版隐秘行动
     */
    CHOOSE_INFILTRATE,

    /**
     * 扩张人口
     */
    INCREASE_POPULATION,

    /**
     * 选择失去人口
     */
    LOSE_POPULATION,

    /**
     * 调整内政行动点
     */
    ADJUST_CA,

    /**
     * 调整下回合的内政行动点
     */
    ADJUST_NEXT_CA,

    /**
     * 失去闲置的工人
     */
    LOSE_UNCONTENT_WORKER,

    /**
     * 失去领袖
     */
    LOSE_LEADER,

    /**
     * 失去殖民地
     */
    LOSE_COLONY,

    /**
     * 拿牌
     */
    TAKE_CARD,

    /**
     * 调整资源库标志物
     */
    TOKEN,

    /**
     * 废弃奇迹
     */
    FLIP_WONDER,

    /**
     * 摧毁其他人的建筑
     */
    DESTORY_OTHERS,

    /**
     * 失去未建造完成的奇迹
     */
    LOSE_UNCOMPLETE_WONDER,

    /**
     * 失去科技点(牌)
     */
    LOSE_SCIENCE,

    /**
     * 宗教裁判所
     */
    INQUISITION,

    /**
     * 瓜分
     */
    PARTITION,

    /**
     * 新版强权政治
     */
    POLITICS_STRENGTH,

    /**
     * 新版文明发展
     */
    DEVELOP_OF_CIVILIZATION,

    /**
     * 旧版政治发展不需弃军事牌
     */
    NO_DISCARD_MILITARY,

    /**
     * 万国来朝
     */
    TAKE_OTHERS,

    /**
     * 劫持抢牌
     */
    CHOOSE_CARD,

    /**
     * 计划发展
     */
    CHOOSE_EVENT,

    /**
     * 自由搬迁
     */
    FERE_MIGRATION
}
