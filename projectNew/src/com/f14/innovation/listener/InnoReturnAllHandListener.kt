package com.f14.innovation.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.consts.ConditionResult
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

/**
 * 归还所有手牌的监听器

 * @author F14eagle
 */
class InnoReturnAllHandListener(gameMode: InnoGameMode, trigPlayer: InnoPlayer, initParam: InnoInitParam?, resultParam: InnoResultParam, ability: InnoAbility?, abilityGroup: InnoAbilityGroup?) : InnoInterruptListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    private var cards: List<InnoCard>? = null

    /**
     * 玩家选择牌之后执行的方法
     * @param player
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun afterChooseCard(player: InnoPlayer, card: InnoCard) {
        // 处理完成后,需要检查是否存在后继的方法需要处理
        // 该方法存放在abilityGroup的conditionAbilities属性中,key=TRUE
        if (this.abilityGroup != null) {
            // 先检查THEN的方法,该方法中需要传入resultParam参数
            var conditionAbilityGroup: InnoAbilityGroup? = abilityGroup.getConditionAbilityGroup(ConditionResult.THEN)
            if (conditionAbilityGroup != null) {
                // 取得AbilityGroup就执行
                // 将选择的牌作为参数传入
                this.resultParam.nextAbilityGroups[ConditionResult.THEN] = this.targetPlayer
                this.cards = BgUtils.toList(card)
            }
            // 然后检查TRUE的方法,该方法中不需要resultParam参数
            conditionAbilityGroup = abilityGroup.getConditionAbilityGroup(ConditionResult.TRUE)
            if (conditionAbilityGroup != null) {
                // 取得AbilityGroup就执行
                this.resultParam.nextAbilityGroups[ConditionResult.TRUE] = player
            }
            // 如果选择的卡牌数量等于最多需要选择的卡牌数量,则检查MAX方法
//            if (this.initParam!!.maxNum in 1..1) {
//                conditionAbilityGroup = abilityGroup.getConditionAbilityGroup(ConditionResult.MAX)
//                if (conditionAbilityGroup != null) {
//                    // 取得AbilityGroup就执行
//                    this.resultParam.nextAbilityGroups[ConditionResult.MAX] = player
//                }
//            }
        }
    }

    override fun beforeListeningCheck(player: InnoPlayer): Boolean {
        // 没有手牌就不用执行
        return !player.hands.isEmpty && super.beforeListeningCheck(player)
    }

    /**
     * 检查玩家的回应情况
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun checkPlayerResponsed(player: InnoPlayer) {
        // 如果手牌为空,则结束回应
        if (player.hands.isEmpty) {
            this.setPlayerResponsed(player)
        }
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) = Unit

    override fun createInterruptParam() = super.createInterruptParam().also { it["cards"] = this.cards }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val cardIds = action.getAsString("cardIds")
        val card = player.hands.getCard(cardIds)
        this.processChooseCard(player, card)
        this.afterChooseCard(player, card)
        this.checkPlayerResponsed(player)
    }


    override val actionString: String
        get() = "ACTION_SELECT_CARD"

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_CHOOSE_CARD

    @Throws(BoardGameException::class)
    override fun onPlayerResponsed(player: InnoPlayer) {
        super.onPlayerResponsed(player)
        this.onProcessChooseCardOver(player)
    }

    /**
     * 玩家选择完所有牌之后执行的方法
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun onProcessChooseCardOver(player: InnoPlayer) {
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
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun processChooseCard(player: InnoPlayer, card: InnoCard) {
        gameMode.game.playerReturnCard(player, card)
    }

}
