package com.f14.TTA.executor.event

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.factory.TTAListenerFactory
import com.f14.TTA.listener.TTAEventListener
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam

/**
 * 需要交互的事件牌能力处理器

 * @author 吹风奈奈
 */
abstract class TTAAlternateAbilityExecutor(param: RoundParam, ability: EventAbility) : TTAEventAbilityExecutor(param, ability) {
    internal var listeningPlayers: List<TTAPlayer>? = null


    protected open fun createEventListener(players: Collection<TTAPlayer>): TTAEventListener? {
        return TTAListenerFactory.createEventListener(gameMode, ability, player)
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        // 取得需要监听的玩家
        val players = if (trigPlayer === null) gameMode.getPlayersByChooser(ability.chooser) else listOfNotNull(trigPlayer)
        if (players.isNotEmpty()) {
            // 创建并添加事件交互监听器
            val l = this.createEventListener(players)
            if (l != null) {
                // 为监听器添加需要监听的玩家
                l.addListeningPlayers(players)
                val result = gameMode.insertListener(l)
                gameMode.game.getPlayersByOrder(player).filter(players::contains).forEach { this.processPlayer(result, it) }
            }
        }
    }

    protected abstract fun processPlayer(result: InterruptParam, p: TTAPlayer)
}
