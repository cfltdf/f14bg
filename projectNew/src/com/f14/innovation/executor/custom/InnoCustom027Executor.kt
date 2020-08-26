package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.listener.custom.InnoCustom027P1Listener
import com.f14.innovation.listener.custom.InnoCustom027P2Listener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoParamFactory
import com.f14.innovation.param.InnoResultParam

/**
 * #027-医药 执行器

 * @author F14eagle
 */
class InnoCustom027Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.targetPlayer
        var actived = false
        if (!player.scores.isEmpty) {
            actived = true
            // 创建一个实际执行效果的监听器(选择计分区中最高时期的1张牌给对方)
            val initParam = InnoParamFactory.createInitParam()
            initParam.msg = "我要求你用你计分区中1张最高时期的牌,与我计分区中1张最低时期的牌交换!"
            val al = InnoCustom027P1Listener(gameMode, player, initParam, this.resultParam, this.ability, this.abilityGroup)
            al.specificCards.addCards(player.scores.getCards())
            this.commandList.insertInterrupteListener(al)
        }

        val mainPlayer = this.mainPlayer
        if (!mainPlayer.scores.isEmpty) {
            actived = true
            // 创建一个实际执行效果的监听器(选择计分区中最低时期的1张牌给对方)
            val initParam = InnoParamFactory.createInitParam()
            initParam.msg = "选择计分区中1张最低时期的牌作为交换!"
            val al = InnoCustom027P2Listener(gameMode, this.mainPlayer, initParam, this.resultParam, this.ability, this.abilityGroup)
            al.specificCards.addCards(mainPlayer.scores.getCards())
            this.commandList.insertInterrupteListener(al)
        }

        if (actived) {
            this.setPlayerActived(player)
        }
    }

}
