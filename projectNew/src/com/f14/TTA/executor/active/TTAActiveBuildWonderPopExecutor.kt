package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.executor.TTABuildWonderExecutor
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils

/**
 * 秦始皇的处理器

 * @author 吹风奈奈
 */
class TTAActiveBuildWonderPopExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val wonder = param.player.uncompletedWonder ?: throw BoardGameException("你没有正在建造的奇迹!")
        val executor = TTABuildWonderExecutor(param, wonder)
        executor.actionCost = 0
        executor.costModify = -999
        executor.buildStep = 1
        executor.cached = true
        executor.check()
        gameMode.game.playerDecreasePopulation(player, 1)
        executor.execute()
        param.afterBuild(wonder)
        this.actived = true
    }

    @Throws(BoardGameException::class)
    override fun check() {
        player.uncompletedWonder ?: throw BoardGameException("你没有正在建造的奇迹!")
        CheckUtils.check(player.tokenPool.unusedWorkers == 0, "你没有空闲人口!")
        super.check()
    }

}
