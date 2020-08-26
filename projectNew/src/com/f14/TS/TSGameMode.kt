package com.f14.TS

import com.f14.TS.component.TSCard
import com.f14.TS.component.TSZeroCard
import com.f14.TS.component.ability.TSAbility
import com.f14.TS.consts.*
import com.f14.TS.consts.ability.TSAbilityType
import com.f14.TS.executor.TSAdjustInfluenceExecutor
import com.f14.TS.factory.InitParamFactory
import com.f14.TS.listener.*
import com.f14.TS.manager.*
import com.f14.bg.GameMode
import com.f14.bg.common.ListMap
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.utils.CollectionUtils
import java.util.*
import kotlin.math.min

class TSGameMode(override val game: TS) : GameMode<TSPlayer, TSConfig, TSReport>(game) {
    /**
     * 当前阶段
     */
    var currentPhase: TSPhase = TSPhase.EARLY
    /**
     * 行动阶段
     */
    var actionPhase: TSActionPhase = TSActionPhase.HEADLINE
    /**
     * 防御等级
     */
    var defcon: Int = 0
    /**
     * 得分 正分为苏联,负分为美国
     */
    var vp: Int = 0
    /**
     * 出牌的轮次
     */
    var turn: Int = 0
    /**
     * #106-北美防空司令部的发生标志
     */
    var flag106 = false
    /**
     * 第零回合包结果
     */
    var zeroCards: MutableList<TSZeroCard> = ArrayList(0)
    /**
     * 当前回合玩家
     */
    var turnPlayer: TSPlayer? = null
    /**
     * 中盘获胜方式
     */
    var victoryType: TSVictoryType? = null
    /**
     * 中盘获胜者
     */
    var winner: TSPlayer? = null
    val cardManager: CardManager = CardManager(this)
    val countryManager: CountryManager = CountryManager(this)
    val validManager: ValidManager = ValidManager(this)
    val scoreManager: ScoreManager = ScoreManager(this)
    val spaceRaceManager: SpaceRaceManager = SpaceRaceManager(this)
    val eventManager: EventManager = EventManager(this)

    /**
     * 变换阶段

     * @param phase
     */
    private fun changePhase(phase: TSPhase) {
        this.currentPhase = phase
        // 加入该阶段的牌
        this.cardManager.addToPlayingDeck(phase)
    }

    /**
     * 检查军事行动力
     */
    private fun checkMilitaryActionPoint() {
        this.report.system("检查军事行动力")
        val ussr = this.game.ussrPlayer
        val usa = this.game.usaPlayer
        this.report.info("当前DEFCON为 " + this.defcon)
        this.report.action(ussr, "军事行动力为 " + ussr.militaryActionWithEffect)
        this.report.action(usa, "军事行动力为 " + usa.militaryActionWithEffect)
        // 检查玩家的军事行动是否大于等于当前DEFCON
        var ussrValue = ussr.militaryActionWithEffect - this.defcon
        var usaValue = usa.militaryActionWithEffect - this.defcon

        if (ussrValue >= 0 && usaValue >= 0) {
            // 如果双方都大于等于当前DEFCON,则都不算分
            this.report.info("双方的军事行动力都达到需求")
        } else {
            // 只计算不够DEFCON的分数
            ussrValue = min(0, ussrValue)
            usaValue = min(0, usaValue)
            // 计算双方的分差,并调整VP,苏联为正分,美国为负分
            val diff = ussrValue - usaValue
            this.game.adjustVp(diff)
        }
    }

    @Throws(BoardGameException::class)
    override fun endGame() {
        super.endGame()
        // 结束时算分
        val endPhase = TSEndPhase()
        endPhase.execute(this)
    }

