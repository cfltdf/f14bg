package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoConsts
import com.f14.innovation.consts.InnoSplayDirection
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #038-发明 执行器

 * @author F14eagle
 */
class InnoCustom038Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        val player = this.targetPlayer
        // 如果你版图中5中颜色的置顶牌都已经展开,则获得成就奇迹
        if (this.isAllTopCardSplayed(player)) {
            val card = gameMode.achieveManager.specialAchieveCards.getCardByIndex(InnoConsts.SPECIAL_ACHIEVE_WONDER)
            if (card != null) {
                gameMode.game.playerAddSpecialAchieveCard(player, card)
                this.setPlayerActived(player)
            }
        }
    }

    /**
     * 判断玩家是否所有5种颜色的置顶牌都已经展开

     * @param player

     * @return
     */
    private fun isAllTopCardSplayed(player: InnoPlayer): Boolean {
        return InnoColor.values().map(player::getCardStack).none { it == null || it.empty || it.splayDirection == InnoSplayDirection.NULL }
    }

}
