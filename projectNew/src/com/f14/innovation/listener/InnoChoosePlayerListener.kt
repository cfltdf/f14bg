package com.f14.innovation.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.consts.ConditionResult
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoGameCmd
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam


/**
 * 选择玩家的监听器
 * @author F14eagle
 */
abstract class InnoChoosePlayerListener(
        gameMode: InnoGameMode,
        trigPlayer: InnoPlayer,
        initParam: InnoInitParam?,
        resultParam: InnoResultParam,
        ability: InnoAbility?,
        abilityGroup: InnoAbilityGroup?
) : InnoInterruptListener(gameMode, trigPlayer, initParam, resultParam, ability, abilityGroup) {

    /**
     * 选择玩家完成后触发的方法
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun afterChoosePlayer(player: InnoPlayer, choosePlayer: InnoPlayer) {
        // 处理完成后,需要检查是否存在后继的方法需要处理
        // 该方法存放在abilityGroup的conditionAbilities属性中,key=TRUE
        if (this.abilityGroup != null) {
            // 先检查THEN的方法,该方法中需要传入resultParam参数
            abilityGroup.getConditionAbilityGroup(ConditionResult.THEN)?.let {
                // 取得AbilityGroup就执行
                this.resultParam.nextAbilityGroups[ConditionResult.THEN] = this.targetPlayer
            }
            // 然后检查TRUE的方法,该方法中不需要resultParam参数
            abilityGroup.getConditionAbilityGroup(ConditionResult.TRUE)?.let {
                // 取得AbilityGroup就执行
                this.resultParam.nextAbilityGroups[ConditionResult.TRUE] = player
            }
        }
    }

    /**
     * 选择玩家完成后触发的方法
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun checkChoosePlayer(player: InnoPlayer, choosePlayer: InnoPlayer) {
        if (player === choosePlayer) throw BoardGameException("不能选择自己!")
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) = Unit

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<InnoPlayer>()
        val choosePosition = action.getAsInt("choosePosition")
        val choosePlayer = gameMode.game.getPlayer(choosePosition)
        this.checkChoosePlayer(player, choosePlayer)
        this.processChoosePlayer(player, choosePlayer)
        this.afterChoosePlayer(player, choosePlayer)
        this.setPlayerResponsed(player)
    }


    override val actionString: String
        get() = "ACTION_CHOOSE_PLAYER"

    override val validCode: Int
        get() = InnoGameCmd.GAME_CODE_CHOOSE_PLAYER

    /**
     * 选择玩家完成后触发的方法
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun processChoosePlayer(player: InnoPlayer, choosePlayer: InnoPlayer)

}
