package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.listener.InnoChooseScoreListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #030-教育 监听器

 * @author F14eagle
 */
class InnoCustom030Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseScoreListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun canChooseCard(player: InnoPlayer, card: InnoCard): Boolean {
        // 只能选择等级最高的牌
        val maxLevel = player.scores.maxLevel
        return card.level >= maxLevel && super.canChooseCard(player, card)
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 你可以选择并归还一张你计分区最高时期的牌,若你如此做,抓一
        // 张比你计分区中剩余的最高时期的牌高两个等级的牌!
        cards.map { gameMode.game.playerRemoveScoreCard(player, it) }.forEach { gameMode.game.playerReturnCard(player, it) }

        val level = player.scores.maxLevel
        gameMode.game.playerDrawCard(player, level + 2, 1)
    }

}
