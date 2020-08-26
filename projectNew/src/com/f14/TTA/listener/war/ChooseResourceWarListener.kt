package com.f14.TTA.listener.war

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAAttackResolutionListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.common.ParamSet
import com.f14.bg.component.IProperty
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil
import kotlin.math.abs
import kotlin.math.min

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class ChooseResourceWarListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, warCard: AttackCard, winner: TTAPlayer, loser: TTAPlayer, warParam: ParamSet) : TTAAttackResolutionListener(gameMode, trigPlayer, warCard, winner, loser, warParam) {
    private var propertyA: CivilizationProperty
    private var propertyB: CivilizationProperty

    init {
        val properties = attackCard.loserEffect.property.allProperties.filter { it.value > 0 }.keys.take(2)
        propertyA = properties[0]
        propertyB = properties[1]
    }

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 该事件总是检查战败方的资源损失情况
        // 检查玩家是否可以自动选择资源
        val loser = this.loser
        val ability = this.attackCard.loserEffect
        // 如果无需选择资源,则不需玩家回应
        val amount = this.needAmount
        if (amount == 0) {
            return false
        }
        // 如果是玩家失去资源,则需要进行一些判断
        if (amount != 0) {
            val num = abs(amount)
            val maxA = loser.getProperty(propertyA)
            val maxB = loser.getProperty(propertyB)
            val propA = ability.property.getProperty(propertyA)
            val propB = ability.property.getProperty(propertyB)
            if (maxA + maxB == 0) {
                // 如果玩家没有任何资源,则不需回应
                return false
            }
            val param = this.getParam<ChooseParam>(loser.position)
            // 如果玩家任一资源总数为0,则自动扣除另一种资源
            if (maxA == 0) {
                param.setProperty(propertyB, min(maxB, num), propB)
                return false
            }
            if (maxB == 0) {
                param.setProperty(propertyA, min(maxA, num), propA)
                return false
            }
            if (!ability.isSingleSelection) {
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
            val param = ChooseParam()
            this.setParam(player.position, param)
        }
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        val cp = this.getParam<ChooseParam>(loser.position)
        param[loser.position] = cp.property
        return param
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // GAME_CODE_CHOOSE_RESOURCE 需要的参数
        // 由战胜方选择战败方的
        res.public("amount", this.needAmount)
        res.public("singleSelection", false)
        res.public("maxPropertyA", this.loser.getProperty(propertyA))
        res.public("maxPropertyB", this.loser.getProperty(propertyB))
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
        val ability = this.attackCard.loserEffect
        val amount = this.needAmount
        if (ability.isSingleSelection) {
            // 判断是否只选择一种资源
            if (numA != 0 && numB != 0) {
                throw BoardGameException("不能同时选择" + propertyA.propertyName + "和" + propertyB.propertyName + "!")
            }
        }
        // 检查选择数量是否和需求的数量相等
        CheckUtils.check(numA + numB != abs(amount), "数量选择错误!")
        // 需要检查战败方玩家是否拥有足够的食物或资源
        val player = this.loser
        if (amount < 0) {
            CheckUtils.check(numA > player.getProperty(propertyA), propertyA.propertyName + "数量不足!")
            CheckUtils.check(numB > player.getProperty(propertyB), propertyB.propertyName + "数量不足!")
        }
        // 设置选择的食物和资源
        val param = this.getParam<ChooseParam>(player.position)
        param.setProperty(propertyA, numA, ability.property.getProperty(propertyA))
        param.setProperty(propertyB, numB, ability.property.getProperty(propertyB))
        // 完成选择
        this.setPlayerResponsed(action.getPlayer<TTAPlayer>())
    }

    override fun getMsg(player: TTAPlayer): String {
        val ability = this.attackCard.loserEffect
        var msg = "你能夺取{0}总数{1}的{3}{2}{4},请选择!"
        val amount = abs(this.needAmount)
        val msg1 = if (ability.isSingleSelection) "或" else "和"
        val nameA = propertyA.propertyName
        val nameB = propertyB.propertyName
        msg = CommonUtil.getMsg(msg, this.loser.reportString, amount, msg1, nameA, nameB)
        return msg
    }

    /**
     * 取得实际需要选择的数量
     * @return
     */
    private val needAmount: Int
        get() = this.attackCard.loserEffect.getRealAmount(warParam)

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_CHOOSE_RESOURCE

    /**
     * 选择的参数
     * @author F14eagle
     */
    class ChooseParam(val property: TTAProperty = TTAProperty()) : IProperty<CivilizationProperty> by property

}
