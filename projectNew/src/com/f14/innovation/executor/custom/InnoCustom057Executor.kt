package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.listener.custom.InnoCustom057P1Listener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #057-分类学 执行器

 * @author F14eagle
 */
class InnoCustom057Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 拿取所有其他玩家手中该颜色牌
        val player = this.targetPlayer
        if (this.resultCards.isNotEmpty()) {
            // 只要取第一张牌的颜色即可
            val color = this.resultCards[0].color!!
            for (p in gameMode.game.players) {
                if (p !== player && !gameMode.game.isTeammates(p, player)) {
                    // 取得所有指定颜色的手牌
                    val cards = p.getHandsByColor(color)
                    if (cards.isNotEmpty()) {
                        cards
                                // 转移手牌
                                .map { gameMode.game.playerRemoveHandCard(p, it) }.forEach { gameMode.game.playerAddHandCard(player, it) }

                    }
                }
            }
            // 创建一个实际执行效果的监听器
            val al = InnoCustom057P1Listener(gameMode, this.targetPlayer, this.initParam!!, this.resultParam, this.ability, this.abilityGroup)
            // 将玩家所有指定颜色的手牌设为待选牌
            al.specificCards.addCards(player.getHandsByColor(color))
            // 插入监听器
            this.commandList.insertInterrupteListener(al)
        }
    }

}
