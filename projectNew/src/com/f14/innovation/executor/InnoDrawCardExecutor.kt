package com.f14.innovation.executor

import com.f14.bg.anim.AnimVar
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoAnimPosition
import com.f14.innovation.consts.InnoVictoryType
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 摸牌的行动

 * @author F14eagle
 */
class InnoDrawCardExecutor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        for (i in 0 until this.initParam!!.num) {
            val card = this.gameMode.drawDecks.draw(this.initParam.level)
            if (card == null) {
                // 如果取不到牌,则结束游戏,将比较玩家得分
                this.gameMode.setVictory(InnoVictoryType.SCORE_VICTORY, null, null)
                break
            } else {
                // 摸的牌加入到返回参数中
                val from = AnimVar.createAnimVar(InnoAnimPosition.DRAW_DECK, "", card.level)
                this.resultParam.addCard(card)
                this.resultParam.putAnimVar(card, from)
                // 刷新牌堆信息
                this.game.sendDrawDeckInfo(null)
            }
        }
    }

}
