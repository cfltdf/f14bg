package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.consts.ActionType
import com.f14.TS.consts.Region
import com.f14.TS.consts.SubRegion
import com.f14.TS.factory.InitParamFactory
import com.f14.TS.listener.Custom104Listener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

/**
 * #104-剑桥五杰 的执行器

 * @author F14eagle
 */
class Custom104Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        // 如果美国手中没有计分牌,就什么都不会发生
        val usa = gameMode.game.usaPlayer
        if (usa.hasScoreCard()) {
            // 如果有,则为苏联玩家创建一个监听器
            val ussr = gameMode.game.ussrPlayer
            val ip = InitParamFactory.createActionInitParam(ussr, card, this.initParam.trigType)
            val l = Custom104Listener(ussr, gameMode, ip)
            val res = gameMode.insertListener(l)
            val confirmString = res.getString("confirmString")
            if (ConfirmString.CONFIRM == confirmString) {
                val card = res.get<TSCard>("card")!!
                // 有可能选到东南亚..这是个子区域
                val regionDesc: String
                val c = TSCountryCondition()
                if ("SOUTHEAST_ASIA" == card.scoreRegion) {
                    val subregion = SubRegion.valueOf(card.scoreRegion!!)
                    regionDesc = SubRegion.getChineseDescr(subregion)
                    c.subRegion = subregion
                } else {
                    val region = Region.valueOf(card.scoreRegion!!)
                    regionDesc = Region.getChineseDescr(region)
                    c.region = region
                }

                // 创建一个在该区域添加1点影响力的监听器
                val initParam = ActionInitParam()
                initParam.listeningPlayer = ussr.superPower
                initParam.targetPower = ussr.superPower
                initParam.actionType = ActionType.ADJUST_INFLUENCE
                initParam.num = 1
                initParam.msg = "请在${regionDesc}分配 {num} 点影响力!"
                initParam.addWc(c)
                val executor = TSAdjustInfluenceExecutor(trigPlayer, gameMode, initParam)
                executor.execute()
            }
        }
    }

}
