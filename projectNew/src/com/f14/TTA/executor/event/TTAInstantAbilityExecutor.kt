package com.f14.TTA.executor.event

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.CivilizationProperty
import com.f14.TTA.consts.EventType
import com.f14.TTA.listener.event.ChooseResourceListener
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import java.util.*
import kotlin.math.min

/**
 * 立即执行的事件牌能力处理器

 * @author 吹风奈奈
 */
class TTAInstantAbilityExecutor(param: RoundParam, ability: EventAbility) : TTAEventAbilityExecutor(param, ability) {


    init {
        trigPlayer = null
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        val players: List<TTAPlayer> = if (trigPlayer != null) {
            // 殖民地的即时奖励
            BgUtils.toList(trigPlayer)
        } else {
            // 事件处理选择的玩家
            gameMode.getPlayersByChooser(ability.chooser)
        }
        if (players.isNotEmpty()) {
            if (ability.eventType == EventType.CHOOSE_EVENT) {
                val events = gameMode.cardBoard.drawUnusedAcientEvent(3)
                gameMode.game.getPlayersByOrder(player).filter(players::contains).forEach { p -> p.roundTempParam[EventType.CHOOSE_EVENT] = events }
                return
            }
            // 遍历所有选取出来的玩家
            gameMode.game.getPlayersByOrder(player).filter(players::contains).forEach {
                when (ability.eventType) {
                    EventType.SCORE // 得到资源/食物/科技/文明
                    -> if (ability.byProperty == null) {
                        gameMode.game.playerAddPoint(it, ability.property)
                    } else {
                        // 如果存在参照属性,则需要乘以参照属性的倍数
                        val multi = it.getProperty(ability.byProperty!!)
                        val res = gameMode.game.playerAddPoint(it, ability.property, multi)
                        gameMode.report.playerAddPoint(it, res)
                    }
                    EventType.DRAW_MILITARY // 摸军事牌
                    -> gameMode.game.playerDrawMilitaryCard(it, ability.amount)
                    EventType.NO_DISCARD_MILITARY // 旧版政治发展回合玩家不需要弃军事牌
                    -> param.needDiscardMilitary = false
                    EventType.POLITICS_STRENGTH // 新版强权政治摸牌,Ⅳ时代中为得分
                    -> if (gameMode.gameOver) {
                        gameMode.game.playerAddCulturePoint(it, ability.amount)
                        gameMode.report.playerAddCulturePoint(it, ability.amount)
                    } else {
                        gameMode.game.playerDrawMilitaryCard(it, ability.amount)
                    }
                    EventType.INCREASE_POPULATION // 免费扩张人口
                    -> {
                        // 最多只能扩张玩家可用的人口数,如果等于0则不进行扩张
                        val num = min(it.tokenPool.availableWorkers, ability.amount)
                        if (num > 0) {
                            gameMode.game.playerIncreasePopulation(it, num)
                            gameMode.report.playerIncreasePopulationCache(it, num)
                        }
                    }
                    EventType.PRODUCE -> { // 生产
                        gameMode.game.playerProduce(it, ability.isProduceFood, ability.isDoConsumption, ability.isProduceResource, ability.isDoCorruption, false)
                    }
                    EventType.LOSE_ALL // 失去所有的资源/粮食
                    -> when (ability.byProperty) {
                        CivilizationProperty.FOOD // 失去所有的粮食
                        -> {
                            val res = gameMode.game.playerAddFood(it, -it.totalFood)
                            gameMode.report.playerAddFood(it, res)
                        }
                        CivilizationProperty.RESOURCE // 失去所有的资源
                        -> {
                            val res = gameMode.game.playerAddResource(it, -it.totalResource)
                            gameMode.report.playerAddResource(it, res)
                        }
                        else -> {
                        }
                    }
                    EventType.TOKEN // 调整标志物
                    -> gameMode.game.playerAddToken(it, ability.property)
                    EventType.ADJUST_CA // 调整内政行动点
                    -> {
                        val num = ability.amount * it.getProperty(ability.byProperty!!)
                        gameMode.game.playerAdjustCivilAction(it, num)
                    }
                    EventType.ADJUST_NEXT_CA // 调整下回合的内政行动点
                    -> {
                        val num = ability.amount * it.getProperty(ability.byProperty!!)
                        it.roundTempParam[CivilizationProperty.CIVIL_ACTION] = num
                    }
                    EventType.LOSE_LEADER  // 失去所有非当前时代的领袖
                    -> {
                        val card = it.leader
                        if (card != null && card.level < gameMode.currentAge) {
                            gameMode.game.playerRemoveCard(it, card)
                        }
                    }
                    EventType.INQUISITION // 宗教裁判所
                    -> gameMode.inquisitionPosition = player.position
                    EventType.TAKE_OTHERS // 万国来朝
                    -> {
                        val property = takeOther(gameMode, it)
                        gameMode.game.playerAddPoint(it, property)
                    }
                    else -> {
                    }
                }
            }
            if (ability.eventType == EventType.LOSE_LEADER) {
                for (p in gameMode.realPlayers) {
                    param.checkWillCard(p)
                }
            }
        }
    }


    @Throws(BoardGameException::class)
    private fun takeOther(gameMode: TTAGameMode, p: TTAPlayer): TTAProperty {
        val players = ArrayList(gameMode.realPlayers)
        players.remove(p)
        val listener = ChooseResourceListener(gameMode, ability, player)
        for (aPlayer in players) {
            listener.addListeningPlayer(aPlayer)
        }
        val res = gameMode.insertListener(listener)
        val property = TTAProperty()
        for (player in gameMode.game.getPlayersByOrder(player)) {
            if (players.contains(player)) {
                val aProperty = res.get<TTAProperty>(player.position)!!
                gameMode.game.playerAddPoint(player, aProperty)
                gameMode.report.printCache(player)
                property.addProperties(aProperty, -1)
            }
        }
        return property
    }

}
