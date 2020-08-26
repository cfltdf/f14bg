package com.f14.TTA.listener.war

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.card.TTACard
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


/**
 * 抢夺殖民地的侵略事件

 * @author F14eagle
 */
class ChooseColonyWarListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, warCard: AttackCard, winner: TTAPlayer, loser: TTAPlayer, warParam: ParamSet) : TTAAttackResolutionListener(gameMode, trigPlayer, warCard, winner, loser, warParam) {


    private var card: TTACard? = null

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        return this.cannotPass()
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()

        this.card = null
    }

    private fun cannotPass(): Boolean {
        // 如果战败方有殖民地,则需要结算
        return loser.getPlayedCard(this.attackCard.loserEffect).isNotEmpty()
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["card"] = card
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
            CheckUtils.check(targetPosition != loser.position, "不能选择指定的玩家!")
            val target = gameMode.game.getPlayer(targetPosition)
            val cardId = action.getAsString("cardId")
            val card = target.getPlayedCard(cardId)
            CheckUtils.check(!attackCard.loserEffect.test(card), "不能选择这张牌!")
            this.card = card
        } else {
            // 判断玩家是否可以结束
            CheckUtils.check(this.cannotPass(), this.getMsg(player))
        }
        this.setPlayerResponsed(player)
    }


    override val actionString: String
        get() = TTACmdString.ACTION_CHOOSE_COLONY

    override fun getMsg(player: TTAPlayer): String {
        var msg = "你夺得了玩家{0}的1个殖民地,请选择!"
        msg = CommonUtil.getMsg(msg, this.loser.reportString)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_DESTORY_OTHERS
}
