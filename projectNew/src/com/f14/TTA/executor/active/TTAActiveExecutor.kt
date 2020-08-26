package com.f14.TTA.executor.active

import com.f14.TTA.component.ability.ActiveAbility
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.RoundStep
import com.f14.TTA.executor.TTAActionExecutor
import com.f14.bg.common.LifeCycle
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils

/**
 * 可激活的能力的处理器
 * @author 吹风奈奈
 */
abstract class TTAActiveExecutor(param: RoundParam, protected var card: TTACard) : TTAActionExecutor(param) {
    var actived: Boolean = false
    val ability: ActiveAbility = card.activeAbility ?: throw BoardGameException("-")

    /**
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun active()

    /**
     * 卡牌使用后处理的事件
     */
    protected open fun afterActived() {
//        val ability = ability
        if (ability.isUseActionPoint) {
            param.useActionPoint(ability.actionType, ability.actionCost)
        }
        // 如果卡牌有使用周期限制,则设置其使用参数
        if (ability.lifeCycle != null) {
            val used = player.params.getInteger(ability)
            when (ability.lifeCycle) {
                LifeCycle.GAME // 一次游戏只能使用一次
                -> player.params.setGameParameter(ability, used + 1)
                LifeCycle.ROUND // 每个回合可以使用一次
                -> player.params.setRoundParameter(ability, used + 1)
            }
        }
        // 如果使用后移除,则移除
        if (ability.sacrifice) {
            gameMode.game.playerRemoveCardDirect(player, card)
            gameMode.report.playerRemoveCardCache(player, card)
        }
        gameMode.report.playerActiveCard(player, card)
        // 刷新玩家的可使用技能列表
        gameMode.game.sendPlayerActivableCards(param.currentStep, player)
    }

    @Throws(BoardGameException::class)
    override fun check() {
        if (ability.activeStep != RoundStep.RESIGNED) {
            CheckUtils.check(!ability.checkCanActive(param.currentStep, player), "你不能使用" + card.reportString + "的能力!")
        }
        if (ability.isUseActionPoint) {
            player.checkActionPoint(ability.actionType, ability.actionCost)
        }
        super.check()
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        if (!this.checked) {
            this.check()
        }
        this.active()
        if (this.actived) {
            this.afterActived()
        }
    }

}
