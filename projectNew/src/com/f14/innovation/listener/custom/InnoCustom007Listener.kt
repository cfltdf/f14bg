package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoConsts
import com.f14.innovation.listener.InnoChooseHandListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #007-石砖工艺 监听器

 * @author F14eagle
 */
class InnoCustom007Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 可以合并任意张带有城堡符号的牌
        for (card in cards) {
            gameMode.game.playerMeldHandCard(player, card)
        }
        // 如果融合的牌达到四张或以上时,立即获得特殊成就"纪念碑"
        if (cards.size >= 4) {
            val card = gameMode.achieveManager.specialAchieveCards.getCardByIndex(InnoConsts.SPECIAL_ACHIEVE_MEMORIAL)
            if (card != null) {
                gameMode.game.playerAddSpecialAchieveCard(player, card)
            }
        }
    }

}
