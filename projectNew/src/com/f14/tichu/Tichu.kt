package com.f14.tichu

import com.f14.F14bg.network.CmdFactory
import com.f14.bg.BoardGame
import com.f14.bg.consts.BgVersion
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.tichu.componet.TichuCard
import com.f14.tichu.componet.TichuCardGroup
import com.f14.tichu.consts.Combination
import com.f14.tichu.consts.TichuGameCmd
import com.f14.tichu.consts.TichuType
import net.sf.json.JSONObject

class Tichu : BoardGame<TichuPlayer, TichuConfig, TichuReport>() {
    override val mode
        get() = gameMode
    private lateinit var gameMode: TichuGameMode

    /**
     * 清除所有玩家已打出的牌
     */
    fun clearAllPlayerPlayedCard() {
        for (player in this.players) {
            player.lastGroup = null
            this.sendPlayerPlayCardInfo(player, null)
        }
    }


    @Throws(BoardGameException::class)
    override fun createConfig(obj: JSONObject): TichuConfig {
        val config = TichuConfig()
        config.versions.add(BgVersion.BASE)
        val mode = obj.getString("mode")
        config.mode = mode
        config.isTeamMatch = true
        config.randomSeat = "RANDOM" == mode
        return config
    }


    override fun initConfig() {
        val config = TichuConfig()
        config.versions.add(BgVersion.BASE)
        config.mode = "RANDOM"
        config.isTeamMatch = true
        this.config = config
    }

    override fun initConst() = Unit

    override fun initReport() {
        super.report = TichuReport(this)
    }

    /**
     * 玩家叫地主
     * @param player
     * @param tichuType
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerCallTichu(player: TichuPlayer, tichuType: TichuType) {
        if (!player.canCallTichu) {
            throw BoardGameException("你已经叫过地主了!")
        }
        if (tichuType == TichuType.SMALL_TICHU) {
            if (player.hands.size != 14) {
                throw BoardGameException("只有在没出过牌之前才能叫小地主!")
            }
        }
        player.tichuType = tichuType
        this.sendPlayerBaseInfo(player, null)
        this.report.playerCallTichu(player, tichuType)
        this.refreshPlayerButton(player)
        // 简单指令用来发音效...
        this.sendSimpleResponse("tichu", null)
    }

    /**
     * 玩家得到手牌
     * @param player
     * @param cards
     */
    fun playerGetCards(player: TichuPlayer, cards: Collection<TichuCard>) {
        player.hands.addCards(cards)
        player.hands.sort()
        this.sendPlayerHandsInfo(player, null)
    }

    /**
     * 玩家得到分数
     * @param player
     */
    fun playerGetRank(player: TichuPlayer, rank: Int) {
        player.rank = rank
        this.sendPlayerBaseInfo(player, null)
    }

    /**
     * 玩家得到分数
     * @param player
     * @param score
     */
    fun playerGetScore(player: TichuPlayer, score: Int) {
        if (score != 0) {
            player.score += score
            this.sendPlayerBaseInfo(player, null)
            this.report.playerGetScore(player, score)
        }
    }

    /**
     * 玩家跳过出牌
     * @param player
     */
    fun playerPass(player: TichuPlayer) {
        player.lastGroup = null
        this.sendPlayerPlayCardInfo(player, null)
        this.report.playerPass(player)
    }

    /**
     * 玩家出牌
     * @param player
     */
    fun playerPlayCards(player: TichuPlayer, group: TichuCardGroup) {
        player.hands.removeCards(group.cards)
        player.lastGroup = group
        this.sendPlayerPlayCardInfo(player, null)
        this.sendPlayerHandsInfo(player, null)
        this.refreshPlayerButton(player)
        this.report.playerPlayCards(player, group)
        // 如果是炸弹,则发声!
        if (group.combination == Combination.BOMBS) {
            this.sendSimpleResponse("bomb", null)
        }
    }

    /**
     * 玩家许愿
     * @param player
     * @param point
     */
    fun playerWishPoint(player: TichuPlayer, point: Int) {
        this.gameMode.wishedPoint = point
        this.sendGameBaseInfo(null)
        if (point > 0) {
            this.report.playerWishPoint(player, point.toDouble())
            // 简单指令用来发音效...
            this.sendSimpleResponse("wish", null)
        }
    }

