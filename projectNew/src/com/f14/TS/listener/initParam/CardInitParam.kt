package com.f14.TS.listener.initParam

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.component.condition.TSCardCondition
import com.f14.TS.component.condition.TSCardConditionGroup
import com.f14.bg.component.ICondition


/**
 * 国家相关的初始化参数

 * @author F14eagle
 */
abstract class CardInitParam : InitParam(), ICondition<TSCard> {
    var conditionGroup = TSCardConditionGroup()

    /**
     * 将条件中的superPower转换成实际的superPower,并返回
     * @param gameMode
     * @param player
     * @return
     */
    fun convertConditionGroup(gameMode: TSGameMode, player: TSPlayer): TSCardConditionGroup {
        // 首先克隆一份条件组
        val res = this.conditionGroup.clone()
        fun converPower(it: TSCardCondition) {
            it.superPower = gameMode.game.convertSuperPower(it.superPower, player)
        }
        // 按照游戏内容取得实际的superPower值
        res.wcs.forEach(::converPower)
        res.bcs.forEach(::converPower)
        return res
    }

    override fun test(obj: TSCard) = this.conditionGroup.test(obj)
}
