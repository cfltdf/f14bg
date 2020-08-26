package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.executor.TTAIncreasePopExecutor
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils

/**
 * 扩张人口的处理器
 * @author 吹风奈奈
 */
class TTAActiveIncreasePopExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    override fun check() {
        super.check()
        val i = card.activeAbility!!.property.getProperty(CivilizationProperty.YELLOW_TOKEN)
        CheckUtils.check(player.tokenPool.availableWorkers <= i, "你已经没有可用的人口了!")
    }

    @Throws(BoardGameException::class)
    override fun active() {
        gameMode.game.playerAddToken(player, card.activeAbility!!.property, -1)
        val executor = TTAIncreasePopExecutor(param)
        executor.actionCost = 0
        executor.costModify = card.activeAbility!!.property.getProperty(CivilizationProperty.FOOD)
        executor.cached = true
        executor.execute()
        this.actived = true
    }

}
