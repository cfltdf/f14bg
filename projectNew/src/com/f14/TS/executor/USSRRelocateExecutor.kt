package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.ActionType
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.exception.BoardGameException
import kotlin.math.abs

class USSRRelocateExecutor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        // 开始监听时,先为监听的玩家创建移除点数的监听器
        var param = initParam.clone()
        // 移除点数时不受条件影响
        param.clearConditionGroup()
        param.actionType = ActionType.ADJUST_INFLUENCE
        param.num = -abs(initParam.num)
        param.limitNum = 0
        param.msg = "请先移除需要重新分配的影响力,最多可以选择 {num} 点!"
        param.isCanLeft = true
        var executor = TSAdjustInfluenceExecutor(trigPlayer, gameMode, param)
        executor.execute()
        if (executor.adjustNum > 0) {
            param = initParam.clone()
            // 只有当存在调整数量时,才会创建添加影响力的监听器
            param.actionType = ActionType.ADJUST_INFLUENCE
            param.num = executor.adjustNum
            param.isCanLeft = false
            param.msg = "请在非美国控制的国家重新分配 {num} 点影响力,每个国家最多可以分配 {limitNum} 点!"
            executor = TSAdjustInfluenceExecutor(trigPlayer, gameMode, param)
            executor.execute()
        }
    }

}
