package com.f14.innovation

import com.f14.bg.GameMode
import com.f14.bg.exception.BoardGameException
import com.f14.innovation.component.InnoCard
import com.f14.innovation.component.InnoCardGroup
import com.f14.innovation.consts.InnoAchieveTrigType
import com.f14.innovation.consts.InnoVictoryType
import com.f14.innovation.listener.InnoRoundListener
import com.f14.innovation.listener.InnoSetupListener
import com.f14.innovation.utils.InnoUtils

class InnoGameMode(override val game: Innovation) : GameMode<InnoPlayer, InnoConfig, InnoReport>(game) {
    lateinit var drawDecks: InnoCardGroup
        private set
    private var startPlayer: InnoPlayer? = null
    lateinit var victoryType: InnoVictoryType
    var victoryPlayer: InnoPlayer? = null
    var victoryObject: InnoCard? = null
    lateinit var achieveManager: InnoAchieveManager
        private set

    /**
     * 检查玩家是否达成成就胜利的条件
     * @param player
     */
    fun checkAchieveVictory(player: InnoPlayer) {
        val i = InnoUtils.getVictoryAchieveNumber(this)
        if (this.getTeamAchieveCardNum(player) >= i) {
            this.setVictory(InnoVictoryType.ACHIEVE_VICTORY, player, null)
        }
    }

    @Throws(BoardGameException::class)
    override fun endGame() {
        super.endGame()
        val endPhase = InnoEndPhase()
        endPhase.execute(this)
    }

    /**
     * 执行特殊成就的检查
     * @param trigType
     * @param player
     */
    fun executeAchieveChecker(trigType: InnoAchieveTrigType, player: InnoPlayer) {
        this.achieveManager.executeAchieveChecker(trigType, player)
    }

    /**
     * 取得玩家队伍的总成就牌数
     * @param player
     * @return
     */
    fun getTeamAchieveCardNum(player: InnoPlayer) = this.game.players.filter { it === player || this.game.isTeammates(it, player) }.sumBy { it.achieveCards.size }

    /**
     * 取得玩家队伍的总分数
     *  @param player
     * @return
     */
    fun getTeamScore(player: InnoPlayer) = this.game.players.filter { it === player || this.game.isTeammates(it, player) }.sumBy(InnoPlayer::score)

    /**
     * 判断玩家是否是敌对的(组队作战时需要实现该方法)
     * @param p1
     * @param p2
     * @return
     */
    fun isEnemy(p1: InnoPlayer, p2: InnoPlayer) = !this.game.isTeammates(p1, p2)

    override val isGameOver: Boolean
        get() = false

    @Throws(BoardGameException::class)
    override fun round() {
        this.waitForRoundAction()
    }

    @Throws(BoardGameException::class)
    override fun setupGame() {
        // 初始化游戏摸牌堆
        this.drawDecks = InnoCardGroup()
        // 取得所有卡牌的副本,添加到摸牌堆中
        val manager = this.game.getResourceManager<InnoResourceManager>()
        val cards = manager.getCardsInstanceByConfig(this.game.config)
        this.drawDecks.addCards(cards)
        this.drawDecks.reshuffle()

        // 从1-9时期各抽一张牌作为成就牌,由于成就牌有专门牌,所以这里直接移出游戏
        (1..9).forEach { this.drawDecks.draw(it) }
        // 初始化成就牌堆
        achieveManager = InnoAchieveManager(this)
        val achieveCards = manager.getAchieveCardsInstanceByConfig(this.game.config)
        achieveManager.loadAchieveCards(achieveCards)

        // 为所有玩家各摸2张等级1的牌作为起始手牌
        this.game.players.forEach { it.addHands(this.drawDecks.draw(1, 2)) }
    }

    /**
     * 设置获胜的方式和玩家
     * @param victoryType
     * @param player
     */
    fun setVictory(victoryType: InnoVictoryType, player: InnoPlayer?, victoryObject: InnoCard?) {
        // 如果游戏已经结束,则不允许再设置
        if (this.game.isPlaying) {
            this.victoryType = victoryType
            this.victoryPlayer = player
            this.victoryObject = victoryObject
            this.game.winGame()
        }
    }

    @Throws(BoardGameException::class)
    override fun startGame() {
        super.startGame()
        // 开始游戏时,所有玩家执行选择起始卡牌的行动
        this.waitForSetupPhase()
        // 确定起始玩家,起始玩家是所有选择的起始牌中字母最小的
        this.startPlayer = this.game.players.minBy { it.startCard!!.englishName!! }
        // 设置起始玩家的开始行动的标记
        this.startPlayer!!.firstAction = true
    }

    /**
     * 等待执行游戏开始的设置阶段
     *  @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForRoundAction() {
        this.addListener(InnoRoundListener(this, this.startPlayer!!))
    }

    /**
     * 等待执行游戏开始的设置阶段
     *  @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun waitForSetupPhase() {
        this.addListener(InnoSetupListener(this))
    }

}
