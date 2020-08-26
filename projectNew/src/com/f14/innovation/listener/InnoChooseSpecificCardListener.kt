package com.f14.innovation.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.consts.ConditionResult
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.InnoCardDeck
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 选择特定的牌的监听器

 * @author F14eagle
 */
open class InnoChooseSpecificCardListener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoInterruptListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {
    var specificCards = InnoCardDeck()
        protected set
    var selectedCards = InnoCardDeck()
        protected set
    private var cards: List<InnoCard>? = null

    /**
     * 玩家选择牌之后执行的方法
     * @param player
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun afterProcessChooseCard(player: InnoPlayer, card: InnoCard) {
        // 处理完成后,需要检查是否存在后继的方法需要处理
        // 该方法存放在abilityGroup的conditionAbilities属性中,key=TRUE
        val abilityGroup = this.abilityGroup ?: return
        // 先检查THEN的方法,该方法中需要传入resultParam参数
        abilityGroup.getConditionAbilityGroup(ConditionResult.THEN)?.let {
            // 取得AbilityGroup就执行
            this.resultParam.nextAbilityGroups[ConditionResult.THEN] = this.targetPlayer
            this.cards = BgUtils.toList(card)
        }
        // 然后检查TRUE的方法,该方法中不需要resultParam参数
        abilityGroup.getConditionAbilityGroup(ConditionResult.TRUE)?.let {
            // 取得AbilityGroup就执行
            this.resultParam.nextAbilityGroups[ConditionResult.TRUE] = player
        }
        // 如果选择的卡牌数量等于最多需要选择的卡牌数量,则检查MAX方法
        if (this.initParam!!.maxNum in 1..1) {
            abilityGroup.getConditionAbilityGroup(ConditionResult.MAX)?.let {
                // 取得AbilityGroup就执行
                this.resultParam.nextAbilityGroups[ConditionResult.MAX] = player
            }
        }
    }

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        return !this.specificCards.empty && super.beforeListeningCheck(player)
    }

    /**
     * 玩家选择牌之后执行的方法

     * @param gameMode

     * @param player

     * @param card

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun beforeProcessChooseCard(player: InnoPlayer, card: InnoCard) {
        // 将该牌从待选列表中移除
        this.specificCards.removeCard(card)
        // 发送移除牌的指令
        gameMode.game.sendPlayerRemoveSpecificCardResponse(player, card)
        this.selectedCards.addCard(card)
    }

    /**
     * 判断是否可以选择该卡牌

     * @param card

     * @return
     */
    protected open fun cannotChooseCard(player: InnoPlayer, card: InnoCard): Boolean {
        // 检查是否可以选择该卡牌
        return this.ability?.test(card)?.not() ?: false
    }

    /**
     * 判断是否可以结束回应

     * @param gameMode

     * @param player

     * @return
     */
    protected open fun canEndResponse(player: InnoPlayer): Boolean {
        return this.specificCards.empty
    }

    /**
     * 对所选的牌进行校验

     * @param gameMode

     * @param player

     * @param card

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun checkChooseCard(player: InnoPlayer, card: InnoCard) {
        if (this.cannotChooseCard(player, card)) {
            throw BoardGameException("你不能选择这张牌!")
        }
    }

    /**
     * 检查玩家的回应情况

     * @param gameMode

     * @param player

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun checkPlayerResponsed(player: InnoPlayer) {
        // 如果待选牌为空,则结束回应
        if (this.canEndResponse(player)) {
            this.onProcessChooseCardOver(player)
            this.setPlayerResponsed(player)
        }
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam() = super.createInterruptParam().also { it["cards"] = this.cards }

    override fun createStartListenCommand(player: InnoPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        res.public("specificCardIds", BgUtils.card2String(this.specificCards.cards))
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val cardId = action.getAsString("cardIds")
        val card = this.specificCards.getCard(cardId)
        this.checkChooseCard(player, card)
        this.beforeProcessChooseCard(player, card)
        this.processChooseCard(player, card)
        this.afterProcessChooseCard(player, card)
        this.checkPlayerResponsed(player)
    }


    override val actionString: String
        get() = ""

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_CHOOSE_SPECIFIC_CARD

    /**
     * 玩家选择完所有牌之后执行的方法

     * @param gameMode

     * @param player

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun onProcessChooseCardOver(player: InnoPlayer) {
        // 处理完成后,需要检查是否存在后继的方法需要处理
        if (this.abilityGroup != null) {
            // 然后检查ANYWAY的方法,该方法中不需要resultParam参数
            val conditionAbilityGroup = abilityGroup.getConditionAbilityGroup(ConditionResult.ANYWAY)
            if (conditionAbilityGroup != null) {
                // 取得AbilityGroup就执行
                this.resultParam.nextAbilityGroups[ConditionResult.ANYWAY] = player
            }
        }
    }


    /**
     * 处理玩家选择的牌

     * @param gameMode

     * @param player

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun processChooseCard(player: InnoPlayer, card: InnoCard) {

    }
}
