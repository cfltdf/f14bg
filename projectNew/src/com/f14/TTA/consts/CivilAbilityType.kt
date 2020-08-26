package com.f14.TTA.consts

/**
 * 内政牌的能力类型

 * @author F14eagle
 */
enum class CivilAbilityType {
    /**
     * 叠放后加到其他牌上的属性
     */
    ATTACH_PROPERTY,

    /**
     * 直接调整个体牌的属性(即加到其他卡牌上)
     */
    ADJUST_UNIT_PROPERTY,

    /**
     * 因其他牌调整玩家属性(即加到自身卡牌上)
     */
    ADJUST_PROPERTY,

    /**
     * 所有个体生产资源
     */
    PRODUCE_RESOURCE,

    /**
     * 增长人口时的费用调整
     */
    PA_POPULATION_COST,

    /**
     * 拿牌后触发的能力
     */
    PA_TAKE_CARD,

    /**
     * 拿取奇迹时的费用调整
     */
    PA_TAKE_CARD_COST,

    /**
     * 拿奇迹无额外CA消耗
     */
    PA_NO_WONDER_EXTRA_CA,

    /**
     * 出牌后触发的能力
     */
    PA_PLAY_CARD,

    /**
     * 革命时将使用军事行动点代替内政行动点
     */
    PA_MILITARY_REVOLUTION,

    /**
     * 出牌费用的调整
     */
    PA_PLAY_CARD_COST,

    /**
     * 建造费用的调整
     */
    PA_BUILD_COST,

    /**
     * 被宣战时得分的能力
     */
    PA_SCORE_UNDERWAR,

    /**
     * 被宣战时建造费用的调整
     */
    PA_BUILD_COST_UNDERWAR,

    /**
     * 影响全局玩家建筑费用调整
     */
    PA_BUILD_COST_GLOBAL,

    /**
     * 影响全局玩家升级费用调整
     */
    PA_UPGRADE_COST_GLOBAL,

    /**
     * 一次可以建造奇迹的步骤
     */
    PA_WONDER_STEP,

    /**
     * 每个回合的临时资源
     */
    PA_TEMPLATE_RESOURCE,

    /**
     * 无视战术牌效果
     */
    PA_IGNORE_TACTICS,

    /**
     * 额外战术牌奖励
     */
    PA_ADDITIONAL_TACTICS_BONUS,

    /**
     * 卡牌使用限制
     */
    PA_USE_CARD_LIMIT,

    /**
     * 对方使用额外的军事行动点
     */
    PA_ADDITIONAL_MILITARY_COST,

    /**
     * 强化加值卡
     */
    PA_ENHANCE_BONUS_CARD,

    /**
     * 强化防御卡
     */
    PA_ENHANCE_DEFENSE_CARD,

    /**
     * 进攻盟友时的属性调整
     */
    PA_ATTACK_ALIAN_ADJUST,

    /**
     * 不能进攻盟友
     */
    PA_CANNOT_ATTACK_ALIAN,

    /**
     * 与盟友的科技合作
     */
    PA_SCIENCE_ASSIST,

    /**
     * 按照盟友的属性调整自己的属性
     */
    ADJUST_PROPERTY_BY_ALIAN,

    /**
     * 进攻盟友后摧毁条约
     */
    PA_END_WHEN_ATTACK_ALIAN,

    /**
     * 不能被作为目标
     */
    PA_CANNOT_BE_TARGET,

    /**
     * 特斯拉的能力
     */
    PA_TESLA_ABILITY,

    /**
     * 增强行动牌
     */
    ADJUST_ACTION_CARD,

    /**
     * 塞牌得分
     */
    ADJUST_EVENT_POINT,

    /**
     * 调整食物消费
     */
    ADJUST_CONSUMPTION,

    /**
     * 双倍生产
     */
    DOUBLE_PRODUCE,

    /**
     * 双倍生产II
     */

    /**
     * BEST_PRODUCE,
     */

    /**
     * 拿牌
     */

    /**
     * TAKE_CARD,
     */

    /**
     * 增加红点消耗
     */
    PA_ADJUST_MILITARY_COST,

