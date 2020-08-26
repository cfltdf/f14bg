package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.AdjustParam
import com.f14.TS.component.TSCountry
import com.f14.TS.consts.*
import com.f14.TS.listener.initParam.OPActionInitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.BgUtils
import com.f14.bg.utils.CheckUtils
import java.util.*
import kotlin.collections.HashSet

/**
 * 使用OP放置影响力的监听器

 * @author F14eagle
 */
class TSAddInfluenceListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: OPActionInitParam) : TSOpActionInterruptListener(trigPlayer, gameMode, initParam) {

    /**
     * 放置影响力
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun adjustInfluence(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val param = this.getParam<InfluenceParam>(player)
        val country = action.getAsString("country")
        val cty = gameMode.countryManager.getCountry(country)
        // 检查是否可以选择该国家
        if (!param.availableCountries.contains(cty.country)) {
            throw BoardGameException("你不能选择这个国家!")
        }
        if (!this.opActionInitParam.test(cty)) {
            throw BoardGameException("你不能选择这个国家!")
        }
        // 检查玩家是否有禁止加点的区域
        if (player.hasEffect(EffectType.EFFECT_94)) {
            val condition = player.forbiddenCondition
            if (condition != null && condition.test(cty)) {
                throw BoardGameException("你不能选择这个国家!")
            }
        }
        // 调整影响力
        param.adjustInfluence(cty)
        // 刷新调整后的国家影响力信息及调整列表
        this.sendTemplateInfluenceInfo(player)
        this.sendInfluenceParamInfo(player)
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为监听的玩家创建参数
        for (p in this.listeningPlayers) {
            val param = InfluenceParam(p)
            param.reset()
            this.setParam(p, param)
        }
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val param = this.getParam<InfluenceParam>(player)
        // 判断点数是否用光
        if (param.leftOP > 0) {
            throw BoardGameException(this.getMsg(player))
        }
    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        // 将玩家实际用掉的点数设置在返回参数中
        val player = this.listeningPlayer
        val param = this.getParam<InfluenceParam>(player)
        res["adjustParams"] = param.getAdjustParams()
        res["originInfluence"] = param.originInfluence
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        // 设置已回应
        this.setPlayerResponsed(player)
    }

    /**
     * 重置选择
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun doReset(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val param = this.getParam<InfluenceParam>(player)
        // 刷新选择过的国家的影响力
        this.sendOriginInfluenceInfo(player)
        // 重置选择
        param.reset()
        // 刷新临时的国家调整列表
        this.sendInfluenceParamInfo(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {
        val subact = action.getAsString("subact")
        if ("country" == subact) {
            // 调整影响力
            this.adjustInfluence(action)
        }
    }


    override val actionString: String
        get() = TSCmdString.ACTION_SELECT_COUNTRY

    override fun getMsg(player: TSPlayer): String {
        // InfluenceParam param = this.getParam<InfluenceParam>(player)!!;
        return this.initParam.msg.replace("""\{num\}""".toRegex(), this.getOP(player, null).toString())
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_ADD_INFLUENCE

    /**
     * 发送玩家的调整参数信息
     * @param p
     */
    private fun sendInfluenceParamInfo(p: TSPlayer) {
        val param = this.getParam<InfluenceParam>(p)
        val res = this.createSubactResponse(p, "influenceParam")
        res.public("countries", param.influenceParam)
        gameMode.game.sendResponse(p, res)
    }

    /**
     * 发送玩家调整过的国家的初始影响力的信息
     *  @param p
     */
    private fun sendOriginInfluenceInfo(p: TSPlayer) {
        val param = this.getParam<InfluenceParam>(p)
        gameMode.game.sendCountriesInfo(param.originInfluence, p)
    }

    override fun sendStartListenCommand(player: TSPlayer, receiver: TSPlayer?) {
        super.sendStartListenCommand(player, receiver)
        // 只会向指定自己发送该监听信息
        this.sendTemplateInfluenceInfo(player)
        this.sendInfluenceParamInfo(player)
    }

    /**
     * 发送玩家调整的临时影响力的信息
     * @param p
     */
    private fun sendTemplateInfluenceInfo(p: TSPlayer) {
        val param = this.getParam<InfluenceParam>(p)
        gameMode.game.sendCountriesInfo(param.templateInfluence, p)
    }

