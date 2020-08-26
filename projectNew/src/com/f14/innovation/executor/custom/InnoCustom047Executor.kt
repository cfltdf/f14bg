package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoConsts
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #047-天文学 执行器

 * @author F14eagle
 */
class InnoCustom047Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.targetPlayer
        // 如果你版图中非紫色置顶牌都是6级或以上,则获得特殊成就"宇宙"
        if (this.isAllTopCardTrue(player)) {
            val card = gameMode.achieveManager.specialAchieveCards.getCardByIndex(InnoConsts.SPECIAL_ACHIEVE_UNIVERSE)
            if (card != null) {
                gameMode.game.playerAddSpecialAchieveCard(player, card)
                this.setPlayerActived(player)
            }
        }
    }

    /**
     * 判断是否所有非紫色置顶牌都是6级或以上

     * @param player

     * @return
     */
    private fun isAllTopCardTrue(player: InnoPlayer): Boolean {
        val cards = player.topCards
        return if (cards.isNotEmpty()) {
            cards.none { it.color != InnoColor.PURPLE && it.level < 6 }
        } else {
            false
        }
    }

}
