package com.f14.TTA.listener.war

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.PopParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAAttackResolutionListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.common.ParamSet
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil
import kotlin.math.min


/**
 * 失去人口侵略/战争事件的监听器

 * @author F14eagle
 */
class LosePopulationWarListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, warCard: AttackCard, winner: TTAPlayer, loser: TTAPlayer, warParam: ParamSet) : TTAAttackResolutionListener(gameMode, trigPlayer, warCard, winner, loser, warParam) {
    internal var popParam: PopParam = PopParam()

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        return this.cannotPass()
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()

        // 设置需要丢掉的总人口数
        popParam.shouldLosePopulation = this.attackCard.loserEffect.getRealAmount(this.warParam)

        popParam.loseFirst = min(loser.tokenPool.unusedWorkers, popParam.shouldLosePopulation)
        popParam.selectedPopulation = popParam.loseFirst
        gameMode.game.refreshDecreasePopulation(loser, popParam.loseFirst)
    }

    private fun cannotPass(): Boolean {
        // 如果玩家已经没有人口了,则可以结束
        if (loser.workers <= popParam.selectedPopulation) {
            return false
        }
        // 如果玩家已经选择了足够的人口,则可以结束
        if (popParam.selectedPopulation >= popParam.shouldLosePopulation) {
            return false
        }
        // 否则不允许结束
        return true
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param[loser.position] = popParam
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val confirm = action.getAsBoolean("confirm")
        if (confirm) {
            val uncontentWorker = action.getAsBoolean("uncontentWorker")
            when {
                uncontentWorker -> {
                    // 选择的是空闲的工人
                    CheckUtils.check(loser.tokenPool.unusedWorkers < popParam.loseFirst + 1, "你没有空闲的工人!")
                    popParam.loseFirst += 1
                    popParam.selectedPopulation += 1
                    gameMode.game.refreshDecreasePopulation(player, popParam.loseFirst)
                }
                loser.tokenPool.unusedWorkers > popParam.loseFirst -> // 有空闲工人的时候必须选择空闲工人
                    throw BoardGameException("你还有空闲的工人!")
                else -> {
                    val cardId = action.getAsString("cardId")
                    val card = loser.buildings.getCard(cardId)
                    CheckUtils.check(!card.needWorker || card.availableCount <= 0, "这张牌上没有工人!")
                    CheckUtils.check(popParam.detail.containsKey(card) && card.availableCount < popParam.detail[card]!! + 1, "这张牌上没有工人!")
                    // 减少人口
                    popParam.destory(card as TechCard)
                    gameMode.game.refreshDecreasePopulation(player, card, popParam.detail[card]!!)
                }
            }
            if (this.cannotPass()) {
                // 如果还需监听,则刷新当前提示信息
                this.refreshMsg(player)
            } else {
                // 如果减少人口后可以结束,则设置玩家回应结束
                this.setPlayerResponsed(player)
            }
        } else {
            // 判断玩家是否可以结束
            CheckUtils.check(this.cannotPass(), this.getMsg(player))
            this.setPlayerResponsed(player)
        }
    }


    override val actionString: String
        get() = TTACmdString.ACTION_LOSE_POPULATION

    override fun getMsg(player: TTAPlayer): String {
        val num = popParam.shouldLosePopulation - popParam.selectedPopulation
        var msg = "你还要失去了{0}个人口,请选择!"
        msg = CommonUtil.getMsg(msg, num)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_LOSE_POP

}
