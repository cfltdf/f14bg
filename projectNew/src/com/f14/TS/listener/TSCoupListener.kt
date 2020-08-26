package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.AdjustParam
import com.f14.TS.component.TSCountry
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.component.condition.TSCountryConditionGroup
import com.f14.TS.consts.EffectType
import com.f14.TS.consts.Region
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.OPActionInitParam
import com.f14.TS.utils.TSRoll
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import java.util.*
import kotlin.math.abs
import kotlin.math.max

/**
 * 政变的监听器

 * @author F14eagle
 */
class TSCoupListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: OPActionInitParam) : TSOpActionInterruptListener(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为监听的玩家创建参数
        for (p in this.listeningPlayers) {
            val param = CoupParam(p)
            this.setParam(p, param)
        }
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    /**
     * 政变

     * @param gameMode

     * @param action

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun coup(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val param = this.getParam<CoupParam>(player)
        val country = action.getAsString("country")
        val cty = gameMode.countryManager.getCountry(country)
        // 检查是否可以选择该国家
        if (!this.opActionInitParam.test(cty)) {
            throw BoardGameException("你不能选择这个国家!")
        }
        param.checkCoup(cty)
        // 检查是否可以政变该国家
        gameMode.validManager.checkCoup(player, cty, gameMode.turnPlayer!!, this.opActionInitParam.isFreeAction)
        param.setCoupTarget(cty)
        // 向玩家发送政变参数
        this.sendCoupParamInfo(player)
    }

    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        // 设置是否政变成功的参数
        val cp = this.getParam<CoupParam>(this.listeningPlayer)
        param["coupParam"] = cp
        param["success"] = cp.success
        if (cp.adjustParam != null) {
            param["targetCountry"] = cp.adjustParam!!.orgCountry
        }
        return param
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val param = this.getParam<CoupParam>(player)
        if (param.adjustParam == null) {
            throw BoardGameException("请选择需要政变的国家!")
        }
        // 设置已回应
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {
        val subact = action.getAsString("subact")
        if ("country" == subact) {
            // 政变
            this.coup(action)
        }
    }


    override val actionString: String
        get() = TSCmdString.ACTION_SELECT_COUNTRY

    override fun getMsg(player: TSPlayer): String {
        // CoupParam param = this.getParam<CoupParam>(player);
        return this.opActionInitParam.msg.replace("""\{num\}""".toRegex(), this.getOP(player, null).toString())
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_COUP

    @Throws(BoardGameException::class)
    override fun onStartListen() {
        super.onStartListen()
    }

    /**
     * 发送玩家的政变参数信息

     * @param gameMode

     * @param p
     */
    private fun sendCoupParamInfo(p: TSPlayer) {
        val param = this.getParam<CoupParam>(p)
        val res = this.createSubactResponse(p, "coupParam")
        if (param.adjustParam != null) {
            val country = param.adjustParam!!.orgCountry
            res.public("countryName", country.name)
            res.public("influence", country.influenceString)
            res.public("battleField", country.isBattleField)
            res.public("stabilization", country.stabilization)
        }
        gameMode.game.sendResponse(p, res)
    }

    override fun sendStartListenCommand(player: TSPlayer, receiver: TSPlayer?) {
        super.sendStartListenCommand(player, receiver)
        // 只会向指定自己发送该监听信息
        this.sendCoupParamInfo(player)
    }

    /**
     * 政变的临时参数

     * @author F14eagle
     */
    inner class CoupParam internal constructor(internal var player: TSPlayer) {

        var adjustParam: AdjustParam? = null
        var success: Boolean = false
        var pastCoup = 0

        init {
            this.init()
        }

        /**
         * 应用政变结果
         */
        fun applyCoup() {
            this.adjustParam!!.apply()
        }

        /**
         * 检查该国家是否可以进行政变

         * @param country
         * *
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        internal fun checkCoup(country: TSCountry) {
            if (!country.hasInfluence(opActionInitParam.targetPower)) {
                throw BoardGameException("该国家没有对方的影响力,不能发动政变!")
            }
        }

        /**
         * 检查修正效果
         * @return
         */
        internal fun checkModify(): Int {
            var res = 0
            // 如果政变目标是在中美洲或者南美洲,则检查是否有拉美暗杀队的效果
            val conditions = TSCountryConditionGroup()
            var c = TSCountryCondition()
            c.region = Region.CENTRAL_AMERICA
            conditions.wcs.add(c)
            c = TSCountryCondition()
            c.region = Region.SOUTH_AMERICA
            conditions.wcs.add(c)
            if (conditions.test(this.adjustParam!!.orgCountry)) {
                val effects = player.getEffects(EffectType.EFFECT_69)
                for (e in effects) {
                    res += e.num
                }
            }
            // 检查政变掷骰修正的效果
            val effects = player.getEffects(EffectType.COUP_ROLL_MODIFIER)
            for (e in effects) {
                res += e.num
            }
            // 新太空条
            val p = gameMode.game.getOppositePlayer(this.player.superPower)
            if (p.hasEffect(EffectType.SR_PRIVILEGE_N4)) {
                res -= 1
            }
            return res
        }

        /**
         * 执行政变
         */
        fun coup() {
            val r = TSRoll.roll()
            // 检查效果对掷骰结果的修正
            val modify = this.checkModify()
            this.adjustParam!!.num = r
            this.adjustParam!!.modify = modify
            // this.adjustParam.op = this.player.getOp(opActionInitParam.card);
            // this.adjustParam.op = opActionInitParam.num;
            val countries = ArrayList<TSCountry>()
            countries.add(this.adjustParam!!.orgCountry)
            this.adjustParam!!.op = getOP(this.player, countries)
            // 掷骰结果总数,不可小于0
            var res = r + modify + this.adjustParam!!.op - 2 * this.adjustParam!!.orgCountry.stabilization
            res = max(0, res)
            // 计算掷骰结果与对方影响力的差值
            val oppoi = this.adjustParam!!.orgCountry.customGetInfluence(this.adjustParam!!.adjustPower)
            val offset = oppoi - res
            if (offset < 0) {
                // 如果差值有余,则将对方影响力调到0,再给自己加上差值的影响力
                this.adjustParam!!.tempCountry.customSetInfluence(this.adjustParam!!.adjustPower, 0)
                this.adjustParam!!.tempCountry.addInfluence(this.adjustParam!!.adjustPower.oppositeSuperPower, abs(offset))
            } else {
                this.adjustParam!!.tempCountry.customSetInfluence(this.adjustParam!!.adjustPower, offset)
            }
            // 如果res>0,则表示政变成功
            if (res > 0) {
                this.success = true
            }
        }

        /**
         * 初始化参数
         */
        internal fun init() {

        }

        fun recoup() {
            val country = this.adjustParam!!.orgCountry
            this.pastCoup = this.adjustParam!!.num
            this.reset()
            this.setCoupTarget(country)
            this.coup()
        }

        /**
         * 重置调整参数
         */
        internal fun reset() {
            this.adjustParam = null
        }

        /**
         * 设置政变目标

         * @param country
         * *
         * @throws BoardGameException
         */
        internal fun setCoupTarget(country: TSCountry) {
            this.adjustParam = opActionInitParam.createAdjustParam(country)
            this.adjustParam!!.pastCoup = this.pastCoup
        }

    }

}
