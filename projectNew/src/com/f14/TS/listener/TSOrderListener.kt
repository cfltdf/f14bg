package com.f14.TS.listener

import com.f14.TS.TSConfig
import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.TSReport
import com.f14.bg.listener.OrderActionListener

/**
 * TS的玩家顺序监听器

 * @author F14eagle
 */
abstract class TSOrderListener(protected var gameMode: TSGameMode) : OrderActionListener<TSPlayer, TSConfig, TSReport>(gameMode) {


    override val playersByOrder: List<TSPlayer>
        get() = gameMode.game.getPlayersByOrder(gameMode.firstPlayer)
}
