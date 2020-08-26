package com.f14.innovation.executor

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoChooseSpecificCardListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 创建选择卡牌的监听器的执行器

 * @author F14eagle
 */
class InnoChooseCardExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        if (this.resultCards.isNotEmpty()) {
            // 创建一个实际执行效果的监听器
            val al = InnoChooseSpecificCardListener(gameMode, this.targetPlayer, this.initParam!!, this.resultParam, this.ability, this.abilityGroup)
            // 将结果参数中的牌设为待选牌
            al.specificCards.addCards(this.resultCards)
            // 插入监听器
            this.commandList.insertInterrupteListener(al)
        }
    }

}
