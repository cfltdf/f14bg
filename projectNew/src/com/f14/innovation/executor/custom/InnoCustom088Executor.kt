package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.listener.custom.InnoCustom088P1Listener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #088-协同合作 执行器

 * @author F14eagle
 */
class InnoCustom088Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 抓两张[9]展示
        val player = this.mainPlayer
        val cards = gameMode.game.playerDrawCard(player, 9, 2, true)
        // 选择其中1张融合,另一张给当前执行玩家融合
        // 创建一个实际执行效果的监听器
        val al = InnoCustom088P1Listener(gameMode, player, this.initParam!!, this.resultParam, this.ability, this.abilityGroup)
        al.specificCards.addCards(cards)
        // 插入监听器
        this.commandList.insertInterrupteListener(al)
    }

}
