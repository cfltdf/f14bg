package com.f14.RFTG.listener

import com.f14.RFTG.RacePlayer
import com.f14.RFTG.card.DevelopAbility
import com.f14.RFTG.card.RaceCard
import com.f14.RFTG.consts.CardType
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
 * 开发阶段的监听器

 * @author F14eagle
 */
class DevelopActionListener(gameMode: RaceGameMode) : RaceActionListener(gameMode) {

    /**
     * 使用卡牌的能力
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun activeCard(action: GameAction) {
        val player = action.getPlayer<RacePlayer>()
        val cardId = action.getAsString("cardId")
        val card = player.getBuiltCard(cardId)
        val ability = card.developAbility ?: throw BoardGameException("不能使用该卡牌!")
        val useNum = this.getCardUseNum(player, card)
        if (useNum <= 0) throw BoardGameException("该卡牌的使用次数已经用完了!")
        // 应用可以使用的能力
        var actived = false
        when (ability.skill) {
            Skill.DISCARD_FOR_DEVELOP_COST -> { // 弃牌调整开发费用
                this.setTempDevelopAbility(player.position, ability)
                actived = true
            }
            else -> Unit
        }

        // 如果使用了该技能,则发送信息到客户端
        if (actived) {
            // 将卡牌的使用次数设为0
            this.setCardUseNum(player, card, 0)
            gameMode.game.useCard(player, card.id)
            // 需要弃牌的话就弃掉该牌
            if (ability.isDiscardAfterActived) {
                gameMode.game.discardPlayedCard(player, card.id)
            }
        }
    }

    /**
     * 成功开发后执行的动作
     * @param player
     * @param cards
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun afterDevelop(player: RacePlayer, cards: List<RaceCard>) {
        for (card in cards) {
            // 如果是本次开发的牌,则不能使用该能力
            for (o in player.builtCards.filterNot(cards::contains)) {
                val a = o.developAbility ?: continue
                if (a.afterDevelopDrawNum > 0) {
                    // 成功开发后摸牌
                    gameMode.game.drawCard(player, a.afterDevelopDrawNum)
                    gameMode.game.sendCardEffectResponse(player, o.id)
                }
            }
        }
    }

    /**
     * 取消选择的卡牌
     * @param player
     */
    private fun cancelCard(player: RacePlayer) {
        val p = this.getParam<DevelopParam>(player.position)
        if (p != null && p.cards.isNotEmpty()) {
            player.addCards(p.cards)
            // 将取消开发的信息发送给自己
            CmdFactory.createGameResultResponse(validCode, player.position)
                    .private("subact", "cancel")
                    .private("cardIds", BgUtils.card2String(p.cards))
                    .send(gameMode)
            p.cards.clear()
            p.cost = 0
        }
    }

    /**
     * 创建玩家的开发参数
     * @param player
     * @param cards
     * @return
     */
    private fun createDevelopParam(player: RacePlayer, cards: List<RaceCard>): DevelopParam {
        val p = DevelopParam()

        for (card in cards) {
            var cost = card.cost
            if (player.isActionSelected(RaceActionType.DEVELOP, RaceActionType.DEVELOP_2)) cost -= 1

            // 检查牌的能力
//            val builtCards = player.builtCards.filter { it.developAbility != null }
            for (o in player.builtCards) {
                val a = o.developAbility ?: continue
                // 如果存在价格调整,并且建造的卡牌符合该能力的要求,则触发效果
                if (a.cost != 0 && a.test(card)) {
                    cost += a.cost
                    p.effectedCards.add(o)
                }
            }

            // 检查临时的开发费用调整值
            cost += this.getTempDevelopCostTotal(player.position, card)

            // 如果费用小于0,则设为0
            if (cost < 0) cost = 0
            p.cost += cost
            p.cards.add(card)
        }
        this.setParam(player.position, p)
        return p
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<RacePlayer>()
        // 取得开发阶段指令中的子动作
        when (val subact = action.getAsString("subact")) {
            "choose" -> {
                var p: DevelopParam? = this.getParam(player.position)
                if (p != null && p.cards.isNotEmpty()) throw BoardGameException("请选择弃牌进行设施的开发!")
                // 选择开发的设施
                val cardIds = action.getAsString("cardIds")
                val cards = player.getCards(cardIds)
                // 暂时只接受1张牌的开发
                if (cards.size != 1) throw BoardGameException("只能开发1个设施!")
                for (card in cards) {
                    if (card.type != CardType.DEVELOPMENT) throw BoardGameException("开发阶段只能选择开发设施!")
                    if (player.hasBuiltCard(card.cardNo)) throw BoardGameException("不能开发相同的设施!")
                }
                p = this.createDevelopParam(player, cards)
                // 暂时先将这些牌打出
                player.playCards(cardIds)
                if (p.cost == 0) {
                    // 如果费用为0,则直接允许开发
                    // 设置已回应
                    this.setPlayerResponsed(player)
                } else {
                    // 只将费用信息发送给自己
                    CmdFactory.createGameResultResponse(validCode, player.position)
                            .private("subact", subact)
                            .private("cardIds", cardIds)
                            .private("cost", p.cost)
                            .send(gameMode, player)
                }
            }
            "discard" -> {
                // 选择丢弃的手牌
                val cardIds = action.getAsString("cardIds")
                val discards = player.getCards(cardIds)
                val p = this.getParam<DevelopParam>(player.position)
                if (p == null || p.cards.isEmpty()) throw BoardGameException("请先选择要开发的设施!")
                if (discards.size != p.cost) throw BoardGameException("弃牌数量错误,你需要弃 " + p.cost + " 张牌!")
                p.discardIds = cardIds
                // 设置已回应
                this.setPlayerResponsed(player)
            }
            "cancel" -> // 取消已选择开发的设施
                cancelCard(player)
            "pass" -> {
                // 跳过行动时先取消选择的卡牌
                cancelCard(player)
                this.setPlayerResponsed(player)
            }
            "active" -> {
                // 取消已选择扩张的星球
                cancelCard(player)
                // 使用卡牌的能力
                this.activeCard(action)
            }
            else -> throw BoardGameException("无效的指令!")
        }
    }


