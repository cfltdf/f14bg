package com.f14.RFTG.listener

import com.f14.RFTG.RaceConfig
import com.f14.RFTG.RacePlayer
import com.f14.RFTG.RaceReport
import com.f14.RFTG.card.Ability
import com.f14.RFTG.card.ConsumeAbility
import com.f14.RFTG.card.RaceCard
import com.f14.RFTG.card.RaceDeck
import com.f14.RFTG.consts.GoodType
import com.f14.RFTG.consts.RaceActionType
import com.f14.RFTG.consts.Skill
import com.f14.RFTG.mode.RaceGameMode
import com.f14.RFTG.network.CmdConst
import com.f14.RFTG.network.CmdFactory
import com.f14.RFTG.utils.RaceUtils
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ActionStep
import com.f14.bg.utils.BgUtils
import java.util.*

class ConsumeActionListener(gameMode: RaceGameMode) : RaceActionListener(gameMode) {

    /**
     * 激活卡牌的能力
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun activeCard(action: GameAction) {
        val player = action.getPlayer<RacePlayer>()
        val cardId = action.getAsString("cardId")
        val card = player.getBuiltCard(cardId)
        val ability = card.consumeAbility ?: throw BoardGameException("不能使用该卡牌!")
        if (!ability.isActive) throw BoardGameException("不能使用该卡牌!")
        val useNum = this.getCardUseNum(player, card)
        if (useNum <= 0) throw BoardGameException("该卡牌的使用次数已经用完了!")
        val p = this.getParam<ConsumeParam>(player.position)
        // 激活当前使用的卡牌
        when (ability.skill) {
            Skill.TRADE // 交易能力
                , Skill.CONSUME // 消费能力
                , Skill.DISCARD_HANDS_FOR_VP // 弃手牌换VP
                , Skill.DIFFERENT_GOOD_CONSUME // 消费不同类型的货物
                , Skill.DISCARD_HANDS_FOR_CARD // 弃手牌换牌
                , Skill.DIFFERENT_GOOD_CONSUME_2 // 消费不同类型的货物2
            -> this.useCard(action, card)
            Skill.CONSUME_REMAINING // 消费剩余的所有货物
            -> {
                val goodCards = player.builtCardsWithGood
                if (goodCards.isEmpty()) throw BoardGameException("没有货物,不能消费!")
                // 得到的VP为消费掉的货物数量-1
                val vp = maxOf(goodCards.size - 1, 0) * p.factor
                gameMode.game.useCard(player, card.id)
                gameMode.game.discardGood(player, BgUtils.card2String(goodCards))
                gameMode.game.getVP(player, vp)
                // 减去使用次数,这个需要在将来调整
                this.setCardUseNum(player, card, 0)
            }
            Skill.SELF_CONSUME -> {
                // 消费自己货物的能力
                if (card.good == null) throw BoardGameException("该星球没有货物!")
                gameMode.game.useCard(player, card.id)
                gameMode.game.discardGood(player, card.id)
                gameMode.game.getVP(player, ability.vp * p.factor)
                // 减去使用次数,这个需要在将来调整
                this.setCardUseNum(player, card, 0)
            }
            else -> throw BoardGameException("未知的消费能力!")
        }
    }

    /**
     * 检查消费能力是否可以进行,如果不能消费则抛出异常
     * @param player
     * @param ability
     * @param goodCards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun checkConsume(player: RacePlayer, ability: ConsumeAbility, goodCards: List<RaceCard>) {
        // 货物数量为0,或者超过最大交易数量时,报错
        if (goodCards.isEmpty() || goodCards.size > ability.goodNum * ability.maxNum) throw BoardGameException("消费的货物数量错误!")
        // 货物数量不能被每次的交易数量整除,报错
        if (goodCards.size % ability.goodNum != 0) throw BoardGameException("消费的货物数量错误!")
        for (card in goodCards) {
            if (card.good == null) throw BoardGameException("该星球没有货物!")
            if (!ability.test(card)) throw BoardGameException("激活的能力不适用于该星球!")
        }
        // 消费能力是强制的,必须用完
        if (goodCards.size < ability.goodNum * ability.maxNum) {
            // 当消费货物的数量小于消费能力允许的上限时,需要检查是否有其他可以消费但是没有消费的货物
            val allGoodCards = player.getBuiltCardsWithGood(ability)
            if (allGoodCards.size > goodCards.size) throw BoardGameException("使用消费能力时必须消费掉所有可以消费的货物!")
        }
    }

    /**
     * 检查是否可以结束消费阶段,如果还剩可消费的货物则不能跳过
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun checkConsumePass(player: RacePlayer) {
        val cards = player.builtCardsWithGood
        if (cards.isNotEmpty()) {
            // 还有存货时,检查可用的能力
            val activeCards = player.getActiveCardsByAbilityType(this.ability)
            for (activeCard in activeCards.filter { this.getCardUseNum(player, it) > 0 }) {
                // 如果该能力还可用
                val ability = activeCard.consumeAbility!!
                when (ability.skill) {
                    Skill.TRADE, Skill.CONSUME_REMAINING, Skill.DIFFERENT_GOOD_CONSUME_2 -> // 交易和消费剩余所有货物的能力
                        throw BoardGameException("还有剩余的货物可以进行消费,不能结束消费!")
                    Skill.CONSUME -> // 普通的消费能力
                        if (this.getValidGoodNum(cards, ability) >= ability.goodNum) throw BoardGameException("还有剩余的货物可以进行消费,不能结束消费!")
                    Skill.DIFFERENT_GOOD_CONSUME -> // 消费不同类型货物的能力
                        if (this.getValidGoodTypeNum(cards, ability) >= ability.goodNum) throw BoardGameException("还有剩余的货物可以进行消费,不能结束消费!")
                    Skill.SELF_CONSUME -> // 消费自己的货物
                        if (activeCard.good != null) throw BoardGameException("还有剩余的货物可以进行消费,不能结束消费!")
                    else -> Unit
                }
            }
        }
    }

    /**
     * 检查不同类型货物的消费能力

     * @param player
     * @param goodCards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun checkDifferentConsume(player: RacePlayer, goodCards: List<RaceCard>) {
        val consumeGoodTypes = HashSet<GoodType>()
        for (card in goodCards) {
            if (card.good == null) throw BoardGameException("该星球没有货物!")
            consumeGoodTypes.add(card.goodType!!)
        }
        val playerGoodTypes = player.builtCards.filter { it.good != null }.map { it.goodType!! }.toSet()
        if (consumeGoodTypes.size != playerGoodTypes.size || !playerGoodTypes.containsAll(consumeGoodTypes)) throw BoardGameException("使用消费能力时必须消费掉所有可以消费的货物!")
    }

    /**
     * 检查玩家是否先进行了交易,如果没有交易则抛出异常

     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun checkTradeSkill(player: RacePlayer) {
        val p = this.getParam<ConsumeParam>(player.position)
        if (p.tradeNum > 0 && player.hasGood()) throw BoardGameException("必须先进行交易!")
    }

    /**
     * 创建玩家的消费参数
     * @param player
     * @return
     */
    private fun createConsumeParam(player: RacePlayer): ConsumeParam {
        val p = ConsumeParam(this.getTradeNum(player), if (player.isActionSelected(RaceActionType.CONSUME_2)) 2 else 1)
        this.setParam(player.position, p)
        return p
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val subact = action.getAsString("subact")
        val player = action.getPlayer<RacePlayer>()
        val p = this.getParam<ConsumeParam>(player.position)
        if ("trade" == subact) {
            // 交易
            if (p.tradeNum <= 0) {
                throw BoardGameException("不能进行交易!")
            }
            val cardIds = action.getAsString("cardIds")
            val cards = player.getBuiltCards(cardIds)
            if (cards.size != 1) {
                throw BoardGameException("每次只能交易一个货物!")
            }
            // 清空生效卡牌列表,因为可能会有多次交易
            p.effectedCards.clear()
            // 交易货物
            this.tradeGood(player, cards[0], true)
            // 发送生效卡牌列表到客户端
            gameMode.game.sendCardEffectResponse(player, BgUtils.card2String(p.effectedCards))
            p.tradeNum -= 1
        } else {
            // 如果使用交易能力,则在执行其他动作前,必须判断是否可以交易
            // 如果可以交易就必须先执行交易
            this.checkTradeSkill(player)
            when (subact) {
                "active" -> // 激活卡牌能力
                    this.activeCard(action)
                "pass" -> {
                    // 跳过之前必须判断是否还有剩余的可消费货物
                    // 消费阶段必须消费掉所有可以消费的货物
                    this.checkConsumePass(player)
                    // 将跳过的信息发送给客户端
                    CmdFactory.createGameResultResponse(validCode, player.position).public("subact", subact).send(gameMode)
                    // 设置已回应
                    this.setPlayerResponsed(player)
                }
                "gamble" -> // 执行赌博能力
                    this.gamble(action)
                else -> throw BoardGameException("无效的指令!")
            }
        }
    }

