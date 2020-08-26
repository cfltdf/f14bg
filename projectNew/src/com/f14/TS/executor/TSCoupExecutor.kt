package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCountry
import com.f14.TS.consts.EffectType
import com.f14.TS.consts.TSVictoryType
import com.f14.TS.factory.InitParamFactory
import com.f14.TS.listener.TSChoiceRecoupListener
import com.f14.TS.listener.TSCoupListener
import com.f14.TS.listener.TSCoupListener.CoupParam
import com.f14.TS.listener.initParam.ChoiceInitParam
import com.f14.TS.listener.initParam.OPActionInitParam
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException

class TSCoupExecutor(trigPlayer: TSPlayer, gameMode: TSGameMode, private var param: OPActionInitParam) : TSExecutor(trigPlayer, gameMode) {
    var confirm: Boolean = false
    var success: Boolean = false
    var targetCountry: TSCountry? = null

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = gameMode.game.getPlayer(param.listeningPlayer)!!
        if (player.hasEffect(EffectType.COUP_TO_LOSE)) {
            // 只要有这个效果在,就会创建一个用来移除该效果的监听器
            val card = gameMode.cardManager.getCardByCardNo(40)
            val ip = InitParamFactory.createActionInitParam(player, card, null)
            val executor = Custom40Executor(player, gameMode, ip)
            executor.execute()
            if (!executor.removed) {
                return
            }
        }
        val cl = TSCoupListener(trigPlayer, gameMode, param)
        val res = gameMode.insertListener(cl)
        val confirmString = res.getString("confirmString")
        if (ConfirmString.CONFIRM == confirmString) {
            val cp = res.get<CoupParam>("coupParam")!!
            // 执行政变
            cp.coup()
            if (player.hasEffect(EffectType.SR_PRIVILEGE_N3)) {
                if (!player.params.getBoolean(EffectType.SR_PRIVILEGE_N3)) {
                    val cip = ChoiceInitParam()
                    cip.targetPower = player.superPower
                    cip.listeningPlayer = player.superPower
                    cip.isCanPass = (false)
                    cip.isCanCancel = (false)
                    cip.msg = "你的投掷结果为: " + cp.adjustParam!!.num + " ,是否重新投掷?"
                    val sl = TSChoiceRecoupListener(player, gameMode, cip)
                    val sr = gameMode.insertListener(sl)
                    if (sr.getInteger("choice") == 1) {
                        player.params.setRoundParameter(EffectType.SR_PRIVILEGE_N3, true)
                        cp.recoup()
                    }
                }
            }
            // 输出战报信息
            gameMode.report.playerDoAction(player, cp.adjustParam!!)
            // 应用政变结果
            cp.applyCoup()
            // 向所有玩家发送影响力的调整结果
            gameMode.game.sendCountryInfo(cp.adjustParam!!.orgCountry, null)

            this.success = cp.success
            this.targetCountry = res.get<TSCountry>("targetCountry")

            // 非无偿的政变可以得到牌OP点数的军事行动
            if (!param.isFreeAction) {
                gameMode.game.playerAdjustMilitaryAction(player, cp.adjustParam!!.op)
            }

            // 检查玩家是否在进行政变后会输掉游戏
            if (player.hasEffect(EffectType.COUP_TO_LOSE)) {
                val opposite = gameMode.game.getOppositePlayer(player.superPower)
                gameMode.game.playerWin(opposite, TSVictoryType.SPECIAL)
            }

            // 如果调整的是战场国,则需要降低DEFCON等级
            if (cp.adjustParam!!.orgCountry.isBattleField) {
                // 检查玩家是否有政变不改变DEFCON的能力
                if (!player.hasEffect(EffectType.FREE_DEFCON_COUP)) {
                    gameMode.game.adjustDefcon(-1)
                }
            }

            // 检查玩家是否有#111-尤里和萨曼莎
            if (player.hasEffect(EffectType.EFFECT_109)) {
                // 如果有,则给对手+1VP
                val opposite = gameMode.game.getOppositePlayer(player.superPower)
                gameMode.game.adjustVp(opposite, 1)
            }

            this.confirm = true
        }
    }

}
