package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.Country
import com.f14.TS.consts.SuperPower
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #84-里根轰炸利比亚 的执行器

 * @author F14eagle
 */
class Custom84Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        // 苏联在利比亚每有2点影响力,美国就得1VP
        val player = gameMode.game.usaPlayer
        val country = gameMode.countryManager.getCountry(Country.LBA)
        val influence = country.customGetInfluence(SuperPower.USSR)
        val vp = influence / 2
        gameMode.game.adjustVp(player, vp)
    }

}
