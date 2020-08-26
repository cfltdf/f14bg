package com.f14.innovation.listener.custom

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.consts.InnoSplayDirection
import com.f14.innovation.listener.InnoInterruptListener
import com.f14.innovation.listener.InnoSplayListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoParamFactory
import com.f14.innovation.param.InnoResultParam
import com.f14.innovation.utils.InnoUtils


/**
 * #083-经验主义 监听器

 * @author F14eagle
 */
class InnoCustom083Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoInterruptListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val colorString = action.getAsString("colors")
        val colorStrings = BgUtils.string2Array(colorString)
        if (colorStrings.size != 2) {
            throw BoardGameException("你必须指定两种颜色!")
        }
        val colors = colorStrings.map { InnoColor.valueOf(it) }.toTypedArray()
        gameMode.report.playerSelectColor(player, colors)
        // 首先指定两种颜色,抓一张[9]展示,若是你指定颜色中的
        // 一种,便将之融合并可以将该颜色的牌向上展开!
        val resultParam = gameMode.game.playerDrawCardAction(player, 9, 1, true)
        if (!resultParam.cards.empty) {
            if (InnoUtils.hasColor(resultParam.cards.cards, *colors)) {
                gameMode.game.playerMeldCard(player, resultParam)
                // 创建一个将该颜色的牌向上展开的询问监听器
                val initParam = InnoParamFactory.createInitParam()
                initParam.color = resultParam.cards.cards[0].color
                initParam.splayDirection = InnoSplayDirection.UP
                initParam.isCanPass = true
                initParam.msg = "你可以将你的" + InnoColor.getDescr(initParam.color!!) + "牌向上展开!"
                next = InnoSplayListener(gameMode, player, initParam, this.resultParam, this.ability, this.abilityGroup)
            } else {
                gameMode.game.playerAddHandCard(player, resultParam)
            }
        }
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_083

}
