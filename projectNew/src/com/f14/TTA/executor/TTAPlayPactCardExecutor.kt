package com.f14.TTA.executor

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.ActiveAbility
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.component.card.PactCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.PactSide
import com.f14.TTA.listener.ChoosePlayerPactListener
import com.f14.TTA.listener.TTAPactListener
import com.f14.bg.exception.BoardGameException

/**
 * 打出条约牌的处理器

 * @author 吹风奈奈
 */
class TTAPlayPactCardExecutor(param: RoundParam, private var card: PactCard) : TTAPoliticalCardExecutor(param) {

    private fun createCard(card: PactCard, alian: TTAPlayer, property: TTAProperty, abilities: MutableList<CivilCardAbility>, activeAbility: ActiveAbility?): PactCard {
        val newCard = card.clone()
//        newCard.alian = alian
        newCard.property = property
        newCard.abilities = abilities
        newCard.activeAbility = activeAbility
        return newCard
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        // 创建选择玩家的监听器
        val l = ChoosePlayerPactListener(gameMode, player, card)
        val param = gameMode.insertListener(l)
        val targetPlayer = param.get<TTAPlayer>("target")
        if (targetPlayer != null) {
            gameMode.report.playerActiveCard(player, targetPlayer, card, null, 0)
            val pl = TTAPactListener(gameMode, player, targetPlayer, card)
            val pactParam = gameMode.insertListener(pl)
            val pactSide = pactParam.get<PactSide>("pactSide")
            if (pactSide != null) {
                card.owner = player
                card.target = targetPlayer
                // 创建触发方和目标方的条约牌副本
                val (playerA, playerB) = when (pactSide) {
                    PactSide.A -> player to targetPlayer
                    PactSide.B -> targetPlayer to player
                }
//                val playerA: TTAPlayer = if (pactSide == PactSide.A) player else targetPlayer
//                val playerB: TTAPlayer = if (pactSide == PactSide.B) player else targetPlayer
                card.a = playerA
                card.b = playerB
                val cardA = card.clone()
                cardA.side = PactSide.A
                val cardB = card.clone()
                cardB.side = PactSide.B
//                cardA = createCard(card, playerB, card.propertyA, card.abilitiesA, card.activeAbilityA)
//                cardB = createCard(card, playerA, card.propertyB, card.abilitiesB, card.activeAbilityB)
                // 如果触发玩家已经拥有自己的条约牌,则废除该条约牌
                player.pact?.let(gameMode.game::removeOvertimeCard)
//                if (player.pact != null) {
//                    gameMode.game.removeOvertimeCard(player.pact!!)
//                }
                // 移除玩家手牌中的条约牌
                gameMode.game.playerRemoveHand(player, card)
                // 发送条约牌的信息
                gameMode.game.sendOvertimeCardInfoResponse(card)
                // 将条约牌副本添加给对应的玩家
                gameMode.game.playerAddCardDirect(playerA, cardA)
                gameMode.report.playerAddCard(playerA, cardA)
                gameMode.game.playerAddCardDirect(playerB, cardB)
                gameMode.report.playerAddCard(playerB, cardB)
                gameMode.game.checkUnitedNation(CivilAbilityType.PA_SCORE_USE_CARD, 1)
            }
            this.finish()
        }
    }

}
