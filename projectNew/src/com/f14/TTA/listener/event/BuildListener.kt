package com.f14.TTA.listener.event

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.CivilCard
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAEventListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam

/**
 * 选择建造的事件监听器
 * @author F14eagle
 */
class BuildListener(
        gameMode: TTAGameMode,
        eventAbility: EventAbility,
        trigPlayer: TTAPlayer
) : TTAEventListener(gameMode, eventAbility, trigPlayer) {

    override fun beforeListeningCheck(player: TTAPlayer) = // 如果玩家没有空闲人口,则跳过该玩家
            player.tokenPool.unusedWorkers > 0

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有玩家创建选择参数
        gameMode.game.players.forEach { this.setParam(it.position, BuildParam()) }
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        this.listeningPlayers
                .filter { this.getParam<BuildParam>(it).build}
                .forEach { param[it.position] = this.getBuildCard(it) }
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val confirm = action.getAsBoolean("confirm")
        val player = action.getPlayer<TTAPlayer>()
        if (confirm) {
            val card = this.getBuildCard(player) ?: throw BoardGameException("你没有可以建造的建筑!")
            // 检查建筑数量限制
            gameMode.game.playerBuildLimitCheck(player, card)
            // 检查玩家是否拥有空闲人口
            gameMode.game.playerFreeWorkerCheck(player)
            val param = this.getParam<BuildParam>(player)
            param.build = true
        }
        // 设置玩家完成回应
        this.setPlayerResponsed(player)
    }

    /**
     * 取得玩家建造的牌(只能返回CivilCard)
     * @param player
     * @return
     */
    private fun getBuildCard(player: TTAPlayer) = player.getPlayedCard(eventAbility)
            .filterIsInstance<CivilCard>()
            .firstOrNull()

    override fun getMsg(player: TTAPlayer): String {
        val card = this.getBuildCard(player)?.reportString ?: ""
        return "你拥有空闲的人口可以免费建造 1个$card,是否建造?"
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_BUILD

    /**
     * 选择的参数
     * @author F14eagle
     */
    internal inner class BuildParam(var build: Boolean = false)
}