    /**
     * 执行赌博行动
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun gamble(action: GameAction) {
        val player = action.getPlayer<RacePlayer>()
        val p = this.getParam<ConsumeParam>(player.position)
        if (p.gambled) {
            throw BoardGameException("你已经赌过了,不能再赌了!")
        }
        if (!player.hasSkill(Skill.SPECIAL_GAMBLE)) {
            throw BoardGameException("只有拥有赌博能力时才能赌博!")
        }
        val cardIds = action.getAsString("cardIds")
        var cards = player.getCards(cardIds)
        val card = cards.singleOrNull() ?: throw BoardGameException("必须选择一张手牌作为赌注!")
        if (card.cost < 1 || card.cost > 6) {
            throw BoardGameException("赌注的费用必须在1-6之间!")
        }
        p.card = card
        // 暂时先弃掉赌注
        player.playCard(card.id)
        gameMode.report.playerGamble(player, card)
        gameMode.game.sendDiscardResponse(player, card.id)
        // 翻开赌注价格数量的牌
        cards = gameMode.draw(p.card!!.cost)
        gameMode.report.playerGambleResult(player, cards)
        p.cards.defaultCards = cards.toMutableList()
        this.addActionStep(player, GambleStep())
    }


    override val ability: Class<ConsumeAbility>
        get() = ConsumeAbility::class.java

    /**
     * 取得玩家交易货物后可以取得的基本牌数
     * @param card
     * @return
     */
    private fun getBaseTradeValue(card: RaceCard) = when (card.goodType) {
        GoodType.NOVELTY -> 2
        GoodType.RARE -> 3
        GoodType.GENES -> 4
        GoodType.ALIEN -> 5
        else -> 0
    }

