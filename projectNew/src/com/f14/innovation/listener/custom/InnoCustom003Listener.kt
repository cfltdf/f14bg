package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.listener.InnoChooseStackListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #003-城邦制 监听器

 * @author F14eagle
 */
class InnoCustom003Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseStackListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        // 如果城堡数量小于4个,就不用执行
        return player.getIconCount(InnoIcon.CASTLE) >= 4 && super.beforeListeningCheck(player)
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 将该牌转移到触发玩家的版图中
        cards.map { gameMode.game.playerRemoveTopCard(player, it.color!!) }.forEach { gameMode.game.playerMeldCard(this.mainPlayer, it) }
    }

}
