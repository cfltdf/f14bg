package com.f14.RFTG.consts

import com.f14.RFTG.card.*


/**
 * 游戏状态

 * @author F14eagle
 */
enum class GameState {
    /**
     * 游戏开始时弃牌
     */
    STARTING_DISCARD,
    /**
     * 回合结束时弃牌
     */
    ROUND_DISCARD,
    /**
     * 玩家选择行动阶段
     */
    CHOOSE_ACTION,
    /**
     * 探索阶段
     */
    ACTION_EXPLORE,
    /**
     * 开发阶段
     */
    ACTION_DEVELOP,
    /**
     * 开发2阶段
     */
    ACTION_DEVELOP_2,
    /**
     * 扩张阶段
     */
    ACTION_SETTLE,
    /**
     * 扩张2阶段
     */
    ACTION_SETTLE_2,
    /**
     * 交易阶段
     */
    ACTION_TRADE,
    /**
     * 消费阶段
     */
    ACTION_CONSUME,
    /**
     * 生产阶段
     */
    ACTION_PRODUCE;


    companion object {

        /**
         * 按照阶段取得对应的能力类
         * @param state
         * @return
         */
        fun getPhaseClass(state: GameState?): Class<out Ability>? {
            return when (state) {
                ACTION_EXPLORE -> ExploreAbility::class.java
                ACTION_DEVELOP -> DevelopAbility::class.java
                ACTION_SETTLE -> SettleAbility::class.java
                ACTION_TRADE -> TradeAbility::class.java
                ACTION_CONSUME -> ConsumeAbility::class.java
                ACTION_PRODUCE -> ProduceAbility::class.java
                else -> null
            }
        }
    }
}
