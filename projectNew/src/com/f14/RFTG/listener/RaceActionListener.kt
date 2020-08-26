package com.f14.RFTG.listener

import com.f14.RFTG.RaceConfig
import com.f14.RFTG.RacePlayer
import com.f14.RFTG.RaceReport
import com.f14.RFTG.card.Ability
import com.f14.RFTG.card.RaceCard
import com.f14.RFTG.mode.RaceGameMode
import com.f14.RFTG.network.CmdConst
import com.f14.RFTG.network.CmdFactory
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ActionListener
import com.f14.bg.utils.BgUtils

/**
 * 银河竞逐用的行动监听器
 * @author F14eagle
 */
abstract class RaceActionListener(protected var gameMode: RaceGameMode) : ActionListener<RacePlayer, RaceConfig, RaceReport>(gameMode) {

    /**
     * 将当前阶段中可以使用的卡牌列表传送到客户端
     * @param gameMode
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun checkActiveCards() {
        val ability = this.ability ?: return
        for (player in gameMode.game.players) {
            val cards = player.getActiveCardsByAbilityType(ability)
            if (cards.isNotEmpty()) {
                val res = this.createActivedCardResponse(player) ?: continue
                gameMode.game.sendResponse(player, res)

                // 设置所有可使用卡牌的使用次数
                for (card in cards) {
                    this.setCardUseNum(player, card, card.getUseNumByType(ability))
                }
            }
        }
    }

    /**
     * 创建玩家可使用卡牌列表的信息
     * @param player
     * @return
     */
    protected fun createActivedCardResponse(player: RacePlayer): BgResponse? {
        val ability = this.ability ?: return null
        val cards = player.getActiveCardsByAbilityType(ability)
        if (cards.isEmpty()) return null
        if (cards.isNotEmpty()) {
            val res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_ACTIVE_CARD_LIST, player.position)
            val list = cards.map { card ->
                mapOf("cardId" to card.id, "activeType" to card.getActiveAbilityByType(ability)!!.activeType)
            }
            res.private("cards", list)
            res.private("cardIds", BgUtils.card2String(cards))
            res.private("cardNum", cards.size)
            return res
        }
        return null
    }

    /**
     * 减少玩家卡牌的可使用次数

     * @param player

     * @param card
     */
    protected fun decreaseCardUseNum(player: RacePlayer, card: RaceCard) {
        this.setCardUseNum(player, card, this.getCardUseNum(player, card) - 1)
    }

    /**
     * 取得该阶段的能力类型
     * @return
     */
    protected abstract val ability: Class<out Ability>?

    /**
     * 取得当前激活能力的卡牌
     * @param player
     * @return
     */
    protected fun getActiveCard(player: RacePlayer): RaceCard {
        return this.getPlayerParamSet(player.position)["activeCard"]!!
    }

    /**
     * 取得玩家卡牌的可使用次数
     * @param player
     * @param card
     * @return
     */
    protected fun getCardUseNum(player: RacePlayer, card: RaceCard): Int {
        return getPlayerParamSet(player.position).getInteger(card)
    }

    /**
     * 设置当前激活能力的卡牌
     * @param player
     * @param card
     */
    protected fun setActiveCard(player: RacePlayer, card: RaceCard) {
        this.getPlayerParamSet(player.position)["activeCard"] = card
    }

    /**
     * 设置玩家的卡牌可以使用的次数
     * @param player
     * @param card
     * @param num
     */
    protected fun setCardUseNum(player: RacePlayer, card: RaceCard, num: Int) {
        this.getPlayerParamSet(player.position)[card] = num
    }
}
