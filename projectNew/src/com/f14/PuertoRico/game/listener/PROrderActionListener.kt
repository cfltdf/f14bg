package com.f14.PuertoRico.game.listener

import com.f14.PuertoRico.game.PRGameMode
import com.f14.PuertoRico.game.PRPlayer
import com.f14.PuertoRico.game.PrConfig
import com.f14.PuertoRico.game.PrReport
import com.f14.bg.listener.OrderActionListener

/**
 * 按顺序逐个执行命令的监听器
 * @author F14eagle
 */
abstract class PROrderActionListener(protected val gameMode: PRGameMode) : OrderActionListener<PRPlayer, PrConfig, PrReport>(gameMode) {
    override val playersByOrder
        get() = gameMode.game.playersByOrder
}
