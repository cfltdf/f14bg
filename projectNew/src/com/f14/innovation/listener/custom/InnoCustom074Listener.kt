package com.f14.innovation.listener.custom

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.listener.InnoInterruptListener
import com.f14.innovation.listener.InnoReturnScoreListener
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoParamFactory
import com.f14.innovation.param.InnoResultParam


/**
 * #074-进化 监听器

 * @author F14eagle
 */
class InnoCustom074Listener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoInterruptListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        when (action.getAsInt("option")) {
            1 -> {
                // 1.抓1张8计分,然后选择一张计分区的牌归还
                gameMode.game.playerDrawAndScoreCard(player, 8, 1)

                // 创建归还计分牌的监听器
                val initParam = InnoParamFactory.createInitParam()
                initParam.num = 1
                initParam.msg = "请归还{num}张计分区的牌!"
                next = InnoReturnScoreListener(gameMode, player, initParam, this.resultParam, this.ability, this.abilityGroup)
            }
            2 -> {
                // 2.抓1张比你计分区中最高时期牌高一时期的牌,收入手牌
                val level = player.scores.maxLevel
                gameMode.game.playerDrawCard(player, level + 1, 1)
            }
            else -> throw BoardGameException("无效的选项!")
        }
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_074

}