    /**
     * 取得玩家交易货物后可以取得的交易技能调整的牌数

     * @param player
     * @param card
     * @return
     */
    private fun getSkillTradeValue(player: RacePlayer, card: RaceCard): Int {
        var res = 0
        // 计算交易能力的调整值
        val p = this.getParam<ConsumeParam>(player.position)
        p.effectedCards.clear()
        val cards = player.builtCards.filter { it.tradeAbility != null }
        for (o in cards) {
            val a = o.tradeAbility!!
            if (a.skill === null) {
                // 如果该能力指定必须要本星球交易才能生效的话,则跳过
                if (a.isCurrent && card !== o) continue
                // 交易能力的调整值
                if (a.drawNum != 0 && a.test(card)) {
                    res += a.drawNum
                    p.effectedCards.add(o)
                }
            } else if (a.skill === Skill.DRAW_FOR_WORLD) {
                // 每个星球摸牌
                val num = RaceUtils.getValidWorldNum(player.builtCards, a.worldCondition)
                if (num != 0) {
                    res += num * a.drawNum
                    p.effectedCards.add(o)
                }
            }
        }
        return res
    }

    /**
     * 取得玩家可以交易货物的数量

     * @param player
     * @return
     */
    private fun getTradeNum(player: RacePlayer) = if (player.isActionSelected(RaceActionType.CONSUME_1)) 1 else 0
//        var res = 0
//        // 选择消费1的玩家可以交易1个货物
//        if (player.isActionSelected(RaceActionType.CONSUME_1)) {
//            res += 1
//        }
//        // 其他卡牌的能力也能提升交易量
//        return res
//    }

    override val validCode: Int
        get() = CmdConst.GAME_CODE_CONSUME

    /**
     * 取得适用于该能力的货物数量

     * @param goodCards
     * @param ability
     * @return
     */
    private fun getValidGoodNum(goodCards: List<RaceCard>, ability: Ability) = goodCards.count { it.good != null && ability.test(it) }

    /**
     * 取得适用于该能力的货物种类的数量

     * @param goodCards
     * @param ability
     * @return
     */
    private fun getValidGoodTypeNum(goodCards: List<RaceCard>, ability: Ability) = goodCards.filter { it.good != null && ability.test(it) }.map { it.goodType!! }.toSet().size

    override fun onAllPlayerResponsed() {
        for (player in gameMode.game.players) {
            gameMode.report.printCache(player)
        }
    }

    @Throws(BoardGameException::class)
    override fun onReconnect(player: RacePlayer) {
        // 发送可以主动使用的卡牌
        this.createActivedCardResponse(player)?.send(gameMode, player)
        super.onReconnect(player)
    }

