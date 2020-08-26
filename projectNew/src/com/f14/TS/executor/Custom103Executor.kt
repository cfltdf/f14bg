package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.action.TSEffect
import com.f14.TS.consts.EffectType
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TSActionPhase
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #103-背叛者 的执行器

 * @author F14eagle
 */
class Custom103Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        if (gameMode.actionPhase == TSActionPhase.HEADLINE) {
            // 如果是在头条阶段,则给苏联方添加取消头条的效果
            val ussr = gameMode.game.ussrPlayer
            val effect = TSEffect()
            effect.effectType = EffectType.CANCEL_HEADLINE
            ussr.addEffect(card, effect)
        } else {
            // 如果是在其他阶段由苏联打出,则给美国1VP
            val player = this.initiativePlayer!!
            if (player.superPower == SuperPower.USSR) {
                gameMode.game.adjustVp(-1)
            }
        }
    }

}
