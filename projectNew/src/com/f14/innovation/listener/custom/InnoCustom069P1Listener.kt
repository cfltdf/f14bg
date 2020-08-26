package com.f14.innovation.listener.custom

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.listener.InnoInterruptListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * #069-出版 监听器(实现排序的监听器)

 * @author F14eagle
 */
class InnoCustom069P1Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoInterruptListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createStartListenCommand(player: InnoPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        val cards = player.getCardStack(this.initParam!!.color!!)!!.cards
        res.public("sortCardIds", BgUtils.card2String(cards))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val cardIds = action.getAsString("cardIds")
        val stack = player.getCardStack(this.initParam!!.color!!)
        val cards = stack!!.getCards(cardIds)
        if (cards.size != stack.size) {
            throw BoardGameException("还有卡牌没有被排序!")
        }
        stack.replaceCards(cards)
        gameMode.game.sendPlayerCardStackResponse(player, this.initParam.color!!, null)
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_069

}