    /**
     * 小观星
     */
    PA_VIEW_TOP_EVENT,

    /**
     * 捷克老祖
     */
    PA_CZECH,

    /**
     * 双倍获利
     */
    PA_DOUBLE_REWARD,

    /**
     * 红美铃
     */
    PA_MEIRIN,

    /**
     * 阵型加成
     */
    PA_TATIC_BONUS,

    /**
     * 少摸军事牌
     */
    PA_MILITARY_DRAW_GLOBAL,

    /**
     * 打出黄牌
     */
    PLAY_NEW_ACTION_CARD,

    /**
     * 任意卡当殖民奖励+1
     */
    PA_CARD_AS_BONUSCARD,

    /**
     * 两次政治行动
     */
    PA_DOUBLE_POLITICAL,

    /**
     * 新版成吉思汗
     */
    PA_GENGHIS,

    /**
     * 把任意建筑升级成为剧院
     */
    PA_UPGRADE_TO_THEATER,

    /**
     * 莎士比亚1
     */
    PA_PLAY_CARD_COST_GROUP,

    /**
     * 莎士比亚2
     */
    PA_BUILD_COST_GROUP,

    /**
     * 新版盖茨
     */
    PA_NEW_GATES_ABILITY,

    /**
     * 新泰姬陵
     */
    PA_NEW_TAJ,

    /**
     * 回合结束是生产
     */
    EXTRA_SCORE_PHASE,

    /**
     * 孙子
     */
    PA_SUNTZU_ABILITY,

    /**
     * 拿牌并打出
     */
    PA_GET_AND_PLAY,

    /**
     * 朱棣直接赢得殖民地
     */
    PA_ZHUDI_ABILITY,

    /**
     * 孙中山可以用内政或军事行动点革命
     */
    PA_PEACE_REVOLUTION,

    /**
     * 秦始皇陵
     */
    PA_SHIHUANG_TOMB,

    /**
     * 圆明园
     */
    PA_YUANMINGYUAN,

    /**
     * 翔霸天特别贡献
     */
    PA_HUBATIAN,

    /**
     * 贸易协定
     */
    PA_TRADE_RESOURCE,

    /**
     * 诸葛亮
     */
    PA_ZHUGELIANG,

    /**
     * 殖民后获得人口
     */
    PA_INCPOP_AFTER_COLO,

    /**
     * 暴动依然生产
     */
    PRODUCE_ON_UPRISING,

    /**
     * 联合国1
     */
    PA_SCORE_EVENT,

    /**
     * 联合国2
     */
    PA_SCORE_USE_CARD,

    /**
     * 不能革命
     */
    PA_NO_REVOLUTION,

    /**
     *
     */
    PA_BONDICA,

    /**
     *
     */
    PA_CONFUCIUS,

    /**
     *
     */
    PA_ZIZKA,

    /**
     *
     */
    DOUBLE_DRAW,

    /**
     *
     */
    PA_NOSTRADAMUS,

    /**
     *
     */
    PA_COLONIZE_RESOURCE,

    /**
     *
     */
    PA_SCORE_DISCARD,

    /**
     *
     */
    PA_SALADINE,

    /**
     *
     */
    SOCRE_INCREASE_POP,

    /**
     *
     */
    PA_PLAY_NEW_CARD_COST,

    /**
     *
     */
    PA_PLAY_CARD_ALTERNATE,

    /**
     * 马丁路德金
     */
    PA_MARTINE,

    /**
     *
     */
    MULTIPLE_UNIT,

    /**
     * 建造时返还资源
     */
    PA_RETURN_COST,

    /**
     * 和平演变科技减少
     */
    PA_PEACE_CHANGE_COST,

    /**
     * 姬路城
     */
    PA_SACRIFICE_UNIT,

    /**
     * 忽略不满意工人
     */
    PA_IGNORE_DISCONTENT,

    /**
     * 丝路
     */
    PA_SILK_ROAD,

    /**
     * 建好后立即获得殖民地
     */
    PA_GET_TERRITORY,

    /**
     * 红十字会
     */
    PA_RED_CROSS,

    /**
     * 时代变动触发
     */
    PA_ADD_AGE_EFFECT
}
