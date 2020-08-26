package com.f14.RFTG.listener

import com.f14.RFTG.RacePlayer
import com.f14.RFTG.card.Ability
import com.f14.RFTG.card.ProduceAbility
import com.f14.RFTG.card.RaceCard
import com.f14.RFTG.consts.ProductionType
import com.f14.RFTG.consts.RaceActionType
import com.f14.RFTG.consts.Skill
import com.f14.RFTG.mode.RaceGameMode
import com.f14.RFTG.network.CmdConst
import com.f14.RFTG.network.CmdFactory
import com.f14.RFTG.utils.RaceUtils
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import java.util.*

class ProduceActionListener(gameMode: RaceGameMode) : RaceActionListener(gameMode) {

    /**
     * 激活卡牌的能力

     * @param gameMode

     * @param action

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun activeCard(action: GameAction) {
        val player = action.getPlayer<RacePlayer>()
        val cardId = action.getAsString("cardId")
        val card = player.getBuiltCard(cardId)
        val ability = card.produceAbility ?: throw BoardGameException("不能使用该卡牌!")
        if (!ability.isActive) {
            throw BoardGameException("不能使用该卡牌!")
        }
        val useNum = this.getCardUseNum(player, card)
        if (useNum <= 0) {
            throw BoardGameException("该卡牌的使用次数已经用完了!")
        }
        val p = this.getParam<ProduceParam>(player.position)
        // 激活当前使用的卡牌
        when (ability.skill) {
            Skill.PRODUCE_WINDFALL // 在意外星球生产货物的能力
            -> this.useCard(action, card)
            Skill.DISCARD_HAND_FOR_PRODUCE // 弃牌生产货物
            -> {
                if (card.good != null) {
                    throw BoardGameException("该星球已经有货物了!")
                }
                val cardIds = action.getAsString("cardIds")
                val discards = player.getCards(cardIds)
                if (discards.size != ability.discardNum) {
                    throw BoardGameException("弃牌数量不正确!")
                }
                p.goodWorlds.add(card)
                gameMode.game.useCard(player, cardId)
                gameMode.game.discardCard(player, cardIds)
                gameMode.game.produceGood(player, cardId)
            }
            else -> throw BoardGameException("未知的生产能力!")
        }
    }

    /**
     * 创建玩家的生产参数

     * @param player

     * @return
     */

    private fun createproduceParam(player: RacePlayer): ProduceParam {
        val p = ProduceParam()
        this.setParam(player.position, p)
        return p
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<RacePlayer>()
        when (val subact = action.getAsString("subact")) {
            "produce" -> {
                // 为意外星球生产货物
                val p = this.getParam<ProduceParam>(player.position)
                if (p.produceWindfallNum <= 0) {
                    throw BoardGameException("你不能在意外星球上生产货物!")
                }
                val cardIds = action.getAsString("cardIds")
                val cards = player.getBuiltCards(cardIds)
                if (cards.size != 1) {
                    throw BoardGameException("只能在一个星球上生产货物!")
                }
                // 生产货物
                this.produceWindFallGood(player, cards[0])
                p.produceWindfallNum--
            }
            "active" -> // 激活卡牌能力
                this.activeCard(action)
            "pass" -> {
                // 将跳过的信息发送给客户端
                CmdFactory.createGameResultResponse(validCode, player.position).public("subact", subact).send(gameMode)
                // 设置已回应
                this.setPlayerResponsed(player)
            }
            else -> throw BoardGameException("无效的指令!")
        }
    }


    override val ability: Class<ProduceAbility>?
        get() = ProduceAbility::class.java

    /**
     * 取得玩家可生产货物的星球

     * @param player

     * @return
     */

    private fun getGoodWorlds(player: RacePlayer): List<RaceCard> {
        // 取得所有可以生产货物的星球
        return player.builtCards.filter {
            // 生产类型并且没有货物的星球可以生产货物
            it.productionType == ProductionType.PRODUCTION && it.good == null && !it.specialProduction
        }
    }

    /**
     * 取得玩家可生产意外星球货物的数量

     * @param player

     * @return
     */
    private fun getProduceWindfallNum(player: RacePlayer): Int {
        var res = 0
        if (player.isActionSelected(RaceActionType.PRODUCE)) {
            res += 1
        }
        return res
    }

    override val validCode: Int
        get() = CmdConst.GAME_CODE_PRODUCE

    /**
     * 取得适用于该能力的货物数量

     * @param goodCards

     * @param ability

     * @return
     */
    private fun getValidGoodNum(goodCards: List<RaceCard>, ability: Ability): Int {
        return goodCards.count { it.good != null && ability.test(it) }
    }

    /**
     * 取得适用于该能力的货物种类的数量

     * @param goodCards

     * @param ability

     * @return
     */
    private fun getValidGoodTypeNum(goodCards: List<RaceCard>, ability: Ability): Int {
        val goodType = goodCards.filter { it.good != null && ability.test(it) }.map { it.goodType!! }.toSet()
        return goodType.size
    }

