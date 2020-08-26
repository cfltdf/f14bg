package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil
import kotlin.math.abs

class SaladineListener(gameMode: TTAGameMode, player: TTAPlayer, val ability: CivilCardAbility) : TTAInterruptListener(gameMode, player) {

    private var propertyA: CivilizationProperty
    private var propertyB: CivilizationProperty
    private var numA: Int = 0
    private var numB: Int = 0
    private var amount = 0

    init {
        val properties = ability.property.allProperties.filter { it.value > 0 }.keys.take(2)
        propertyA = properties[0]
        propertyB = properties[1]
        this.amount = 1
        this.addListeningPlayer(trigPlayer)
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        val property = TTAProperty()
        property.setProperty(propertyA, ability.property.getProperty(propertyA) * numA)
        property.setProperty(propertyB, ability.property.getProperty(propertyB) * numB)
        param["property"] = property
        return param
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 参数中传递需要选择的资源数量和类型
        res.public("amount", amount)
        res.public("singleSelection", true)
        res.public("maxPropertyA", player.getProperty(propertyA))
        res.public("maxPropertyB", player.getProperty(propertyB))
        res.public("nameA", propertyA.propertyName)
        res.public("nameB", propertyB.propertyName)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: BgAction.GameAction) {
        numA = action.getAsInt("numA")
        numB = action.getAsInt("numB")
        // 不能选择负数
        CheckUtils.check(numA < 0 || numB < 0, "数量选择错误!")
        // 检查选择数量是否和需求的数量相等
        CheckUtils.check(numA + numB != abs(amount), "数量选择错误!")
        val player = action.getPlayer<TTAPlayer>()
        // 完成选择
        this.setPlayerResponsed(player)
    }

    override fun getMsg(player: TTAPlayer): String {
        var msg = "你获得{1}的{0}或{3}的{2},请选择!"
        val nameA = propertyA.propertyName
        val numA = ability.property.getProperty(propertyA)
        val nameB = propertyB.propertyName
        val numB = ability.property.getProperty(propertyB)
        msg = CommonUtil.getMsg(msg, nameA, numA, nameB, numB)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_CHOOSE_RESOURCE

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        super.onAllPlayerResponsed()
    }
}



