package com.f14.RFTG.listener

import com.f14.RFTG.RacePlayer
import com.f14.RFTG.card.ExploreAbility
import com.f14.RFTG.card.RaceCard
import com.f14.RFTG.consts.RaceActionType
import com.f14.RFTG.consts.Skill
import com.f14.RFTG.mode.RaceGameMode
import com.f14.RFTG.network.CmdConst
import com.f14.RFTG.network.CmdFactory
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import java.util.*

/**
 * 探索阶段的监听器

 * @author F14eagle
 */
class ExploreActionListener(gameMode: RaceGameMode) : RaceActionListener(gameMode) {

    /**
     * 创建玩家的探索参数

     * @param player

     * @return
     */

    private fun createExploreParam(player: RacePlayer): ExploreParam {
        val p = ExploreParam()
        if (player.isActionSelected(RaceActionType.EXPLORE_1)) {
            p.drawNum += 1
            p.keepNum += 1
        }
        if (player.isActionSelected(RaceActionType.EXPLORE_2)) {
            p.drawNum += 5
        }
        // 将玩家该阶段的能力牌的效果应用到参数中
        val cards = player.builtCards.filter { it.exploreAbility != null }
        for (o in cards) {
            val a = o.exploreAbility!!
            if (a.drawNum != 0 || a.keepNum != 0) {
                p.drawNum += a.drawNum
                p.keepNum += a.keepNum
                p.effectedCards.add(o)
            }
            // 设置是否可以探索手牌的参数
            if (a.skill == Skill.EXPLORE_HAND) {
                p.exploreHand = true
            }
        }
        p.discardNum = p.drawNum - p.keepNum
        this.setParam(player.position, p)
        return p
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<RacePlayer>()
        val p = this.getParam<ExploreParam>(player.position)
        val cardIds = action.getAsString("cardIds")
        val cards = player.getCards(cardIds)
        if (cards.size != p.discardNum) {
            throw BoardGameException("弃牌数量不正确,你需要弃 " + p.discardNum + " 张牌!")
        }
        if (!p.exploreHand && !p.drawnCards!!.containsAll(cards)) {
            throw BoardGameException("不能从手牌中选择弃牌!")
        }
        // 将丢弃的牌放入弃牌堆
        gameMode.game.discardCard(player, cardIds)
        // 将玩家的回应状态设为已回应
        this.setPlayerResponsed(action.getPlayer<RacePlayer>().position)
    }


    override val ability: Class<ExploreAbility>
        get() = ExploreAbility::class.java

    override val validCode: Int
        get() = CmdConst.GAME_CODE_EXPLORE

    override fun onAllPlayerResponsed() {
        for (player in gameMode.game.players) {
            gameMode.report.printCache(player)
        }
    }

    @Throws(BoardGameException::class)
    override fun onReconnect(player: RacePlayer) {
        // 将摸牌和弃牌的数量发送到客户端
        val p = this.getParam<ExploreParam>(player.position)
        if (p != null) {
            val res = CmdFactory.createGameResponse(this.validCode, player.position)
            res.private("cardIds", BgUtils.card2String(p.drawnCards!!))
            res.private("exploreHand", p.exploreHand)
            gameMode.game.sendResponse(player, res)
        }
    }

    @Throws(BoardGameException::class)
    override fun onStartListen() {
        gameMode.report.system("探索阶段")
        // 设置所有玩家的摸牌和弃牌数量
        for (player in gameMode.game.players) {
            val p = this.createExploreParam(player)
            p.drawnCards = gameMode.draw(p.drawNum)
            // 如果有特殊能力生效的卡,则把这些卡返回到客户端
            if (p.effectedCards.isNotEmpty()) {
                gameMode.game.sendCardEffectResponse(player, BgUtils.card2String(p.effectedCards))
            }
            player.addCards(p.drawnCards!!)
            gameMode.report.playerAddCard(player, p.drawnCards!!)
            gameMode.report.printCache(player)
            gameMode.game.sendDrawCardResponse(player, BgUtils.card2String(p.drawnCards!!))
            // 将摸牌和弃牌的数量发送到客户端
            val res = CmdFactory.createGameResponse(this.validCode, player.position)
            res.private("cardIds", BgUtils.card2String(p.drawnCards!!))
            res.private("exploreHand", p.exploreHand)
            gameMode.game.sendResponse(player, res)
        }
        // 刷新牌堆数量
        gameMode.game.sendRefreshDeckResponse()
    }

    /**
     * 探索用的参数

     * @author F14eagle
     */
    internal inner class ExploreParam {
        var drawNum = 2
        var keepNum = 1
        var discardNum = 1
        var drawnCards: List<RaceCard>? = null
        var effectedCards: MutableList<RaceCard> = ArrayList()
        var exploreHand = false
    }

}
