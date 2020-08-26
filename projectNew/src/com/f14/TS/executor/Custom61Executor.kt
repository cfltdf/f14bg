package com.f14.TS.executor

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.Country
import com.f14.TS.consts.SuperPower
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException

/**
 * #61-石油输出国家组织 的执行器

 * @author F14eagle
 */
class Custom61Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam) : TSActionExecutor(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun execute() {
        // 苏联每控制一个下列国家就获得1VP
        // 埃及、伊朗、利比亚、沙特阿拉伯、伊拉克、海湾诸国和委内瑞拉
        val player = gameMode.game.ussrPlayer

        val countries = arrayOf(Country.EGY, Country.IRI, Country.LBA, Country.KSA, Country.IRQ, Country.SOG, Country.VEN)
        val vp = countries.map(gameMode.countryManager::getCountry).count { it.controlledPower == SuperPower.USSR }
        gameMode.game.adjustVp(player, vp)
    }

}
