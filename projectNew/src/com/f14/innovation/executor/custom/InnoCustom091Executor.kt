package com.f14.innovation.executor.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.executor.InnoActionExecutor
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import java.util.*

/**
 * #091-核裂变 执行器

 * @author F14eagle
 */
class InnoCustom091Executor(gameMode: InnoGameMode, player: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoActionExecutor(gameMode, player, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    public override fun doAction() {
        // 抓1张[10]展示,如果是红色就清场...
        val player = this.targetPlayer
        val cards = gameMode.game.playerDrawCard(player, 10, 1, true)
        if (cards.isNotEmpty()) {
            if (cards[0].color == InnoColor.RED) {
                // 清场了清场了....
                for (p in gameMode.game.players) {
                    val hands = ArrayList(p.hands.getCards())
                    val scores = ArrayList(p.scores.getCards())
                    p.clearAllCards()
                    // 发送移除卡牌的指令
                    gameMode.game.sendPlayerRemoveHandsResponse(p, hands, null)
                    gameMode.game.sendPlayerRemoveScoresResponse(p, scores, null)
                    gameMode.game.sendPlayerCardStacksInfoResponse(p, null)
                    gameMode.game.sendPlayerIconsInfoResponse(p, null)
                }
                // 移除该卡牌其他的行动
                this.commandList.clear()
            }
        }
    }

}