    /**
     * 刷新玩家的按键情况
     * @param player
     */
    fun refreshPlayerButton(player: TichuPlayer?) {
        if (player == null) {
            for (p in this.players) {
                this.refreshPlayerButton(p)
            }
        } else {
            player.tichuButton = player.canCallTichu && player.hands.size == 14
            player.bombButton = player.hasBomb
            this.sendPlayerButtonInfo(player)
        }
    }

    /**
     * 发送所有玩家的手牌信息
     * @param receiver
     */
    fun sendAllPlayersHandsInfo(receiver: TichuPlayer?) {
        for (player in this.players) {
            this.sendPlayerHandsInfo(player, receiver)
        }
    }

    /**
     * 发送基本游戏信息
     * @param receiver
     */
    fun sendGameBaseInfo(receiver: TichuPlayer?) {
        val res = CmdFactory.createGameResponse(TichuGameCmd.GAME_CODE_BASE_INFO, -1)
        res.public("round", this.gameMode.round)
        res.public("player1", this.getPlayer(0).name)
        res.public("player2", this.getPlayer(1).name)
        res.public("player3", this.getPlayer(2).name)
        res.public("player4", this.getPlayer(3).name)
        res.public("score1", this.gameMode.getGroup(0).score)
        res.public("score2", this.gameMode.getGroup(1).score)
        res.public("wishedPoint", this.gameMode.wishedPoint)
        this.sendResponse(receiver, res)
    }

    @Throws(BoardGameException::class)
    override fun sendGameInfo(receiver: TichuPlayer?) {
        this.sendGameBaseInfo(receiver)
    }

    @Throws(BoardGameException::class)
    override fun sendInitInfo(receiver: TichuPlayer?) = Unit

    /**
     * 发送玩家的基本信息
     * @param player
     * @param receiver
     */
    fun sendPlayerBaseInfo(player: TichuPlayer, receiver: TichuPlayer?) {
        val res = CmdFactory.createGameResponse(TichuGameCmd.GAME_CODE_PLAYER_INFO, player.position)
        res.public("playerInfo", player.toMap())
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家的按键信息

     * @param player
     */
    fun sendPlayerButtonInfo(player: TichuPlayer?) {
        if (player == null) {
            // 向所有玩家发送各自的按键信息
            for (p in this.players) {
                this.sendPlayerButtonInfo(p)
            }
        } else {
            val res = CmdFactory.createGameResponse(TichuGameCmd.GAME_CODE_PLAYER_BUTTON, player.position)
            res.public("tichuButton", player.tichuButton)
            res.public("bombButton", player.bombButton)
            this.sendResponse(player, res)
        }
    }

    /**
     * 发送玩家的手牌信息
     * @param player
     * @param receiver
     */
    fun sendPlayerHandsInfo(player: TichuPlayer, receiver: TichuPlayer?) {
        val res = CmdFactory.createGameResponse(TichuGameCmd.GAME_CODE_PLAYER_HAND, player.position)
        res.private("cardIds", BgUtils.card2String(player.hands.cards))
        res.public("num", player.hands.size)
        this.sendResponse(receiver, res)
    }

    /**
     * 发送玩家最后出牌的信息
     * @param player
     * @param receiver
     */
    fun sendPlayerPlayCardInfo(player: TichuPlayer, receiver: TichuPlayer?) {
        val res = CmdFactory.createGameResponse(TichuGameCmd.GAME_CODE_PLAYER_PLAY_CARD, player.position)
        if (player.lastGroup != null) {
            res.public("cardIds", BgUtils.card2String(player.lastGroup!!.cards))
            res.public("combination", player.lastGroup!!.combination!!)
        }
        this.sendResponse(receiver, res)
    }

    @Throws(BoardGameException::class)
    public override fun sendPlayerPlayingInfo(receiver: TichuPlayer?) {
        for (player in this.players) {
            // 发送玩家的基本信息
            this.sendPlayerBaseInfo(player, receiver)
            // 发送玩家手牌信息
            this.sendPlayerHandsInfo(player, receiver)
            // 发送玩家最后出牌的信息
            this.sendPlayerPlayCardInfo(player, receiver)
        }
        // 发送玩家的按键信息
        this.sendPlayerButtonInfo(receiver)
    }

    @Throws(BoardGameException::class)
    override fun setupGame() {
        this.config.playerNumber = this.currentPlayerNumber
        this.gameMode = TichuGameMode(this)
    }

}
