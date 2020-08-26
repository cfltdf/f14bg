package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.exception.BoardGameException


abstract class TSListenerExecutor(trigPlayer: TSPlayer, gameMode: TSGameMode, protected val initParam: ActionInitParam) : TSExecutor(trigPlayer, gameMode) {
    /**
     * 取得触发行动的主动玩家

     * @return
     */
    protected var initiativePlayer: TSPlayer? = null

    init {
        this.loadInitParam()
    }

    @Throws(BoardGameException::class)
    abstract override fun execute()

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

}
