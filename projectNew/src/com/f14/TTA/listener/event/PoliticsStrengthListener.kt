package com.f14.TTA.listener.event

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAEventListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.BgUtils
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class PoliticsStrengthListener(gameMode: TTAGameMode, ability: EventAbility, trigPlayer: TTAPlayer) : TTAEventListener(gameMode, ability, trigPlayer) {
    internal var discard: MutableList<TTACard> = ArrayList()
    internal var amount: Int = -this.eventAbility.amount


    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 如果是最后一回合,则改为减分,不需回应
        if (gameMode.gameOver) {
            return false
        }
        // 如果玩家手牌数小于等于需要弃牌数,则不需回应,弃掉所有牌
        if (player.militaryHands.size <= amount) {
            discard.addAll(player.militaryHands.cards)
            return false
        }
        return true
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        for (p in this.listeningPlayers) {
            param[p.position] = discard
        }
        return param
    }

    override fun createStartListenCommand(player: TTAPlayer): BgResponse {
        val res = super.createStartListenCommand(player)

        // 将军事牌作为参数传递到客户端
        res.private("multiSelection", true)
        res.private("cardIds", BgUtils.card2String(player.militaryHands.cards))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val cardIds = action.getAsString("cardIds")
        val cards = player.militaryHands.getCards(cardIds)
        CheckUtils.check(cards.size != amount, "弃牌数量错误," + this.getMsg(player))
        discard.addAll(cards)

        this.setPlayerResponsed(player)
    }

    override fun getMsg(player: TTAPlayer): String {
        var msg = "你需要弃掉{0}张军事牌!"
        msg = CommonUtil.getMsg(msg, amount)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_DISCARD_MILITARY
}
