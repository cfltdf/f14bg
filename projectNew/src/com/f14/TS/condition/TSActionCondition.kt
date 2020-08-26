package com.f14.TS.condition

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.listener.initParam.ConditionInitParam
import com.f14.bg.exception.BoardGameException


/**
 * TS的条件判断类

 * @author F14eagle
 */
abstract class TSActionCondition(protected val trigPlayer: TSPlayer, protected val gameMode: TSGameMode, protected val initParam: ConditionInitParam) {

    /**
     * 取得触发行动的主动玩家
     * @return
     */
    protected lateinit var initiativePlayer: TSPlayer

    init {
        this.loadInitParam()
    }

    /**
     * 返回参数中的卡牌
     * @return
     */

    protected val card: TSCard
        get() = this.initParam.card!!

    /**
     * 装载初始化参数

     */
    protected fun loadInitParam() {
        // 设置主动玩家
        val player = gameMode.game.getPlayer(this.initParam.listeningPlayer)
        if (player != null) {
            initiativePlayer = player
        }
    }

    /**
     * 执行条件判断
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    abstract fun test(): Boolean
}
