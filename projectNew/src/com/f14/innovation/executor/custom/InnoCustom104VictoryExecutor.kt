package com.f14.innovation.executor.custom

import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoVictoryExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #104-自助服务 判断胜利条件的执行器

 * @author F14eagle
 */
class InnoCustom104VictoryExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoVictoryExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    override // 如果你比其他玩家拥有更多的成就,你获得胜利
    val victoryPlayer: InnoPlayer?
        get() {
            val player = this.targetPlayer
            return if (gameMode.game.players.any { it !== player && it.achieveCards.size >= player.achieveCards.size }) null else player
        }

}
