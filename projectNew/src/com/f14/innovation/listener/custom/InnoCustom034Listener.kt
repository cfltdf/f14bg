package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoChoosePlayerStackListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #034-罗盘 监听器

 * @author F14eagle
 */
class InnoCustom034Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChoosePlayerStackListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun canChoosePlayer(player: InnoPlayer, target: InnoPlayer): Boolean {
        // 只能选择触发能力的玩家
        return target === this.mainPlayer
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, target: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, target, cards)
        // 将目标玩家的置顶牌转移到自己的版图
        cards.map { gameMode.game.playerRemoveTopCard(target, it.color!!) }.forEach { gameMode.game.playerMeldCard(player, it) }
    }

}
