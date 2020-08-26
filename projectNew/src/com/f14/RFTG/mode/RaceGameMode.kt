package com.f14.RFTG.mode

import com.f14.F14bg.consts.GameType
import com.f14.F14bg.utils.ResourceUtils
import com.f14.RFTG.RFTG
import com.f14.RFTG.RaceConfig
import com.f14.RFTG.RacePlayer
import com.f14.RFTG.RaceReport
import com.f14.RFTG.card.RaceCard
import com.f14.RFTG.card.RaceDeck
import com.f14.RFTG.card.SpecialAbility
import com.f14.RFTG.component.GoalManager
import com.f14.RFTG.consts.*
import com.f14.RFTG.listener.*
import com.f14.RFTG.manager.RaceResourceManager
import com.f14.bg.GameMode
import com.f14.bg.consts.BgVersion
import com.f14.bg.exception.BoardGameException

open class RaceGameMode(final override val game: RFTG) : GameMode<RacePlayer, RaceConfig, RaceReport>(game) {
    /**
     * 总VP数
     */
    var totalVp: Int = game.currentPlayerNumber * 12
    /**
     * 取得允许的行动数
     */
    open val actionNum = 1
    /**
     * 取得所有允许的行动
     * @return
     */
    open val validActions: Array<RaceActionType> = arrayOf(
            RaceActionType.EXPLORE_1,
            RaceActionType.EXPLORE_2,
            RaceActionType.DEVELOP,
            RaceActionType.SETTLE,
            RaceActionType.CONSUME_1,
            RaceActionType.CONSUME_2,
            RaceActionType.PRODUCE
    )
    /**
     * 取得当前状态
     */
    var state: GameState = GameState.CHOOSE_ACTION
        protected set
    /**
     * 起始发牌数
     */
    val startNumber: Int = 6
    /**
     * 手牌上限
     */
    val handsLimit: Int = 10
    /**
     * 建筑上限
     */
    val builtNum: Int = 12
    val raceDeck: RaceDeck = RaceDeck()
    val goalManager = GoalManager()

    /**
     * 检查指定阶段的目标
     * @param state
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun checkGoal(state: GameState) {
        // 如果不使用goal,则直接返回
        if (!this.game.config.isUseGoal) return
        // 遍历所有需要检查的目标
        this.goalManager.checkGoals
                // 如果是指定阶段的目标,则检查其情况
                .filter { it.phases.contains(state) }
                .forEach { g ->
                    // 计算最符合该目标的玩家目标指数
                    val gvs = g.getGoalPlayers(this.game.players)
                    val cgv = g.currentGoalValue
                    when (g.goalType) {
                        GoalType.FIRST -> // 当有玩家可以取得该目标时,将目标添加给玩家
                            this.game.getGoal(g, gvs)
                        GoalType.MOST -> when (gvs.size){
                            0 -> // 如果没人能够达到该目标的要求,则将目标退回
                                this.game.returnGoal(g)
                            1 -> // 如果该目标之前没有被人取得,并且只有1个玩家可以取得该目标,将目标添加给玩家
                                if (cgv == null) this.game.getGoal(g, gvs)
                                else if (cgv.player !== gvs.single().player) {
                                    // 如果只有1个玩家达到目标,并且该玩家不是目标的原拥有者,则需要将目标转移给新的玩家
                                    this.game.returnGoal(g)
                                    this.game.getGoal(g, gvs)
                                }
                            else -> // 有多个玩家达到目标时，如果当前拥有目标的玩家在这些玩家中,则目标不变;否则将目标退回
                                if (cgv != null && gvs.none { it.player === cgv.player }) this.game.returnGoal(g)
                        }
                    }
                }
    }

    /**
     * 弃牌,将弃牌放入弃牌堆
     * @param cards
     */
    fun discard(cards: List<RaceCard>) = this.raceDeck.discard(cards)

    /**
     * 弃牌,将弃牌放入弃牌堆
     * @param card
     */
    fun discard(card: RaceCard) = this.raceDeck.discard(card)

    /**
     * 摸牌
     * @return
     */
    fun draw() = this.raceDeck.draw()

    /**
     * 摸牌
     * @param num
     * @return
     */
    fun draw(num: Int) = this.raceDeck.draw(num)

    @Throws(BoardGameException::class)
    override fun endGame() {
        super.endGame()
        // 结束时算分
        val endPhase = RaceEndPhase()
        endPhase.execute(this)
    }

