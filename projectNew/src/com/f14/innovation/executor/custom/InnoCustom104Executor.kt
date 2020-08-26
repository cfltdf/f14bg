package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.listener.custom.InnoCustom104Listener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #104-自助服务 执行器

 * @author F14eagle
 */
class InnoCustom104Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 执行除了自助服务外其他指定的效果
        val player = this.targetPlayer
        val cards = player.topCards.filter { it.cardIndex != 104 }
        if (cards.isNotEmpty()) {
            this.setPlayerActived(player)
            // 创建一个实际执行效果的监听器
            val al = InnoCustom104Listener(gameMode, player, this.initParam!!, this.resultParam, this.ability, this.abilityGroup)
            al.specificCards.addCards(cards)
            // 插入监听器
            this.commandList.insertInterrupteListener(al)
        }
    }

}
