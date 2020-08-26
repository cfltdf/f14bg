package com.f14.PuertoRico.game

import com.f14.PuertoRico.component.*
import com.f14.PuertoRico.consts.Character
import com.f14.PuertoRico.consts.GameState
import com.f14.PuertoRico.consts.GoodType
import com.f14.PuertoRico.consts.Part
import com.f14.PuertoRico.game.listener.*
import com.f14.PuertoRico.manager.PRResourceManager
import com.f14.bg.GameMode
import com.f14.bg.exception.BoardGameException
import java.util.*

open class PRGameMode(final override val game: PuertoRico) : GameMode<PRPlayer, PrConfig, PrReport>(game) {
    val validCharacters: Array<Character> = arrayOf(Character.BUILDER, Character.CAPTAIN, Character.CRAFTSMAN, Character.MAYOR, Character.PROSPECTOR, Character.SETTLE, Character.TRADER)
    var totalVp: Int = 0
    var builtNum: Int = 0
    var actionNum: Int = 0
    var startDoubloon: Int = 0
    val ccards: PRDeck<CharacterCard>
    val partPool: PrPartPool
    val plantations: PRTileDeck
    val plantationsDeck: PRTileDeck
    val quarriesDesk: PRTileDeck
    val forestDeck: PRTileDeck
    val tradeHouse: TradeHouse
    val buildingPool: BuildingPool

    var shipPort: ShipPort
    var notEnoughColonist = false
    protected var state: GameState? = null

    init {

        builtNum = 12
        actionNum = 1

        partPool = PrPartPool()
        this.initPartPool()

        shipPort = ShipPort()
        this.initShip()

        // 允许4个货物交易的交易所
        tradeHouse = TradeHouse(4)

        val rm = game.getResourceManager<PRResourceManager>()
        // 初始化角色牌堆
        val cs = rm.getCharacterCards()
        this.regroupCharacterCards(cs)
        ccards = PRDeck(cs)

        // 初始化种植园牌堆
        plantations = PRTileDeck()
        plantationsDeck = PRTileDeck(rm.getPlantations(this.game.config))
        quarriesDesk = PRTileDeck(rm.getQuarries(this.game.config))

        buildingPool = BuildingPool()
        if (this.game.config.isBaseGame) {
            // 如果该游戏只是基础版游戏,则直接初始化建筑物板块
            buildingPool.addCards(rm.getBuildings(this.game.config))
            buildingPool.sort()
        } else {
            // 否则的话就为扩充版游戏整理数据
            // 设置使用版本所有的建筑
            this.buildingPool.setAllBuildings(rm.getBuildings(this.game.config))
            if (game.config.isRandom) {
                // 随机抽取使用的建筑
                this.buildingPool.randomChooseBuildings()
            }
        }
        //初始化森林牌堆
        this.forestDeck = PRTileDeck(rm.getForest(this.game.config))

    }

    @Throws(BoardGameException::class)
    override fun endGame() {
        super.endGame()
        // 结束时算分
        val endPhase = PrEndPhase()
        endPhase.execute(this)
    }

    /**
     * 回合结束
     */
    override fun endRound() {
        super.endRound()
        // 所有未使用的角色+1块钱
        this.ccards.cards.filter(CharacterCard::isCanUse).forEach { it.doubloon++ }
        // 清除所有玩家选择的角色
        this.game.players.forEach {
            it.character = null
            it.isUsedDoublePriv = false
        }
        // 总督前进一位,并设置当前玩家为总督
        this.game.governor = this.game.getNextPlayer(this.game.governor)
        this.game.roundPlayer = this.game.governor
    }

    /**
     * 取得剩余的资源数量
     * @param part
     * @return
     */
    fun getAvailablePartNum(part: Any) = partPool.getAvailableNum(part)

