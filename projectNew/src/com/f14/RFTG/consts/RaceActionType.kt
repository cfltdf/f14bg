package com.f14.RFTG.consts

import com.f14.bg.exception.BoardGameException


/**
 * 银河竞逐的行动类型

 * @author F14eagle
 */
enum class RaceActionType {
    /**
     * 探索 - +1+1
     */
    EXPLORE_1,

    /**
     * 探索 - +5
     */
    EXPLORE_2,

    /**
     * 开发
     */
    DEVELOP,

    /**
     * 2人规则 - 开发
     */
    DEVELOP_2,

    /**
     * 扩张
     */
    SETTLE,

    /**
     * 2人规则 - 扩张
     */
    SETTLE_2,

    /**
     * 消费 - 交易
     */
    CONSUME_1,

    /**
     * 消费 - 2VP
     */
    CONSUME_2,

    /**
     * 生产
     */
    PRODUCE;

    val chinese: String
        get() = when (this) {
            EXPLORE_1 -> "[探索+1/+1]"
            EXPLORE_2 -> "[探索+5/+0]"
            DEVELOP -> "[开发]"
            DEVELOP_2 -> "[开发x2]"
            SETTLE -> "[扩张]"
            SETTLE_2 -> "[扩张x2]"
            CONSUME_1 -> "[交易]"
            CONSUME_2 -> "[消费x2]"
            PRODUCE -> "[生产]"
        }


    companion object {

        /**
         * 按照行动代码取得行动类型对象,如果没有取得对应的行动类型则抛出异常
         * @param actionCode
         * @return
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        fun getActionType(actionCode: String): RaceActionType {
            try {
                return valueOf(actionCode)
            } catch (e: Exception) {
                throw BoardGameException("无效的行动代码!")
            }

        }
    }
}
