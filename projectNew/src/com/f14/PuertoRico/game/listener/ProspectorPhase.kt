package com.f14.PuertoRico.game.listener

import com.f14.PuertoRico.consts.GameCmdConst
import com.f14.PuertoRico.game.PRGameMode
import com.f14.bg.InstantPhase
import com.f14.bg.exception.BoardGameException


/**
 * 淘金者

 * @author F14eagle
 */
class ProspectorPhase(private var gameMode: PRGameMode) : InstantPhase<PRGameMode>(gameMode) {

    @Throws(BoardGameException::class)
    override fun doAction() {
        // 选择淘金者阶段的玩家得到1块钱
        val player = gameMode.game.roundPlayer!!
        var doubloon = 1
        // 拥有双倍特权则再得到1块钱
        if (player.canUseDoublePriv) {
            doubloon += 1
        }
        // 检查玩家是否使用了双倍特权
        player.checkUsedDoublePriv()
        gameMode.game.getDoubloon(player, doubloon)
        gameMode.report.getDoubloon(player, doubloon)
    }

    override val validCode: Int
        get() = GameCmdConst.GAME_CODE_PROSPECTOR

}
