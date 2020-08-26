package com.f14.innovation.executor

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoVictoryType
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 获胜判断的执行器

 * @author F14eagle
 */
abstract class InnoVictoryExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.victoryPlayer
        if (player != null) {
            this.setPlayerActived(player)
            gameMode.setVictory(InnoVictoryType.SPECIAL_VICTORY, player, this.commandList.mainCard)
        }
    }

    /**
     * 判断是否获胜
     * @return
     */
    abstract val victoryPlayer: InnoPlayer?

}