    override val ability: Class<DevelopAbility>
        get() = DevelopAbility::class.java

    /**
     * 取得玩家在本回合中的临时开发调整能力
     * @param position
     * @return
     */
    private fun getTempDevelopAbility(position: Int): DevelopAbility? {
        return this.getPlayerParamSet(position).get<DevelopAbility>("development")
    }

    /**
     * 取得玩家在本回合中开发指定设施的全部临时费用调整值
     * @param position
     * @param card
     * @return
     */
    private fun getTempDevelopCostTotal(position: Int, card: RaceCard): Int {
        val ability = getTempDevelopAbility(position)
        return if (ability != null && ability.test(card)) ability.discardCost else 0
    }

    override val validCode: Int
        get() = CmdConst.GAME_CODE_DEVELOP

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        // 将所有玩家的选择情况发送到客户端
        for (player in gameMode.game.players) {
            val p = this.getParam<DevelopParam>(player.position)
            if (p == null || p.cards.isEmpty()) {
                // 跳过开发
                CmdFactory.createGameResultResponse(validCode, player.position)
                        .public("subact", "pass")
                        .send(gameMode)
                gameMode.report.playerPass(player)
            } else {
                // 执行开发
                player.addBuiltCards(p.cards)
                gameMode.report.playerPlayCards(player, p.cards)
                // 将弃牌信息发送给客户端
                gameMode.game.discardCard(player, p.discardIds ?: "")
                // 将直接出牌信息发送给客户端
                gameMode.game.sendPlayCardResponse(player, BgUtils.card2String(p.cards))
                gameMode.game.sendCardEffectResponse(player, BgUtils.card2String(p.effectedCards))
                this.afterDevelop(player, p.cards)
            }
            gameMode.report.printCache(player)
        }
    }

    @Throws(BoardGameException::class)
    override fun onReconnect(player: RacePlayer) {
        val p = this.getParam<DevelopParam>(player.position)
        if (p != null && p.cards.isNotEmpty()) {
            // 如果有选择开发的设施,则将其返回到客户端
            gameMode.game.sendDrawCardResponse(player, BgUtils.card2String(p.cards))
        }
        // 取消选择开发的设施
        this.cancelCard(player)
    }

    @Throws(BoardGameException::class)
    override fun onStartListen() {
        gameMode.report.system("开发阶段")
        // 在开始开发阶段时,处理某些卡牌的特殊能力
        for (player in gameMode.game.players) {
            val cards = player.builtCards.filter { it.developAbility != null }
            for (o in cards) {
                val a = o.developAbility!!
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
     * 设置玩家在本回合中临时的开发调整能力
     * @param position
     * @param ability
     */
    private fun setTempDevelopAbility(position: Int, ability: DevelopAbility) {
        this.getPlayerParamSet(position)["development"] = ability
    }

    private inner class DevelopParam {
        var cost = 0
        var cards: MutableList<RaceCard> = ArrayList()
        var discardIds: String? = null
        var effectedCards: MutableList<RaceCard> = ArrayList()
    }

}