    @Throws(BoardGameException::class)
    override fun onStartListen() {
        gameMode.report.system("消费阶段")
        // 创建消费阶段的参数
        gameMode.game.players.forEach {
            CmdFactory.createGameResponse(validCode, it.position).private("tradeNum", this.createConsumeParam(it).tradeNum).send(gameMode)
        }
//        for (player in gameMode.game.players) {
//            val p = this.createConsumeParam(player)
//            val r = CmdFactory.createGameResponse(validCode, player.position)
//            r.private("tradeNum", p.tradeNum)
//            res.add(r)
//        }

        // 在阶段开始时,处理某些卡牌的特殊能力
        for (player in gameMode.game.players) {
            val cards = player.builtCards.filter { it.consumeAbility != null }
            for (o in cards) {
                val a = o.consumeAbility!!
                if (a.onStartDrawNum > 0) {
                    // 在回合开始时摸牌
                    gameMode.game.drawCard(player, a.onStartDrawNum)
                    gameMode.game.sendCardEffectResponse(player, o.id)
                }
                if (a.onStartVp != 0) {
                    // 在回合开始时得到VP
                    val p = this.getParam<ConsumeParam>(player.position)
                    gameMode.game.getVP(player, a.onStartVp * p.factor)
                }
            }
        }

        // 检查并发送可主动使用的卡牌
        this.checkActiveCards()
    }

    /**
     * 交易货物
     * @param player
     * @param card
     * @param withTradeSkill
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun tradeGood(player: RacePlayer, card: RaceCard, withTradeSkill: Boolean) {
        if (card.good == null) {
            throw BoardGameException("该星球没有货物!")
        }
        var drawNum = this.getBaseTradeValue(card)
        if (withTradeSkill) {
            drawNum += this.getSkillTradeValue(player, card)
        }
        gameMode.report.playerTrade(player, card)
        gameMode.game.drawCard(player, drawNum)
        gameMode.game.discardGood(player, card.id)
    }

    /**
     * 使用卡牌的能力
     * @param action
     * @param activeCard
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun useCard(action: GameAction, activeCard: RaceCard) {
        val player = action.getPlayer<RacePlayer>()
        val p = this.getParam<ConsumeParam>(player.position)
        val ability = activeCard.consumeAbility!!
        val cardIds = action.getAsString("cardIds")
        val cards: List<RaceCard>
        if (this.getCardUseNum(player, activeCard) == 0) {
            throw BoardGameException("你已经使用过这张卡!")
        }
        when (ability.skill) {
            Skill.TRADE -> {
                // 使用交易能力
                cards = player.getBuiltCards(cardIds)
                if (cards.size != 1) {
                    throw BoardGameException("交易数量不正确!")
                }
                p.effectedCards.clear()
                this.tradeGood(player, cards[0], ability.isTradeWithSkill)
                // 发送生效卡牌列表到客户端
                gameMode.game.useCard(player, activeCard.id)
                gameMode.report.playerUseCard(player, activeCard)
                gameMode.game.sendCardEffectResponse(player, BgUtils.card2String(p.effectedCards))
            }
            Skill.CONSUME -> {
                // 使用消费能力
                cards = player.getBuiltCards(cardIds)
                // 检查是否可以进行消费,如果不能消费则抛出异常
                this.checkConsume(player, ability, cards)
                gameMode.game.useCard(player, activeCard.id)
                gameMode.report.playerUseCard(player, activeCard)
                gameMode.game.discardGood(player, cardIds)
                gameMode.report.playerConsume(player, cards)
                if (ability.drawNum > 0) {
                    gameMode.game.drawCard(player, cards.size / ability.goodNum * ability.drawNum)
                }
                if (ability.vp > 0) {
                    gameMode.game.getVP(player, cards.size / ability.goodNum * ability.vp * p.factor)
                }
            }
            Skill.DIFFERENT_GOOD_CONSUME -> {
                // 消费不同类型货物的能力
                cards = player.getBuiltCards(cardIds)
                if (cards.size != ability.goodNum) {
                    throw BoardGameException("消费的货物数量错误!")
                }
                // 检查货物的类型是否都不相同
                if (cards.size != cards.distinctBy(RaceCard::goodType).size) throw BoardGameException("必须选择不同类型的货物进行消费!")
                gameMode.game.useCard(player, activeCard.id)
                gameMode.report.playerUseCard(player, activeCard)
                gameMode.game.discardGood(player, cardIds)
                gameMode.report.playerConsume(player, cards)
                gameMode.game.getVP(player, ability.vp * p.factor)
            }
            Skill.DISCARD_HANDS_FOR_VP -> {
                // 弃手牌换VP
                cards = player.getCards(cardIds)
                if (cards.isEmpty() || cards.size > ability.discardNum * ability.maxNum) throw BoardGameException("选择的卡牌数量错误!")
                gameMode.game.useCard(player, activeCard.id)
                gameMode.report.playerUseCard(player, activeCard)
                gameMode.game.discardCard(player, cardIds)
                gameMode.game.getVP(player, cards.size)
            }
            Skill.DISCARD_HANDS_FOR_CARD -> {
                // 弃手牌换牌
                cards = player.getCards(cardIds)
                if (cards.isEmpty() || cards.size > ability.discardNum * ability.maxNum) {
                    throw BoardGameException("选择的卡牌数量错误!")
                }
                gameMode.game.useCard(player, activeCard.id)
                gameMode.report.playerUseCard(player, activeCard)
                gameMode.game.discardCard(player, cardIds)
                gameMode.game.drawCard(player, ability.drawNum)
            }
            Skill.DIFFERENT_GOOD_CONSUME_2 -> {
                // 消费不同类型货物的能力2
                cards = player.getBuiltCards(cardIds)
                if (cards.isEmpty() || cards.size > 4) {
                    throw BoardGameException("消费的货物数量错误!")
                }
                // 检查货物的类型是否都不相同
                if (cards.size != cards.distinctBy(RaceCard::goodType).size) {
                    throw BoardGameException("必须选择不同类型的货物进行消费!")
                }
                // 消费能力是强制的,检查是否存在可以消费而未消费的货物
                this.checkDifferentConsume(player, cards)
                gameMode.game.useCard(player, activeCard.id)
                gameMode.report.playerUseCard(player, activeCard)
                gameMode.game.discardGood(player, cardIds)
                gameMode.report.playerConsume(player, cards)
                gameMode.game.getVP(player, cards.size * ability.vp * p.factor)
            }
            else -> throw BoardGameException("未知的消费能力!")
        }
        this.setCardUseNum(player, activeCard, 0)
    }

    /**
     * 消费阶段的子步骤
     * @author F14eagle
     */
    internal enum class ConsumeStep {
        /**
         * 赌博阶段
         */
        GAMBLE_STEP
    }

