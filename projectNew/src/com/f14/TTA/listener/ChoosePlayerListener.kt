package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

abstract class ChoosePlayerListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, val usedCard: TTACard) : TTAInterruptListener(gameMode, trigPlayer) {

    init {
        this.addListeningPlayer(trigPlayer)
    }

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 如果当前游戏只有2个人,则自动选择对方
        if (gameMode.game.realPlayerNumber == 2){
            val param = this.getChooseParam(player)
            param.targetPlayer = gameMode.gameRank.players.singleOrNull { it !== player }
            return false
        }
        return true
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有玩家创建参数
        for (player in gameMode.game.players) {
            val param = ChooseParam()
            this.setParam(player.position, param)
        }
    }

    /**
     * 选择玩家时触发的方法
     * @param player
     * @param target
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun choosePlayer(player: TTAPlayer, target: TTAPlayer)

    override fun createInterruptParam(): InterruptParam {
        // 如果玩家选择了目标
        val param = this.getChooseParam(this.trigPlayer)
        return super.createInterruptParam().apply {
            this["target"] = param.targetPlayer
        }
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val confirm = action.getAsBoolean("confirm")
        val player = action.getPlayer<TTAPlayer>()
        if (confirm) {
            val targetPosition = action.getAsInt("targetPosition")
            val target = gameMode.game.getPlayer(targetPosition)
            if (player === target) throw BoardGameException("不能选择自己作为目标!")
            if (target.resigned) throw BoardGameException("不能选择已体面退出游戏的玩家!")
            this.choosePlayer(player, target)
            // 选择玩家完成后,设置选择参数,并且设置玩家已回应状态
            val param = this.getChooseParam(player)
            param.targetPlayer = target
        }
        this.setPlayerResponsed(player)
    }

    /**
     * 取得玩家的选择参数
     * @param player
     * @return
     */
    protected fun getChooseParam(player: TTAPlayer): ChooseParam {
        return this.getParam(player.position)
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_CHOOSE_PLAYER

    override fun setListenerInfo(res: BgResponse) = super.setListenerInfo(res) // 将使用牌的id设置到指令中
            .public("usedCardId", this.usedCard.id)

    /**
     * 选择玩家的参数
     * @author F14eagle
     */
    protected inner class ChooseParam {
        var targetPlayer: TTAPlayer? = null
    }
}
