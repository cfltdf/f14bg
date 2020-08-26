package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCountry
import com.f14.TS.consts.*
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import java.util.*
import kotlin.math.min

/**
 * 选择国家并执行相应行动的监听器

 * @author F14eagle
 */
class TSCountryActionListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为监听的玩家创建参数
        for (p in this.listeningPlayers) {
            val param = CountryParam(p)
            param.reset()
            this.setParam(p, param)
        }
    }

    /**
     * 选择国家

     * @param gameMode

     * @param action

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun chooseCountry(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val param = this.getParam<CountryParam>(player)
        val country = action.getAsString("country")
        val cty = gameMode.countryManager.getCountry(country)
        // 检查是否可以选择该国家
        if (!this.actionInitParam.test(cty)) {
            throw BoardGameException("你不能选择这个国家!")
        }
        // 如果北约生效,则苏联不能在美国控制的欧洲国家打局部战争
        if (this.card != null && this.card!!.tsCardNo == 36) {
            // 36=局部战争
            if (player.superPower == SuperPower.USSR && player.hasEffect(EffectType.PROTECT_EUROPE) && cty.region == Region.EUROPE && cty.isControlledByOpposite(SuperPower.USSR)) {
                throw BoardGameException("北约发生后不能在美国控制的欧洲国家进行局部战争!")
            }
        }

        // 调整影响力
        param.chooseCountry(cty)
        // 刷新选择后的国家列表
        this.sendCountryParamInfo(player)
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {
        // 判断选择的国家数量是否匹配
        val player = action.getPlayer<TSPlayer>()
        val param = this.getParam<CountryParam>(player)
        val availnum = gameMode.countryManager.getAvailableCountryNum(this.actionInitParam)
        var neednum = this.actionInitParam.countryNum
        // 如果游戏中可选国家的数量少于需要选择的国家数量,则只需要取得可选国家的数量即可
        neednum = min(availnum, neednum)
        if (param.selectedCountries.size < neednum) {
            throw BoardGameException(this.getMsg(player))
        }
    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        val player = this.listeningPlayer
        val param = this.getParam<CountryParam>(player)
        res["countries"] = param.selectedCountries
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

     * @param gameMode

     * @param action

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun doReset(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val param = this.getParam<CountryParam>(player)
        // 重置选择
        param.reset()
        // 刷新选择的国家列表
        this.sendCountryParamInfo(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {
        val subact = action.getAsString("subact")
        if ("country" == subact) {
            // 选择国家
            this.chooseCountry(action)
        }
    }


    override val actionString: String
        get() = TSCmdString.ACTION_SELECT_COUNTRY

    val actionInitParam: ActionInitParam
        get() = super.initParam as ActionInitParam

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_COUNTRY_ACTION

    /**
     * 发送玩家选择国家的参数信息

     * @param gameMode

     * @param p
     */
    private fun sendCountryParamInfo(p: TSPlayer) {
        val param = this.getParam<CountryParam>(p)
        val res = this.createSubactResponse(p, "countryParam")
        val list = ArrayList<Map<String, Any>>()
        for (o in param.selectedCountries) {
            val map = HashMap<String, Any>()
            map["countryName"] = o.name
            map["influence"] = o.influenceString
            list.add(map)
        }
        res.public("countries", list)
        gameMode.game.sendResponse(p, res)
    }

    override fun sendStartListenCommand(player: TSPlayer, receiver: TSPlayer?) {
        super.sendStartListenCommand(player, receiver)
        // 只会向指定自己发送该监听信息
        this.sendCountryParamInfo(player)
    }

    /**
     * 选择国家的临时参数

     * @author F14eagle
     */
    internal inner class CountryParam(var player: TSPlayer) {
        var countries: MutableSet<TSCountry> = LinkedHashSet()

        init {
            this.init()
        }

        /**
         * 选择国家

         * @param country
         * *
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        fun chooseCountry(country: TSCountry) {
            if (!this.countries.contains(country)) {
                val countryNum = actionInitParam.countryNum
                if (countryNum == 1) {
                    // 如果国家是单选的话,则移除原来选择的国家,选中新的国家
                    this.countries.clear()
                } else if (this.selectedCountries.size >= countryNum) {
                    throw BoardGameException("你不能选择更多的国家了!")
                }
                this.countries.add(country)
            }
        }

        /**
         * 取得选中的国家

         * @return
         */
        val selectedCountries: Collection<TSCountry>
            get() = this.countries

        /**
         * 初始化参数
         */
        fun init() {}

        /**
         * 重置调整参数
         */
        fun reset() {
            this.countries.clear()
        }

    }

}
