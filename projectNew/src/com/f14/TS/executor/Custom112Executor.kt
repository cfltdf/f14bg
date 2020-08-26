package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCountry
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.consts.ActionType
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.TS.listener.initParam.OPActionInitParam
import com.f14.bg.exception.BoardGameException


class Custom112Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {

    /**
     * 创建政变监听器
     * @param gameMode
     * @param targetCountry - 不能进行政变的国家
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    private fun createCoupExecutor(gameMode: TSGameMode, targetCountry: TSCountry?): TSCoupExecutor {
        // 创建监听器参数
        val player = this.initiativePlayer!!
        val initParam = OPActionInitParam()
        initParam.listeningPlayer = player.superPower
        initParam.targetPower = player.superPower.oppositeSuperPower
        initParam.actionType = ActionType.COUP
        initParam.card = this.card
        initParam.trigType = this.initParam.trigType
        initParam.isCanCancel = this.initParam.isCanCancel
        initParam.isCanPass = this.initParam.isCanPass
        initParam.num = this.initParam.num
        initParam.msg = this.initParam.msg
        initParam.isFreeAction = false
        // 取得条件组的副本,否则可能会出问题
        initParam.conditionGroup = this.initParam.conditionGroup.clone()
        if (targetCountry != null) {
            // 如果存在不能政变的国家,则加入到条件中
            val bc = TSCountryCondition()
            bc.country = targetCountry.country
            initParam.addBc(bc)
        }
        // 创建监听器
        return TSCoupExecutor(player, gameMode, initParam)
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        var executor = this.createCoupExecutor(gameMode, null)
        executor.execute()
        if (executor.confirm && executor.success) {
            val targetCountry = executor.targetCountry
            executor = this.createCoupExecutor(gameMode, targetCountry)
            executor.execute()
        }
    }

}