    /**
     * 取得玩家建筑卡牌的上限
     * @param player
     * @return
     */
    fun getBuiltNum(player: RacePlayer): Int = builtNum +
            (player.getAbilityBySkill(SpecialAbility::class.java, Skill.SPECIAL_WORLD_LIMIT)?.adjustNum ?: 0)

    /**
     * 取得当前牌堆的数量
     * @return
     */
    val deckSize: Int
        get() = this.raceDeck.cards.size


    /**
     * 取得玩家的手牌上限
     * @param player
     * @return
     */
    fun getHandsLimit(player: RacePlayer): Int = handsLimit +
            (player.getAbilityBySkill(SpecialAbility::class.java, Skill.SPECIAL_HAND_LIMIT)?.adjustNum ?: 0)

    /**
     * 取得选择指定行动的玩家
     * @param actionType
     * @return
     */
    fun getPlayerByAction(actionType: RaceActionType) = game.players.filter { actionType in it.actionTypes }.toSet()

    /**
     * 取得选择指定行动的玩家
     * @return
     */
    fun getPlayerByAction(actionTypes: Array<out RaceActionType>) = this.game.players
            .filter { (it.actionTypes intersect actionTypes.toList()).isNotEmpty() }
            .toSet()

    /**
     * 回合初始化
     */
    override fun initRound() {
        super.initRound()
        // 清除所有玩家的行动
        this.game.players.forEach {
            it.actionTypes.clear()
            it.roundDiscardNum = 0
        }
    }

    /**
     * 判断行动是否合法
     * @param action
     * @return
     */
    fun isActionValid(action: RaceActionType) = action in this.validActions

    /**
     * 判断是否所有的玩家都选择了行动
     * @return
     */
    protected val isAllPlayersChooseAction: Boolean
        get() = this.game.players.none { it.actionTypes.isEmpty() }

    // 当VP耗尽或者有玩家的建筑达到限定个数时,游戏结束
    override val isGameOver: Boolean
        get() = totalVp <= 0 || game.players.any { it.builtCards.size >= this.getBuiltNum(it) }

    /**
     * 判断当前游戏是否需要选择起始星球
     * @return
     */
    val needChooseStartWorld: Boolean
        get() = this.game.config.versions.containsAll(setOf(BgVersion.EXP1, BgVersion.EXP2))

    /**
     * 判断是否有人需要弃牌
     * @return
     */
    val needDiscard: Boolean
        get() = this.game.players.any { it.handSize > this.getHandsLimit(it) }

    /**
     * 移除指定的行动
     * @param players
     * @param action
     */
    private fun removeAction(players: Collection<RacePlayer>, action: RaceActionType) = players.forEach { it.actionTypes.remove(action) }

    /**
     * 移除指定的行动
     * @param players
     * @param actions
     */
    private fun removeAction(players: Collection<RacePlayer>, actions: Array<out RaceActionType>) = actions.forEach { removeAction(players, it) }

    @Throws(BoardGameException::class)
    override fun round() {
        this.waitForAction()
        this.waitForExplore()
        this.waitForDevelop()
        this.waitForSettle()
        this.waitForConsume()
        this.waitForProduce()
        this.waitForRoundDiscard()
    }

    /**
     * 设置游戏状态,同时将所有玩家的状态也设置为相同的状态
     * @param state
     */
    fun setGameState(state: GameState) {
        this.state = state
        this.game.players.forEach { it.state = state }
    }

