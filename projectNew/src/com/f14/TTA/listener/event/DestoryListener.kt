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
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil


/**
 * 摧毁事件的监听器

 * @author F14eagle
 */
class DestoryListener(gameMode: TTAGameMode, ability: EventAbility, trigPlayer: TTAPlayer) : TTAEventListener(gameMode, ability, trigPlayer) {

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 如果可以跳过选择,则玩家不必回应
        return !this.canPass(player)
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有玩家创建人口参数
        for (player in gameMode.game.players) {
            val param = PopParam()
            param.shouldLosePopulation = this.eventAbility.amount
            this.setParam(player.position, param)
        }
    }

    /**
     * 检查玩家是否可以跳过选择

     * @param player

     * @return
     */
    private fun canPass(player: TTAPlayer): Boolean {
        val param = this.getParam<PopParam>(player)
        if (param.shouldLosePopulation <= param.selectedPopulation) {
            return true
        }
        // 如果玩家没有该事件指定的摧毁对象,则可以跳过
        for (card in player.buildings.cards) {
            if (this.eventAbility.test(card) && card.needWorker) {
                val min = param.detail.getOrDefault(card as TechCard, 0)
                if (card.availableCount > min) {
                    return false
                }
            }
        }
        return true
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        for (p in this.listeningPlayers) {
            val pp = this.getParam<PopParam>(p.position)
            if (pp != null) {
                param[p.position] = pp
            }
        }
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<PopParam>(player)
        val confirm = action.getAsBoolean("confirm")
        if (confirm) {
            val cardId = action.getAsString("cardId")
            val card = player.buildings.getCard(cardId)
            CheckUtils.check(!this.eventAbility.test(card), "该事件不能选择这张牌!")
            CheckUtils.check(!card.needWorker || card.availableCount <= 0, "这张牌上没有工人!")
            param.destory(card as TechCard)
            gameMode.game.refreshDecreasePopulation(player, card, param.detail[card]!!)
            if (this.canPass(player)) {
                this.setPlayerResponsed(player)
            } else {
                this.refreshMsg(player)
            }
        } else {
            // 判断玩家是否可以结束
            CheckUtils.check(!this.canPass(player), this.getMsg(player))
            this.setPlayerResponsed(player)
        }
    }


    override val actionString: String
        get() = TTACmdString.ACTION_DESTORY

    override fun getMsg(player: TTAPlayer): String {
        var msg = "你需要摧毁{0}个{1},请选择!"
        msg = CommonUtil.getMsg(msg, this.eventAbility.amount, this.eventAbility.descr)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_DESTORY
}
