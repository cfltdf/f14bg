package com.f14.TTA.listener.event

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAEventListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.component.IProperty
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil
import kotlin.math.abs
import kotlin.math.min


/**
 * 选择资源的监听器

 * @author F14eagle
 */
class ChooseResourceListener(gameMode: TTAGameMode, eventAbility: EventAbility, trigPlayer: TTAPlayer) : TTAEventListener(gameMode, eventAbility, trigPlayer) {
    private val propertyA: CivilizationProperty
    private val propertyB: CivilizationProperty

    init {
        val properties = eventAbility.property.allProperties.filter { it.value > 0 }.keys.take(2)
        propertyA = properties[0]
        propertyB = properties[1]
    }

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 检查玩家是否可以自动选择资源
        // 如果无需选择资源,则不需玩家回应
        if (eventAbility.amount == 0) {
            return false
        }
        // 如果是玩家失去资源,则需要进行一些判断
        if (eventAbility.amount < 0) {
            val num = abs(eventAbility.amount)
            val maxA = player.getProperty(propertyA)
            val maxB = player.getProperty(propertyB)
            val propA = eventAbility.property.getProperty(propertyA)
            val propB = eventAbility.property.getProperty(propertyB)
            if (maxA + maxB == 0) {
                // 如果玩家没有任何资源,则不需回应
                return false
            }
            val param = this.getParam<ChooseParam>(player.position)
            // 如果玩家任一资源总数为0,则自动扣除另一种资源
            if (maxA == 0) {
                param.setProperty(propertyB, min(maxB, num), propB)
                return false
            }
            if (maxB == 0) {
                param.setProperty(propertyA, min(maxA, num), propA)
                return false
            }
            if (!eventAbility.isSingleSelection) {
                // 在多选的情况下
                // 如果玩家的资源总数少于等于要扣除的总数,则自动扣光全部资源
                if (maxA + maxB <= num) {
                    param.setProperty(propertyA, maxA, propA)
                    param.setProperty(propertyB, maxB, propB)
                    return false
                }
            }
        }
        return true
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有玩家创建选择参数
        for (player in gameMode.game.players) {
            this.setParam(player.position, ChooseParam())
        }
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        for (player in this.listeningPlayers) {
            val cp = this.getParam<ChooseParam>(player.position)
            if (eventAbility.amount < 0) {
                cp.property.multi(-1)
            }
            param[player.position] = cp.property
        }
        return param
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 参数中传递需要选择的资源数量和类型
        res.public("amount", eventAbility.amount)
        res.public("singleSelection", eventAbility.isSingleSelection)
        res.public("maxPropertyA", player.getProperty(propertyA))
        res.public("maxPropertyB", player.getProperty(propertyB))
        res.public("nameA", propertyA.propertyName)
        res.public("nameB", propertyB.propertyName)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val numA = action.getAsInt("numA")
        val numB = action.getAsInt("numB")
        // 不能选择负数
        CheckUtils.check(numA < 0 || numB < 0, "数量选择错误!")
        if (eventAbility.isSingleSelection) {
            // 判断是否只选择一种资源
            if (numA != 0 && numB != 0) {
                throw BoardGameException("不能同时选择" + propertyA.propertyName + "和" + propertyB.propertyName + "!")
            }
        }
        // 检查选择数量是否和需求的数量相等
        CheckUtils.check(numA + numB != abs(eventAbility.amount), "数量选择错误!")
        // 如果是付出资源的情况,需要检查玩家是否拥有足够的食物或资源
        val player = action.getPlayer<TTAPlayer>()
        if (eventAbility.amount < 0) {
            CheckUtils.check(numA > player.getProperty(propertyA), propertyA.propertyName + "数量不足!")
            CheckUtils.check(numB > player.getProperty(propertyB), propertyB.propertyName + "数量不足!")
        }
        // 得到或者扣掉对应的食物和资源
        val param = this.getParam<ChooseParam>(player.position)
        param.setProperty(propertyA, numA, eventAbility.property.getProperty(propertyA))
        param.setProperty(propertyB, numB, eventAbility.property.getProperty(propertyB))
        // 完成选择
        this.setPlayerResponsed(player)
    }

    override fun getMsg(player: TTAPlayer): String {
        var msg = "你{0}总数{1}的{3}{2}{4},请选择!"
        val msg1 = if (eventAbility.amount < 0) "失去" else "得到"
        val msg2 = if (eventAbility.isSingleSelection) "或" else "和"
        val nameA = propertyA.propertyName
        val nameB = propertyB.propertyName
        msg = CommonUtil.getMsg(msg, msg1, eventAbility.amount, msg2, nameA, nameB)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_CHOOSE_RESOURCE

    /**
     * 选择的参数
     * @author F14eagle
     */
    class ChooseParam(val property: TTAProperty = TTAProperty()) : IProperty<CivilizationProperty> by property
}
