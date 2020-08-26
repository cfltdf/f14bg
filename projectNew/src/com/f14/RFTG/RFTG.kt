package com.f14.RFTG

import com.f14.RFTG.card.Goal
import com.f14.RFTG.card.GoalValue
import com.f14.RFTG.card.RaceCard
import com.f14.RFTG.mode.RaceGame2P
import com.f14.RFTG.mode.RaceGameMode
import com.f14.RFTG.network.CmdConst
import com.f14.RFTG.network.CmdFactory
import com.f14.bg.BoardGame
import com.f14.bg.consts.BgVersion
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.utils.StringUtils
import net.sf.json.JSONObject
import java.util.*

/**
 * Race for the galaxy
 * @author F14eagle
 */
class RFTG : BoardGame<RacePlayer, RaceConfig, RaceReport>() {
    override val mode
        get() = gameMode
    lateinit var gameMode: RaceGameMode

    @Throws(BoardGameException::class)
    override fun createConfig(obj: JSONObject): RaceConfig {
        val config = RaceConfig()
        // 必须选择基础版本
        config.versions.add(BgVersion.BASE)
        val useGoal = obj.getBoolean("useGoal")
        val versions = obj.getString("versions")
        if (!versions.isNullOrEmpty()) {
            val vs = BgUtils.string2Array(versions)
            Collections.addAll(config.versions, *vs)
        } else {
            // 如果不适用扩充,则不能开启goal
            if (useGoal) {
                throw BoardGameException("必须选择扩充版本才能开启目标任务!")
            }
        }
        config.isUseGoal = useGoal
        return config
    }