    /**
     * 游戏初始化设置
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun setupGame() {
        val config = this.game.config
        val rm = ResourceUtils.getResourceManager<RaceResourceManager>(GameType.RFTG)
        // 如果使用目标模式,则初始化目标
        if (config.isUseGoal) {
            this.goalManager.addGoalsToDefaultDeck(rm.getGoals(config))
            this.goalManager.initGoals()
        }

        // 如果不需要选择起始星球,则直接为玩家发起始牌组
        if (!this.needChooseStartWorld) {
            // 给所有玩家发起始星球牌
            val startPlanets = RaceDeck(rm.getStartCards(config))
            startPlanets.reset()
            this.game.players.forEach { it.startWorld = startPlanets.draw() }
            // 将其他所有的牌和选剩下的起始星球牌作为默认牌堆
            val defaultCards = rm.getOtherCards(config).toMutableList()
            defaultCards.addAll(startPlanets.cards)
            this.raceDeck.defaultCards = defaultCards
            this.raceDeck.reset()
            // 给所有玩家发起始手牌
            this.game.players.forEach { o ->
                val cards = this.draw(this.startNumber)
                o.addCards(cards)
                this.report.playerAddCard(o, cards)
                if (o.startWorld!!.productionType == ProductionType.WINDFALL) {
                    o.startWorld!!.good = this.draw()
                }
            }
        }
    }

    @Throws(BoardGameException::class)
    override fun startGame() {
        super.startGame()
        // 如果需要选择起始星球,则开始对应的监听
        if (this.needChooseStartWorld) {
            this.waitForStartingWorld()
        } else {
            this.waitForStartingDiscard()
        }
        game.players.forEach(game.report::printCache)
    }

    /**
     * 等待玩家选择行动阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForAction() {
        log.info("等待玩家选择行动阶段...")
        this.setGameState(GameState.CHOOSE_ACTION)
        val al = ChooseActionListener(this)
        this.addListener(al)
        log.info("玩家选择行动完成!")
    }

    @Throws(BoardGameException::class)
    protected fun waitForPhase(gameState: GameState, listener: (RaceGameMode) -> RaceActionListener, vararg types: RaceActionType) {
        this.setGameState(gameState)
        val players = this.getPlayerByAction(types)
        // 当有玩家选择该行动时,开始执行
        if (players.isNotEmpty()) {
            val al = listener(this)
            this.addListener(al)
            // 执行完行动后,移除该玩家选择行动
            this.removeAction(players, types)
            // 检查目标
            this.checkGoal(gameState)
        }
    }


    /**
     * 等待玩家执行消费阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForConsume() {
        log.info("进入消费阶段...")
        this.waitForPhase(GameState.ACTION_CONSUME, ::ConsumeActionListener, RaceActionType.CONSUME_1, RaceActionType.CONSUME_2)
        log.info("消费阶段结束!")
    }

    /**
     * 等待玩家执行开发阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForDevelop() {
        log.info("进入开发阶段...")
        this.waitForPhase(GameState.ACTION_DEVELOP, ::DevelopActionListener, RaceActionType.DEVELOP)
        log.info("开发阶段结束!")
    }

    /**
     * 等待玩家执行探索阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForExplore() {
        log.info("进入探索阶段...")
        this.setGameState(GameState.ACTION_EXPLORE)
        this.waitForPhase(GameState.ACTION_EXPLORE, ::ExploreActionListener, RaceActionType.EXPLORE_1, RaceActionType.EXPLORE_2)
        log.info("探索阶段结束!")
    }

    /**
     * 等待玩家执行生产阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForProduce() {
        log.info("进入生产阶段...")
        this.setGameState(GameState.ACTION_PRODUCE)
        this.waitForPhase(GameState.ACTION_PRODUCE, ::ProduceActionListener, RaceActionType.PRODUCE)
        log.info("生产阶段结束!")
    }

    /**
     * 等待玩家执行回合结束弃牌的动作
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForRoundDiscard() {
        log.info("进入检查手牌上限阶段...")
        this.setGameState(GameState.ROUND_DISCARD)
        if (this.needDiscard) {
            val al = RoundDiscardActionListener(this)
            this.addListener(al)
            // 检查目标
            this.checkGoal(GameState.ROUND_DISCARD)
        }
        log.info("检查手牌上限阶段结束!")
    }

    /**
     * 等待玩家执行扩张阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForSettle() {
        log.info("进入扩张阶段...")
        this.setGameState(GameState.ACTION_SETTLE)
        this.waitForPhase(GameState.ACTION_SETTLE, ::SettleActionListener, RaceActionType.SETTLE)
        log.info("扩张阶段结束!")
    }

    /**
     * 等待游戏开始时玩家弃牌
     * @throws InterruptedException
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForStartingDiscard() {
        log.info("等待玩家弃牌...")
        this.setGameState(GameState.STARTING_DISCARD)
        val al = StartingDiscardListener(this)
        this.addListener(al)
    }

    /**
     * 等待游戏开始时玩家选择起始星球
     * @throws InterruptedException
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForStartingWorld() {
        log.info("等待玩家选择起始牌组...")
        this.setGameState(GameState.STARTING_DISCARD)
        val al = StartingWorldListener(this)
        this.addListener(al)
    }
}
