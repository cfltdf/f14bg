package com.f14.innovation.listener

import com.f14.bg.listener.ListenerType
import com.f14.bg.listener.OrderActionListener
import com.f14.innovation.InnoConfig
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.InnoReport

/**
 * Inno的玩家顺序监听器

 * @author F14eagle
 */
abstract class InnoOrderListener(protected var gameMode: InnoGameMode, protected var startPlayer: InnoPlayer, listenerType: ListenerType) : OrderActionListener<InnoPlayer, InnoConfig, InnoReport>(gameMode, listenerType) {


    override val playersByOrder: List<InnoPlayer>
        get() {
            return gameMode.game.getPlayersByOrder(this.startPlayer)
        }
}
