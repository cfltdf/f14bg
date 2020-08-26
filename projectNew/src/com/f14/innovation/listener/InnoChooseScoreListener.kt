package com.f14.innovation.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam
import java.util.*

/**
 * 选择分数的监听器

 * @author F14eagle
 */
open class InnoChooseScoreListener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseCardListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {
    protected var chooseNum = 0

    protected var chooseCards: MutableList<InnoCard> = ArrayList()

    @Throws(BoardGameException::class)
    override fun beforeProcessChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        // 发送移除牌的指令
        gameMode.game.sendPlayerRemoveChooseScoreCardsResponse(player, cards)
        this.chooseNum += cards.size
        this.chooseCards.addAll(cards)
        super.beforeProcessChooseCard(player, cards)
    }

    /**
     * 对所选的牌进行校验
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun checkChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        if (cards.isEmpty()) {
            throw BoardGameException("请选择计分区的牌!")
        }
        if (this.initParam != null) {
            if (this.initParam.num > 0) {
                if (cards.size + this.chooseNum > this.initParam.num) {
                    throw BoardGameException("你最多只能选择" + this.initParam.num + "张计分区的牌!")
                }
            }
            // if(this.getInitParam().maxNum>0){
            // if(cards.size()>this.getInitParam().maxNum){
            // throw new
            // BoardGameException("你至多只能选择"+this.getInitParam().maxNum+"张计分区的牌!");
            // }
            // }
        }
        if (this.cannotChooseCard(player, cards)) {
            throw BoardGameException("你不能选择这张牌!")
        }
    }

    /**
     * 检查玩家的回应情况
     * @param player
     */
    protected fun checkPlayerResponsed(player: InnoPlayer) {
        // 如果达到选择数量,则结束回应
        if (this.initParam!!.num <= this.chooseNum || this.getAvailableCardNum(player) == 0) {
            this.setPlayerResponsed(player)
        }
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createStartListenCommand(player: InnoPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        res.private("scoreCardIds", BgUtils.card2String(player.scores.getCards()))
        res.private("num", this.initParam!!.num)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val cardIds = action.getAsString("cardIds")
        val cards = player.scores.getCards(cardIds)
        this.checkChooseCard(player, cards)
        this.beforeProcessChooseCard(player, cards)
        this.processChooseCard(player, cards)
        this.afterProcessChooseCard(player, this.chooseCards)
        this.checkPlayerResponsed(player)
    }


    override val actionString: String
        get() = ""

    /**
     * 取得所有可供选择牌的数量

     * @param player

     * @return
     */
    override fun getAvailableCardNum(player: InnoPlayer): Int {
        return player.scores.getCards().count { this.canChooseCard(player, it) }
    }

    override val validCode: Int
        get() = if (this.initParam?.num == 1) {
            InnoGameCmd.GAME_CODE_CHOOSE_SCORE_CARD
        } else {
            InnoGameCmd.GAME_CODE_CHOOSE_SCORE_CARDS
        }

    /**
     * 处理玩家选择的牌
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) {

    }
}
