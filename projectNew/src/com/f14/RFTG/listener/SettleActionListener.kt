package com.f14.RFTG.listener

import com.f14.RFTG.RacePlayer
import com.f14.RFTG.card.RaceCard
import com.f14.RFTG.card.SettleAbility
import com.f14.RFTG.consts.CardType
import com.f14.RFTG.consts.ProductionType
import com.f14.RFTG.consts.RaceActionType
import com.f14.RFTG.consts.Skill
import com.f14.RFTG.mode.RaceGameMode
import com.f14.RFTG.network.CmdConst
import com.f14.RFTG.network.CmdFactory
import com.f14.RFTG.utils.RaceUtils
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils

/**
 * 扩张阶段的监听器
 * @author F14eagle
 */
class SettleActionListener(gameMode: RaceGameMode) : RaceActionListener(gameMode) {

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
        val useNum = this.getCardUseNum(player, card)
        if (useNum <= 0) {
            throw BoardGameException("该卡牌的使用次数已经用完了!")
        }
        // 应用可以使用的能力
        var actived = false
        val ability = card.settleAbility ?: throw BoardGameException("不能使用该卡牌!")
        when (ability.skill) {
            Skill.DISCARD_FOR_FREE // 弃牌免费放置
            -> {
                this.setTempCostAbility(player.position, ability)
                val p = this.getParam<SettleParam>(player.position)
                if (p.cards.isNotEmpty()) {
                    // 如果已经有选择的星球,则直接降低费用
                    p.cost += ability.discardCost
                    if (p.cost < 0) {
                        p.cost = 0
                    }
                }
                actived = true
            }
            Skill.DISCARD_FOR_MILITARY // 弃牌加军事力
            -> {
                this.setTempMilitaryAbility(player.position, ability)
                actived = true
            }
            Skill.DISCARD_HAND_FOR_MILITARY // 弃手牌加军事力
            -> {
                val cardIds = action.getAsString("cardIds")
                val cards = player.getCards(cardIds)
                if (cards.isEmpty() || cards.size > ability.discardNum * ability.maxNum) {
                    throw BoardGameException("选择的卡牌数量错误!")
                }
                // 将调整值保存到缓存中
                setTempMilitary(player.position, cards.size * ability.discardMilitary)
                gameMode.game.discardCard(player, cardIds)
                actived = true
            }
            Skill.DISCARD_TO_CONQUER_NON_MILITARY // 弃牌后可以以军事力占领非军事星球
            -> {
                setTempConquerNonMilitaryAbility(player.position, ability)
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
     * 设置购买军事星球的能力
     * @param position
     * @param ability
     */
    private fun addMilitaryBuyAbility(position: Int, ability: SettleAbility) {
        val abilities = this.getMilitaryBuyAbilities(position)
        abilities.add(ability)
    }

    /**
     * 成功扩张后执行的动作
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun afterSettle(player: RacePlayer, cards: List<RaceCard>) {
        if (player.isActionSelected(RaceActionType.SETTLE) || player.isActionSelected(RaceActionType.SETTLE_2)) {
            // 选择扩张指令的玩家可以在扩张后摸一张牌
            gameMode.game.drawCard(player, 1)
        }

        // 扩张的牌如果是意外星球,则需要直接生产一个货物
        for (card in cards) {
            if (card.productionType == ProductionType.WINDFALL) {
                gameMode.game.produceGood(player, card.id)
            }

            // 检查卡牌的能力
//            val abilityCards = player.builtCards.filter { it.settleAbility != null }
            for (o in player.builtCards) {
                // 如果是本次开发的牌,则不能使用该能力
                if (cards.contains(o)) {
                    continue
                }
                val a = o.settleAbility ?: continue
                // 建造后摸牌的能力
                if (a.afterSettleDrawNum > 0) {
                    gameMode.game.drawCard(player, a.afterSettleDrawNum)
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
        val p = this.getParam<SettleParam>(player.position)
        if (p.cards.isNotEmpty()) {
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
     * 判断玩家是否可以以军事力占领指定的非军事星球
     * @param player
     * @param card
     * @return
     */
    private fun canConquerNonMilitaryWorld(player: RacePlayer, card: RaceCard): Boolean {
        val ability = this.getTempConquerNonMilitaryAbility(player.position)
        return ability != null && ability test card
    }

    /**
     * 创建玩家的开发参数
     * @param player
     * @param cards
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun createSettleParam(player: RacePlayer, cards: List<RaceCard>): SettleParam {
        val p = SettleParam()
        for (card in cards) {
            if (card.type == CardType.NON_MILITARY_WORLD) {
                // 检查玩家是否可以以军事力占领非军事星球
                if (this.canConquerNonMilitaryWorld(player, card)) {
                    val ability = this.getTempConquerNonMilitaryAbility(player.position)
                    val military = player.getMilitary(card) + this.getAdjustedMilitary(player, card)
                    if (military >= card.cost + (ability?.discardCost ?: 0)) {
                        // 可以以军事力占领该星球
                        p.cards.add(card)
                        continue
                    }
                }
                // 非军事星球才需要计算价格
                var cost = card.cost
                // 检查牌的能力
//                val abilityCards = player.builtCards.filter { it.settleAbility != null }
                for (o in player.builtCards) {
                    val a = o.settleAbility ?: continue
                    // 如果存在价格调整,并且建造的卡牌符合该能力的要求,则触发效果
                    if (a.cost != 0 && a.test(card)) {
                        cost += a.cost
                        p.effectedCards.add(o)
                    }
                }
                // 加上临时调整值
                cost += this.getTempCost(player.position, card)
                // 如果费用小于0,则设为0
                if (cost < 0) {
                    cost = 0
                }
                p.cost += cost
                p.cards.add(card)
            } else if (card.type == CardType.MILITARY_WORLD) {
                // 军事星球的计算逻辑
                val military = player.getMilitary(card) + this.getAdjustedMilitary(player, card)
                if (military < card.cost) {
                    // 当用军事力无法占领星球时,检查是否可以购买星球
                    if (this.isMilitaryBuyable(player.position, card)) {
                        // 如果可以购买军事星球,则计算价格
                        // 因为可能存在多个购买军事星球的能力,取最合适的那个能力
                        val ability = this.getFittestBuyMilitaryAbility(player.position, card)
                                ?: throw BoardGameException("你不能购买军事星球!")
                        val cloneCard = card.clone()
                        // 克隆相同属性的普通星球,价格是军事星球价格加上购买能力的调整值
                        cloneCard.type = CardType.NON_MILITARY_WORLD
                        cloneCard.cost += ability.buyCost
                        // 计算购买该军事星球的价格调整
                        var cost = cloneCard.cost
                        // 检查牌的能力
//                        val abilityCards = player.builtCards.filter { it.settleAbility != null }
                        for (o in player.builtCards) {
                            val a = o.settleAbility ?: continue
                            // 如果存在价格调整,并且建造的卡牌符合该能力的要求,则触发效果
                            if (a.cost != 0 && a.test(cloneCard)) {
                                cost += a.cost
                                p.effectedCards.add(o)
                            }
                        }
                        // 加上临时调整值
                        cost += this.getTempCost(player.position, cloneCard)
                        // 加上购买军事星球的费用调整值
                        cost += this.getMilitaryBuyCost(player.position)
                        // 如果费用小于0,则设为0
                        if (cost < 0) {
                            cost = 0
                        }
                        p.cost += cost
                        p.cards.add(card)
                    } else {
                        throw BoardGameException("你的军事力不足以占领该星球!")
                    }
                } else {
                    p.cards.add(card)
                }
            }
        }
        this.setParam(player.position, p)
        return p
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<RacePlayer>()
        // 取得扩张阶段指令中的子动作
        when (val subact = action.getAsString("subact")) {
            "choose" -> {
                var p = this.getParam<SettleParam>(player.position)
                if (p.cards.isNotEmpty()) {
                    throw BoardGameException("请选择弃牌进行星球的扩张!")
                }
                // 选择扩张的星球
                val cardIds = action.getAsString("cardIds")
                val cards = player.getCards(cardIds)
                if (cards.isEmpty() || cards.size > this.getSettleNum(player.position)) {
                    throw BoardGameException("扩张数量错误!")
                }
                if (RaceUtils.checkDuplicate(cards)) {
                    throw BoardGameException("不能扩张相同的星球!")
                }
                for (card in cards) {
                    if (card.type == CardType.MILITARY_WORLD || card.type == CardType.NON_MILITARY_WORLD) {
                        if (player.hasBuiltCard(card.cardNo)) {
                            throw BoardGameException("不能扩张相同的星球!")
                        }
                    } else {
                        throw BoardGameException("扩张阶段只能选择星球!")
                    }
                }
                // 扩张普通星球
                p = this.createSettleParam(player, cards)
                // 暂时先将这张牌打出
                player.playCards(cardIds)
                if (p.cost == 0) {
                    // 如果费用为0,则直接允许扩张
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
                val discardIds = action.getAsString("cardIds")
                val discards = player.getCards(discardIds)
                val p = this.getParam<SettleParam>(player.position)
                if (p.cards.isEmpty()) {
                    throw BoardGameException("请先选择要扩张的星球!")
                }
                if (discards.size != p.cost) {
                    throw BoardGameException("弃牌数量错误,你需要弃 " + p.cost + " 张牌!")
                }
                p.discardIds = discardIds
                // 设置已回应
                this.setPlayerResponsed(player)
            }
            "cancel" -> // 取消已选择扩张的星球
                cancelCard(player)
            "pass" -> {
                cancelCard(player)
                // 设置已回应
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


    override val ability: Class<SettleAbility>
        get() = SettleAbility::class.java

    /**
     * 取得玩家在本次扩张阶段的临时军事力修正值
     * @param player
     * @param card
     * @return
     */
    private fun getAdjustedMilitary(player: RacePlayer, card: RaceCard): Int =
            getTempMilitaryTotal(player.position, card)

    /**
     * 取得最适合指定星球的购买能力
     * @param position
     * @param card
     * @return
     */
    private fun getFittestBuyMilitaryAbility(position: Int, card: RaceCard)
            = this.getMilitaryBuyAbilities(position).filter { it test card }.maxBy(SettleAbility::buyCost)

    /**
     * 取得购买军事星球的能力
     * @param position
     * @return
     */

    private fun getMilitaryBuyAbilities(position: Int): MutableList<SettleAbility> = with(this.getPlayerParamSet(position)) {
        this.get<MutableList<SettleAbility>>("buyAbility")
                ?: ArrayList<SettleAbility>().also { this["buyAbility"] = it }
    }

    /**
     * 取得购买军事星球时的费用调整能力
     * @param position
     * @return
     */
    private fun getMilitaryBuyCost(position: Int): Int {
        val ability = this.getMilitaryBuyCostAbility(position)
        return ability?.buyCost ?: 0
    }

    /**
     * 取得购买军事星球时的费用调整能力
     * @param position
     * @return
     */

    private fun getMilitaryBuyCostAbility(position: Int): SettleAbility? {
        return this.getPlayerParamSet(position).get<SettleAbility>("buyAbilityCost")
    }

    /**
     * 取得允许的扩张数量,默认为1
     * @param position
     */
    private fun getSettleNum(position: Int): Int {
        return maxOf(this.getPlayerParamSet(position).getInteger("settleNum"), 1)
    }

    /**
     * 取得玩家在本回合中的临时军事力调整能力
     * @param position
     * @return
     */
    private fun getTempConquerNonMilitaryAbility(position: Int): SettleAbility? {
        return this.getPlayerParamSet(position).get<SettleAbility>("conquerNonMilitary")
    }

    /**
     * 取得玩家在本回合中扩张指定星球的临时扩张调整费用
     * @param position
     * @param card     扩张的星球
     * @return
     */
    private fun getTempCost(position: Int, card: RaceCard): Int {
        val ability = getTempCostAbility(position)
        return ability?.takeIf { it test card }?.discardCost ?: 0
    }

    /**
     * 取得玩家在本回合中临时的扩张费用调整能力
     * @param position
     * @return
     */
    private fun getTempCostAbility(position: Int): SettleAbility? {
        return this.getPlayerParamSet(position).get<SettleAbility>("cost")
    }

    /**
     * 取得玩家在本回合中临时的军事力调整值
     * @param position
     * @return
     */
    private fun getTempMilitary(position: Int): Int {
        return getPlayerParamSet(position).getInteger("militaryValue")
    }

    /**
     * 取得玩家在本回合中的临时军事力调整能力
     * @param position
     * @return
     */
    private fun getTempMilitaryAbility(position: Int): SettleAbility? {
        return this.getPlayerParamSet(position).get<SettleAbility>("military")
    }

    /**
     * 取得玩家在本回合中扩张指定星球的全部临时军事力调整值
     * @param position
     * @param card     扩张的星球
     * @return
     */
    private fun getTempMilitaryTotal(position: Int, card: RaceCard)
            = getTempMilitaryAbility(position)?.takeIf { it test card }?.discardMilitary ?: 0

    override val validCode: Int
        get() = CmdConst.GAME_CODE_SETTLE

    /**
     * 判断该玩家是否可以购买军事星球
     * @param position
     * @param card
     * @return
     */
    private fun isMilitaryBuyable(position: Int, card: RaceCard): Boolean {
        val abilities = this.getMilitaryBuyAbilities(position)
        return abilities.any { it.test(card) }
    }

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        for (player in gameMode.game.players) {
            val p = this.getParam<SettleParam>(player.position)
            if (p.cards.isEmpty()) {
                // 跳过扩张
                CmdFactory.createGameResultResponse(validCode, player.position).public("subact", "pass").send(gameMode)
            } else {
                player.addBuiltCards(p.cards)
                gameMode.report.playerPlayCards(player, p.cards)
                gameMode.game.sendPlayCardResponse(player, BgUtils.card2String(p.cards))
                gameMode.game.discardCard(player, p.discardIds ?: "")
                gameMode.game.sendCardEffectResponse(player, BgUtils.card2String(p.effectedCards))
                this.afterSettle(player, p.cards)
            }
            gameMode.report.printCache(player)
        }
    }

    @Throws(BoardGameException::class)
    override fun onReconnect(player: RacePlayer) {
        // 发送可以主动使用的卡牌
        val res = this.createActivedCardResponse(player)
        if (res != null) {
            gameMode.game.sendResponse(player, res)
        }
        val p = this.getParam<SettleParam>(player.position)
        if (p.cards.isNotEmpty()) {
            // 如果有选择开发的设施,则将其返回到客户端
            gameMode.game.sendDrawCardResponse(player, BgUtils.card2String(p.cards))
        }
        // 取消选择开发的设施
        this.cancelCard(player)
    }

    @Throws(BoardGameException::class)
    override fun onStartListen() {
        gameMode.report.system("扩张阶段")
        for (player in gameMode.game.players) {
            // 判断玩家是否有购买军事星球的能力
            // 判断玩家是否有扩展阶段的特殊能力
            for (ability in player.builtCards.mapNotNull(RaceCard::settleAbility)) {
                when (ability.skill) {
                    Skill.BUY_MILITARY_WORLD // 购买军事星球的能力
                    -> this.addMilitaryBuyAbility(player.position, ability)
                    Skill.DOUBLE_SETTLE // 双重扩张
                    -> this.setSettleNum(player.position, 2)
                    Skill.BUY_MILITARY_COST // 购买军事星球时的费用调整
                    -> this.setMilitaryBuyCostAbility(player.position, ability)
                    else -> Unit
                }
            }
        }
        // 检查并发送可主动使用的卡牌
        this.checkActiveCards()
    }

    /**
     * 设置购买军事星球时的费用调整能力
     * @param position
     * @param ability
     */
    private fun setMilitaryBuyCostAbility(position: Int, ability: SettleAbility) {
        this.getPlayerParamSet(position)["buyAbilityCost"] = ability
    }

    /**
     * 设置允许的扩张数量
     * @param position
     * @param num
     */
    private fun setSettleNum(position: Int, num: Int) {
        this.getPlayerParamSet(position)["settleNum"] = num
    }

    /**
     * 设置玩家在本回合中临时的占领非军事星球的能力
     * @param position
     * @param ability
     */
    private fun setTempConquerNonMilitaryAbility(position: Int, ability: SettleAbility) {
        this.getPlayerParamSet(position)["conquerNonMilitary"] = ability
    }

    /**
     * 设置玩家在本回合中临时的扩张费用调整能力
     * @param position
     * @param ability
     */
    private fun setTempCostAbility(position: Int, ability: SettleAbility) {
        this.getPlayerParamSet(position)["cost"] = ability
    }

    /**
     * 设置玩家在本回合中临时的军事力调整值
     * @param position
     * @param military
     */
    private fun setTempMilitary(position: Int, military: Int) {
        val res = getTempMilitary(position) + military
        this.getPlayerParamSet(position)["militaryValue"] = res
    }

    /**
     * 设置玩家在本回合中临时的军事力调整能力
     * @param position
     * @param ability
     */
    private fun setTempMilitaryAbility(position: Int, ability: SettleAbility) {
        this.getPlayerParamSet(position)["military"] = ability
    }

    internal class SettleParam {
        var cost = 0
        var discardIds: String? = null
        val cards: MutableList<RaceCard> = ArrayList()
        val effectedCards: MutableList<RaceCard> = ArrayList()
    }

}
