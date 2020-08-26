package com.f14.TTA.listener.war

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAAttackResolutionListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.common.ParamSet
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.StringUtils

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class InfiltrateListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, warCard: AttackCard, winner: TTAPlayer, loser: TTAPlayer, warParam: ParamSet) : TTAAttackResolutionListener(gameMode, trigPlayer, warCard, winner, loser, warParam) {
    var cardId: String? = null

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        return this.cannotPass()
    }

    /**
     * 检查玩家是否可以跳过选择
     * @return
     */
    private fun cannotPass(): Boolean {
        // 如果目标玩家有领袖或在建奇迹,则需要监听结算
        val leader = loser.leader
        val wonder = loser.uncompletedWonder
        if (leader == null) {
            if (wonder != null) {
                this.cardId = wonder.id
            }
            return false
        } else if (wonder == null) {
            this.cardId = leader.id
            return false
        }
        return true
    }

    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["cardId"] = cardId
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
            // 只能选择未完成奇迹或领袖
            val wonder = target.uncompletedWonder
            val leader = target.leader
            if (wonder != null && wonder.id == cardId) {
                this.cardId = cardId
            } else if (leader!!.id == cardId) {
                this.cardId = cardId
            } else {
                throw BoardGameException("不能选择这张牌!")
            }
        } else {
            CheckUtils.check(this.cannotPass(), this.getMsg(player))
        }
        this.setPlayerResponsed(player)
    }


    override val actionString: String
        get() = TTACmdString.ACTION_CHOOSE_INFILTRATE

    override fun getMsg(player: TTAPlayer): String {
        return "你可以移除玩家${this.loser.reportString}的领袖或正在建造的奇迹,请选择!"
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_DESTORY_OTHERS
}