    override fun endRound() {
        super.endRound()
        // 回合结束时,如果保留计分牌,算输
        val ussr = this.game.ussrPlayer.hasScoreCard()
        val usa = this.game.usaPlayer.hasScoreCard()
        if (ussr && usa) {
            // 如果两者都有计分牌,直接中断游戏,2个SB!
            this.game.interruptGame()
            this.endGame()
        } else if (ussr) {
            // 苏联保留则美国胜
            this.game.playerWin(this.game.usaPlayer, TSVictoryType.SCORE_CARD)
        } else if (usa) {
            // 美国保留则苏联胜
            this.game.playerWin(this.game.ussrPlayer, TSVictoryType.SCORE_CARD)
        }

        // 计算双方的军事力得分
        this.checkMilitaryActionPoint()

        // 移除所有回合效果的牌
        val cards = this.eventManager.removeTurnEffectCards()
        for (card in cards) {
            for (player in this.game.players) {
                player.removeEffect(card)
            }
        }
        this.game.sendRemoveActivedCardsResponse(cards, null)
        // #86这个牌比较特殊,该牌只在回合结束时移除玩家的效果
        if (this.eventManager.isCardActived(86)) {
            val card = this.cardManager.getCardByCardNo(86)!!
            for (player in this.game.players) {
                player.removeEffect(card)
            }
        }

        // 检查玩家是否有太空竞赛特权-回合结束时可以丢弃手牌
        try {
            this.waitForDiscardPhase()
        } catch (e: BoardGameException) {
            log.fatal("执行回合弃牌阶段发生错误!", e)
        }

    }

    /**
     * @return
     */
    val firstPlayer: TSPlayer
        get() {
            if (this.currentPhase == TSPhase.EARLY && this.game.usaPlayer.hasEffect(EffectType.USA_GO_FIRST)) {
                return this.game.usaPlayer
            }
            return this.game.ussrPlayer
        }

    /**
     * 取得本回合行动轮的上限次数,为所有玩家最多的行动轮次数

     * @return
     */
    private val turnLimit: Int
        get() {
            return game.players.map(TSPlayer::actionRoundNumber).max() ?: 0
        }

    override fun initRound() {
        super.initRound()
        // 回合开始时,设置所有玩家的太空竞赛参数
        for (player in this.game.players) {
            player.params.setRoundParameter("spaceRaceChance", 1)
        }
    }

    override // 回合数结束
    val isGameOver: Boolean
        get() = this.round > TSConsts.MAX_ROUND

    @Throws(BoardGameException::class)
    override fun round() {
        // 回合开始阶段,round1不执行
        if (this.round > 1) {
            this.roundStartPhase()
        }
        // 设置行动轮数
        this.turn = 1
        this.game.sendBaseInfo(null)
        // 回合开始时先进行头条阶段
        this.report.system("头条阶段")
        this.actionPhase = TSActionPhase.HEADLINE
        this.waitForHeadLine()

        this.actionPhase = TSActionPhase.ACTION_ROUND
        // 开始玩家的行动
        // 设置玩家默认的当前回合行动轮数
        var turnLimit = TSConsts.getRoundTurnNum(this.round)
        for (player in this.game.players) {
            player.actionRoundNumber = turnLimit
        }
        turnLimit = this.turnLimit
        while (this.turn <= turnLimit) {
            this.report.system("第 " + this.turn + " 行动轮")
            this.waitForRoundAction()
            this.turn += 1
            // 每个回合都需要更新回合数限制
            turnLimit = this.turnLimit
            this.game.sendBaseInfo(null)
        }
    }

    /**
     * 回合开始阶段
     */
    private fun roundStartPhase() {
        if (this.round == 4) {
            // 第4回合进入冷战中期
            this.changePhase(TSPhase.MID)
        } else if (this.round == 8) {
            // 第8回合进入冷战后期
            this.changePhase(TSPhase.LATE)
        }
        // 将牌补到回合手牌上限
        val handLimit = TSConsts.getRoundHandsNum(this.round)
        this.game.dealCards(handLimit)
        for (player in this.game.players) {
            /*
             * int drawNum = handLimit - player.hands.size(); if (drawNum > 0) {
			 * this.getGame().playerDrawCard(player, drawNum); }
			 */
            // 双方的军事点数清零
            this.game.playerSetMilitaryAction(player, 0)
        }
        // DEFCON等级+1
        this.game.adjustDefcon(1)
        // 中国牌设置为可用
        val owner = this.game.getPlayer(this.cardManager.chinaOwner)
        owner?.let { this.game.changeChinaCardOwner(it, true) }
    }

