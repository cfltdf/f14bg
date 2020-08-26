package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.action.ActionParam
import com.f14.TS.consts.EffectType
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.ability.ActionParamType
import com.f14.TS.factory.GameActionFactory
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #92-恐怖主义 的执行器

 * @author F14eagle
 */
class Custom92Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        // 对手弃掉1张牌
        val player = this.initiativePlayer!!
        val target = gameMode.game.getOppositePlayer(player.superPower)
        var num = 1
        // 如果对手是美国,并且有需要多丢牌的效果,则丢2张
        if (target.superPower == SuperPower.USA && target.hasEffect(EffectType.EFFECT_82)) {
            num += 1
        }

        val param = ActionParam()
        param.paramType = ActionParamType.RANDOM_DISCARD_CARD
        param.targetPower = target.superPower
        param.num = num
        val action = GameActionFactory.createGameAction(gameMode, target, null, param)
        val ar = gameMode.game.executeAction(action)
        // 把弃掉的牌丢到弃牌堆中
        if (!ar.cards.isEmpty()) {
            for (card in ar.cards) {
                gameMode.game.discardCard(card)
            }
        }
    }

}
