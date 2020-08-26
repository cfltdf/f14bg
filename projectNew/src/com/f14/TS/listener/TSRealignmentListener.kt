package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.action.TSEffect
import com.f14.TS.component.RealignmentAdjustParam
import com.f14.TS.component.TSCountry
import com.f14.TS.consts.EffectType
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.RealignmentInitParam
import com.f14.TS.utils.TSRoll
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import java.util.*

/**
 * 调整阵营的监听器

 * @author F14eagle
 */
class TSRealignmentListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: RealignmentInitParam) : TSOpActionInterruptListener(trigPlayer, gameMode, initParam) {
    private var param: RealignmentParam? = null

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为监听的玩家创建参数
        for (p in this.listeningPlayers) {
            if (this.param == null) {
                this.param = RealignmentParam(p)
            }
            // param.leftNum = this.realignInitParam.num;
            this.setParam(p, param!!)
        }
    }

    override fun canCancel(action: GameAction): Boolean {
        val player = action.getPlayer<TSPlayer>()
        val param = this.getParam<RealignmentParam>(player)
        // 如果玩家已经用过了点数,则不能取消
        return param.usedNum <= 0 && super.canCancel(action)
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        // 将玩家实际用掉的点数设置在返回参数中
        val player = this.listeningPlayer
        val param = this.getParam<RealignmentParam>(player)
        res["realignmentParam"] = param
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val param = this.getParam<RealignmentParam>(player)
        if (param.adjustParam == null) {
            throw BoardGameException("请选择需要调整阵营的国家!")
        }
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {
        val subact = action.getAsString("subact")
        if ("country" == subact) {
            // 调整阵营
            this.realignment(action)
        }
    }


    override val actionString: String
        get() = TSCmdString.ACTION_SELECT_COUNTRY

    val realignInitParam: RealignmentInitParam
        get() = super.initParam as RealignmentInitParam

    override fun getMsg(player: TSPlayer): String {
        // InfluenceParam param = this.getParam<RealignmentParam>(player)!!;
        return this.initParam.msg.replace("""\{num\}""".toRegex(), this.getOP(player, null).toString())
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_REALIGNMENT

    /**
     * 调整阵营

     * @param action

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun realignment(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val param = this.getParam<RealignmentParam>(player)
        val country = action.getAsString("country")
        val cty = gameMode.countryManager.getCountry(country)
        // 检查是否可以选择该国家
        if (!this.realignInitParam.test(cty)) {
            throw BoardGameException("你不能选择这个国家!")
        }
        param.checkRealignment(cty)
        gameMode.validManager.checkRealignment(player, cty, gameMode.turnPlayer!!, this.realignInitParam.isFreeAction)
        // 调整影响力
        param.setRealignmentTarget(cty)
        this.sendRealignmentParamInfo(player)
    }

    /**
     * 发送玩家的调整阵营参数信息

     * @param p
     */
    private fun sendRealignmentParamInfo(p: TSPlayer) {
        val param = this.getParam<RealignmentParam>(p)
        val res = this.createSubactResponse(p, "realignmentParam")
        if (param.adjustParam != null) {
            val country = param.adjustParam!!.orgCountry
            res.public("countryName", country.name)
            res.public("influence", country.influenceString)
            res.public("ussrBonus", param.adjustParam!!.getRealignmentInfo(SuperPower.USSR).bonus)
            res.public("usaBonus", param.adjustParam!!.getRealignmentInfo(SuperPower.USA).bonus)
        }
        gameMode.game.sendResponse(p, res)
    }

    override fun sendStartListenCommand(player: TSPlayer, receiver: TSPlayer?) {
        super.sendStartListenCommand(player, receiver)
        // 只会向指定自己发送该监听信息
        this.sendRealignmentParamInfo(player)
    }

    fun setRealignmentParam(param: RealignmentParam?) {
        this.param = param
    }

    /**
     * 调整影响力的临时参数

     * @author F14eagle
     */
    inner class RealignmentParam internal constructor(internal var player: TSPlayer) {
        // int leftNum;

        var adjustParam: RealignmentAdjustParam? = null
        var usedNum: Int = 0
        internal var countries: MutableCollection<TSCountry> = HashSet()

        init {
            this.init()
        }

        /**
         * 确定调整阵营
         */
        fun applyRealignment() {
            this.adjustParam!!.apply()
        }

        /**
         * 检查调整阵营的修正值

         * @return
         */
        internal fun checkRealigmentModifier(superPower: SuperPower, country: TSCountry): Int {
            val player = gameMode.game.getPlayer(superPower)!!
            val effects = player.getEffects(EffectType.REALIGMENT_ROLL_MODIFIER)
            return effects.sumBy(TSEffect::num)
        }

        /**
         * 检查该国家是否可以调整阵营

         * @param country
         * *
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        internal fun checkRealignment(country: TSCountry) {
            if (!country.hasInfluence(realignInitParam.targetPower)) {
                throw BoardGameException("该国家没有对方的影响力,不能调整阵营!")
            }
        }

        /**
         * 取得剩余的OP

         * @return
         */
        val leftOP: Int
            get() = getOP(player, countries) - usedNum

        /**
         * 检查是否拥有剩余的op对country进行调整阵营的操作
         * @param country
         * @return
         */
        internal fun hasLeftOP(country: TSCountry): Boolean {
            val countries = HashSet(this.countries)
            countries.add(country)
            val op = getOP(player, countries)
            return this.usedNum < op
        }

        /**
         * 初始化参数
         */
        internal fun init() = Unit

        /**
         * 调整阵营
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        fun realignment() {
            val adjustParam = this.adjustParam!!
            if (!this.hasLeftOP(adjustParam.tempCountry)) {
                throw BoardGameException("你没有多余的OP了!")
            }

            val ussr = adjustParam.getRealignmentInfo(SuperPower.USSR)
            val usa = adjustParam.getRealignmentInfo(SuperPower.USA)
            ussr.roll = TSRoll.roll()
            usa.roll = TSRoll.roll()
            // 调整值修正
            ussr.modify = this.checkRealigmentModifier(SuperPower.USSR, adjustParam.tempCountry)
            usa.modify = this.checkRealigmentModifier(SuperPower.USA, adjustParam.tempCountry)

            val offset = ussr.total - usa.total
            when {
                offset > 0 -> // 苏联调整成功,移除美国影响力
                    adjustParam.tempCountry.addInfluence(SuperPower.USA, -offset)
                offset < 0 -> // 美国调整成功,移除苏联影响力
                    adjustParam.tempCountry.addInfluence(SuperPower.USSR, offset)
            }
            this.usedNum += 1
            this.countries.add(adjustParam.tempCountry)
        }

        /**
         * 设置调整阵营的目标
         * @param country
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        internal fun setRealignmentTarget(country: TSCountry) {
            this.adjustParam = realignInitParam.createAdjustParam(country)
            // 计算调整加值
            val bonus = gameMode.countryManager.getRealignmentBonus(country)
            this.adjustParam!!.setRealignmentBonus(bonus)
        }
    }

}
