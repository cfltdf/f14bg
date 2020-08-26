package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException


/**
 * TS的行动执行器

 * @author F14eagle
 */
abstract class TSActionExecutor(trigPlayer: TSPlayer, gameMode: TSGameMode, protected var initParam: ExecutorInitParam) : TSExecutor(trigPlayer, gameMode) {
    /**
     * 取得触发行动的主动玩家
     * @return
     */
    protected var initiativePlayer: TSPlayer? = null

    val card: TSCard
        get() = initParam.card!!

    init {
        this.loadInitParam()
    }

    /**
     * 执行该行动执行器

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    abstract override fun execute()

    /**
     * 装载初始化参数

     * @param initParam
     */
    protected fun loadInitParam() {
        // 设置主动玩家
        val player = gameMode.game.getPlayer(this.initParam.listeningPlayer)
        if (player != null) {
            /*
             * TSPlayer player = null; switch(this.initParam.listeningPlayer){
			 * case USA: case USSR: player =
			 * this.mode.getGame().getPlayer(this.initParam.listeningPlayer)
			 * ; break; case CURRENT_PLAYER: player =
			 * this.mode.getGame().getCurrentPlayer(); break; case
			 * PLAYED_CARD_PLAYER: player = this.trigPlayer; break; }
			 * if(player!=null){ initiativePlayer = player; }
			 */
            initiativePlayer = player
        }
    }
}
