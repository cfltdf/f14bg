package com.f14.TTA.executor.event

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.listener.InterruptParam


/**
 * 摧毁其他玩家的事件牌能力处理器
 * @author 吹风奈奈
 */
class TATEventDestroyOthersExecutor(param: RoundParam, ability: EventAbility) : TTAAlternateAbilityExecutor(param, ability) {

    override fun processPlayer(result: InterruptParam, p: TTAPlayer) {
        for (player in gameMode.realPlayers) {
            val card = result.get<List<TechCard>>(player.position)?.firstOrNull() ?: continue
            // 拆除目标玩家的建筑
            gameMode.game.playerDestroy(player, card, 1)
            gameMode.report.playerDestroy(player, card, 1)
            gameMode.report.printCache(player)
        }
    }

}
