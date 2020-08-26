package com.f14.TTA.consts

/**
 * TTA发送指令时用到的一些字符串常量

 * @author F14eagle
 */
object TTACmdString {
    /**
     * 当前世纪
     */
    const val CURRENT_AGE = "CURRENT_AGE"
    /**
     * 当前文明牌剩余数量
     */
    const val CIVIL_REMAIN = "CIVIL_REMAIN"
    /**
     * 当前事件剩余数量
     */
    const val EVENT_REMAIN = "EVENT_REMAIN"
    /**
     * 当前军事牌剩余数量
     */
    const val MILITARY_REMAIN = "MILITARY_REMAIN"

    /**
     * 玩家取消请求
     */
    const val CANCEL_REQUEST = "cancel"
    /**
     * 玩家结束回合
     */
    const val ACTION_PASS = "pass"
    /**
     * 玩家从摸牌区拿牌
     */
    const val ACTION_TAKE_CARD = "ACTION_TAKE_CARD"
    /**
     * 玩家建造建筑/部队/奇迹
     */
    const val ACTION_BUILD = "ACTION_BUILD"
    /**
     * 玩家建造的请求
     */
    const val REQUEST_BUILD = "REQUEST_BUILD"
    /**
     * 玩家升级建筑/部队
     */
    const val ACTION_UPGRADE = "ACTION_UPGRADE"
    /**
     * 玩家升级对象的请求
     */
    const val REQUEST_UPGRADE = "REQUEST_UPGRADE"
    /**
     * 玩家升级目标的请求
     */
    const val REQUEST_UPGRADE_TO = "REQUEST_UPGRADE_TO"
    /**
     * 玩家摧毁建筑/部队
     */
    const val ACTION_DESTORY = "ACTION_DESTORY"
    /**
     * 玩家摧毁建造的请求
     */
    const val REQUEST_DESTORY = "REQUEST_DESTORY"
    /**
     * 玩家拷贝阵型的请求
     */
    const val REQUEST_COPY_TAC = "REQUEST_COPY_TAC"
    /**
     * 玩家单选的请求
     */
    const val REQUEST_SELECT = "REQUEST_SELECT"
    /**
     * 玩家扩张人口
     */
    const val ACTION_POPULATION = "ACTION_POPULATION"
    /**
     * 玩家打出手牌
     */
    const val ACTION_PLAY_CARD = "ACTION_PLAY_CARD"
    /**
     * 玩家更换政府
     */
    const val ACTION_CHANGE_GOVERMENT = "ACTION_CHANGE_GOVERMENT"
    /**
     * 玩家选择建造奇迹的步骤
     */
    const val ACTION_WONDER_STEP = "ACTION_WONDER_STEP"
    /**
     * 玩家选择失去人口
     */
    const val ACTION_LOSE_POPULATION = "ACTION_LOSE_POPULATION"
    /**
     * 玩家选择殖民地
     */
    const val ACTION_CHOOSE_COLONY = "ACTION_CHOOSE_COLONY"
    /**
     * 玩家选择领袖或未建成奇迹
     */
    const val ACTION_CHOOSE_INFILTRATE = "ACTION_CHOOSE_INFILTRATE"
    /**
     * 玩家选择奇迹
     */
    const val ACTION_CHOOSE_WONDER = "ACTION_CHOOSE_WONDER"
    /**
     * 玩家使用卡牌能力
     */
    const val ACTION_ACTIVE_CARD = "ACTION_ACTIVE_CARD"
    /**
     * 玩家选择科技
     */
    const val ACTION_CHOOSE_SCIENCE = "ACTION_CHOOSE_SCIENCE"
    /**
     * 玩家请求废除条约
     */
    const val REQUEST_BREAK_PACT = "REQUEST_BREAK_PACT"
    /**
     * 玩家选择废除条约
     */
    const val ACTION_BREAK_PACT = "ACTION_BREAK_PACT"

    /**
     * 政治行动 - 结束政治行动
     */
    const val POLITICAL_PASS = "POLITICAL_PASS"
    /**
     * 政治行动 - 放置事件牌
     */
    const val POLITICAL_EVENT = "POLITICAL_EVENT"
    /**
     * 政治行动 - 弃军事牌
     */
    const val POLITICAL_DISCARD = "POLITICAL_DISCARD"
    /**
     * 体面退出游戏
     */
    const val RESIGN = "RESIGN"
    /**
     * 查理曼
     */
    const val ACTION_CHARLEMAGNE = "ACTION_CHARLEMAGNE"
    /**
     * 2选1
     */
    const val ACTION_SCORE_BETWEEN = "ACTION_SCORE_BETWEEN"
    /**
     * 谷物交易
     */
    const val ACTION_EXCHANGE = "ACTION_EXCHANGE"
    /**
     * 选择建造或升级
     */
    const val ACTION_BUILD_UPGRADE = "ACTION_BUILD_UPGRADE"
    /**
     * 捷克老祖
     */
    const val ABILITY_CZECH = "ABILITY_CZECH"
    /**
     * 新版文明发展
     */
    const val ACTION_DEV_OF_CIV = "ACTION_DEV_OF_CIV"
}