    /**
     * 玩家弃牌并将弃牌信息发送到客户端
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun discardCard(player: RacePlayer, cardIds: String) {
        val cards = player.playCards(cardIds)
        if (cards.isNotEmpty()) {
            this.gameMode.discard(cards)
            this.report.playerDiscard(player, cards)
            this.sendDiscardResponse(player, BgUtils.card2String(cards))
        }
    }

    /**
     * 玩家选择弃掉货物并将弃货信息发送到客户端
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun discardGood(player: RacePlayer, cardIds: String) {
        val cards = player.discardGoods(cardIds)
        this.gameMode.discard(cards)
        // 传入的cardIds是弃掉货物的星球的id
        this.sendDiscardGoodResponse(player, cardIds)
    }

    /**
     * 玩家弃掉已打出的卡牌并将弃牌信息发送到客户端
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun discardPlayedCard(player: RacePlayer, cardIds: String) {
        val cards = player.discardPlayedCards(cardIds)
        if (cards.isNotEmpty()) {
            this.gameMode.discard(cards)
            this.report.playerDiscardPlayed(player, cards)
            this.sendDiscardPlayedCardResponse(player, cardIds)
        }
    }

    /**
     * 玩家摸牌并将摸牌信息发送到客户端
     * @param player
     * @param drawNum
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun drawCard(player: RacePlayer, drawNum: Int) {
        val cards = this.gameMode.draw(drawNum)
        player.addCards(cards)
        gameMode.report.playerAddCard(player, cards)
        this.sendDrawCardResponse(player, BgUtils.card2String(cards))
        // 刷新牌堆数量
        this.sendRefreshDeckResponse()
    }

    /**
     * 玩家得到牌并将信息发送到客户端
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getCard(player: RacePlayer, cards: List<RaceCard>) {
        player.addCards(cards)
        gameMode.report.playerAddCard(player, cards)
        this.sendDrawCardResponse(player, BgUtils.card2String(cards))
    }

    /**
     * 玩家得到目标并发送信息到客户端
     * @param goal
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getGoal(goal: Goal, goalValues: List<GoalValue>) {
        if (goalValues.isNotEmpty()) {
            this.gameMode.goalManager.removeGoal(goal)
            for (gv in goalValues) {
                gv.player.addGoal(goal)
                goal.currentGoalValue = gv
                this.report.playerGetGoal(gv.player, goal)
                this.sendPlayerGetGoalResponse(gv.player, goal)
            }
            this.sendSupplyLostGoalResponse(goal)
        }
    }

    /**
     * 玩家得到VP并发送信息到客户端
     * @param player
     * @param vp
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getVP(player: RacePlayer, vp: Int) {
        player.vp += vp
        this.gameMode.totalVp -= vp
        this.sendGetVPResponse(player, vp, this.gameMode.totalVp)
        this.report.playerGetVp(player, vp)
    }

    override fun initConfig() {
        this.config = RaceConfig()
        this.config.isUseGoal = true
        this.config.versions.add(BgVersion.BASE)
        this.config.versions.add(BgVersion.EXP1)
    }

    override fun initConst() {}

    override fun initReport() {
        this.report = RaceReport(this)
    }

    /**
     * 玩家从手牌中打出牌并将打牌信息发送到客户端
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playCard(player: RacePlayer, cardIds: String) {
        val cards = player.playCards(cardIds)
        player.addBuiltCards(cards)
        this.sendPlayCardResponse(player, BgUtils.card2String(cards))
    }

    /**
     * 直接为星球生产货物并发送信息到客户端
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun produceGood(player: RacePlayer, cardIds: String) {
        val cards = player.getBuiltCards(cardIds)
        if (cards.isNotEmpty()) {
            for (card in cards) card.good = this.gameMode.draw()
            this.report.playerProduce(player, cards)
            this.sendProduceGoodResponse(player, cardIds)
            // 刷新牌堆数量
            this.sendRefreshDeckResponse()
        }
    }

    /**
     * 玩家将目标退回公共区并发送信息到客户端
     * @param goal
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun returnGoal(goal: Goal) {
        if (goal.currentGoalValue != null) {
            val player = goal.currentGoalValue!!.player
            player.removeGoal(goal)
            goal.currentGoalValue = null
            this.gameMode.goalManager.addGoal(goal)
            this.report.returnGoal(player, goal)
            this.sendPlayerLostGoalResponse(player, goal)
            this.sendSupplyGetGoalResponse(goal)
        }
    }

    /**
     * 发送玩家的卡牌能力生效的指令,只有在卡牌列表不为空时才发送
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendCardEffectResponse(player: RacePlayer, cardIds: String) {
        if (cardIds.isNotEmpty()) {
            CmdFactory.createCardEffectResponse(player.position, cardIds)
                    .send(this)
        }
    }

    /**
     * 发送玩家直接打牌的指令
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendDirectPlayCardResponse(player: RacePlayer, cardIds: String) {
        CmdFactory.createDirectPlayCardResponse(player.position, cardIds)
                .send(this)
    }

    /**
     * 发送玩家弃掉货物的指令
     * @param player
     * @param cardIds 弃掉货物的星球的id
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendDiscardGoodResponse(player: RacePlayer, cardIds: String) {
        CmdFactory.createDiscardGoodResponse(player.position, cardIds)
                .send(this)
    }

    /**
     * 发送玩家弃掉已打出卡牌的指令
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendDiscardPlayedCardResponse(player: RacePlayer, cardIds: String) {
        CmdFactory.createDiscardPlayedCardResponse(player.position, cardIds)
                .send(this)
    }

    /**
     * 发送玩家弃牌的指令
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendDiscardResponse(player: RacePlayer, cardIds: String) {
        CmdFactory.createDiscardResponse(player.position, cardIds)
                .send(this)
    }

    /**
     * 发送玩家摸牌的指令
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendDrawCardResponse(player: RacePlayer, cardIds: String) {
        CmdFactory.createDrawCardResponse(player.position, cardIds)
                .send(this)
    }

    @Throws(BoardGameException::class)
    override fun sendGameInfo(receiver: RacePlayer?) {
        // 发送公共区的目标信息
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_SUPPLY_REFRESH_GOAL, -1)
                .public("goalIds", BgUtils.card2String(this.gameMode.goalManager.getGoals()))
                .send(this, receiver)
    }

    /**
     * 发送玩家得到VP的指令
     * @param player
     * @param vp
     * @param remainvp
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendGetVPResponse(player: RacePlayer, vp: Int, remainvp: Int) {
        CmdFactory.createGetVPResponse(player.position, vp, remainvp)
                .send(this)
    }

    @Throws(BoardGameException::class)
    override fun sendInitInfo(receiver: RacePlayer?) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_SETUP, -1).public("totalVP", this.gameMode.totalVp)
                .public("deckSize", this.gameMode.deckSize)
                .public("actionNum", this.gameMode.actionNum)
                .send(this, receiver)
    }

    /**
     * 发送玩家从手牌中打牌的指令
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendPlayCardResponse(player: RacePlayer, cardIds: String) {
        CmdFactory.createPlayCardResponse(player.position, cardIds)
                .send(this)
    }

    /**
     * 发送玩家得到目标的信息到客户端
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendPlayerGetGoalResponse(player: RacePlayer, goal: Goal) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_PLAYER_GET_GOAL, player.position)
                .public("goalId", goal.id)
                .send(this)
    }

    /**
     * 发送玩家失去目标的信息到客户端
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendPlayerLostGoalResponse(player: RacePlayer, goal: Goal) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_PLAYER_LOST_GOAL, player.position)
                .public("goalId", goal.id)
                .send(this)
    }

    @Throws(BoardGameException::class)
    override fun sendPlayerPlayingInfo(receiver: RacePlayer?) {
        // 将所有玩家现有的建筑发送到客户端
        for (p in this.players) {
            p.hands.takeIf { it.isNotEmpty() }
                    ?.let { CmdFactory.createDrawCardResponse(p.position, BgUtils.card2String(it)) }
                    ?.send(this, receiver)
            p.builtCards.takeIf { it.isNotEmpty() }
                    ?.let { CmdFactory.createDirectPlayCardResponse(p.position, BgUtils.card2String(it)) }
                    ?.send(this, receiver)
            p.builtCardsWithGood.takeIf { it.isNotEmpty() }
                    ?.let { CmdFactory.createProduceGoodResponse(p.position, BgUtils.card2String(it)) }
                    ?.send(this, receiver)
            CmdFactory.createGetVPResponse(p.position, p.vp, this.gameMode.totalVp)
                    .send(this, receiver)
            // 将玩家的目标发送到客户端
            if (p.goals.isNotEmpty()) {
                CmdFactory.createGameResponse(CmdConst.GAME_CODE_PLAYER_REFRESH_GOAL, p.position)
                        .public("goalIds", BgUtils.card2String(p.goals))
                        .send(this, receiver)
            }
            // 将玩家选择的行动发送到客户端
            if (p.actionTypes.isNotEmpty()) {
                CmdFactory.createGameResultResponse(CmdConst.GAME_CODE_SHOW_ACTION, p.position)
                        .public("actionTypes", StringUtils.list2String(p.actionTypes))
                        .send(this, receiver)
            }
        }
    }

    /**
     * 发送生产货物的指令
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendProduceGoodResponse(player: RacePlayer, cardIds: String) {
        CmdFactory.createProduceGoodResponse(player.position, cardIds)
                .send(this)
    }

    /**
     * 发送刷新牌堆数量的指令
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendRefreshDeckResponse() {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_REFRESH_DECK, -1)
                .public("deckSize", this.gameMode.deckSize)
                .send(this)
    }

    /**
     * 发送公共资源堆得到目标的信息到客户端
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendSupplyGetGoalResponse(goal: Goal) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_SUPPLY_GET_GOAL, -1)
                .public("goalId", goal.id)
                .send(this)
    }

    /**
     * 发送公共资源堆失去目标的信息到客户端
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendSupplyLostGoalResponse(goal: Goal) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_SUPPLY_LOST_GOAL, -1)
                .public("goalId", goal.id)
                .send(this)
    }

    /**
     * 发送玩家使用卡牌的指令
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendUseCardResponse(player: RacePlayer, cardIds: String) {
        CmdFactory.createUseCardResponse(player.position, cardIds)
                .send(this)
    }

    /**
     * 设置游戏, 该方法中设置gameMode
     * @throws BoardGameException
     */
    @Synchronized
    @Throws(BoardGameException::class)
    override fun setupGame() {
        log.info("设置游戏...")
        val num = this.currentPlayerNumber
        log.info("游戏人数: $num")
        this.gameMode = if (num == 2) RaceGame2P(this) else RaceGameMode(this)
    }

    /**
     * 玩家使用卡牌并把使用卡牌的信息发送到客户端
     * @param player
     * @param cardIds
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun useCard(player: RacePlayer, cardIds: String) {
        this.sendUseCardResponse(player, cardIds)
    }

}
