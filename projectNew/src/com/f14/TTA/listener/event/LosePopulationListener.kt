package com.f14.TTA.listener.event

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.PopParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAEventListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.common.ParamSet
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil
import kotlin.math.min


/**
 * 失去人口事件的监听器

 * @author F14eagle
 */
class LosePopulationListener(gameMode: TTAGameMode, ability: EventAbility, trigPlayer: TTAPlayer) : TTAEventListener(gameMode, ability, trigPlayer) {

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 如果玩家没有人口..则跳过..(丫也太悲剧了...)
        val param = this.getParam<PopParam>(player)
        val ps = ParamSet()
        ps["unhappy"] = player.tokenPool.unhappyWorkers
        param.shouldLosePopulation = eventAbility.getRealAmount(ps)
        param.loseFirst = min(player.tokenPool.unusedWorkers, param.shouldLosePopulation)
        param.selectedPopulation = param.loseFirst
        gameMode.game.refreshDecreasePopulation(player, param.loseFirst)
        return !this.canPass(player)
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有玩家创建人口参数
        for (player in gameMode.game.players) {
            val param = PopParam()
            this.setParam(player.position, param)
        }
    }

    /**
     * 检查玩家是否可以结束选择人口

     * @param player

     * @return
     */
    private fun canPass(player: TTAPlayer): Boolean {
        val param = this.getParam<PopParam>(player.position)
        // 如果玩家已经没有人口了,则可以结束
        if (player.workers <= param.selectedPopulation) {
            return true
        }
        // 如果玩家已经选择了足够的人口,则可以结束
        if (param.selectedPopulation >= param.shouldLosePopulation) {
            return true
        }
        // 否则不允许结束
        return false
    }


    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        // 所有人都选择完成后,输出各人的选择情况
        for (player in this.listeningPlayers) {
            val param = this.getParam<PopParam>(player.position)
            if (param != null && param.selectedPopulation > 0) {
                res[player.position] = param
            }
        }
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val confirm = action.getAsBoolean("confirm")
        if (confirm) {
            val param = this.getParam<PopParam>(player.position)
            val uncontentWorker = action.getAsBoolean("uncontentWorker")
            when {
                uncontentWorker -> {
                    // 选择的是空闲的工人
                    CheckUtils.check(player.tokenPool.unusedWorkers < param.loseFirst + 1, "你没有空闲的工人!")
                    param.loseFirst += 1
                    param.selectedPopulation += 1
                    gameMode.game.refreshDecreasePopulation(player, param.loseFirst)
                }
                player.tokenPool.unusedWorkers > param.loseFirst -> // 有空闲工人的时候必须选择空闲工人
                    throw BoardGameException("你还有空闲的工人!")
                else -> {
                    val cardId = action.getAsString("cardId")
                    val card = player.buildings.getCard(cardId)
                    CheckUtils.check(!card.needWorker || card.availableCount <= 0, "这张牌上没有工人!")
                    CheckUtils.check(param.detail.containsKey(card) && card.availableCount <= param.detail[card]!!, "这张牌上没有工人!")
                    // 减少人口
                    param.destory(card as TechCard)
                    gameMode.game.refreshDecreasePopulation(player, card, param.detail[card]!!)
                }
            }
            if (this.canPass(player)) {
                // 如果减少人口后可以结束,则设置玩家回应结束
                this.setPlayerResponsed(player)
            } else {
                // 如果不能结束,则刷新当前提示信息
                this.refreshMsg(player)
            }
        } else {
            // 判断玩家是否可以结束
            CheckUtils.check(!this.canPass(player), "请选择要失去的人口!")
            this.setPlayerResponsed(player)
        }
    }


    override val actionString: String
        get() = TTACmdString.ACTION_LOSE_POPULATION

    override fun getMsg(player: TTAPlayer): String {
        val param = this.getParam<PopParam>(player.position)
        val num = param.shouldLosePopulation - param.selectedPopulation
        var msg = "你还要失去了{0}个人口,请选择!"
        msg = CommonUtil.getMsg(msg, num)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_LOSE_POP

}
