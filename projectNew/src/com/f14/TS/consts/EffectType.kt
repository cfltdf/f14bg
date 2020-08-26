package com.f14.TS.consts

/**
 * TS的持续效果类型

 * @author F14eagle
 */
enum class EffectType {
    /**
     * 调整OP
     */
    ADJUST_OP,

    /**
     * 额外OP,当所有点数都用在某个符合国家条件的情况下
     */
    ADDITIONAL_OP,

    /**
     * 只要进行政变就会输掉游戏
     */
    COUP_TO_LOSE,

    /**
     * 政变不会影响DEFCON
     */
    FREE_DEFCON_COUP,

    /**
     * 临时的行动轮数
     */
    TEMPLATE_ACTION_ROUND,

    /**
     * 调整阵营掷骰修正
     */
    REALIGMENT_ROLL_MODIFIER,

    /**
     * 政变的掷骰修正
     */
    COUP_ROLL_MODIFIER,

    /**
     * 额外的军事行动力 (该效果废弃了,配置中没有这些效果)
     */
    ADDITIONAL_MA_POINT,

    /**
     * 取消头条
     */
    CANCEL_HEADLINE,

    /**
     * 困境/捕熊陷阱
     */
    QUAGMIRE,

    /**
     * #49-导弹嫉妒的效果,下回合必须用导弹嫉妒作为行动
     */
    EFFECT_49,

    /**
     * #50的效果,如果下个行动轮美国不打出联合国干涉,则苏联得到3VP
     */
    EFFECT_50,

    /**
     * #59-鲜花反战 的效果,美国每打出一张战争牌,苏联+2VP
     */
    EFFECT_59,

    /**
     * #60-U2事件 的效果,如果联合国作为事件方式被打出,苏联+1VP
     */
    EFFECT_60,

    /**
     * #69-拉美暗杀队 的效果,自己在中美和南美的政变结果+1,对手-1
     */
    EFFECT_69,

    /**
     * #73-穿梭外交 的效果,苏联在下一次中东或者亚洲计分时,战场国数量-1
     */
    EFFECT_73,

    /**
     * #82-伊朗人质危机 的效果,美国在#92事件中要丢2张牌
     */
    EFFECT_82,

    /**
     * #94-切尔诺贝利 的效果,不能在指定的区域通过使用OP的行动放置影响力
     */
    EFFECT_94,

    /**
     * #101-台湾决议 的效果,美国在计分时如果控制台湾,则算作战场国
     */
    EFFECT_101,

    /**
     * #109-尤里和萨曼莎 的效果,美国本轮每政变一次,苏联就+1VP
     */
    EFFECT_109,

    /**
     * 太空竞赛特权1 - 每回合可以进行2次太空竞赛
     */
    SR_PRIVILEGE_1,

    /**
     * 太空竞赛特权2 - 对方先亮出头条
     */
    SR_PRIVILEGE_2,

    /**
     * 太空竞赛特权3 - 每回合结束时可以弃掉1张手牌
     */
    SR_PRIVILEGE_3,

    /**
     * 太空竞赛特权4 - 每回合可以进行8个行动轮
     */
    SR_PRIVILEGE_4,

    /**
     * 苏联不能在欧洲政变/调整阵营
     */
    PROTECT_EUROPE,

    /**
     * 苏联不能在日本政变/调整阵营
     */
    PROTECT_JAPAN,

    /**
     * 苏联不能在欧洲政变
     */
    PROTECT_EUROPE_COUP,

    /**
     * 取消对法国的保护
     */
    PROTECT_CANCELD_FRANCE,

    /**
     * 取消对西德的保护
     */
    PROTECT_CANCELD_WEST_GERMAN,

    /**
     * 克里姆林流感
     */
    KREMLIN_FLU,

    /**
     * 美国优先于苏联玩家行动
     */
    USA_GO_FIRST,

    /**
     * 第一回合美国不受核危机状态限制影响并且核危机状态不会低于2
     */
    USA_IGNORE_DEFCON,

    /**
     * 社会主义政府在第1和第2回合不再有效
     */
    USSR_NO_SOCIALIST,

    /**
     * 苏伊士运河危机事件不再有效
     */
    USSR_NO_SUEZ,

    /**
     * 封锁事件不再有效
     */
    USSR_NO_BLOCKADE,

    /**
     * 新太空竞赛特权1 - 回合结束+2军事行动
     */
    SR_PRIVILEGE_N1,

    /**
     * 新太空竞赛特权2 - 每回合强塞少花1OP
     */
    SR_PRIVILEGE_N2,

    /**
     * 新太空竞赛特权3 - 每轮可以重投一次政变骰
     */
    SR_PRIVILEGE_N3,

    /**
     * 新太空竞赛特权4 - 对方政变结果-1
     */
    SR_PRIVILEGE_N4
}
