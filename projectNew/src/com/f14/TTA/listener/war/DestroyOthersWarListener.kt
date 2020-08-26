package com.f14.TTA.listener.war

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.PopParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAAttackResolutionListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.common.ParamSet
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil
import com.f14.utils.StringUtils
import kotlin.math.min

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

open class DestroyOthersWarListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, warCard: AttackCard, winner: TTAPlayer, loser: TTAPlayer, warParam: ParamSet) : TTAAttackResolutionListener(gameMode, trigPlayer, warCard, winner, loser, warParam) {
    protected var popParam = PopParam()

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        return this.cannotPass()
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
    }

    protected open fun cannotPass(): Boolean {
        // 如果战败方有可选建筑,则需要继续监听
        return loser.buildings.cards.any { this.attackCard.loserEffect.test(it) && it.availableCount > 0 }
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param[loser.position] = popParam
        return param
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // GAME_CODE_EVENT_DESTORY_OTHERS 需要的参数
        val availablePositions = intArrayOf(loser.position)
        res.public("availablePositions", StringUtils.array2String(availablePositions))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val confirm = action.getAsBoolean("confirm")
        if (confirm) {
            val targetPosition = action.getAsInt("targetPosition")
            CheckUtils.check(targetPosition != this.loser.position, "不能选择指定的玩家!")
            val target = gameMode.game.getPlayer(targetPosition)
            val cardId = action.getAsString("cardId")
            val card = target.getPlayedCard(cardId)
            CheckUtils.check(!this.attackCard.loserEffect.test(card), "不能选择这张牌!")
            CheckUtils.check(!card.needWorker || card.availableCount <= 0, "这张牌上没有工人!")

            // 结算战败方效果
            val num = min(this.attackCard.loserEffect.amount, card.availableCount)
            popParam.destory(card as TechCard, num)
        } else {
            CheckUtils.check(this.cannotPass(), this.getMsg(player))
        }
        this.setPlayerResponsed(player)
    }


    override val actionString: String
        get() = TTACmdString.ACTION_DESTORY

    override fun getMsg(player: TTAPlayer): String {
        var msg = "你可以摧毁玩家{0}最多{1}个同类型同等级的城市建筑,请选择!"
        msg = CommonUtil.getMsg(msg, this.loser.reportString, this.attackCard.loserEffect.amount)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_DESTORY_OTHERS

}
