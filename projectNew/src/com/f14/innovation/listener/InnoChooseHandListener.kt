package com.f14.innovation.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 选择手牌的监听器

 * @author F14eagle
 */
open class InnoChooseHandListener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseCardListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    /**
     * 对所选的牌进行校验
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun checkChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        if (cards.isEmpty()) throw BoardGameException("请选择手牌!")
        val initParam = this.initParam
        if (initParam != null) {
            if (initParam.num > 0 && cards.size != initParam.num && this.getAvailableCardNum(player) >= initParam.num) {
                throw BoardGameException("你必须选择" + initParam.num + "张手牌!")
            }
            if (initParam.maxNum > 0 && cards.size > initParam.maxNum) {
                throw BoardGameException("你至多只能选择" + initParam.maxNum + "张手牌!")
            }
        }
        if (this.cannotChooseCard(player, cards)) {
            throw BoardGameException("你不能选择这张牌!")
        }
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) = Unit

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val cardIds = action.getAsString("cardIds")
        val cards = player.hands.getCards(cardIds)
        this.checkChooseCard(player, cards)
        this.processChooseCard(player, cards)
        this.afterProcessChooseCard(player, cards)
        this.setPlayerResponsed(player)
    }


    override val actionString: String
        get() = when {
            this.initParam?.num == 1 -> "ACTION_SELECT_CARD"
            else -> "ACTION_SELECT_CARDS"
        }

    /**
     * 取得所有可供选择牌的数量
     * @param player
     * @return
     */
    override fun getAvailableCardNum(player: InnoPlayer) = player.hands.getCards().count { this.canChooseCard(player, it) }

    override val validCode: Int
        get() = when {
            this.initParam?.num == 1 -> InnoGameCmd.GAME_CODE_CHOOSE_CARD
            else -> InnoGameCmd.GAME_CODE_CHOOSE_CARDS
        }

    /**
     * 处理玩家选择的牌
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) = Unit

}
