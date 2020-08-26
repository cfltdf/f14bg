package com.f14.RFTG.listener

import com.f14.RFTG.RacePlayer
import com.f14.RFTG.card.Ability
import com.f14.RFTG.card.RaceCard
import com.f14.RFTG.card.RaceDeck
import com.f14.RFTG.consts.ProductionType
import com.f14.RFTG.consts.StartWorldType
import com.f14.RFTG.manager.RaceResourceManager
import com.f14.RFTG.mode.RaceGameMode
import com.f14.RFTG.network.CmdConst
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils

class StartingWorldListener(gameMode: RaceGameMode) : RaceActionListener(gameMode) {

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        // 为所有玩家抽取2个待选起始星球
        val rm = gameMode.game.getResourceManager<RaceResourceManager>()
        val startWorlds = rm.getStartCards(gameMode.game.config)
        val normalDeck = RaceDeck()
        val militaryDeck = RaceDeck()
        // 按照起始星球类型分2个牌堆
        val group = startWorlds.groupBy(RaceCard::startWorldType)
        group[StartWorldType.NORMAL]?.let(normalDeck::addCards)
        group[StartWorldType.MILITARY]?.let(militaryDeck::addCards)
        normalDeck.shuffle()
        militaryDeck.shuffle()
        // 为所有玩家从2个牌堆中各抽取1张牌待选
        for (player in gameMode.game.players) {
            // 为玩家创建星球参数
            val p = this.createWorldParam(player)
            p.startWorlds.addCards(listOfNotNull(normalDeck.draw(), militaryDeck.draw()))
        }
        // 将剩下的起始星球和其他牌洗混后,发给玩家起始手牌
        // 将其他所有的牌和选剩下的起始星球牌作为默认牌堆
        val defaultCards = rm.getOtherCards(gameMode.game.config) + normalDeck.cards + militaryDeck.cards
        gameMode.raceDeck.defaultCards = defaultCards
        gameMode.raceDeck.reset()
        // 给所有玩家发起始手牌
        for (player in gameMode.game.players) {
            val p = this.getParam<WorldParam>(player.position)
            val cards = gameMode.draw(gameMode.startNumber)
            p.hands.cards.addAll(cards)
        }
    }

    override fun createStartListenCommand(player: RacePlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 将用户选择星球的参数发送到客户端
        val p = this.getParam<WorldParam>(player.position)
        res.private("startWorldIds", BgUtils.card2String(p.startWorlds.cards))
        res.private("handIds", BgUtils.card2String(p.hands.cards))
        return res
    }

    /**
     * 为玩家创建选择星球的参数
     * @param player
     * @return
     */
    private fun createWorldParam(player: RacePlayer): WorldParam {
        val p = WorldParam()
        this.setParam(player.position, p)
        return p
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<RacePlayer>()
        val startWorldIds = action.getAsString("startWorldIds")
        val handIds = action.getAsString("handIds")
        val p = this.getParam<WorldParam>(player.position)
        val discardWorlds = p.startWorlds.getCards(startWorldIds)
        if (discardWorlds.size != 1) {
            throw BoardGameException("你必须选择1个起始星球!")
        }
        val startWorld = this.getStartWorld(p.startWorlds.cards, discardWorlds)
        val discardHands = p.hands.getCards(handIds)
        val num = p.hands.size - startWorld.startHandNum
        if (discardHands.size != num) {
            throw BoardGameException("弃牌数量错误,你需要弃 $num 张牌!")
        }
        p.startWorld = startWorld
        gameMode.discard(discardWorlds)
        gameMode.report.playerPlayCards(player, BgUtils.toList(p.startWorld))
        // 玩家得到起始手牌
        p.hands.cards.removeAll(discardHands)
        gameMode.game.getCard(player, p.hands.cards)
        gameMode.report.playerDiscard(player, discardWorlds)
        gameMode.report.playerDiscard(player, discardHands)
        gameMode.discard(discardHands)
        this.setPlayerResponsed(player)
    }


    override val ability: Class<out Ability>?
        get() = null

    /**
     * 取得选择的起始星球
     * @param startWorlds
     * @param discardWorlds
     * @return
     */
    private fun getStartWorld(startWorlds: List<RaceCard>, discardWorlds: List<RaceCard>) = startWorlds.first { it !in discardWorlds }

    override val validCode: Int
        get() = CmdConst.GAME_CODE_STARTING_WORLD

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        // 展示所有玩家的起始星球
        for (player in gameMode.game.players) {
            val p = this.getParam<WorldParam>(player.position)
            player.addBuiltCard(p.startWorld!!)
            gameMode.game.sendDirectPlayCardResponse(player, p.startWorld!!.id)
            // 如果该星球是意外星球,则直接生产一个货物
            if (p.startWorld!!.productionType == ProductionType.WINDFALL) {
                gameMode.game.produceGood(player, p.startWorld!!.id)
            }
            gameMode.report.printCache(player)
        }
    }

    internal inner class WorldParam {
        var startWorlds = RaceDeck()
        var hands = RaceDeck()

        var startWorld: RaceCard? = null
    }

}