    internal class ConsumeParam(var tradeNum: Int, var factor: Int, var effectedCards: MutableSet<RaceCard> = HashSet(), var gambled: Boolean = false, var card: RaceCard? = null, var cards: RaceDeck = RaceDeck())

    /**
     * 赌博的步骤
     * @author F14eagle
     */
    internal inner class GambleStep : ActionStep<RacePlayer, RaceConfig, RaceReport>(this) {

        @Throws(BoardGameException::class)
        override fun doAction(action: GameAction) {
            val player = action.getPlayer<RacePlayer>()
            val subact = action.getAsString("subact")
            val p = getParam<ConsumeParam>(player.position)
            when (subact) {
                "gamble" -> {
                    // 选择赌注
                    if (!this.winGamble(p)) {
                        throw BoardGameException("你输掉了赌博,不能选择奖励!")
                    }
                    val cardIds = action.getAsString("cardIds")
                    val cards = p.cards.getCards(cardIds)
                    if (cards.size != 1) {
                        throw BoardGameException("你只能选择一张牌作为赌博的奖励!")
                    }
                    // 将选择的奖励连同赌注一起给玩家
                    val rewards = ArrayList(cards)
                    rewards.add(p.card!!)
                    gameMode.game.getCard(player, rewards)
                    // 将其余的牌放入弃牌堆
                    p.cards.cards.removeAll(cards)
                    gameMode.discard(p.cards.cards)
                }
                "pass" -> {
                    // 直接结束该行动,将赌注和翻开的牌放入弃牌堆
                    gameMode.discard(p.card!!)
                    gameMode.discard(p.cards.cards)
                }
                else -> throw BoardGameException("无效的指令!")
            }
            // 完成赌博,设置参数
            p.gambled = true
            p.card = null
            p.cards.clear()
        }

        override val actionCode: Int
            get() = CmdConst.GAME_CODE_GAMBLE

        override val stepCode: String
            get() = ConsumeStep.GAMBLE_STEP.toString()

        @Throws(BoardGameException::class)
        override fun onStepOver(player: RacePlayer) {
            this.createStepOverResponse(player).send(gameMode)
        }

        @Throws(BoardGameException::class)
        override fun onStepStart(player: RacePlayer) {
            val p = getParam<ConsumeParam>(player.position)
            // 将赌注和翻开的牌发送到客户端
            this.createStepStartResponse(player).public("betIds", p.card!!.id).public("cardIds", BgUtils.card2String(p.cards.cards)).public("winGamble", this.winGamble(p)).send(gameMode)
        }

        /**
         * 判断是否赢得赌博
         * @param p
         * @return
         */
        private fun winGamble(p: ConsumeParam) =// 如果翻开的牌中有比赌注费用高的牌,则赢得赌博
                p.cards.cards.any { it.cost > p.card!!.cost }

    }
}