    /**
     * 按照id取得角色牌,没有取到则抛出异常
     * @param cardId
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getCharacterCard(cardId: String) = this.ccards.getCard(cardId)

    protected open fun initPartPool() {
        // 初始化配件数量
        partPool.setPart(Part.QUARRY, 8)
        partPool.setPart(GoodType.CORN, 10)
        partPool.setPart(GoodType.INDIGO, 11)
        partPool.setPart(GoodType.SUGAR, 11)
        partPool.setPart(GoodType.TOBACCO, 9)
        partPool.setPart(GoodType.COFFEE, 9)
    }

    protected open fun initShip() {
        // 设置人数不同时不同数量的配件
        when (this.game.currentPlayerNumber) {
            2 -> {
                // 2人游戏
                totalVp = 65
                partPool.setPart(Part.COLONIST, 42)
                startDoubloon = 3
                shipPort.add(Ship(4))
                shipPort.add(Ship(6))
            }
            3 // 3人游戏
            -> {
                totalVp = 75
                partPool.setPart(Part.COLONIST, 58)
                startDoubloon = 2
                shipPort.add(Ship(4))
                shipPort.add(Ship(5))
                shipPort.add(Ship(6))
            }
            4 // 4人游戏
            -> {
                totalVp = 100
                partPool.setPart(Part.COLONIST, 79)
                startDoubloon = 3
                shipPort.add(Ship(5))
                shipPort.add(Ship(6))
                shipPort.add(Ship(7))
            }
            else -> {
                // case 5: //5人游戏
                totalVp = 122
                partPool.setPart(Part.COLONIST, 100)
                startDoubloon = 4
                shipPort.add(Ship(6))
                shipPort.add(Ship(7))
                shipPort.add(Ship(8))
            }
        }
    }

    /**
     * 回合初始化
     */
    override fun initRound() {
        super.initRound()
        // 重置可选角色
        this.ccards.cards.forEach { it.isCanUse = true }
    }

    /**
     * 判断角色是否合法
     * @return
     */
    fun isCharacterValid(character: Character): Boolean {
        return Arrays.binarySearch(this.validCharacters, character) >= 0
    }

    override // 当VP耗尽
    // 或者有玩家的建筑达到限定个数时
    // 或者移民不够分配了
    val isGameOver: Boolean
        get() = when {
            totalVp <= 0 -> true
            game.players.any { it.buildingsSize >= this.builtNum } -> true
            else -> this.notEnoughColonist
        }

    /**
     * 判断是否需要进行船长结束时的弃货阶段
     * @return
     */
    fun needCaptainEnd(): Boolean = // 只要有玩家的资源不为空就需要弃货
            game.players.none { it.resources.isEmpty }

    /**
     * 按照当前游戏人数重新整理角色卡,剔除不需要的角色卡
     * @param cards
     */
    protected fun regroupCharacterCards(cards: MutableList<CharacterCard>) {
        // 3人游戏不使用淘金者
        // 2,4人游戏使用1个淘金者
        // 5人游戏使用所有角色
        var remove = 0
        when (this.game.currentPlayerNumber) {
            3 -> remove = 2
            2, 4 -> remove = 1
        }
        cards.removeAll(cards.filter { it.character == Character.PROSPECTOR }.take(remove))
    }

    @Throws(BoardGameException::class)
    override fun round() {
        do {
            // 向所有玩家发送角色卡的信息
            this.game.sendCharacterCardInfo()
            this.game.sendPlayerActionInfo()
            this.waitForCharacter()
            when (this.game.roundPlayer!!.character) {
                Character.PROSPECTOR // 淘金者阶段
                -> this.waitForProspector()
                Character.MAYOR // 市长阶段
                -> this.waitForMajor()
                Character.SETTLE // 拓荒者阶段
                -> this.waitForSettle()
                Character.TRADER // 商人阶段
                -> this.waitForTrader()
                Character.CRAFTSMAN // 手工业者阶段
                -> this.waitForCraftsman()
                Character.BUILDER // 建筑师阶段
                -> this.waitForBuilder()
                Character.CAPTAIN // 船长阶段
                -> {
                    this.waitForCaptain()
                    this.waitForCaptainEnd()
                }
            }
            this.report.line()
            this.game.nextPlayerRound()
            // 如果跑了一圈,则回合结束
        } while (game.roundPlayer !== game.governor)
    }

