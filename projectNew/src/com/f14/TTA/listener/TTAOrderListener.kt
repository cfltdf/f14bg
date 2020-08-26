package com.f14.TTA.listener

import com.f14.TTA.TTAConfig
import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.TTAReport
import com.f14.bg.listener.OrderActionListener

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

abstract class TTAOrderListener(var gameMode: TTAGameMode) : OrderActionListener<TTAPlayer, TTAConfig, TTAReport>(gameMode) {

    override val playersByOrder: List<TTAPlayer>
        get() = gameMode.game.getPlayersByOrder(gameMode.game.startPlayer!!)
}