    /**
     * 调整影响力的临时参数
     * @author F14eagle
     */
    internal inner class InfluenceParam(var player: TSPlayer) {
        var usedNum: Int = 0
        /**
         * 每次可调整的数量
         */
        var adjustValue = 1
        var adjustParams: MutableMap<String, AdjustParam> = LinkedHashMap()
        /**
         * 允许放置影响力的国家
         */
        var availableCountries: MutableSet<Country> = HashSet()
        /**
         * 放置过影响力的国家
         */
        var countries: MutableCollection<TSCountry> = HashSet()
        var overUsedOp = 0

        init {
            // 设置允许放置影响力的国家列表
            this.availableCountries = gameMode.countryManager.getAvailableCountries(this.player.superPower).toMutableSet()
        }

        /**
         * 调整影响力
         * @param country
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        fun adjustInfluence(country: TSCountry) {
            CheckUtils.check(!this.hasLeftOP(country), "你没有多余的OP了!")
            val ap: AdjustParam = this.adjustParams.computeIfAbsent(country.id) {
                opActionInitParam.createAdjustParam(country)
            }
            var needop = 1
            var priv = false
            if (ap.tempCountry.isControlledByOpposite(this.player.superPower)) {
                // 新版太空特权
                priv = player.hasEffect(EffectType.SR_PRIVILEGE_N2)
                        && player.params.getInteger(EffectType.SR_PRIVILEGE_N2) != gameMode.turn
                        && overUsedOp == 0
                // 如果要在被对方控制的国家放影响力,需要2个OP
                if (!priv) {
                    needop = 2
                }
            }
            val leftNum = this.getLeftOP(country)
            if (leftNum < needop) {
                // 如果没有使用过点数,则删除该参数
                if (ap.num == 0) {
                    this.adjustParams.remove(country.id)
                }
                throw BoardGameException("你的OP不够放置该影响力!")
            }
            // 按照初始化参数中设定的值调整该国家的影响力
            ap.tempCountry.addInfluence(ap.adjustPower, this.adjustValue)
            ap.num += this.adjustValue
            ap.op += needop

            this.usedNum += needop
            this.countries.add(ap.tempCountry)
            if (priv || needop > 1) {
                this.overUsedOp++
            }
        }

        /**
         * 确定影响力调整
         */
        fun applyAdjust() {
            for (ap in this.adjustParams.values) {
                ap.orgCountry.customSetInfluence(SuperPower.USSR, ap.tempCountry.customGetInfluence(SuperPower.USSR))
                ap.orgCountry.customSetInfluence(SuperPower.USA, ap.tempCountry.customGetInfluence(SuperPower.USA))
            }
            if (this.overUsedOp > 0) {
                player.params.setRoundParameter(EffectType.SR_PRIVILEGE_N2, gameMode.turn)
            }
        }

        /**
         * 取得指定国家的调整参数
         * @param countryId
         * @return
         */
        fun getAdjustParam(countryId: String) = this.adjustParams[countryId]

        /**
         * 取得所有调整参数
         * @return
         */
        fun getAdjustParams() = this.adjustParams.values

        /**
         * 取得调整参数
         * @return
         */
        val influenceParam: List<Map<String, Any>>
            get() = BgUtils.toMapList(this.adjustParams.values)

        /**
         * 取得剩余的OP
         * @return
         */
        val leftOP: Int
            get() = getOP(player, countries) - usedNum

        /**
         * 取得剩余的OP
         * @return
         */
        fun getLeftOP(country: TSCountry) = getOP(player, setOf(country, *this.countries.toTypedArray())) - usedNum

        /**
         * 取得调整过影响力的国家的原始信息
         * @return
         */
        val originInfluence: List<TSCountry>
            get() = adjustParams.values.map(AdjustParam::orgCountry)

        /**
         * 取得临时调整影响力的国家信息
         * @return
         */
        val templateInfluence: List<TSCountry>
            get() = adjustParams.values.map(AdjustParam::tempCountry)

        /**
         * 检查是否拥有剩余的op对country进行调整阵营的操作
         * @param country
         * @return
         */
        fun hasLeftOP(country: TSCountry) = this.getLeftOP(country) > 0

        /**
         * 重置调整参数
         */
        fun reset() {
            this.usedNum = 0
            this.overUsedOp = 0
            this.adjustParams.clear()
            this.countries.clear()
        }
    }

}