    /**
     * 游戏初始化设置
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun setupGame() {
        // 给玩家分配初始的种植园板块
        val players = this.game.playersByOrder
        val cornIndex = when (players.size) {
            2 -> 1 // 2人游戏时,从第2个玩家开始得到玉米;
            5 -> 3 // 5人游戏时,从第4个玩家开始得到玉米
            else -> 2 // 3,4人游戏时,从第3个玩家开始得到玉米;
        }

        fun addTile(goodType: GoodType): (PRPlayer) -> Unit = { this.plantationsDeck.takeTileByGoodType(goodType)?.let(it::addTile) }
        players.take(cornIndex).forEach(addTile(GoodType.INDIGO))
        players.drop(cornIndex).forEach(addTile(GoodType.CORN))
        // 设置玩家的起始金钱
        players.forEach { it.doubloon = this.startDoubloon }

        // 打乱种植园板块,抽取人数+1的种植园板块
        this.plantationsDeck.shuffle()
        this.plantations.addCards(this.plantationsDeck.draw(this.game.currentPlayerNumber + 1))

        // 放置默认的船上的移民,为游戏人数
        this.partPool.takePart(Part.COLONIST, this.game.currentPlayerNumber)
        this.partPool.putPart(Part.SHIP_COLONIST, this.game.currentPlayerNumber)
    }

    @Throws(BoardGameException::class)
    override fun startGame() {
        // 先发送玩家的位置信息
        // 如果使用了扩充,并且需要手动选择建筑,则进行选择建筑的阶段
        if (!this.game.config.isBaseGame && !this.game.config.isRandom) {
            this.waitForChooseBuilding()
        }
        super.startGame()
    }

    /**
     * 等待玩家执行建筑师阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForBuilder() {
        log.info("进入建筑师阶段...")
        val al = BuilderListener(this)
        this.addListener(al)
        log.info("建筑师阶段结束!")
    }

    /**
     * 等待玩家执行船长阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForCaptain() {
        log.info("进入船长阶段...")
        val al = CaptainListener(this)
        this.addListener(al)
        log.info("船长阶段结束!")
    }

    /**
     * 等待玩家执行船长弃货阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForCaptainEnd() {
        log.info("进入船长弃货阶段...")
        // 如果不需要弃货则直接跳过
        if (this.needCaptainEnd()) {
            val al = CaptainEndListener(this)
            this.addListener(al)
        }
        log.info("船长弃货阶段结束!")
    }

    /**
     * 等待玩家选择角色
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForCharacter() {
        log.info("等待玩家选择角色...")
        val al = ChooseCharacterListener(this)
        this.addListener(al)
        log.info("玩家选择角色完成!")
    }

    /**
     * 等待玩家执行选择建筑阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForChooseBuilding() {
        log.info("开始选择使用的建筑...")
        // this.setGameState(GameState.PHASE_CHOOSE_BUILDING);
        val al = ChooseBuildingListener(this)
        this.addListener(al)
        log.info("选择建筑完成!")
    }

    /**
     * 等待玩家执行手工业者阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForCraftsman() {
        log.info("进入手工业者阶段...")
        val al = CraftsmanListener(this)
        this.addListener(al)
        log.info("手工业者阶段结束!")
    }

    /**
     * 等待玩家执行市长阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForMajor() {
        log.info("进入市长阶段...")
        val al = MajorListener(this)
        this.addListener(al)
        log.info("市长阶段结束!")
    }

    /**
     * 等待玩家执行淘金者阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForProspector() {
        log.info("进入淘金者阶段...")
        val phase = ProspectorPhase(this)
        phase.execute()
        log.info("淘金者阶段结束!")
    }

    /**
     * 等待玩家执行拓荒者阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForSettle() {
        log.info("进入拓荒者阶段...")
        val al = SettleListener(this)
        this.addListener(al)
        log.info("拓荒者阶段结束!")
    }

    /**
     * 等待玩家执行商人阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun waitForTrader() {
        log.info("进入商人阶段...")
        val al = TraderListener(this)
        this.addListener(al)
        log.info("商人阶段结束!")
    }

}
