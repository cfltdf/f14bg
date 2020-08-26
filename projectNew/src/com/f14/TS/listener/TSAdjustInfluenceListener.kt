package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.AdjustParam
import com.f14.TS.component.TSCountry
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.BgUtils
import java.util.*
import kotlin.math.abs
import kotlin.math.min

/**
 * 调整影响力的监听器

 * @author F14eagle
 */
class TSAdjustInfluenceListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    /**
     * 调整影响力
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
        if (!this.actionInitParam.test(cty)) {
            throw BoardGameException("你不能选择这个国家!")
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
        // 判断是否已经用足了点数
        val player = action.getPlayer<TSPlayer>()
        val param = this.getParam<InfluenceParam>(player)
        if (!this.actionInitParam.isCanLeft && param.leftNum > 0) {
            // 如果存在剩余未操作点数,则检查,所有可操作点数的上限
            val initParam = this.actionInitParam
            val countries = gameMode.countryManager.getCountriesByCondition(initParam)
            if (initParam.num >= 0) {
                // 添加影响力,需要检查最多可以添加的点数,是否达到已操作点数
                val limitNum = if (initParam.limitNum == 0) 99 else initParam.limitNum // 0的话表示一个国家上的加点没有限制
                val availNum = countries.size * limitNum
                if (abs(initParam.num) - param.leftNum < availNum) {
                    throw BoardGameException(this.getMsg(player))
                }
            } else {
                // 减少影响力,需要检查最多可减少的点数,是否达到已操作点数
                val availNum = countries.sumBy {
                    // 最多只能移除国家有的影响力
                    min(initParam.limitNum, it.customGetInfluence(initParam.targetPower))
                }
                if (abs(initParam.num) - param.leftNum < availNum) {
                    throw BoardGameException(this.getMsg(player))
                }
            }
        }
    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        // 将玩家实际用掉的点数设置在返回参数中
        val player = this.listeningPlayer
        val param = this.getParam<InfluenceParam>(player)
        val adjustNum = abs(this.actionInitParam.num) - param.leftNum
        res["adjustParams"] = param.getAdjustParams()
        res["originInfluence"] = param.originInfluence
        res["adjustNum"] = adjustNum
        res["player"] = player
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        // 判断点数是否用光
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

    val actionInitParam: ActionInitParam
        get() = super.initParam as ActionInitParam

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_ADJUST_INFLUENCE

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
     * @param p
     */
    private fun sendOriginInfluenceInfo(p: TSPlayer) {
        val param = this.getParam<InfluenceParam>(p)
        gameMode.game.sendCountriesInfo(param.originInfluence, p)
    }

    override fun sendStartListenCommand(player: TSPlayer, receiver: TSPlayer?) {
        super.sendStartListenCommand(player, receiver)
        // 只会向指定自己发送该监听信息
        if (receiver != null && player == receiver) {
            this.sendTemplateInfluenceInfo(receiver)
            this.sendInfluenceParamInfo(receiver)
        }
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
        var leftNum: Int = 0
        /**
         * 每次可调整的数量
         */
        var adjustValue: Int = 0
        var adjustParams: MutableMap<String, AdjustParam> = LinkedHashMap()

        init {
            this.adjustValue = if (actionInitParam.num > 0) 1 else -1
        }

        /**
         * 调整影响力
         * @param country
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        fun adjustInfluence(country: TSCountry) {
            if (this.leftNum <= 0) {
                throw BoardGameException("你没有多余的可操作点数了!")
            }
            var ap: AdjustParam? = this.getAdjustParam(country.id)
            if (ap == null) {
                // 如果不存在调整参数,则检查调整个数的上限
                val countryNum = actionInitParam.countryNum
                if (countryNum > 0 && this.adjustParams.size >= countryNum) {
                    throw BoardGameException(getMsg(player))
                }
                // 检查是否可以从该国家移除影响力
                if (this.adjustValue < 0 && country.customGetInfluence(actionInitParam.targetPower) <= 0) {
                    throw BoardGameException("该国家没有影响力,不能移除!")
                }
                // 创建调整参数
                ap = actionInitParam.createAdjustParam(country)
                this.adjustParams[country.id] = ap
            }
            // 检查单个国家的调整数量是否超过上限
            val limitNum = actionInitParam.limitNum
            if (limitNum > 0 && abs(ap.num + this.adjustValue) > limitNum) {
                throw BoardGameException(getMsg(player))
            }
            // 检查是否还可以从该国家移除影响力
            if (this.adjustValue < 0 && ap.tempCountry.customGetInfluence(actionInitParam.targetPower) <= 0) {
                throw BoardGameException("该国家没有影响力,不能移除!")
            }
            // 按照初始化参数中设定的值调整该国家的影响力
            ap.tempCountry.addInfluence(ap.adjustPower, this.adjustValue)
            ap.num += this.adjustValue
            this.leftNum -= abs(this.adjustValue)
        }

        /**
         * 确定影响力调整
         */
        fun applyAdjust() {
            this.adjustParams.values.forEach(AdjustParam::apply)
        }

        /**
         * 取得指定国家的调整参数
         * @param countryId
         * @return
         */
        fun getAdjustParam(countryId: String): AdjustParam? {
            return this.adjustParams[countryId]
        }

        /**
         * 取得所有调整参数
         * @return
         */
        fun getAdjustParams(): Collection<AdjustParam> {
            return this.adjustParams.values
        }

        /**
         * 取得调整参数
         * @return
         */
        /*
             * List<Map<String, Object>> res = new
			 * ArrayList<Map<String,Object>>(); for(AdjustParam ap :
			 * this.adjustParams.values()){ Map<String, Object> o = new
			 * HashMap<String, Object>(); o.put("countryName",
			 * ap.orgCountry.name); o.put("num", ap.num); res.add(o); } return
			 * res;
			 */
        val influenceParam: List<Map<String, Any>>
            get() = BgUtils.toMapList(this.adjustParams.values)

        /**
         * 取得调整过影响力的国家的原始信息
         * @return
         */
        val originInfluence: List<TSCountry>
            get() {
                return adjustParams.values.map(AdjustParam::orgCountry)
            }

        /**
         * 取得临时调整影响力的国家信息
         * @return
         */
        val templateInfluence: List<TSCountry>
            get() {
                return adjustParams.values.map(AdjustParam::tempCountry)
            }

        /**
         * 重置调整参数
         */
        fun reset() {
            this.leftNum = abs(actionInitParam.num)
            this.adjustParams.clear()
        }
    }

}
