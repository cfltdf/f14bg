package com.f14.TTA.component.card

import com.f14.TTA.TTAPlayer
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.AdjustType
import com.f14.TTA.consts.CardType


/**
 * 行动点消耗对象

 * @author F14eagle
 */
class ActionCost {
    var actionType: ActionType = ActionType.MILITARY
    var actionCost: Int = 0
    var adjustType: AdjustType? = null
    var targetType: CardType? = null

    /**
     * 取得对目标玩家使用时需要的行动点数
     * @param target
     * @return
     */
    fun getActionCost(target: TTAPlayer): Int {
        return when (this.adjustType) {
            AdjustType.BY_LEVEL ->  // 按目标等级计算行动点数(暂时只有这一种方式)
                when (this.targetType) {
                    CardType.LEADER -> target.leader// 当前的领袖
                    CardType.WONDER -> target.uncompletedWonder// 在建的奇迹
                    else -> null
                }?.level ?: 0
            else -> this.actionCost
        }
    }
}
