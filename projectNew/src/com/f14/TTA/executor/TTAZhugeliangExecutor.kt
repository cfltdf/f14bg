package com.f14.TTA.executor

import com.f14.TTA.TTAResourceManager
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.component.param.RoundParam
import com.f14.bg.exception.BoardGameException


/**
 * 获取并打出牌的处理器
 * @author 吹风奈奈
 */
class TTAZhugeliangExecutor(param: RoundParam, var ability: CivilCardAbility) : TTAActionExecutor(param) {

    @Throws(BoardGameException::class)
    override fun execute() {
        try {
            val card = gameMode.game.getResourceManager<TTAResourceManager>().getCardByNo("DIYN01")?.firstOrNull()?.clone()
                    ?: throw BoardGameException("找不到所要的牌!")
            // 检查玩家是否可以拿牌
            player.checkTakeCard(card)
            gameMode.game.playerAddHand(player, card)
            gameMode.report.playerAddCardCache(player, card)
            gameMode.report.playerActiveCard(player, ability.abilityType)
        } catch (e: BoardGameException) {
            player.sendException(gameMode.game.room.id, e)
        }
    }

}
