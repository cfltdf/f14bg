package com.f14.TS

import com.f14.F14bg.network.CmdFactory
import com.f14.TS.action.TSGameAction
import com.f14.TS.component.*
import com.f14.TS.component.ability.TSAbility
import com.f14.TS.consts.*
import com.f14.TS.consts.ability.ActionParamType
import com.f14.TS.consts.ability.TSAbilityGroupType
import com.f14.TS.consts.ability.TSAbilityType
import com.f14.TS.executor.*
import com.f14.TS.factory.ActionFactory
import com.f14.TS.factory.GameActionFactory
import com.f14.TS.factory.InitParamFactory
import com.f14.TS.listener.TSHeadLineListener.HeadLineParam
import com.f14.TS.listener.TSResignConfirmListener
import com.f14.TS.listener.TSViewHandListener
import com.f14.TS.utils.TSRoll
import com.f14.bg.FixedOrderBoardGame
import com.f14.bg.consts.BgVersion
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import net.sf.json.JSONObject
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class TS : FixedOrderBoardGame<TSPlayer, TSConfig, TSReport>() {
    override val mode
        get() = gameMode
    lateinit var gameMode: TSGameMode

    var spplayers: MutableMap<SuperPower, TSPlayer> = HashMap()

    /**
     * 执行卡牌效果
     * @param player
     * @param card
     * @return 返回是否直接处理完成
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun activeCardEvent(player: TSPlayer, card: TSCard): ActiveResult {
        // 检查该卡牌的事件是否可以生效
        var canActive = gameMode.eventManager.canActiveCard(card)
        if (canActive) {
            // 如果是计分牌则直接进行计分
            if (card.cardType == CardType.SCORING) this.executeScore(card)
            else {
                val abilityGroup = card.abilityGroup
                if (abilityGroup != null) when (abilityGroup.groupType) {
                    TSAbilityGroupType.NORMAL // 直接生效方式
                    -> this.processAbilities(abilityGroup.abilities, player, card)
                    TSAbilityGroupType.CHOICE -> { // 选择触发的能力
                        // 创建选择触发器
                        val initParam = InitParamFactory.createChoiceInitParam(gameMode, player, card, TrigType.EVENT)
                        val executor = TSChoiceExecutor(player, gameMode, initParam)
                        executor.execute()
                    }
                    TSAbilityGroupType.AUTO_DECISION -> { // 自动判断执行哪个行动
                        if (abilityGroup.test(gameMode, player)) { // 符合条件的执行group1
                            this.processAbilities(abilityGroup.abilitiesGroup1, player, card)
                        } else { // 不符合的执行group2
                            this.processAbilities(abilityGroup.abilitiesGroup2, player, card)
                        }
                    }
                    TSAbilityGroupType.ACTIVE_CONDITION -> { // 判断该卡牌是否可以生效
                        // 创建选择触发器
                        val initParam = InitParamFactory.createConditionInitParam(gameMode, player, card, abilityGroup.activeParam!!)
                        val condition = ActionFactory.createActionCondition(player, gameMode, initParam)
                        if (condition.test()) {
                            // 如果符合条件,则生效
                            this.processAbilities(abilityGroup.abilities, player, card)
                        } else {
                            // 否则设置结果为未生效
                            canActive = false
                        }
                    }
                    TSAbilityGroupType.CONDITION_DECISION -> { // 按照条件判断执行哪个行动
                        // 创建选择触发器
                        val initParam = InitParamFactory.createConditionInitParam(gameMode, player, card, abilityGroup.activeParam!!)
                        val condition = ActionFactory.createActionCondition(player, gameMode, initParam)
                        if (condition.test()) {
                            // 符合条件的执行group1
                            this.processAbilities(abilityGroup.abilitiesGroup1, player, card)
                        } else {
                            // 不符合的执行group2
                            this.processAbilities(abilityGroup.abilitiesGroup2, player, card)
                        }
                    }
                }
            }
        }
        val res = ActiveResult(player, canActive)
        // 处理生效后的卡牌
        this.processActivedCard(card, res)
        return res
    }

    /**
     * 设置第零回合事件结果
     * @param card
     * @param ussrModify
     * @param usaModify
     * @param cancelEffect
     */
    fun addZeroCard(card: TSZeroCard, ussrModify: Int, usaModify: Int, cancelEffect: Boolean) {
        val roll = TSRoll.roll()
        val result = minOf(maxOf(1, if (cancelEffect) roll else roll - ussrModify + usaModify), 6)
        card.doResult(result)
        gameMode.zeroCards.add(card)
        this.report.zeroRoll(card, roll, ussrModify, usaModify, result, cancelEffect)
        this.sendAddZeroCardsResponse(BgUtils.toList(card), null)
    }

    /**
     * 调整DEFCON等级
     * @param num
     */
    fun adjustDefcon(num: Int) {
        this.report.adjustDefcon(num)
        this.setDefcon(this.gameMode.defcon + num)
    }

    /**
     * 调整国家的影响力
     * @param country
     * @param power
     * @param num
     */
    fun adjustInfluence(country: TSCountry, power: SuperPower, num: Int) {
        val ap = AdjustParam(power, ActionType.ADJUST_INFLUENCE, country)
        ap.num = num
        ap.tempCountry.addInfluence(power, num)
        // 输出战报
        this.report.doAction(ap)
        ap.apply()
        this.sendCountryInfo(country, null)
    }

    /**
     * 调整VP,苏联为正分,美国为负分
     * @param num
     */
    fun adjustVp(num: Int) {
        this.report.adjustVp(num)
        this.setVp(this.gameMode.vp + num)
    }

    /**
     * 为指定玩家调整VP
     * @param player
     * @param num
     */
    fun adjustVp(player: TSPlayer, num: Int) {
        val value = this.convertVp(player, num)
        this.report.adjustVp(value)
        this.setVp(this.gameMode.vp + value)
    }

    /**
     * 改变中国牌的所属玩家和使用状态
     * @param player
     * @param canUse
     */
    fun changeChinaCardOwner(player: TSPlayer?, canUse: Boolean) {
        this.gameMode.cardManager.changeChinaCardOwner(player, canUse)
        this.report.playerGetChinaCard(player, canUse)
        this.sendChinaCardInfo(null)
    }

    /**
     * 检测在行动之前需要处理的效果
     * @param player
     * @param trigType
     * @param card
     */
    fun checkForceEffect(player: TSPlayer, trigType: TrigType?, card: TSCard?) {
        if (player.hasEffect(EffectType.EFFECT_50)) {
            // 检查玩家是否有#50-“我们会埋葬你的”的效果
            // 如果美国不以事件方式打出联合国干涉,则苏联得到3VP
            if (card == null || card.tsCardNo != 32 || trigType != TrigType.EVENT) {
                this.adjustVp(3)
            }
            // 无论如何,移除该效果
            val c = gameMode.cardManager.getCardByCardNo(50)!!
            this.playerRemoveActivedCard(player, c)
        }
    }

    /**
     * 取得实际的SuperPower值
     * @param from
     * @param player 参照玩家对象
     * @return
     */
    fun convertSuperPower(from: SuperPower, player: TSPlayer): SuperPower {
        return when (from) {
            SuperPower.PLAYED_CARD_PLAYER -> player.superPower
            SuperPower.OPPOSITE_PLAYER -> player.superPower.oppositeSuperPower
            SuperPower.CURRENT_PLAYER -> gameMode.turnPlayer!!.superPower
            else -> from
        }
    }

    /**
     * 将玩家得到的VP转换成正负数,苏联为正分,美国为负分
     * @param player
     * @param vp
     * @return
     */
    fun convertVp(player: TSPlayer, vp: Int): Int {
        return when (player.superPower) {
            SuperPower.USSR -> vp
            SuperPower.USA -> -vp
            else -> vp
        }
    }


    @Throws(BoardGameException::class)
    override fun createConfig(obj: JSONObject): TSConfig {
        val config = TSConfig()
        obj.getString("versions")?.split(",".toRegex())?.dropLastWhile(String::isEmpty)?.let(config.versions::addAll)
        val ussrPlayer = obj.getInt("ussrPlayer")
        config.ussrPlayer = ussrPlayer
        // 如果没有指定苏联玩家,则为随机座位
        config.randomSeat = ussrPlayer < 0
        // 设置让点数量
        config.point = obj.getInt("point")
        try {
            config.isNewSpaceRace = obj.getBoolean("newSpaceRace")
        } catch (ex: Exception) {
            config.isNewSpaceRace = false
        }

        return config
    }

    /**
     * 分发手牌
     * @param handLimit
     */
    fun dealCards(handLimit: Int) {
        val cardMap = this.players.map { it to ArrayList<TSCard>() }.toMap()
        val drawMap = (0 until handLimit).flatMap { i -> this.players.filter { it.hands.size + i < handLimit } }
        val sendDetail = drawMap.fold(false) { sendDetail, player ->
            val newDeck = this.gameMode.cardManager.playingDeck.size == 0
            val newCard = this.gameMode.cardManager.playingDeck.draw(1)
            cardMap[player]?.addAll(newCard)
            sendDetail or newDeck
        }
        for (p in this.players) {
            val cards = cardMap[p]
            this.sendPlayerAddHandsResponse(p, cards!!, null)
            this.report.playerDrawCards(p, cards)
            p.addCards(cards)
        }
        this.sendDeckInfo(sendDetail, null)
    }

    /**
     * 把牌丢到弃牌堆中
     * @param card
     */
    fun discardCard(card: TSCard) {
        // 如果弃牌堆中已经存在该牌,则不用再添加了
        if (!this.gameMode.cardManager.playingDeck.discards.contains(card)) {
            this.gameMode.cardManager.playingDeck.discard(card)
            this.sendAddDiscardResponse(card, null)
        }
    }

    /**
     * 把牌丢到弃牌堆中
     * @param cards
     */
    fun discardCards(cards: Collection<TSCard>) {
        this.gameMode.cardManager.playingDeck.discard(cards)
        this.sendAddDiscardsResponse(cards, null)
    }

    @Throws(BoardGameException::class)
    override fun doCheat(msg: String): Boolean {
        if (super.doCheat(msg)) {
            return true
        }
        val s = msg.split(" ".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
        when (s[0]) {
            "get_card" -> {
                val pos = Integer.parseInt(s[1])
                val p = if (pos == 0) this.ussrPlayer else this.usaPlayer
                (2 until s.size).map { gameMode.cardManager.getCardByCardNo(Integer.parseInt(s[it]))!! }.forEach { this.playerGetCard(p, it) }
                return true
            }
            "score" -> {
                try {
                    val pos = Integer.parseInt(s[1])
                    val p = if (pos == 0) this.ussrPlayer else this.usaPlayer
                    (2 until s.size).filter { it % 2 == 0 }.forEach { i ->
                        val type = s[i].toLowerCase()
                        val num = s[i + 1].toInt()
                        when (type) {
                            "vp" -> this.adjustVp(p, num)
                            "ma" -> this.playerAdjustMilitaryAction(p, num)
                            "sp" -> this.playerAdvanceSpaceRace(p, num)
                            "def" -> this.adjustDefcon(num)
                            else -> {
                            }
                        }
                    }
                } catch (e: Exception) {
                    throw BoardGameException(e.message)
                }

                return true
            }
            "influence" -> {
                try {
                    val pos = Integer.parseInt(s[1])
                    val p = if (pos == 0) this.ussrPlayer else this.usaPlayer
                    (2 until s.size).filter { it % 2 == 0 }.forEach { i ->
                        val country = s[i].toLowerCase()
                        val num = s[i + 1].toInt()
                        val cty = gameMode.countryManager.getCountry(country)
                        this.adjustInfluence(cty, p.superPower, num)
                    }
                } catch (e: Exception) {
                    throw BoardGameException(e.message)
                }
                return true
            }
            "nextroll" -> {
                try {
                    val sp = when (Integer.parseInt(s[1])) {
                        0 -> SuperPower.USSR
                        1 -> SuperPower.USA
                        else -> SuperPower.NONE
                    }
                    (2 until s.size).map { s[it].toInt() }.filterNot { it < 0 || it > 6 }.forEach { TSRoll.cheat(sp, it) }
                } catch (e: Exception) {
                    throw BoardGameException(e.message)
                }
                return true
            }
        }
        return false
    }

    /**
     * 执行行动参数
     * @param action
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun executeAction(action: TSGameAction?): ActionResult {
        val res = ActionResult()
        if (action?.paramType != null) {
            val target = this.getPlayer(action.targetPower)
            when (action.paramType) {
                ActionParamType.ADJUST_DEFCON // 调整DEFCON
                -> this.adjustDefcon(action.num)
                ActionParamType.SET_DEFCON // 设置DEFCON
                -> this.setDefcon(action.num)
                ActionParamType.ADJUST_VP -> { // 调整VP
                    val factor = when (action.targetPower) {
                        SuperPower.USSR -> 1
                        SuperPower.USA -> -1
                        else -> 0
                    }
                    val vp = factor * action.num
                    this.adjustVp(vp)
                }
                ActionParamType.ADJUST_INFLUENCE // 调整影响力
                -> this.adjustInfluence(action.country!!, action.targetPower, action.num)
                ActionParamType.SET_INFLUENCE -> { // 设置影响力
                    this.setInfluence(action.country!!, action.targetPower, action.num)
                }
                ActionParamType.ADVANCE_SPACE_RACE -> { // 太空竞赛前进
                    this.playerAdvanceSpaceRace(target!!, action.num)
                }
                ActionParamType.ADJUST_MILITARY_ACTION -> { // 调整军事行动力
                    this.playerAdjustMilitaryAction(target!!, action.num)
                }
                ActionParamType.WAR -> { // 战争
                    this.executeWarAction(action)
                }
                ActionParamType.RANDOM_DISCARD_CARD -> { // 用户随机弃牌
                    // 返回弃掉的牌
                    this.executeRandomDiscardAction(action)?.let(res.cards::addAll)
                }
                ActionParamType.DISCARD_CARD -> { // 弃牌
                    this.report.playerDiscardCard(target!!, action.card!!)
                    this.playerPlayCard(target, action.card!!)
                }
                ActionParamType.CLEAR_INFLUENCE -> { // 清除影响力
                    this.setInfluence(action.country!!, SuperPower.USA, 0)
                    this.setInfluence(action.country!!, SuperPower.USSR, 0)
                    val drawnCards = gameMode.cardManager.playingDeck.draw(4)
                    this.discardCards(drawnCards)
                    this.report.playerDiscardCards(target!!, drawnCards)
                }
                ActionParamType.ADD_STABILIZATION_BONUS -> {
                    action.country!!.addStabilizationBonus(action.num)
                    this.sendCountryInfo(action.country!!, null)
                }
                ActionParamType.TREAT_AS_BATTLEFIELD -> {
                    action.country!!.isBattleField = true
                }
                else -> {
                }
            }
        }
        return res
    }

    /**
     * 执行头条
     * @param headlines
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun executeHeadLine(headlines: List<HeadLineParam>) {
        for (headline in headlines) {
            // 设置当前玩家
            gameMode.turnPlayer = headline.player
            // 判断玩家是否存在被取消头条的能力
            if (headline.player.hasEffect(EffectType.CANCEL_HEADLINE)) {
                // 如果存在,则直接弃牌
                report.playerDiscardCard(headline.player, headline.card!!)
                this.discardCard(headline.card!!)
            } else {
                // 执行前置事件（目前只有鲜花反战）
                this.onPlayerHeadline(headline.player, headline.card)
                // 否则卡牌生效
                report.playerActiveCard(headline.player, headline.card!!)
                this.activeCardEvent(headline.player, headline.card!!)
            }
        }
    }

    /**
     * 执行随机弃牌行动
     * @param action
     * @return
     */

    private fun executeRandomDiscardAction(action: TSGameAction): List<TSCard>? {
        val target = this.getPlayer(action.targetPower)!!
        return if (target.hands.empty) {
            null
        } else {
            // 如果玩家有手牌则必须随机弃掉手牌
            val deck = TSCardDeck()
            deck.addCards(target.hands.cards)
            // 将卡牌本身从待选列表中移除...
            deck.removeCard(action.relateCard)
            deck.shuffle()
            val cards = deck.draw(action.num)
            this.playerRemoveHands(target, cards)
            this.report.playerDiscardCards(target, cards)
            cards
        }
    }

    /**
     * 执行计分牌
     * @param card
     */
    fun executeScore(card: TSCard) {
        val param = this.gameMode.scoreManager.executeScore(card.scoreRegion!!, false)
        this.report.playerRegionScore(this.ussrPlayer, param.ussr)
        this.report.playerRegionScore(this.usaPlayer, param.usa)
        this.adjustVp(param.vp)
    }

    /**
     * 执行战争行动
     * @param action
     */
    private fun executeWarAction(action: TSGameAction) {
        val trigPlayer = this.getOppositePlayer(action.targetPower)
        val country = action.country!!
        // 计算目标超级大国在战争国家周围控制的国家数量
        var num = gameMode.countryManager.getAdjacentCountriesNumber(country, action.targetPower)
        // 如果目标国家是目标超级大国的邻国则增加修正
        if (country.adjacentPowers.contains(action.targetPower)) {
            num++
        }
        if (action.includeSelf) {
            // 检查是否需要判断如果自己占领了该战争国家,也会修正最终的点数
            if (country.controlledPower == action.targetPower) {
                num++
            }
        }
        // 每个控制的邻国都会修正需要丢到的点数
        val needNum = action.limitNum + num
        val roll = TSRoll.roll()
        // 掷骰结果大于等于需求值则为成功
        val success = roll >= needNum
        this.report.playerWar(trigPlayer, country, roll, num, success)
        if (success) {
            this.adjustVp(trigPlayer, action.num)
            // 将目标超级大国在该国的所有影响力换成自己的
            val influence = country.customGetInfluence(action.targetPower)
            this.setInfluence(country, action.targetPower, 0)
            this.adjustInfluence(country, trigPlayer.superPower, influence)
        }
    }

    /**
     * 取得对方势力的玩家
     * @param power
     * @return
     */
    fun getOppositePlayer(power: SuperPower): TSPlayer {
        return this.getPlayer(power.oppositeSuperPower)!!
    }

    /**
     * 按照势力取得玩家
     * @param power
     * @return
     */
    fun getPlayer(power: SuperPower): TSPlayer? {
        return this.spplayers[power]
    }

    /**
     * 取得美国玩家
     * @return
     */
    val usaPlayer: TSPlayer
        get() = this.getPlayer(SuperPower.USA)!!

    /**
     * 取得苏联玩家
     * @return
     */
    val ussrPlayer: TSPlayer
        get() = this.getPlayer(SuperPower.USSR)!!

    override fun initConfig() {
        val config = TSConfig()
        config.versions.add(BgVersion.BASE)
        config.versions.add(BgVersion.EXP1)
        // 默认设置是随机决定苏联玩家,苏联让2点
        config.randomSeat = true
        config.point = 2
        config.isNewSpaceRace = false
        this.config = config
    }

    override fun initConst() {

    }

    override fun initPlayersSeat() {
        if (this.config.randomSeat) {
            // 如果是随机座位,则打乱玩家的顺序
            this.regroupPlayers()
        } else {
            // 否则的话,需要按照指定的顺位设置玩家的座位
            // 苏联玩家位置等于0时不需要更改顺位
            val orders = if (this.config.ussrPlayer == 1) {
                arrayOf(this.getPlayer(1), this.getPlayer(0))
            } else {
                arrayOf(this.getPlayer(0), this.getPlayer(1))
            }
            this.clearPlayers()
            for (i in orders.indices) {
                orders[i].position = i
                this.players.add(orders[i])
            }
            // 设置起始玩家
            this.startPlayer = this.getPlayer(0)
            this.currentPlayer = this.startPlayer
        }
    }

    override fun initReport() {
        super.report = TSReport(this)
    }

    /**
     * 判断该牌是否是中国牌
     * @param card
     * @return
     */
    fun isChinaCard(card: TSCard?): Boolean {
        return this.gameMode.cardManager.chinaCard === card
    }

    /**
     * 在玩家选择进行的动作时触发的方法
     * @param player
     * @param trigType
     * @param card
     */
    fun onPlayerAction(player: TSPlayer, trigType: TrigType?, card: TSCard?) {
        checkForceEffect(player, trigType, card)
        if (player.hasEffect(EffectType.EFFECT_59)) {
            // 检查玩家是否有#59-鲜花反战的效果
            // 如果#97-邪恶帝国已经生效则#59将没有效果(如果生效则不会有#59的效果)
            // 如果有,则在打出战争牌时,并且该战争牌可以正常生效,对方+2VP
            if (card != null && card.isWar && trigType != null && gameMode.eventManager.canActiveCard(card)) {
                // 实现时取对家即可
                val opposite = this.getOppositePlayer(player.superPower)
                this.adjustVp(opposite, 2)
            }
        }
        if (gameMode.eventManager.isCardActived(60)) {
            // 检查#60-U2事件是否生效
            // 如果有,则在以事件方式打出联合国时,苏联+1VP
            if (card != null && card.tsCardNo == TSConsts.UNI_CARD_NO && trigType == TrigType.EVENT) {
                // 给苏联玩家+1分
                this.adjustVp(1)
            }
        }
        if (this.isChinaCard(card)) {
            // 如果是美国玩家打出中国牌,则检查#101台湾决议是否生效,如果生效则移除
            if (player.superPower == SuperPower.USA && gameMode.eventManager.isCardActived(101)) {
                val c = gameMode.eventManager.getActivedCard(101)
                if (c != null) {
                    this.removeActivedCard(c)
                }
            }
        }
    }

    /**
     * 在玩家选择头条时触发的方法
     * @param player
     * @param card
     */
    fun onPlayerHeadline(player: TSPlayer, card: TSCard?) {
        if (player.hasEffect(EffectType.EFFECT_59)) {
            // 检查玩家是否有#59-鲜花反战的效果
            // 如果#97-邪恶帝国已经生效则#59将没有效果(如果生效则不会有#59的效果)
            // 如果有,则在打出战争牌时,并且该战争牌可以正常生效,对方+2VP
            if (card != null && card.isWar && gameMode.eventManager.canActiveCard(card)) {
                // 实现时取对家即可
                val opposite = this.getOppositePlayer(player.superPower)
                this.adjustVp(opposite, 2)
            }
        }

    }

    /**
     * 调整玩家的军事行动力
     * @param player
     * @param num
     */
    fun playerAdjustMilitaryAction(player: TSPlayer, num: Int) {
        this.report.playerAdjustMilitaryAction(player, num)
        this.playerSetMilitaryAction(player, player.getProperty(TSProperty.MILITARY_ACTION) + num)
    }

    /**
     * 玩家太空竞赛等级提升
     * @param player
     * @param num
     */
    fun playerAdvanceSpaceRace(player: TSPlayer, num: Int) {
        // 如果num>1,则中间的几个太空竞赛格子将被跳过,不取得任何VP
        player.properties.addProperty(TSProperty.SPACE_RACE, num)
        this.report.playerAdvanceSpaceRace(player, num)
        val vp = this.gameMode.spaceRaceManager.takeVp(player)
        if (vp > 0) {
            this.adjustVp(player, vp)
        }
        // 检查玩家太空竞赛的特权效果
        this.gameMode.spaceRaceManager.checkSpaceRacePrivilege()
        this.sendPlayerPropertyInfo(player, null)
    }

    /**
     * 玩家摸牌
     * @param player
     * @param drawNum
     */
    fun playerDrawCard(player: TSPlayer, drawNum: Int) {
        val orgSize = this.gameMode.cardManager.playingDeck.size
        // 如果牌堆的数量不够摸牌,则会重洗弃牌堆,需要重新发送弃牌堆的信息
        val sendDetail = drawNum > orgSize
        val cards = this.gameMode.cardManager.playingDeck.draw(drawNum)
        player.hands.addCards(cards)
        this.sendPlayerAddHandsResponse(player, cards, null)
        this.report.playerDrawCards(player, cards)
        this.sendDeckInfo(sendDetail, null)
    }

    /**
     * 玩家得到手牌
     * @param player
     * @param card
     */
    fun playerGetCard(player: TSPlayer, card: TSCard) {
        player.hands.addCard(card)
        this.sendPlayerAddHandResponse(player, card, null)
        this.report.playerGetCard(player, card)
        this.sendDeckInfo(false, null)
    }

    /**
     * 玩家打出牌
     * @param player
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerPlayCard(player: TSPlayer, card: TSCard) {
        player.takeCard(card.id)
        this.sendPlayerRemoveHandResponse(player, card, null)
    }

    /**
     * 玩家移除生效的卡牌
     * @param player
     * @param card
     */
    fun playerRemoveActivedCard(player: TSPlayer, card: TSCard) {
        player.removeEffect(card)
        gameMode.eventManager.removeActivedCard(card)
        this.sendRemoveActivedCardResponse(card, null)
        this.report.playerRemoveActiveCard(player, card)
    }

    /**
     * 玩家移除手牌
     * @param player
     * @param card
     */
    fun playerRemoveHand(player: TSPlayer, card: TSCard) {
        player.hands.removeCard(card)
        this.sendPlayerRemoveHandResponse(player, card, null)
    }

    /**
     * 玩家移除手牌
     * @param player
     * @param cards
     */
    fun playerRemoveHands(player: TSPlayer, cards: List<TSCard>) {
        player.hands.removeCards(cards)
        this.sendPlayerRemoveHandsResponse(player, cards, null)
    }

    /**
     * 投降处理
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerResigen(player: TSPlayer) {
        val initParam = InitParamFactory.createChoiceInitParam(gameMode, player, null, TrigType.EVENT)
        initParam.listeningPlayer = player.superPower
        initParam.isCanPass = true
        initParam.msg = "你是否确定要投降?"
        val l = TSResignConfirmListener(player, gameMode, initParam)
        val res = gameMode.insertListener(l)
        val choice = res.getInteger("choice")
        if (choice == 1) {
            val p = this.getOppositePlayer(player.superPower)
            this.playerWin(p, TSVictoryType.RESIGN)
        }
    }

    /**
     * 设置玩家的军事行动力
     * @param player
     * @param num
     */
    fun playerSetMilitaryAction(player: TSPlayer, num: Int) {
        player.properties.setProperty(TSProperty.MILITARY_ACTION, num)
        this.report.playerSetMilitaryAction(player)
        this.sendPlayerPropertyInfo(player, null)
    }

    /**
     * 玩家进行太空竞赛
     * @param player
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerSpaceRace(player: TSPlayer, card: TSCard) {
        // 检查太空竞赛是否成功
        val roll = TSRoll.roll()
        val success = gameMode.spaceRaceManager.checkRoll(player, roll)
        report.playerSpaceRace(player, card, roll, success)
        if (success) {
            // 如果成功,则玩家的太空竞赛+1
            this.playerAdvanceSpaceRace(player, 1)
        }
        if (TSConsts.CHINA_CARD_NO != card.tsCardNo) {
            // 无论是否成功,牌入弃牌堆
            this.playerRemoveHand(player, card)
            this.discardCard(card)
        }
        // 设置本回合玩家太空竞赛已使用的次数
        player.addSpaceRaceTimes(1)
    }

    /**
     * 玩家游戏胜利
     * @param player
     * @param victoryType
     */
    fun playerWin(player: TSPlayer?, victoryType: TSVictoryType) {
        this.gameMode.winner = player
        this.gameMode.victoryType = victoryType
        this.winGame()
    }

    /**
     * 处理TS的能力
     * @param abilities
     * @param player
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun processAbilities(abilities: List<TSAbility>?, player: TSPlayer, card: TSCard?) {
        if (abilities != null && abilities.isNotEmpty()) {
            for (a in abilities) {
                val ap = a.actionParam
                when (a.abilityType) {
                    TSAbilityType.ACTION_PARAM_EFFECT -> { // 行动参数效果
                        val ga = GameActionFactory.createGameAction(gameMode, player, card, ap)
                        this.executeAction(ga)
                    }
                    TSAbilityType.ADD_EFFECT -> { // 为玩家添加效果
                        val effect = GameActionFactory.createEffect(gameMode, player, card!!, ap, a)
                        val target = this.getPlayer(effect.targetPower) ?: throw BoardGameException("-")
                        target.addEffect(card, effect)
                    }
                    TSAbilityType.ADJUST_INFLUENCE -> { // 调整影响力,需要用户选择
                        val initParam = InitParamFactory.createActionInitParam(gameMode, player, card, a, TrigType.EVENT)
                        val executor = TSAdjustInfluenceExecutor(player, gameMode, initParam)
                        executor.execute()
                    }
                    TSAbilityType.CHOOSE_COUNTRY_ACTION -> { // 选择国家,执行行动
                        val initParam = InitParamFactory.createActionInitParam(gameMode, player, card, a, TrigType.EVENT)
                        val executor = TSCountryActionExecutor(player, gameMode, initParam, ap)
                        executor.execute()
                    }
                    TSAbilityType.CHOOSE_CARD_ACTION -> { // 选择卡牌,执行行动
                        val initParam = InitParamFactory.createCardActionInitParam(gameMode, player, card, a, TrigType.EVENT)
                        val executor = TSCardActionExecutor(player, gameMode, initParam)
                        executor.execute()
                    }
                    TSAbilityType.ACTIVE_DISCARD_CARD -> { // 随机弃牌并生效
                        val ga = GameActionFactory.createGameAction(gameMode, player, card, ap)
                        val ar = this.executeAction(ga)
                        ar.cards.forEach {
                            if (a.cardCondGroup test it) {
                                // 如果弃掉的牌符合条件,则直接生效
                                this.activeCardEvent(player, it)
                            } else {
                                // 否则入弃牌对堆
                                this.discardCard(it)
                            }
                        }
                    }
                    TSAbilityType.VIEW_OPPOSITE_HAND -> { // 看对手的手牌
                        val initParam = InitParamFactory.createActionInitParam(gameMode, player, card, a, TrigType.EVENT)
                        val l = TSViewHandListener(player, gameMode, initParam)
                        gameMode.insertListener(l)
                    }
                    TSAbilityType.OP_ACTION -> { // 使用OP进行行动
                        val initParam = InitParamFactory.createOpActionParam(gameMode, player, card, a, TrigType.EVENT)
                        TSOpActionExecutor(player, gameMode, initParam).execute()
                    }
                    TSAbilityType.ACTION_EXECUTOR -> { // 调用行动执行器
                        val initParam = InitParamFactory.createExecutorInitParam(gameMode, player, card, a, TrigType.EVENT)
                        val executor = ActionFactory.createActionExecutor(player, gameMode, initParam)
                        executor.execute()
                    }
                    TSAbilityType.ACTION_LISTENER -> { // 调用行动监听器
                        val initParam = InitParamFactory.createActionInitParam(gameMode, player, card, a, TrigType.EVENT)
                        val executor = ActionFactory.createListenerExecutor(player, gameMode, initParam)
                        executor.execute()
                    }
                    TSAbilityType.MOVE_CARD_TO_MIDDLE -> {
                        val res = gameMode.cardManager.removeCards(a.cardCondGroup)
                        gameMode.cardManager.addCardToPlayingDeck(res, TSPhase.MID)
                        this.report.addCardToMid(res)
                    }
                    TSAbilityType.REMOVE_CARD -> {
                        val res = gameMode.cardManager.removeCards(a.cardCondGroup)
                        this.report.trashCard(res)
                    }
                    TSAbilityType.REPLACE_NEW_CARD -> {
                        val res = gameMode.cardManager.removeCards(a.cardCondGroup)
                        for (c in res) {
                            val newCard = gameMode.cardManager.getReplaceCard(c.tsCardNo)
                                    ?: throw BoardGameException("-")
                            gameMode.cardManager.addCardToPlayingDeck(listOf(newCard), newCard.phase)
                            this.report.replaceCard(c, newCard)
                            if (c.cardType == CardType.SCORING && c.scoreRegion == "EUROPE") {
                                gameMode.scoreManager.replaceEuropeScore()
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    /**
     * 处理生效后的卡牌
     * @param card
     * @param result
     */
    fun processActivedCard(card: TSCard, result: ActiveResult) {
        if (this.isChinaCard(card)) {
            // 如果是中国牌,则按照设置加入到生效卡牌列表
            val target = this.convertSuperPower(card.durationResult!!.target, result.activePlayer!!)
            gameMode.eventManager.addActivedCard(target, card)
            this.sendAddActivedCardResponse(card, target, null)
        } else {
            if (card.tsCardNo == TSConsts.FIRST_LIGHTNING_NO && !result.eventActived) {
                // 首次闪电
                this.adjustDefcon(-1)
            }
            if (!card.ignoreAfterEvent) {
                // 如果不能忽略该卡牌生效后的处理
                if (!result.eventActived || !card.removeAfterEvent) {
                    // 如果卡牌没有生效,或者生效后不弃掉,则进弃牌堆
                    this.discardCard(card)
                } else {
                    // 否则进废牌堆
                    this.trashCard(card)
                }
            } else {
                // 否则的话,该牌暂时就不显示了
                // 暂时只有#49-导弹嫉妒需要这个效果
            }

            // 如果卡牌生效,则检查是否要加入到生效卡牌列表
            if (result.eventActived && card.durationResult != null) {
                // target为空时则取NONE,表示全局事件
                val target = this.convertSuperPower(card.durationResult!!.target, result.activePlayer!!)
                gameMode.eventManager.addActivedCard(target, card)
                this.sendAddActivedCardResponse(card, target, null)
            }
        }
    }

    /**
     * 移除生效的卡牌
     * @param card
     */
    fun removeActivedCard(card: TSCard) {
        gameMode.eventManager.removeActivedCard(card)
        // 同时从玩家身上移除该卡牌的效果
        for (player in this.players) {
            player.removeEffect(card)
        }
        this.sendRemoveActivedCardResponse(card, null)
    }

    /**
     * 发送行动记录
     * @param record
     */
    fun sendActionRecord(record: ActionRecord) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_ACTION_RECORD, -1).public("record", record.toMap()).send(this)
    }

    /**
     * 发送行动记录
     */
    fun sendActionRecords(records: Collection<ActionRecord>, receiver: TSPlayer?) {
        val list = BgUtils.toMapList(records)
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_ACTION_RECORD, -1).public("records", list).send(this, receiver)
    }

    /**
     * 发送当前全局生效事件的信息
     * @param receiver
     */
    fun sendActivedCardsInfo(receiver: TSPlayer?) {
        val powers = arrayOf(SuperPower.NONE, SuperPower.USSR, SuperPower.USA)
        for (o in powers) {
            val cards = gameMode.eventManager.getActivedCards(o)
            this.sendAddActivedCardsResponse(cards, o, receiver)
        }
        if (gameMode.zeroCards.isNotEmpty()) {
            this.sendAddZeroCardsResponse(gameMode.zeroCards, receiver)
        }
    }

    /**
     * 发送添加生效卡牌的信息
     * @param card
     * @param target
     * @param receiver
     */
    fun sendAddActivedCardResponse(card: TSCard, target: SuperPower, receiver: TSPlayer?) {
        this.sendAddActivedCardsResponse(BgUtils.toList(card), target, receiver)
    }

    /**
     * 发送添加生效卡牌的信息
     * @param cards
     * @param target
     * @param receiver
     */
    fun sendAddActivedCardsResponse(cards: Collection<TSCard>, target: SuperPower, receiver: TSPlayer?) {
        if (cards.isNotEmpty()) {
            CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_ADD_ACTIVED_CARD, -1).public("cardIds", BgUtils.card2String(cards)).public("target", target).send(this, receiver)
        }
    }

    /**
     * 发送在弃牌堆中加牌的信息
     * @param card
     * @param receiver
     */
    fun sendAddDiscardResponse(card: TSCard, receiver: TSPlayer?) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_ADD_DISCARD, -1).public("cardIds", card.id).send(this, receiver)
        // 需要同时刷新牌堆信息
        this.sendDeckInfo(false, receiver)
    }

    /**
     * 发送在弃牌堆中加牌的信息
     * @param cards
     * @param receiver
     */
    fun sendAddDiscardsResponse(cards: Collection<TSCard>, receiver: TSPlayer?) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_ADD_DISCARD, -1).public("cardIds", BgUtils.card2String(cards)).send(this, receiver)
        // 需要同时刷新牌堆信息
        this.sendDeckInfo(false, receiver)
    }

    fun sendAddZeroCardsResponse(cards: List<TSZeroCard>, receiver: TSPlayer?) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_ADD_ZERO_CARD, -1).public("cards", cards.map(TSZeroCard::toMap)).send(this, receiver)
    }

    /**
     * 发送所有国家的信息(影响力,是否控制)
     * @param receiver
     */
    fun sendAllCountriesInfo(receiver: TSPlayer?) {
        this.sendCountriesInfo(this.gameMode.countryManager.allCountries, receiver)
    }

    /**
     * 发送游戏的基本信息(DEFCON,当前回合,当前轮数,VP)
     * @param receiver
     */
    fun sendBaseInfo(receiver: TSPlayer?) {
        val round = this.gameMode.round
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_BASE_INFO, -1).public("defcon", this.gameMode.defcon).public("round", round).public("turn", this.gameMode.turn).public("vp", this.gameMode.vp).public("maxTurn", TSConsts.getRoundTurnNum(round)).public("phase", TSPhase.getChineseDesc(this.gameMode.currentPhase)).public("isNewSpaceRace", this.config.isNewSpaceRace).send(this, receiver)
    }

    /**
     * 发送中国牌的相关信息
     * @param receiver
     */
    fun sendChinaCardInfo(receiver: TSPlayer?) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_CHINA_CARD, -1).public("superPower", this.gameMode.cardManager.chinaOwner).public("canUse", this.gameMode.cardManager.chinaCanUse).public("cardId", this.gameMode.cardManager.chinaCard.id).send(this, receiver)
    }

    /**
     * 刷新国家的信息(影响力,是否控制)
     * @param countries
     * @param receiver
     */
    fun sendCountriesInfo(countries: Collection<TSCountry>, receiver: TSPlayer?) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_COUNTRY_INFO, -1).public("countries", BgUtils.toMapList(countries)).send(this, receiver)
    }

    /**
     * 刷新国家的信息(影响力,是否控制)
     * @param country
     * @param receiver
     */
    fun sendCountryInfo(country: TSCountry, receiver: TSPlayer?) {
        val list = ArrayList<TSCountry>()
        list.add(country)
        this.sendCountriesInfo(list, receiver)
    }

    /**
     * 发送游戏牌堆的信息(当前牌堆数量,弃牌堆数量,弃牌堆卡牌,弃牌堆中最后出的一张牌)
     * @param receiver
     */
    fun sendDeckInfo(sendDetail: Boolean, receiver: TSPlayer?) {
        val discards = this.gameMode.cardManager.playingDeck.discards
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_DECK_INFO, -1).public("playingCardNum", this.gameMode.cardManager.playingDeck.size).public("discardNum", discards.size).public("lastCardId", discards.lastOrNull()?.id).send(this, receiver)
        // 是否发送弃牌堆明细
        if (sendDetail) {
            CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_ADD_DISCARD, -1).public("cardIds", BgUtils.card2String(discards)).public("reload", true).send(this, receiver)
            CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_TRASH_CARD, -1).public("cardIds", BgUtils.card2String(this.gameMode.cardManager.trashDeck.cards)).public("reload", true).send(this, receiver)
        }
    }

    @Throws(BoardGameException::class)
    override fun sendGameInfo(receiver: TSPlayer?) {
        // 发送当前游戏信息(DEFCON,当前回合,当前轮数,VP)
        this.sendBaseInfo(receiver)
        // 发送当前游戏的牌堆信息
        this.sendDeckInfo(true, receiver)
        // 发送中国牌的信息
        this.sendChinaCardInfo(receiver)
        // 发送当前全局生效事件的信息
        this.sendActivedCardsInfo(receiver)

        // 发送所有国家影响力的信息
        this.sendAllCountriesInfo(receiver)
        // 发送最近的行动记录
        this.sendRecentActionRecords(receiver)
    }

    @Throws(BoardGameException::class)
    override fun sendInitInfo(receiver: TSPlayer?) {

    }

    /**
     * 发送玩家得到手牌的信息
     * @param player
     * @param card
     * @param receiver
     */
    fun sendPlayerAddHandResponse(player: TSPlayer, card: TSCard, receiver: TSPlayer?) {
        this.sendPlayerAddHandsResponse(player, BgUtils.toList(card), receiver)
    }

    /**
     * 发送玩家得到手牌的信息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerAddHandsResponse(player: TSPlayer, cards: List<TSCard>, receiver: TSPlayer?) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_ADD_HANDS, player.position).public("cardNum", cards.size).public("handNum", player.hands.size).private("cardIds", BgUtils.card2String(cards)).send(this, receiver)
    }

    @Throws(BoardGameException::class)
    override fun sendPlayerPlayingInfo(receiver: TSPlayer?) {
        for (player in this.players) {
            // 发送玩家的基本信息(太空竞赛,军事行动)
            this.sendPlayerPropertyInfo(player, receiver)

            // 发送玩家的手牌信息
            this.sendPlayerAddHandsResponse(player, player.hands.cards, receiver)

            // 发送玩家持续生效事件的信息

        }
    }

    /**
     * 刷新玩家的基本信息(太空竞赛,军事行动)
     * @param player
     * @param receiver
     */
    fun sendPlayerPropertyInfo(player: TSPlayer, receiver: TSPlayer?) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_PLAYER_INFO, player.position).public("spaceRace", player.getProperty(TSProperty.SPACE_RACE)).public("militaryAction", player.getProperty(TSProperty.MILITARY_ACTION)).send(this, receiver)
    }

    /**
     * 发送玩家失去手牌的信息
     * @param player
     * @param receiver
     */
    fun sendPlayerRemoveHandResponse(player: TSPlayer, card: TSCard, receiver: TSPlayer?) {
        this.sendPlayerRemoveHandsResponse(player, BgUtils.toList(card), receiver)
    }

    /**
     * 发送玩家失去手牌的信息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerRemoveHandsResponse(player: TSPlayer, cards: List<TSCard>, receiver: TSPlayer?) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_REMOVE_HANDS, player.position).public("cardNum", cards.size).public("handNum", player.hands.size).private("cardIds", BgUtils.card2String(cards)).send(this, receiver)
    }

    /**
     * 发送最近10条行动记录
     */
    fun sendRecentActionRecords(receiver: TSPlayer?) {
        this.sendActionRecords(this.report.getRecentRecords(10), receiver)
    }

    /**
     * 发送移除生效卡牌的信息
     * @param card
     * @param receiver
     */
    fun sendRemoveActivedCardResponse(card: TSCard, receiver: TSPlayer?) {
        this.sendRemoveActivedCardsResponse(BgUtils.toList(card), receiver)
    }

    /**
     * 发送移除生效卡牌的信息
     * @param cards
     * @param receiver
     */
    fun sendRemoveActivedCardsResponse(cards: Collection<TSCard>, receiver: TSPlayer?) {
        if (cards.isNotEmpty()) {
            CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_REMOVE_ACTIVED_CARD, -1).public("cardIds", BgUtils.card2String(cards)).send(this, receiver)
        }
    }

    /**
     * 发送在弃牌堆中加牌的信息
     * @param card
     * @param receiver
     */
    fun sendRemoveDiscardResponse(card: TSCard, receiver: TSPlayer?) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_REMOVE_DISCARD, -1).public("cardIds", card.id).send(this, receiver)
        // 需要同时刷新牌堆信息
        this.sendDeckInfo(false, receiver)
    }

    /**
     * 发送清空弃牌堆的信息
     * @param receiver
     */
    fun sendRemoveDiscardsAll(receiver: TSPlayer?) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_REMOVE_DISCARD, -1).public("removeAll", true).send(this, receiver)
    }

    /**
     * 发送从弃牌堆中移出牌的信息
     * @param cards
     * @param receiver
     */
    fun sendRemoveDiscardsResponse(cards: Collection<TSCard>, receiver: TSPlayer?) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_REMOVE_DISCARD, -1).public("cardIds", BgUtils.card2String(cards)).send(this, receiver)
        // 需要同时刷新牌堆信息
        this.sendDeckInfo(false, receiver)
    }

    /**
     * 发送将牌移出游戏的信息
     * @param card
     * @param receiver
     */
    fun sendTrashCardResponse(card: TSCard, receiver: TSPlayer?) {
        this.sendTrashCardsResponse(listOf(card), receiver)
    }

    /**
     * 发送将牌移出游戏的信息
     * @param receiver
     */
    fun sendTrashCardsResponse(cards: Collection<TSCard>, receiver: TSPlayer?) {
        CmdFactory.createGameResponse(TSGameCmd.GAME_CODE_TRASH_CARD, -1).public("cardIds", BgUtils.card2String(cards)).send(this, receiver)
    }

    /**
     * 设置DEFCON等级
     * @param defcon
     */
    fun setDefcon(defcon: Int) {
        var newDefCon = when {
            gameMode.turnPlayer === this.usaPlayer && gameMode.round <= 1 && this.usaPlayer.hasEffect(EffectType.USA_IGNORE_DEFCON) -> max(2, defcon)
            else -> max(1, defcon)
        }
        newDefCon = min(5, newDefCon)
        this.gameMode.defcon = newDefCon
        this.report.printDefcon(newDefCon)
        this.sendBaseInfo(null)

        when (newDefCon) {
            1 ->
                // 获胜者为当前行动轮玩家的对家
                this.playerWin(this.getOppositePlayer(this.gameMode.turnPlayer?.superPower
                        ?: SuperPower.NONE), TSVictoryType.DEFCON)
            2 ->
                // 如果是在回合出牌阶段,并且#106-北美防空司令部在场
                if (this.gameMode.eventManager.isCardActived(106) && this.gameMode.actionPhase === TSActionPhase.ACTION_ROUND) {
                    // 并且需要美国控制加拿大...
                    try {
                        val country = this.gameMode.countryManager.getCountry(Country.CAN)
                        if (country.controlledPower == SuperPower.USA) {
                            // 则将106标志设为可触发
                            this.usaPlayer.params.setGameParameter(106, true)
                        }
                    } catch (e: BoardGameException) {
                        log.error("获取加拿大国家失败...这是毛情况啊- -", e)
                    }

                }
            else -> {
            }
        }
    }

    /**
     * 设置国家的影响力
     * @param country
     * @param power
     * @param num
     */
    fun setInfluence(country: TSCountry, power: SuperPower, num: Int) {
        val ap = AdjustParam(power, ActionType.SET_INFLUENCE, country)
        ap.tempCountry.customSetInfluence(power, num)
        // 输出战报
        this.report.doAction(ap)
        ap.apply()
        this.sendCountryInfo(country, null)
    }

    @Throws(BoardGameException::class)
    override fun setupGame() {
        this.config.playerNumber = this.currentPlayerNumber
        this.gameMode = TSGameMode(this)
    }

    /**
     * 设置VP,苏联为正分,美国为负分
     * @param vp
     */
    fun setVp(vp: Int) {
        // VP的返回在-20到20之间
        val realVp = minOf(maxOf(-20, vp), 20)
        this.gameMode.vp = realVp
        this.report.printVp(realVp)
        this.sendBaseInfo(null)

        // 任一方VP到20直接结束游戏
        if (abs(realVp) == 20) {
            this.gameMode.victoryType = TSVictoryType.VP
            this.gameMode.winner = if (realVp > 0) this.ussrPlayer else this.usaPlayer
            // 直接结束游戏
            this.winGame()
        }
    }

    /**
     * 从弃牌堆中拿牌
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun takeDiscardCard(card: TSCard) {
        this.gameMode.cardManager.playingDeck.takeDiscardCard(card.id)
        this.sendRemoveDiscardResponse(card, null)
    }

    /**
     * 把牌丢到弃牌堆中
     * @param card
     */
    fun trashCard(card: TSCard) {
        this.gameMode.cardManager.trashDeck.addCard(card)
        this.sendTrashCardResponse(card, null)
        this.report.trashCard(card)
    }
}
