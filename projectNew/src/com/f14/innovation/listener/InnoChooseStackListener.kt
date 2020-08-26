package com.f14.innovation.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.consts.ConditionResult
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.InnoCardDeck
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.component.condition.InnoCardCondition
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam

/**
 * 选择牌堆的监听器

 * @author F14eagle
 */
open class InnoChooseStackListener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoChooseCardListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    protected var selectedCards = InnoCardDeck()

    /**
     * 选择牌完成后触发的方法
     * @param gameMode
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun afterProcessChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        this.selectedCards.addCards(cards)
        super.afterProcessChooseCard(player, this.selectedCards.cards)
//        // 处理完成后,需要检查是否存在后继的方法需要处理
//        // 该方法存放在abilityGroup的conditionAbilities属性中,key=TRUE
//        if (this.abilityGroup != null) {
//            // 先检查THEN的方法,该方法中需要传入resultParam参数
//            var conditionAbilityGroup: InnoAbilityGroup? = abilityGroup!!.getConditionAbilityGroup(ConditionResult.THEN)
//            if (conditionAbilityGroup != null) {
//                // 取得AbilityGroup就执行
//                // 将选择的牌作为参数传入
//                this.resultParam.nextAbilityGroups.put(ConditionResult.THEN, this.targetPlayer)
//                this.cards = cards
//            }
//            // 然后检查TRUE的方法,该方法中不需要resultParam参数
//            conditionAbilityGroup = abilityGroup!!.getConditionAbilityGroup(ConditionResult.TRUE)
//            if (conditionAbilityGroup != null) {
//                // 取得AbilityGroup就执行
//                this.resultParam.nextAbilityGroups.put(ConditionResult.TRUE, player)
//            }
//            // 如果选择的卡牌数量等于最多需要选择的卡牌数量,则检查MAX方法
//            if (this.initParam!!.maxNum > 0 && cards.size >= this.initParam!!.maxNum) {
//                conditionAbilityGroup = abilityGroup!!.getConditionAbilityGroup(ConditionResult.MAX)
//                if (conditionAbilityGroup != null) {
//                    // 取得AbilityGroup就执行
//                    this.resultParam.nextAbilityGroups.put(ConditionResult.MAX, player)
//                }
//            }
//        }
    }

    /**
     * 判断是否可以选择该卡牌
     * @param card
     * @return
     */
    override fun canChooseCard(player: InnoPlayer, card: InnoCard): Boolean {
        if (ability != null) {
            // 检查是否可以选择该卡牌
            if (!ability.test(card)) {
                return false
            }
            // 需要额外检查牌堆的展开方式
            val stack = player.getCardStack(card.color!!)!!
            val black = ability.bcs.mapNotNull(InnoCardCondition::splayDirection).none { it == stack.splayDirection }
            val white = ability.wcs.mapNotNull(InnoCardCondition::splayDirection).none { it != stack.splayDirection }
            return black and white
        }
        return true
    }

    /**
     * 判断是否可以结束回应
     * @param gameMode
     * @param player
     * @return
     */
    protected open fun canEndResponse(player: InnoPlayer): Boolean {
        if (this.getAvailableCardNum(player) == 0) {
            return true
        }
        if (this.initParam != null) {
            if (this.selectedCards.size < this.initParam.num) {
                return false
            }
        }
        return true
    }

    /**
     * 对所选的牌进行校验
     * @param gameMode
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun checkChooseCard(player: InnoPlayer, cards: List<InnoCard>) {
        if (cards.isEmpty()) {
            throw BoardGameException("请选择置顶牌!")
        }
        if (this.initParam != null) {
            if (this.initParam.maxNum > 0) {
                if (cards.size > this.initParam.maxNum) {
                    throw BoardGameException("你至多只能选择" + this.initParam.maxNum + "张置顶牌!")
                }
            }
        }
        if (this.cannotChooseCard(player, cards)) {
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
    protected fun checkPlayerResponsed(player: InnoPlayer) {
        // 如果待选牌为空,则结束回应
        if (this.canEndResponse(player)) {
            this.onProcessChooseCardOver(player)
            this.setPlayerResponsed(player)
        }
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) = Unit

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        // 选择置顶牌时只指定了牌堆的颜色,需要按照牌堆的颜色取得对应的置顶牌
        val colorString = action.getAsString("colors").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择置顶牌!")
        val colorStrings = BgUtils.string2Array(colorString)
        val colors = colorStrings.map { InnoColor.valueOf(it) }.toTypedArray()
        val cards = player.getTopCards(*colors)
        this.checkChooseCard(player, cards)
        this.processChooseCard(player, cards)
        this.afterProcessChooseCard(player, cards)
        this.checkPlayerResponsed(player)
    }

    override val actionString: String
        get() = "ACTION_CHOOSE_STACK"

    /**
     * 取得所有可供选择牌的数量
     * @param player
     * @return
     */
    override fun getAvailableCardNum(player: InnoPlayer): Int {
        return player.topCards.count { this.canChooseCard(player, it) }
    }

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_CHOOSE_STACK

    /**
     * 玩家选择完所有牌之后执行的方法
     * @param gameMode
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun onProcessChooseCardOver(player: InnoPlayer) {
        // 处理完成后,需要检查是否存在后继的方法需要处理
        if (this.abilityGroup != null) {
            // 然后检查ANYWAY的方法,该方法中不需要resultParam参数
            abilityGroup.getConditionAbilityGroup(ConditionResult.ANYWAY)?.let {
                // 取得AbilityGroup就执行
                this.resultParam.nextAbilityGroups[ConditionResult.ANYWAY] = player
            }
            // 然后检查HAVE的方法,如果有选择牌,则执行
            if (!this.selectedCards.empty) {
                abilityGroup.getConditionAbilityGroup(ConditionResult.HAVE)?.let {
                    // 取得AbilityGroup就执行
                    this.resultParam.nextAbilityGroups[ConditionResult.HAVE] = player
                }
            }
        }
    }

    /**
     * 处理玩家选择的牌
     * @param gameMode
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun processChooseCard(player: InnoPlayer, cards: List<InnoCard>) = Unit

}