    /**
     * 判断玩家是否是生产最多符合能力的货物的星球
     * @param gameMode
     * @param player
     * @param ability
     * @return
     */
    private fun isMostProduced(player: RacePlayer, ability: Ability): Boolean {
        val op = this.getParam<ProduceParam>(player.position)
        val onum = this.getValidGoodNum(op.goodWorlds, ability)
        return gameMode.game.players.filter { it !== player }.map { this.getParam<ProduceParam>(it.position) }.none { onum <= this.getValidGoodNum(it.goodWorlds, ability) }
    }

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        // 检查所有玩家对于生产货物而生效的能力
        for (player in gameMode.game.players) {
            var drawNum = 0
            val effectedCards = ArrayList<RaceCard>()
            val p = this.getParam<ProduceParam>(player.position)
            for (card in player.builtCards) {
                val ability = card.produceAbility ?: continue
                when (ability.skill) {
                    null -> {
                        // 其他的能力
                        if (ability.drawAfterProduced != 0 && p.goodWorlds.contains(card)) {
                            drawNum += ability.drawAfterProduced
                            effectedCards.add(card)
                        }
                    }
                    Skill.DRAW_FOR_WORLD -> {
                        // 每个星球摸牌
                        val num = RaceUtils.getValidWorldNum(player.builtCards, ability)
                        if (num != 0) {
                            drawNum += num * ability.drawNum
                            effectedCards.add(card)
                        }
                    }
                    Skill.DRAW_FOR_PRODUCED_GOOD_TYPE -> {
                        // 每个生产的货物种类摸牌
                        val num = this.getValidGoodTypeNum(p.goodWorlds, ability)
                        if (num != 0) {
                            drawNum += num * ability.drawAfterProduced
                            effectedCards.add(card)
                        }
                    }
                    Skill.DRAW_FOR_MUST_PRODUCED ->
                        // 生产最多时摸牌
                        if (this.isMostProduced(player, ability)) {
                            drawNum += ability.drawAfterProduced
                            effectedCards.add(card)
                        }
                    Skill.DRAW_FOR_PRODUCED_WORLD -> {
                        // 每个生产货物的星球摸牌
                        val num = this.getValidGoodNum(p.goodWorlds, ability)
                        if (num != 0) {
                            drawNum += num * ability.drawAfterProduced
                            effectedCards.add(card)
                        }
                    }
                    else -> Unit
                }
            }
            if (drawNum != 0) {
                gameMode.game.sendCardEffectResponse(player, BgUtils.card2String(effectedCards))
                gameMode.game.drawCard(player, drawNum)
            }
            gameMode.report.printCache(player)
        }
    }

    @Throws(BoardGameException::class)
    override fun onReconnect(player: RacePlayer) {
        // 发送可以主动使用的卡牌
        val res = this.createActivedCardResponse(player) ?: return
        gameMode.game.sendResponse(player, res)
    }

    @Throws(BoardGameException::class)
    override fun onStartListen() {
        gameMode.report.system("生产阶段")
        val res = ArrayList<BgResponse>()
        // 开始生成阶段时,为所有可以生产的星球生产货物
        for (player in gameMode.game.players) {
            // 创建玩家的生产参数
            val p = this.createproduceParam(player)
            // 为玩家可生产货物的星球生产货物
            p.goodWorlds.addAll(this.getGoodWorlds(player))
            gameMode.game.produceGood(player, BgUtils.card2String(p.goodWorlds))
            p.produceWindfallNum = this.getProduceWindfallNum(player)
            // 创建生产货物的指令并发送到客户端
            val r = CmdFactory.createGameResponse(validCode, player.position)
            r.private("produceWindfallNum", p.produceWindfallNum)
            res.add(r)
        }
        gameMode.game.sendResponse(res)

        // 在阶段开始时,处理某些卡牌的特殊能力
        for (player in gameMode.game.players) {
            val cards = player.builtCards.filter { it.produceAbility != null }
            for (o in cards) {
                val a = o.produceAbility!!
                if (a.onStartDrawNum > 0) {
                    // 在回合开始时摸牌
                    gameMode.game.drawCard(player, a.onStartDrawNum)
                    gameMode.game.sendCardEffectResponse(player, o.id)
                }
            }
        }

        // 检查并发送可主动使用的卡牌
        this.checkActiveCards()
    }

    /**
     * 生产意外星球的货物

     * @param player

     * @param card

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun produceWindFallGood(player: RacePlayer, card: RaceCard) {
        if (card.productionType != ProductionType.WINDFALL) {
            throw BoardGameException("该星球不是意外星球!")
        }
        if (card.good != null) {
            throw BoardGameException("该星球已经有货物了!")
        }
        gameMode.game.produceGood(player, card.id)
        val p = this.getParam<ProduceParam>(player.position)
        p.goodWorlds.add(card)
    }

    /**
     * 使用卡牌的能力

     * @param gameMode

     * @param action

     * @param activeCard

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun useCard(action: GameAction, activeCard: RaceCard) {
        val player = action.getPlayer<RacePlayer>()
        // ProduceParam p = this.getParam<ProduceParam>(player.getPosition());
        val ability = activeCard.produceAbility!!
        val cardIds = action.getAsString("cardIds")
        val cards: List<RaceCard>
        when (ability.skill) {
            Skill.PRODUCE_WINDFALL // 在意外星球生产货物的能力
            -> {
                cards = player.getBuiltCards(cardIds)
                val card = try {
                    cards.single()
                } catch (e: Exception) {
                    throw BoardGameException("每次只能在1个星球上生产货物!")
                }
                if (!ability.test(card)) {
                    throw BoardGameException("该能力不能用于指定的星球!")
                }
                if (ability.isCanTargetSelf && card == activeCard) {
                    throw BoardGameException("该能力不能用于指定的星球!")
                }
                this.produceWindFallGood(player, card)
                // 发送生效卡牌列表到客户端
                gameMode.game.useCard(player, activeCard.id)
            }
            else -> throw BoardGameException("未知的生产能力!")
        }
        this.setCardUseNum(player, activeCard, 0)
    }

    internal inner class ProduceParam {
        var produceWindfallNum = 0
        var goodWorlds: MutableList<RaceCard> = ArrayList()
    }

}
