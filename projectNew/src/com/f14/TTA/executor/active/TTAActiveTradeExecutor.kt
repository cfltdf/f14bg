package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils
import kotlin.math.abs

/**
 * 交易处理器

 * @author 吹风奈奈
 */
class TTAActiveTradeExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val food = card.activeAbility!!.property.getProperty(CivilizationProperty.FOOD)
        if (food < 0) {
            // 检查是否有足够的食物用以扣除
            CheckUtils.check(player.totalFood < abs(food), "你没有足够的食物用来交易!")
        }
        val resource = card.activeAbility!!.property.getProperty(CivilizationProperty.RESOURCE)
        if (resource < 0) {
            // 检查是否有足够的资源用以扣除
            CheckUtils.check(player.totalResource < abs(resource), "你没有足够的资源用来交易!")
        }
        // 执行食物和资源的交易行为
        gameMode.game.playerAddPoint(player, card.activeAbility!!.property)
        this.actived = true
    }

}
