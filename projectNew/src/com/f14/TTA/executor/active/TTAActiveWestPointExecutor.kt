package com.f14.TTA.executor.active

import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.executor.TTAPlayTechCardExecutor
import com.f14.TTA.factory.TTAExecutorFactory
import com.f14.TTA.listener.TTARequestSelectCardListener
import com.f14.TTA.listener.TTARequestSelectListener
import com.f14.bg.exception.BoardGameException

/**
 * 查理曼的处理器

 * @author 吹风奈奈
 */
class TTAActiveWestPointExecutor(param: RoundParam, card: TTACard) : TTAActiveExecutor(param, card) {

    @Throws(BoardGameException::class)
    override fun active() {
        val listener = TTARequestSelectListener(gameMode, player, card, TTACmdString.REQUEST_SELECT, "建造,出牌", "请选择你想要进行的动作")
        val res = gameMode.insertListener(listener)
        when (res.getInteger("sel")) {
            0 -> this.actived = requestBuild()
            1 -> this.actived = requestPlayCard()
        }
    }

    @Throws(BoardGameException::class)
    private fun requestBuild(): Boolean {
        val listener = TTARequestSelectCardListener(gameMode, player, card, TTACmdString.ACTION_BUILD, "请选择要建造的建筑,部队或奇迹!")
        listener.condition = ability
        val res = gameMode.insertListener(listener)
        val subact = res.getString("subact")
        if (TTACmdString.ACTION_BUILD == subact) {
            val buildCard = res.get<TTACard>("card") ?: throw BoardGameException("找不到这张卡")
            val executor = TTAExecutorFactory.createBuildExecutor(param, buildCard)
            executor.costModify = buildCard.level * ability.property.getProperty(CivilizationProperty.RESOURCE)
            executor.cached = true
            executor.execute()
            return true
        }
        return false
    }

    @Throws(BoardGameException::class)
    private fun requestPlayCard(): Boolean {
        val listener = TTARequestSelectCardListener(gameMode, player, card, TTACmdString.ACTION_PLAY_CARD, "请选择要打出的手牌!")
        listener.condition = ability
        val res = gameMode.insertListener(listener)
        val subact = res.getString("subact")
        if (TTACmdString.ACTION_PLAY_CARD == subact) {
            val playCard = res.get<TechCard>("card") ?: throw BoardGameException("找不到这张卡")
            val executor = TTAPlayTechCardExecutor(param, playCard)
            executor.costModify = playCard.level * ability.property.getProperty(CivilizationProperty.SCIENCE)
            executor.cached = true
            executor.execute()
            return true
        }
        return false
    }

}
