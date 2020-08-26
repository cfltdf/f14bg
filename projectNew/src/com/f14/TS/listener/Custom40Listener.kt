package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCountry
import com.f14.TS.consts.Country
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import java.util.*

/**
 * #40-古巴导弹危机

 * @author F14eagle
 */
class Custom40Listener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    private var countryList: MutableMap<String, TSCountry> = LinkedHashMap()
    private var country: TSCountry? = null

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 设置玩家可以选择的国家
        if (this.listeningPlayer.superPower == SuperPower.USSR) {
            // 苏联可以选择古巴
            val country = gameMode.countryManager.getCountry(Country.CUB)
            countryList[country.id] = country
        } else {
            // 美国可以选择西德或土耳其
            var country = gameMode.countryManager.getCountry(Country.TUR)
            countryList[country.id] = country
            country = gameMode.countryManager.getCountry(Country.WGER)
            countryList[country.id] = country
        }
    }

    override fun canCancel(action: GameAction): Boolean {
        return true
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["country"] = country
        return param
    }

    override fun createStartListenCommand(player: TSPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 设置可选择的国家列表
        val list = ArrayList<Map<String, Any>>()
        for (country in this.countryList.values) {
            val map = LinkedHashMap<String, Any>()
            map["id"] = country.id
            map["chinese"] = country.reportString
            map["influenceString"] = country.influenceString
            list.add(map)
        }
        res.public("countryList", list)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val countryId = action.getAsString("countryId")
        val country = this.countryList[countryId] ?: throw BoardGameException("你不能选择这个国家!")
        if (country.customGetInfluence(player.superPower) < 2) {
            throw BoardGameException("该国家的影响力不够!")
        }
        this.country = country
        // 设置玩家完成回应
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }

    override fun getMsg(player: TSPlayer): String {
        return "你需要从以下国家移除2点影响力,才能进行政变!"
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_40

}
