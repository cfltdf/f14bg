package com.f14.TTA.listener.war

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.Condition
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAInterruptListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil
import com.f14.utils.StringUtils

/**
 * 选择对方科技的战争事件

 * @author F14eagle
 */
class ChooseScienceListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, private var attackCard: AttackCard, winner: TTAPlayer, private var loser: TTAPlayer, private var sciencePoint: Int) : TTAInterruptListener(gameMode, trigPlayer) {
    private var card: TechCard? = null

    init {
        this.addListeningPlayer(winner)
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param[loser.position] = card
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
            // 暂时该方法中只能选择科技牌
            val specialCard = card as TechCard
            CheckUtils.check(specialCard.costScience > this.sciencePoint, "剩余的科技点数不够夺取该科技牌!")
            if (gameMode.game.isVersion2) {
                val condition = Condition()
                condition.cardSubType = specialCard.cardSubType
                val cards = player.getPlayedCard(condition)
                for (c in cards) {
                    CheckUtils.check(c.level >= specialCard.level, "新版不允许夺取相同或更低等级的同类科技牌!")
                }
            }
            this.card = card
        }
        this.setPlayerResponsed(player)
    }


    override val actionString: String
        get() = TTACmdString.ACTION_CHOOSE_SCIENCE

    override fun getMsg(player: TTAPlayer): String {
        var msg = "你还能夺取玩家{0}总数{1}的科技点数,也可以选择蓝色科技牌来代替这些科技点数,请选择!"
        msg = CommonUtil.getMsg(msg, this.loser.reportString, this.sciencePoint)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_DESTORY_OTHERS

}
