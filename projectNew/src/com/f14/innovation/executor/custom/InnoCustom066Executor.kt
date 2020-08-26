package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.listener.custom.InnoCustom066P1Listener
import com.f14.innovation.listener.custom.InnoCustom066P2Listener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoParamFactory
import com.f14.innovation.param.InnoResultParam

/**
 * #066-公共卫生 执行器

 * @author F14eagle
 */
class InnoCustom066Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 拿取所有其他玩家手中该颜色牌
        val player = this.targetPlayer
        var actived = false
        if (!player.hands.isEmpty) {
            actived = true
            // 创建一个实际执行效果的监听器(选择最高时期的2张牌给对方)
            val initParam = InnoParamFactory.createInitParam()
            initParam.msg = "我要求你选择2张最高时期的手牌,与我选择的1张最低时期的手牌交换!"
            val al = InnoCustom066P1Listener(gameMode, player, initParam, this.resultParam, this.ability, this.abilityGroup)
            al.specificCards.addCards(player.hands.getCards())
            this.commandList.insertInterrupteListener(al)
        }

        val mainPlayer = this.mainPlayer
        if (!mainPlayer.hands.isEmpty) {
            actived = true
            // 创建一个实际执行效果的监听器(选择最低时期的1张牌给对方)
            val initParam = InnoParamFactory.createInitParam()
            initParam.msg = "选择1张最低时期的手牌作为交换!"
            val al = InnoCustom066P2Listener(gameMode, this.mainPlayer, initParam, this.resultParam, this.ability, this.abilityGroup)
            al.specificCards.addCards(mainPlayer.hands.getCards())
            this.commandList.insertInterrupteListener(al)
        }

        if (actived) {
            this.setPlayerActived(player)
        }
    }

}
