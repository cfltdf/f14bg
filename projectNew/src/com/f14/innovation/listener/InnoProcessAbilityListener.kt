package com.f14.innovation.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.command.InnoCommand
import com.f14.innovation.command.InnoCommandList
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoActiveType
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 执行卡牌能力的执行器
 * @author F14eagle
 */
open class InnoProcessAbilityListener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoInterruptListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) = Unit

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) = Unit

    /**
     * 取得返回结果中的牌
     * @return
     */
    val resultCards: List<InnoCard>
        get() = this.resultParam.cards.cards

    override val validCode: Int
        get() = 0

    @Throws(BoardGameException::class)
    override fun onStartListen() {
        super.onStartListen()
        // 开始监听时,处理所选卡牌的能力
        val player = this.targetPlayer
        val card = this.resultCards.firstOrNull() ?: return
        // 发送dogma的效果
        gameMode.game.playerDogmaCard(player, card)
        val commandList = InnoCommandList(gameMode, player)
        // 清理命令列表以供使用
        commandList.reset(card)
        card.abilityGroups.filter { it.activeType == InnoActiveType.NORMAL }.mapTo(commandList) { InnoCommand(player, player, it, card) }
        // 处理命令列表
        this.processCommandList(commandList)
    }

    /**
     * 处理命令列表
     * @param gameMode
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun processCommandList(commandList: InnoCommandList) {
        var cmd = commandList.pop()
        while (cmd != null) {
            // 处理命令
            gameMode.game.processInnoCommand(cmd, commandList)
            cmd = commandList.pop()
        }
        this.setAllPlayerResponsed()
    }

}
