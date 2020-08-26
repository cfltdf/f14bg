package com.f14.innovation.listener.custom

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoSplayDirection
import com.f14.innovation.listener.InnoChooseHandListener
import com.f14.innovation.listener.InnoSplayListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoParamFactory
import com.f14.innovation.param.InnoResultParam

/**
 * #006-法典 监听器

 * @author F14eagle
 */
class InnoCustom006Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseHandListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    override fun canChooseCard(player: InnoPlayer, card: InnoCard): Boolean {
        if (!super.canChooseCard(player, card)) {
            return false
        }
        // 必须已经有该颜色牌的置顶牌
        return player.hasCardStack(card.color!!)
    }

    @Throws(BoardGameException::class)
    override fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        super.processChooseCard(player, cards)
        // 将该卡牌垫底
        for (card in cards) {
            gameMode.game.playerTuckHandCard(player, card)
        }
        // 执行完成后,创建一个询问是否展开该牌堆的监听器
        val param = InnoParamFactory.createInitParam()
        param.color = cards[0].color
        param.splayDirection = InnoSplayDirection.LEFT
        param.msg = "你可以将你的" + InnoColor.getDescr(param.color!!) + "牌堆向左展开!"
        param.isCanPass = true
        next = InnoSplayListener(gameMode, player, param, InnoResultParam(), null, null)
    }

}
