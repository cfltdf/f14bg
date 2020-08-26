package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.AdjustParam
import com.f14.TS.component.TSCountry
import com.f14.TS.consts.Country
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.BgUtils
import java.util.*

/**
 * #53-南非动荡的监听器

 * @author F14eagle
 */
class Custom53Listener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

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
        // 检查是否可以选择该国家,该逻辑在param中的方法实现
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
        if (!param.isFinish) {
            throw BoardGameException(getMsg(player))
        }
    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        // 将玩家实际用掉的点数设置在返回参数中
        val player = this.listeningPlayer
        val param = this.getParam<InfluenceParam>(player)
        res["influenceParam"] = param
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

    val actionInitParam: ActionInitParam
        get() = super.initParam as ActionInitParam

    override val actionString: String
        get() = TSCmdString.ACTION_SELECT_COUNTRY

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
    inner class InfluenceParam constructor(var player: TSPlayer) {
        var adjustParams: MutableMap<String, AdjustParam> = LinkedHashMap()
        /**
         * 加在南非的影响力
         */
        var mainPoint = 0
        /**
         * 加在邻近国家的影响力
         */
        var subPoint = 0

        /**
         * 调整影响力
         * @param country
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        fun adjustInfluence(country: TSCountry) {
            if (this.isFinish) {
                throw BoardGameException("你没有多余的可操作点数了!")
            }
            var ap: AdjustParam? = this.getAdjustParam(country.id)
            if (ap == null) {
                // 如果不存在调整参数,检查该国家是否可以选择
                // 只能选择南非和南非的邻国
                val adj = country.isAdjacentTo(Country.RSA)
                if (country.country != Country.RSA && !adj) {
                    throw BoardGameException(getMsg(player))
                }
                if (adj) {
                    // 如果是邻国,则需要判断,如果南非已经加了2点,则不能选择
                    if (this.mainPoint == 2) {
                        throw BoardGameException(getMsg(player))
                    }
                    // 可以两个邻国一个一点所以实现——如果已经有选择其他的邻国,也不能选择的代码注释掉
                    /*
                     * if(this.subPoint>0){ throw new
					 * BoardGameException(getMsg(player)); }
					 */
                }
                // 创建调整参数
                ap = actionInitParam.createAdjustParam(country)
                this.adjustParams[country.id] = ap
            }

            if (country.country == Country.RSA) {
                // 如果是在南非加影响力,则先检查是否还能加
                if (mainPoint == 2) {
                    throw BoardGameException(getMsg(player))
                }
                // 如果有在邻国添加过影响力,则只能在南非加1点
                if (this.adjustParams.size > 1 && mainPoint == 1) {
                    throw BoardGameException(getMsg(player))
                }
                mainPoint += 1
            } else {
                // 如果是邻国,则检查是否还能加
                if (subPoint == 2) {
                    throw BoardGameException(getMsg(player))
                }
                subPoint += 1
            }
            // 按照初始化参数中设定的值调整该国家的影响力
            ap.tempCountry.addInfluence(ap.adjustPower, 1)
            ap.num += 1
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
        val influenceParam: List<Map<String, Any>>
            get() = BgUtils.toMapList(this.adjustParams.values)

        /**
         * 取得调整过影响力的国家的原始信息
         * @return
         */
        val originInfluence: List<TSCountry>
            get() = this.adjustParams.values.map(AdjustParam::orgCountry)

        /**
         * 取得临时调整影响力的国家信息
         * @return
         */
        val templateInfluence: List<TSCountry>
            get() = this.adjustParams.values.map(AdjustParam::tempCountry)

        /**
         * 判断点数是否用光
         * @return
         */
        val isFinish: Boolean
            get() = mainPoint == 2 || mainPoint == 1 && subPoint == 2

        /**
         * 重置调整参数
         */
        fun reset() {
            this.adjustParams.clear()
            mainPoint = 0
            subPoint = 0
        }
    }

}