    @Throws(BoardGameException::class)
    override fun setupGame() {
        // 设置初始值
        this.defcon = 5
        this.turn = 1
        this.vp = 0
        // this.flag106 = false;

        // 设置玩家1为USSR,玩家2为USA
        this.game.getPlayer(0).superPower = SuperPower.USSR
        this.game.getPlayer(1).superPower = SuperPower.USA
        this.game.spplayers[SuperPower.USSR] = this.game.getPlayer(0)
        this.game.spplayers[SuperPower.USA] = this.game.getPlayer(1)

        // 设置所有国家的默认影响力
        for (c in this.countryManager.allCountries) {
            c.stabilizationBonus = 0
            c.checkControlledPower()
        }

    }

    @Throws(BoardGameException::class)
    override fun startGame() {
        super.startGame()
        if (this.game.config.versions.contains("ZERO")) {
            this.waitForZeroRound()
        }
        // 开始游戏
        // 执行游戏开始时放置影响力的监听器
        this.waitForSetupPhase()
    }

    /**
     * 等待执行回合结束时的弃牌阶段

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForDiscardPhase() {
        // 检查是否需要为玩家创建该监听器
        for (player in this.game.playersByOrder) {
            // 如果玩家拥有手牌,并且有该太空竞赛的特权,则创建监听器
            if (!player.hands.empty && player.hasEffect(EffectType.SR_PRIVILEGE_3)) {
                val initParam = InitParamFactory.createActionInitParam(player, null, null)
                val l = TSRoundDiscardListener(player, this, initParam)
                this.addListener(l)
            }
        }
    }

    /**
     * 等待执行头条阶段

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForHeadLine() {
        this.addListener(TSHeadLineListener(this))
    }

    /**
     * 等待执行玩家回合行动

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForRoundAction() {
        this.addListener(TSRoundListener(this))
    }

    /**
     * 等待执行游戏开始的设置阶段

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForSetupPhase() {

        val num = TSConsts.getRoundHandsNum(round)

        // 把中国牌给苏联并设为可用
        this.cardManager.chinaOwner = SuperPower.USSR
        this.cardManager.chinaCanUse = true
        this.report.playerGetChinaCard(game.ussrPlayer, true)

        val leftAbilities = ListMap<TSPlayer, TSAbility>()
        val firstCards = ListMap<TSPlayer, TSCard>()

        for (c in this.zeroCards) {
            this.report.line()
            this.report.showZeroCardResult(c)
            if (c.abilityGroup != null) {
                for (ability in c.abilityGroup!!.abilities) {
                    val player = this.game.getPlayer(ability.trigPower) ?: this.game.getPlayer(c.superPower)!!
                    when (ability.abilityType) {
                        TSAbilityType.ADD_EFFECT -> {
                            if (ability.actionParam.descr.isNotEmpty()) this.report.info(ability.actionParam.descr)
                            this.game.processAbilities(BgUtils.toList(ability), player, c)
                        }
                        TSAbilityType.REMOVE_CARD, TSAbilityType.REPLACE_NEW_CARD, TSAbilityType.MOVE_CARD_TO_MIDDLE, TSAbilityType.ACTION_PARAM_EFFECT, TSAbilityType.ACTION_EXECUTOR -> this.game.processAbilities(BgUtils.toList(ability), player, c)
                        TSAbilityType.TAKE_CARD_FIRST -> {
                            val initParam = InitParamFactory.createActionInitParam(player, c, TrigType.EVENT)
                            val cards = cardManager.removeCards(ability.cardCondGroup).toMutableList()
                            val executor = TakeCardExecutor(player, this, initParam, cards)
                            executor.execute()
                            firstCards.getList(player).addAll(executor.selectedCards)
                        }
                        TSAbilityType.ADJUST_INFLUENCE -> {
                            val initParam = InitParamFactory.createActionInitParam(this, player, c, ability, TrigType.EVENT)
                            val descr = initParam.realMsg.replace("请", "在布置阶段可以")
                            this.report.action(player, descr)
                            leftAbilities.add(player, ability)
                        }
                        else -> Unit
                    }
                }
            }
        }
        this.report.line()

        do {
            var restart = false
            // 重设卡牌
            this.cardManager.reset()
            // 为所有玩家发手牌
            this.cardManager.playingDeck.reshuffle()
            for (player in this.game.players) {
                val cards = firstCards.getList(player)
                this.game.sendPlayerAddHandsResponse(player, cards, null)
                player.hands.addCards(cards)
                this.game.playerDrawCard(player, num - player.hands.size)
            }
            val p: TSPlayer = this.game.players.lastOrNull { it.scoreCardsCount == 3 } ?: break
            val param = this.insertListener(TSChoiceResetHandListener(this, p))
            if (param.getInteger("choice") == 1) {
                game.report.playerResetHand(p)
                // 重设卡牌
                for (player in this.game.players) {
                    this.game.sendPlayerRemoveHandsResponse(player, player.hands.cards, null)
                    player.hands.reset()
                }
                restart = true
            }
        } while (restart)

        // 为所有玩家创建分配影响力的监听器
        for (player in this.game.playersByOrder) {
            this.report.action(player, "开始放置默认影响力!")
            val initParam = InitParamFactory.createSetupInfluence(player.superPower)!!
            val executor = TSAdjustInfluenceExecutor(player, this, initParam)
            executor.execute()
            this.game.processAbilities(leftAbilities.getList(player), player, null)
        }
        // 检查是否有让点,如果有让点,则由美国玩家放置让点
        val point = this.game.config.point
        when {
            point > 0 -> {
                this.report.action(this.game.ussrPlayer, "让${point}点")
                val initParam = InitParamFactory.createGivenPointInfluence(SuperPower.USA, point)
                val executor = TSAdjustInfluenceExecutor(game.usaPlayer, this, initParam)
                executor.execute()
            }
            point < 0 -> {
                this.report.action(this.game.usaPlayer, "让${-point}点")
                val initParam = InitParamFactory.createGivenPointInfluence(SuperPower.USSR, -point)
                val executor = TSAdjustInfluenceExecutor(game.ussrPlayer, this, initParam)
                executor.execute()
            }
        }
    }

    @Throws(BoardGameException::class)
    private fun waitForZeroRound() {
        this.round = 0
        super.initRound()
        for (p in arrayOf(this.game.usaPlayer, this.game.ussrPlayer)) {
            val cards = this.cardManager.zeroHandDeck.cards.filter { it.superPower == p.superPower }
            p.hands.addCards(cards)
            this.game.sendPlayerAddHandsResponse(p, cards, null)
            this.game.report.playerDrawCards(p, cards)
        }
        for (i in 1..2) {
            this.report.line()
            this.report.info("第${i}轮危机牌")
            val cards = ArrayList(this.cardManager.getZeroDeck(i))
            CollectionUtils.shuffle(cards)
            for (c in cards) {
                this.report.line()
                this.report.info("翻出危机牌${c.reportString}")
                this.addListener(TSZeroCardListener(this, c))
            }
        }
        this.report.line()
        for (player in this.game.players) {
            this.game.report.playerRemoveCards(player, player.hands.cards)
            this.game.playerRemoveHands(player, player.hands.cards.toList())
        }
        super.endRound()
    }

}
