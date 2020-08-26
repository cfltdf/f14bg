package com.f14.TTA

import com.f14.F14bg.network.CmdFactory
import com.f14.TTA.component.ProductionInfo
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.component.card.*
import com.f14.TTA.component.param.CostParam
import com.f14.TTA.consts.*
import com.f14.TTA.factory.TTACmdFactory
import com.f14.TTA.listener.TTAConfirmListener
import com.f14.TTA.listener.TTAShowCardListener
import com.f14.TTA.manager.TTAConstManager
import com.f14.bg.FixedOrderBoardGame
import com.f14.bg.action.BgResponse
import com.f14.bg.component.Card
import com.f14.bg.consts.BgVersion
import com.f14.bg.consts.TeamMode
import com.f14.bg.exception.BoardGameException
import com.f14.bg.player.Player
import com.f14.bg.utils.BgUtils
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CollectionUtils
import net.sf.json.JSONObject
import java.util.*
import kotlin.math.min

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

class TTA : FixedOrderBoardGame<TTAPlayer, TTAConfig, TTAReport>() {
    override val mode
        get() = gameMode
    lateinit var gameMode: TTAGameMode
        private set

    /**
     * 玩家可否离开（不影响游戏进行）
     * @param player
     * @return
     */
    override fun canLeave(player: Player): Boolean {
        val p = player as TTAPlayer
        return this.isPlayingGame(p) && p.resigned
    }

    /**
     */
    private fun checkPastCard(card: TTACard?): Boolean = when {
        card == null -> false
        card.level <= gameMode.currentAge - 1 -> true
        card.activeAbility != null -> card.activeAbility!!.abilityType in arrayOf(ActiveAbilityType.PA_HUBATIAN, ActiveAbilityType.PA_NARODNI)
        else -> false
    }

    /**
     * @param player
     * @return
     */
    private fun checkPlayerUprising(player: TTAPlayer) = this.config.isUprising && player.isUprising

    /**
     * 检查临时费用的使用情况
     * @param player
     * @param card
     * @param cp
     * @param property
     */
    fun checkTemplateResource(player: TTAPlayer, card: TTACard?, cp: CostParam, property: CivilizationProperty) {
        if (cp.cost.getProperty(property) == 0) return
        for (a in player.tempResManager.tempResAbility) {
            if (a.test(card)) {
                // 如果该临时资源可以用在card上
                val restp = player.tempResManager.getTempRes(a) ?: continue
                val used = TTAProperty()
                // 取得该技能剩余的临时资源
                val restResource = restp.getProperty(property)
                used.addProperty(property, min(cp.cost.getProperty(property), restResource))
                // 如果该技能的临时资源够付了,则支付对应的临时费用
                // 否则则支付所有的临时费用
                cp.useAbility(a, used)
                // 如果已经不需支付任何费用了,则不再检查其他临时资源
                if (cp.cost.getProperty(property) == 0) return
            }
        }
        for (a in player.tempResManager.alternateTempResAbility) {
            // 如果该临时资源可以用在card上
            if (a.test(card)) {
                val restp = player.tempResManager.getAlternateTempRes(a)
                if (restp.getProperty(property) > 0) {
                    val c = player.abilityManager.getAbilityCard(a.abilityType) ?: continue
                    try {
                        player.checkEnoughResource(restp, -1)
                        val listener = TTAConfirmListener(gameMode, player, "你是否使用 ${c.name} 的能力?")
                        val res = gameMode.insertListener(listener)
                        if (res.getBoolean(player.position)) {
                            cp.useAbility(a, restp)
                        }
                    } catch (ignored: BoardGameException) {
                    }
                    // 如果已经不需支付任何费用了,则不再检查其他临时资源
                    if (cp.cost.getProperty(property) == 0) return
                }
            }
        }
    }


    @Throws(BoardGameException::class)
    override fun createConfig(obj: JSONObject): TTAConfig {
        val config = TTAConfig()
        config.versions = LinkedHashSet()
        config.ageCount = TTAConsts.AGE_COUNT
        config.mode = TTAMode.valueOf(obj.getString("mode"))
        config.isTeamMatch = obj.getBoolean("teamMatch")
        config.teamMode = BgUtils.withDefault(TeamMode.RANDOM) { TeamMode.valueOf(obj.getString("teamMode")) }
        config.randomSeat = BgUtils.withDefault(true) { obj.getBoolean("randomSeat") }
        config.isRevoltDraw = BgUtils.withDefault(false) { obj.getBoolean("revoltDraw") }
        config.isExpansionUsed = BgUtils.withDefault(false) { obj.getBoolean("expansionUsed") }
        config.isNewAgeUsed = BgUtils.withDefault(false) { obj.getBoolean("newAgeUsed") }
        config.isTouhouUsed = BgUtils.withDefault(false) { obj.getBoolean("touhouUsed") }
        config.isExpansionCN = BgUtils.withDefault(false) { obj.getBoolean("expansionCN") }
        config.isExpansionDIY = BgUtils.withDefault(false) { obj.getBoolean("expansionDIY") }
        config.isExpansionDIY = BgUtils.withDefault(false) { obj.getBoolean("expansionDIY") }
        config.isExpansion14 = BgUtils.withDefault(false) { obj.getBoolean("expansion14") }
        config.isNoLimit = BgUtils.withDefault(false) { obj.getBoolean("noLimit") }
        config.isHideAvalable = BgUtils.withDefault(false) { obj.getBoolean("hideAvalable") }
        config.isLazyMemory = BgUtils.withDefault(false) { obj.getBoolean("lazyMemory") }
        config.isBalanced22 = BgUtils.withDefault(false) { obj.getBoolean("balanced22") }
        config.isGlobalConflict = BgUtils.withDefault(false) { obj.getBoolean("globalConflict") }
        if (config.isExpansionDIY) throw BoardGameException("奈奈DIY目前不可用!")
        if (config.isNewAgeUsed) {
            // 新版
            config.versions.add("V2BASE")
            if (config.isExpansionUsed) config.versions.add("V2EX")
            if (config.isExpansionCN) config.versions.add("CN")
            if (config.isExpansionDIY) config.versions.add("V2DIY")
            if (config.isExpansion14) config.versions.add("F14")
        } else {
            // 原版
            config.versions.add("BASE")
            if (config.isExpansionUsed) config.versions.add("BGO")
            if (config.isTouhouUsed) config.versions.add("TH")
            if (config.isExpansionCN) config.versions.add("CN")
            if (config.isExpansionDIY) config.versions.add("DIY")
            if (config.isExpansion14) config.versions.add("F14")
        }
        return config
    }

    /**
     * 作弊代码
     * @param msg
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun doCheat(msg: String): Boolean {
        if (super.doCheat(msg)) {
            return true
        }
        val s = msg.split(" ".toRegex()).dropLastWhile(String::isEmpty)
        when (s[0]) {
            "addage" -> {
                gameMode.cardBoard.newAge()
                return true
            }
            "top" -> {
                CheckUtils.check(!gameMode.cardBoard.topCard(s[1]), "Card not found!")
                return true
            }
            "top%" -> {
                CheckUtils.check(!gameMode.cardBoard.topCardByNo(s[1]), "Card not found!")
                return true
            }
            "add_hand" -> {
                val pos = s[1].toInt()
                val p = this.getPlayer(pos)
                (2 until s.size).map { gameMode.cardBoard.getTempCardByNo(s[it]) }.forEach {
                    when {
                        it.cardType == CardType.WONDER -> this.playerGetWonder(p, it as WonderCard)
                        else -> this.playerAddHand(p, it)
                    }
                }
                return true
            }
            "add_board" -> {
                val pos = s[1].toInt()
                val p = this.getPlayer(pos)
                (2 until s.size).forEach { i ->
                    val card = gameMode.cardBoard.getTempCardByNo(s[i])
                    this.playerAddCardDirect(p, card)
                    this.report.playerAddCard(p, card)
                }
                return true
            }
            "next_event" -> {
                val card = gameMode.cardBoard.getTempCardByNo(s[1])
                gameMode.cardBoard.addNextEvent(card)
                return true
            }
            "score" -> {
                val pos = s[1].toInt()
                val p = this.getPlayer(pos)
                val property = TTAProperty()
                for (i in 2 until s.size step 2) {
                    val type = s[i].toLowerCase()
                    val num = s[i + 1].toIntOrNull() ?: continue
                    when (type) {
                        "cp", "culture" -> property.addProperty(CivilizationProperty.CULTURE, num)
                        "sp", "science" -> property.addProperty(CivilizationProperty.SCIENCE, num)
                        "rp", "resource" -> property.addProperty(CivilizationProperty.RESOURCE, num)
                        "fp", "food" -> property.addProperty(CivilizationProperty.FOOD, num)
                        "yt", "yellow" -> property.addProperty(CivilizationProperty.YELLOW_TOKEN, num)
                        "bt", "blue" -> property.addProperty(CivilizationProperty.BLUE_TOKEN, num)
                        "ca", "civil" -> property.addProperty(CivilizationProperty.CIVIL_ACTION, num)
                        "ma", "military" -> property.addProperty(CivilizationProperty.MILITARY_ACTION, num)
                        "pi", "popinc" -> this.playerIncreasePopulation(p, num)
                        "pd", "popdec" -> this.playerDecreasePopulation(p, num)
                    }
                }
                this.playerAddPoint(p, property)
                this.playerAddToken(p, property)
                this.playerAddAction(p, property)
                this.report.printCache(p)
                return true
            }
            else -> return false
        }
    }

    /**
     * 处理临时资源的使用情况
     * @param cp
     */
    fun executeTemplateResource(player: TTAPlayer, cp: CostParam) {
        cp.usedAbilities.forEach { (a, p) ->
            if (player.tempResManager.getTempRes(a) != null) {
                player.tempResManager.useTemplateResource(a, p)
                p.multi(-1)
                this.report.playerExecuteTemporaryResource(player, p)
            } else {
                player.tempResManager.useAlternateTemplateResource(a, p)
                this.report.playerActiveCardCache(player, (a as CivilCardAbility).abilityType)
            }
        }
    }

    /**
     * 玩家体面退出游戏
     * @param player
     */
    fun forceResign(player: TTAPlayer) {
        gameMode.resignedPlayerPosition[player] = gameMode.resignedPlayerNumber
        player.resigned = true
        gameMode.gameRank.removePlayer(player)
    }

    /**
     * 还在游戏的玩家数量（除去体退的）
     * @return
     */
    val realPlayerNumber: Int
        get() = gameMode.gameRank.playerNumber

    /**
     * 使游戏结束的人数
     */
    val endGamePlayerNumber: Int
        get() = if (this.isTeamMatch) 2 else 1

    override fun initConfig() {
        val config = TTAConfig()
        config.versions.add(BgVersion.BASE)
        config.ageCount = TTAConsts.AGE_COUNT
        config.mode = TTAMode.FULL
        config.isTeamMatch = false
        config.teamMode = TeamMode.RANDOM
        config.randomSeat = true
        config.isRevoltDraw = false
        config.isExpansionUsed = false
        config.isNewAgeUsed = false
        config.isTouhouUsed = false
        config.isExpansionCN = false
        config.isExpansionDIY = false
        config.isExpansion14 = false
        config.isNoLimit = false
        config.isHideAvalable = false
        config.isLazyMemory = false
        config.isBalanced22 = false
        config.isGlobalConflict = false
        this.config = config
    }

    override fun initConst() {}

    /**
     * 初始化玩家的座位信息
     */
    @Throws(BoardGameException::class)
    override fun initPlayersSeat() {
        when {
            !this.config.randomSeat -> {
                // 如果不是随机座位
                // 设置座位号
                this.players.withIndex().forEach { (i, p) -> p.position = i }
                // 设置起始玩家和当前玩家
                this.startPlayer = this.getPlayer(0)
                this.currentPlayer = this.startPlayer
            }
            this.isTeamMatch && this.config.teamMode == TeamMode.FIXED -> {
                // 如果是team match, 并且选择了13 vs 24, 则需要按照特殊的规定排列位置
                // 先设置好玩家的team
                this.players.withIndex().forEach { (i, p) -> p.team = i % 2 }
                // 打乱玩家的顺序
                val players = ArrayList(this.players)
                CollectionUtils.shuffle(players)
                this.players.clear()
                this.players.add(players.removeAt(0))
                var lastTeam = this.players[0].team
                while (players.isNotEmpty()) {
                    val p = players.firstOrNull { it.team != lastTeam } ?: throw BoardGameException("设置座位时发生错误!")
                    players.remove(p)
                    this.players.add(p)
                    lastTeam = p.team
                }
                // 设置座位号
                this.players.withIndex().forEach { (i, p) -> p.position = i }
                // 设置起始玩家和当前玩家
                this.startPlayer = this.players[0]
                this.currentPlayer = this.startPlayer
            }
            else -> {
                this.regroupPlayers()
            }
        }
    }

    override fun initPlayerTeams() = when {
        this.isTeamMatch -> this.players.forEach { it.team = it.position % 2 } // 13 vs 24
        this.isTichuMode -> Unit
        else -> super.initPlayerTeams()
    }

    override fun initReport() {
        super.report = TTAReport(this)
    }
    // 必须要4人游戏才会是组队赛

    override val isTeamMatch: Boolean
        get() = this.players.size == 4 && super.isTeamMatch

    // 必须要新版3人游戏才会是地主模式
    val isTichuMode: Boolean
        get() = this.players.size == 3 && this.isVersion2 && super.isTeamMatch

    /**
     * 新版检测
     */
    val isVersion2: Boolean
        get() = this.config.isNewAgeUsed

    /**
     * 玩家调整CA/MA
     * @param player
     * @param property
     */
    fun playerAddAction(player: TTAPlayer, property: TTAProperty) {
        this.playerAddCivilAction(player, property.getProperty(CivilizationProperty.CIVIL_ACTION))
        this.playerAddMilitaryAction(player, property.getProperty(CivilizationProperty.MILITARY_ACTION))
    }

    /**
     * 玩家打出手牌到面板
     * @param player
     * @param card
     */
    fun playerAddCard(player: TTAPlayer, card: TTACard) {
        this.playerRemoveHand(player, card)
        this.playerAddCardDirect(player, card)
    }

    /**
     * 玩家直接打出牌,包括 领袖/政府/各种科技/殖民地/战术牌
     * @param player
     * @param card
     */
    fun playerAddCardDirect(player: TTAPlayer, card: TTACard) {
        val result = player.addCard(card)
        this.sendPlayerAddCardResponse(player, card, null)
        this.playerAddToken(player, result)
        this.playerAddAction(player, result)
        this.playerRefreshProperty(player)
        // 检查打出的牌是否拥有可以使用的能力,如果有则刷新玩家的可使用能力列表
        if (card.activeAbility != null) {
            // 暂时只可能在NORMAL阶段触发该方法
            if (player === currentPlayer) this.sendPlayerActivableCards(RoundStep.NORMAL, player)
        }
        // 玩家卡牌变化时,刷新所有与玩家有关联属性能力的玩家属性
        this.refreshRelationPlayerProperty(player, card)
    }

    /**
     * 玩家调整内政行动点数
     * @param player
     * @param num
     */
    fun playerAddCivilAction(player: TTAPlayer, num: Int, report: Boolean = true) {
        if (num != 0) {
            player.addCivilAction(num)
            this.sendPlayerCardToken(player, player.government!!, null)
            if (report) this.report.playerAddCivilAction(player, num)
        }
    }

    private inline fun diff(func: () -> Int, block: () -> Unit): Int {
        val orig = func()
        block()
        val new = func()
        return new - orig
    }

    /**
     * 玩家文明点数调整
     * @param player
     * @param num
     * @return 返回调整后与调整前的文明点数差值
     */
    fun playerAddCulturePoint(player: TTAPlayer, num: Int): Int {
        val res = diff(player::culturePoint) { player.addCulturePoint(num) }
        if (res != 0) this.sendPlayerCivilizationInfo(player, null)
        return res
    }

    /**
     * 玩家添加事件牌
     * @param player
     * @param card   添加的事件牌
     * @return 返回触发的事件牌
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerAddEvent(player: TTAPlayer, card: MilitaryCard): MilitaryCard {
        // 将事件和殖民地牌埋入未来事件牌堆,玩家得到事件牌对应的分数,
        // 并翻开当前事件牌堆的第一张牌,处理该事件
        val res = gameMode.cardBoard.addEvent(player, card)
        val currCard = res.card
        this.playerRemoveHand(player, card)
        val culturePoint = card.level + player.abilityManager.getAbilitiesByType(CivilAbilityType.ADJUST_EVENT_POINT).filter { it test card }.sumBy(CivilCardAbility::amount)
        this.playerAddCulturePoint(player, culturePoint)
        this.report.playerAddCulturePoint(player, culturePoint)
        this.report.playerAddEvent(player, card, currCard)

        // 刷新事件牌堆
        this.sendBaseInfo(null)
        this.sendAddEventResponse(player, card, false, null)
        this.sendRemoveEventResponse(res.player, currCard, res.futureToCurrent, null)
        this.sendAddPastEventResponse(res.card, null)
        return currCard
    }

    /**
     * 玩家食物调整
     * @param player
     * @param num
     * @return 返回调整后与调整前的食物差值
     */
    fun playerAddFood(player: TTAPlayer, num: Int) = when (num) {
        0 -> 0
        else -> {
            val info = player.addFood(num)
            this.sendPlayerCardToken(player, info.keys, null)
            this.sendPlayerBoardTokens(player, null)
            info.totalValue
        }
    }

    /**
     * 玩家得到手牌
     * @param player
     * @param cards
     */
    fun playerAddHand(player: TTAPlayer, cards: List<TTACard>) {
        player.addHand(cards)
        this.sendPlayerAddHandResponse(player, cards, null)
    }

    /**
     * 玩家得到手牌
     * @param player
     * @param card
     */
    fun playerAddHand(player: TTAPlayer, card: TTACard) {
        this.playerAddHand(player, listOf(card))
    }

    fun playerAdjustCivilAction(player: TTAPlayer, num: Int) {
        if (num < 0) {
            this.playerAddCivilAction(player, num)
            this.report.printCache(player)
            this.sendAlert(player, "你失去" + -num + "个内政行动点")
        }
    }

    /**
     * 玩家调整军事行动点数
     * @param player
     * @param num
     */
    fun playerAddMilitaryAction(player: TTAPlayer, num: Int, report: Boolean = false) {
        if (num != 0) {
            player.addMilitaryAction(num)
            this.sendPlayerCardToken(player, player.government!!, null)
            if (report) this.report.playerAddMilitaryAction(player, num)
        }
    }

    /**
     * 玩家按照property中的资源/食物/文明点数/科技点数调整对应的数值
     * @param player
     * @param property
     * @return 返回调整后与调整前的属性差额
     */

    fun playerAddPoint(player: TTAPlayer, property: TTAProperty): TTAProperty {
        val res = this.playerAddPoint(player, property, 1)
        this.report.playerAddPoint(player, res)
        return res
    }

    /**
     * 玩家按照property中的资源/食物/文明点数/科技点数调整对应的数值
     * @param player
     * @param property
     * @param multi    倍数
     * @return 返回调整后与调整前的属性差额
     */

    fun playerAddPoint(player: TTAPlayer, property: TTAProperty, multi: Int): TTAProperty {
        return listOf(CivilizationProperty.SCIENCE to this::playerAddSciencePoint, CivilizationProperty.CULTURE to this::playerAddCulturePoint, CivilizationProperty.FOOD to this::playerAddFood, CivilizationProperty.RESOURCE to this::playerAddResource).fold(TTAProperty()) { res, (p, f) ->
            val num = f(player, property.getProperty(p) * multi)
            if (num != 0) res.setProperty(p, num)
            res
        }
    }

    /**
     * 玩家资源调整
     * @param player
     * @param num
     * @return 返回调整后与调整前的资源差值
     */
    fun playerAddResource(player: TTAPlayer, num: Int) = when (num) {
        0 -> 0
        else -> {
            val info = player.addResource(num)
            this.sendPlayerCardToken(player, info.keys, null)
            this.sendPlayerBoardTokens(player, null)
            info.totalValue
        }
    }

    /**
     * 玩家科技点数调整
     * @param player
     * @param num
     * @return 调整后与调整前的科技差值
     */
    fun playerAddSciencePoint(player: TTAPlayer, num: Int): Int {
        val res = diff(player::sciencePoint) { player.addSciencePoint(num) }
        if (res != 0) this.sendPlayerCivilizationInfo(player, null)
        return res
    }

    /**
     * 玩家按照property中的黄色标志物/蓝色标志物调整对应的数值
     * @param player
     * @param property
     * @param multi    倍数
     * @return 返回实际调整的数量
     */
    fun playerAddToken(player: TTAPlayer, property: TTAProperty, multi: Int = 1): TTAProperty {
        val res = listOf(
                CivilizationProperty.YELLOW_TOKEN to player::addAvailableWorker,
                CivilizationProperty.BLUE_TOKEN to player.tokenPool::addAvailableBlues
        ).fold(TTAProperty()) { res, (p, f) ->
            val num = f(property.getProperty(p) * multi)
            if (num != 0) res.setProperty(p, num)
            res
        }
        this.report.playerAddToken(player, res)
        // 向所有玩家发送玩家的标志物信息
        this.sendPlayerBoardTokens(player, null)
        return res
    }

    /**
     * 玩家将一张牌埋在目标牌下
     * @param player
     * @param card
     * @param destCard 目标牌
     */
    fun playerAttachCard(player: TTAPlayer, card: TTACard, destCard: TTACard) {
        if (destCard.attachedCards == null) destCard.attachedCards = ArrayList()
        destCard.attachedCards!!.add(card)
        this.gameMode.cardBoard.attachedCards[card] = destCard
        // 检测所有目标卡的玩家,刷新属性
        gameMode.realPlayers.filter { it.allPlayedCard.contains(destCard) }.forEach(this::playerRefreshProperty)
        this.sendPlayerAttachCardResponse(player, card, destCard, null)
        this.report.playerAttachCard(player, card, destCard)
    }

    /**
     * 玩家建造建筑,部队
     * @param player
     * @param card
     */
    fun playerBuild(player: TTAPlayer, card: CivilCard) {
        player.build(card)
        this.playerRefreshProperty(player)
        this.sendPlayerCardToken(player, card, null)
    }

    /**
     * 检查城市建筑限制
     * @param player
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerBuildLimitCheck(player: TTAPlayer, card: CivilCard) {
        if (card.cardType == CardType.BUILDING) {
            var limit = player.buildLimit
            if (card.cardSubType in arrayOf(CardSubType.LAB, CardSubType.LIBRARY) && gameMode.inquisitionPosition != -1) limit -= 1
            CheckUtils.check(player.getBuildingNumber(card.cardSubType) >= limit, "你现有的政府不能再建造更多这样的建筑了!")
        }
    }

    /**
     * 玩家建造奇迹
     * @param player
     * @param step
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerBuildWonder(player: TTAPlayer, step: Int) {
        val card = player.uncompletedWonder ?: throw BoardGameException("没有正在建造的奇迹")
        player.buildWonder(step)
        this.sendPlayerBoardTokens(player, null)
        this.sendPlayerCardToken(player, card, null)
    }

    /**
     * 玩家完成奇迹
     * @param player
     */
    @Throws(BoardGameException::class)
    fun playerCompleteWonder(player: TTAPlayer) {
        val card = player.uncompletedWonder ?: throw BoardGameException("没有正在建造的奇迹")
        val result = player.completeWonder()

        // 如果奇迹已经建造完成,则发送玩家得到奇迹的消息
        this.sendPlayerWonderCompleteResponse(player)
        this.playerAddToken(player, result)
        this.playerAddAction(player, result)
        // 检查完成的奇迹是否会立即带来文明点数,如果有则直接加给玩家
        val cp = player.getScoreCulturePoint(card.scoreAbilities)
        this.playerAddCulturePoint(player, cp)
        this.report.playerAddCulturePoint(player, cp)
        // 检查该奇迹是否拥有可以使用的能力,如果有则刷新玩家的可使用能力列表
        card.activeAbility?.let {
            when (it.abilityType) {
                ActiveAbilityType.PA_MANHATTAN ->
                    // 曼哈顿移除所有被宣言的战争
                    player.getPlayedCard(it).filterIsInstance(AttackCard::class.java).filter { it.target === player }.forEach(this::removeOvertimeCard)
                ActiveAbilityType.PA_NARODNI -> {
                    // 国家图书馆消耗所有内政行动点,每个获得5文化分
                    val actionPoints = player.getAvailableActionPoint(ActionType.CIVIL)
                    this.report.playerAddCulturePoint(player, actionPoints * 5)
                    this.playerAddCulturePoint(player, actionPoints * 5)
                    this.playerAddCivilAction(player, -actionPoints)
                }
                else ->
                    // 暂时只可能在NORMAL阶段建造奇迹
                    if (player === currentPlayer) this.sendPlayerActivableCards(RoundStep.NORMAL, player)
            }
        }
        if (card.abilities.any { it.abilityType == CivilAbilityType.PA_RED_CROSS }) {
            for (p in gameMode.realPlayers.filter { it !== player }) {
                val c = p.allPlayedCard.single { it.activeAbility?.abilityType == ActiveAbilityType.PA_RED_CROSS }
                this.playerRemoveCardDirect(p, c)
            }
        }
        this.sendPlayerCardToken(player, card, null)
        this.playerRefreshProperty(player)
        // 建造完成时,刷新所有与玩家有关联属性能力的玩家属性
        this.refreshRelationPlayerProperty(player, card)
    }

    /**
     * 执行粮食消耗
     * @param player
     */
    private fun playerConsume(player: TTAPlayer) {
        // 检查玩家的粮食供应
        val cp = CostParam()
        val consumption = player.consumption
        val food = player.totalFood
        cp.cost.addProperty(CivilizationProperty.FOOD, consumption)
        this.checkTemplateResource(player, null, cp, CivilizationProperty.FOOD)
        if (food < cp.cost.getProperty(CivilizationProperty.FOOD)) {
            // 每缺少一个食物扣4点文明点
            val point = 4 * (cp.cost.getProperty(CivilizationProperty.FOOD) - food)
            cp.cost.addProperty(CivilizationProperty.CULTURE, point)
        }
        // 扣除粮食
        this.playerAddPoint(player, cp.cost, -1)
        this.report.playerCostPoint(player, cp.cost, "消费")
        this.executeTemplateResource(player, cp)
    }

    /**
     * 执行腐败
     * @param player
     */
    private fun playerCorrupt(player: TTAPlayer) {
        val cp = CostParam()
        var corruption = TTAConstManager.getResourceCorruption(player.tokenPool.availableBlues, isVersion2)
        val totalResource = player.totalResource
        if (this.isVersion2 && corruption > totalResource) {
            // 新版资源不够支付时扣食物,扣完为止
            val foodCost = corruption - totalResource
            corruption = totalResource
            cp.cost.addProperty(CivilizationProperty.FOOD, foodCost)
        }
        cp.cost.addProperty(CivilizationProperty.RESOURCE, corruption)
        this.checkTemplateResource(player, null, cp, CivilizationProperty.RESOURCE)
        this.playerAddPoint(player, cp.cost, -1)
        this.report.playerCostPoint(player, cp.cost, "腐败")
        this.executeTemplateResource(player, cp)
    }

    /**
     * 玩家减少人口(减少的是空闲人口)
     * @param player
     * @param num
     */
    fun playerDecreasePopulation(player: TTAPlayer, num: Int) {
        val res = player.decreasePopulation(num)
        if (res != 0) {
            this.sendPlayerBoardTokens(player, null)
            this.playerRefreshProperty(player)
        }
    }

    /**
     * 玩家减少人口(拆除card并减少人口)
     * @param player
     * @param detail
     */
    fun playerDecreasePopulation(player: TTAPlayer, detail: Map<TechCard, Int>) {
        val res = this.playerDestroy(player, detail)
        this.playerDecreasePopulation(player, res)
    }

    /**
     * 玩家减少人口(拆除card并减少人口)
     * @param player
     * @param card
     * @param num
     */
    fun playerDecreasePopulation(player: TTAPlayer, card: TechCard, num: Int) {
        val res = this.playerDestroy(player, card, num)
        this.playerDecreasePopulation(player, res)
    }

    /**
     * 玩家摧毁建筑,部队
     * @param player
     * @param detail
     */
    fun playerDestroy(player: TTAPlayer, detail: Map<TechCard, Int>): Int {
        val res = detail.entries.sumBy { (k, v) -> player.destroy(k, v) }
        if (res != 0) {
            this.playerRefreshProperty(player)
            this.sendPlayerCardToken(player, detail.keys, null)
        }
        return res
    }

    /**
     * 玩家摧毁建筑,部队
     * @param player
     * @param card
     */
    fun playerDestroy(player: TTAPlayer, card: TechCard, num: Int): Int {
        val res = player.destroy(card, num)
        if (res != 0) {
            this.playerRefreshProperty(player)
            this.sendPlayerCardToken(player, card, null)
        }
        return res
    }

    /**
     * 玩家将牌放入弃牌堆
     * @param player
     * @param cards
     */
    fun playerDiscardHand(player: TTAPlayer, cards: List<TTACard>) {
        // 只有军事牌会被放入弃牌堆
        if (cards.isNotEmpty()) {
            this.playerRemoveHand(player, cards)
            this.gameMode.cardBoard.discardCards(cards)
            this.report.playerDiscardMilitaryHand(player, cards)
            player.abilityManager.getAbility(CivilAbilityType.PA_SCORE_DISCARD)?.let { ability ->
                val num = cards.count { ability test it }
                this.playerAddPoint(player, ability.property, num)
            }
        }
    }

    /**
     * 玩家摸军事牌
     * @param player
     */
    fun playerDrawMilitaryCard(player: TTAPlayer) {
        if (this.config.isRevoltDraw || !this.checkPlayerUprising(player)) {
            // 玩家摸当前军事行动点数的军事牌
            // 最多只能摸3张军事牌
            var num = min(player.getAvailableActionPoint(ActionType.MILITARY), player.militaryDraw)
            if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_SUNTZU_ABILITY)) num = 3
            num += gameMode.getPlayerAbilities(CivilAbilityType.PA_MILITARY_DRAW_GLOBAL).filter { (k, v) -> v !== player || k.effectSelf }.keys.sumBy(CivilCardAbility::amount)
            if (player.abilityManager.hasAbilitiy(CivilAbilityType.DOUBLE_DRAW)) {
                num *= 2
            }
            this.playerDrawMilitaryCard(player, num)
            this.report.printCache(player)
        } else {
            this.report.playerCannotDrawWarning(player)
        }
    }

    /**
     * 玩家摸军事牌
     * @param player
     * @param num
     */
    fun playerDrawMilitaryCard(player: TTAPlayer, num: Int) {
        if (num > 0) {
            val cards = this.gameMode.cardBoard.drawMilitaryCard(num)
            if (cards.isNotEmpty()) {
                this.playerAddHand(player, cards)
                this.report.playerDrawMilitary(player, cards)
            }
        }
    }

    /**
     * 检查是否有人口建造
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerFreeWorkerCheck(player: TTAPlayer) {
        CheckUtils.check(player.tokenPool.unusedWorkers <= 0, "你没有空闲的人口!")
    }

    /**
     * 玩家得到奇迹
     * @param player
     * @param card
     */
    fun playerGetWonder(player: TTAPlayer, card: WonderCard) {
        player.uncompletedWonder = card
        this.sendPlayerGetWonderResponse(player, card, null)
    }

    /**
     * 玩家扩张人口
     * @param player
     * @param num
     */
    fun playerIncreasePopulation(player: TTAPlayer, num: Int) {
        val res = player.increasePopulation(num)
        if (res != 0) {
            this.sendPlayerBoardTokens(player, null)
            for (ca in player.abilityManager.getAbilitiesByType(CivilAbilityType.SOCRE_INCREASE_POP)) {
                this.playerAddPoint(player, ca.property, res)
            }
            this.playerRefreshProperty(player)
        }
    }

    fun playerPlayBonusCard(player: TTAPlayer, bonusCards: List<TTACard>) {
        if (bonusCards.isNotEmpty()) {
            this.playerRemoveHand(player, bonusCards)
            this.tryDiscardCards(bonusCards)
            this.report.playerBonusCardPlayed(player, bonusCards)
        }
    }

    /**
     * 玩家生产粮矿,按顺序执行
     * @param player
     * @param produceFood
     * @param doConsumption
     * @param produceResouce
     * @param doCorruption
     */
    fun playerProduce(player: TTAPlayer, produceFood: Boolean, doConsumption: Boolean, produceResouce: Boolean, doCorruption: Boolean, doExtraProduce: Boolean) {
        if (this.isVersion2) {
            // 新版，先腐败再生产
            if (doCorruption) this.playerCorrupt(player)
            if (produceFood) this.playerProduceFood(player, doExtraProduce)
            if (doConsumption) this.playerConsume(player)
            if (produceResouce) this.playerProduceResource(player, doExtraProduce)
        } else {
            // 原版，先生产再腐败
            if (produceFood) this.playerProduceFood(player, doExtraProduce)
            if (doConsumption) this.playerConsume(player)
            if (produceResouce) this.playerProduceResource(player, doExtraProduce)
            if (doCorruption) this.playerCorrupt(player)
        }

        // 刷新玩家文明的信息
        this.sendPlayerCivilizationInfo(player, null)
    }

    /**
     * 玩家进行生产的详情
     * @param player
     * @param map
     * @return
     */
    private fun playerProduceDetail(player: TTAPlayer, map: Map<CivilCard, Int>, property: CivilizationProperty): ProductionInfo {
        val res = ProductionInfo(property)
        map.asSequence().sortedByDescending { it.key.property.getProperty(property) }.map { (k, v) -> k to k.addBlues(-player.tokenPool.addAvailableBlues(-v)) }.toMap(res)
        return res
    }

    /**
     * 玩家生产粮食
     * @param player
     * @param doExtraProduce
     */
    private fun playerProduceFood(player: TTAPlayer, doExtraProduce: Boolean) {
        // 农田生产
        val res = this.playerProduceDetail(player, player.productionFromFarm, CivilizationProperty.FOOD)
        // 检查玩家生产食物的特殊能力
        if (doExtraProduce) {
            val extra = player.properties.getProperty(CivilizationProperty.EXTRA_FOOD)
            if (extra != 0) res += player.addFood(extra)
        }
        this.sendPlayerCardToken(player, res.keys, null)
        this.sendPlayerBoardTokens(player, null)
        // 最终生产报告
        this.report.playerProduceFood(player, res.totalValue)
    }

    /**
     * 玩家生产资源
     * @param player
     * @param doExtraProduce
     */
    private fun playerProduceResource(player: TTAPlayer, doExtraProduce: Boolean) {
        // 矿山生产
        val res = this.playerProduceDetail(player, player.productionFromMine, CivilizationProperty.RESOURCE)
        // 特斯拉产矿
        if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_TESLA_ABILITY)) {
            res += this.playerProduceDetail(player, player.productionFromLab, CivilizationProperty.RESOURCE)
        }
        // 检查玩家生产资源的特殊能力
        if (doExtraProduce) {
            val extra = player.properties.getProperty(CivilizationProperty.EXTRA_RESOURCE)
            if (extra != 0) res += player.addResource(extra)
        }
        this.sendPlayerCardToken(player, res.keys, null)
        this.sendPlayerBoardTokens(player, null)
        this.report.playerProduceResource(player, res.totalValue)
    }

    fun playerReattachCard(player: TTAPlayer, newCard: TTACard, oldCard: TTACard) {
        oldCard.attachedCards?.forEach { c -> this.playerAttachCard(player, c, newCard) }
        oldCard.attachedCards = null
    }

    /**
     * 刷新玩家信息
     * @param player
     */
    fun playerRefreshProperty(player: TTAPlayer) {
        player.refreshProperties()
        this.sendPlayerCivilizationInfo(player, null)
    }

    /**
     * 玩家移除卡牌列上的卡牌(法雅节)
     * @param player
     * @param card
     * @param index
     */
    fun playerRemoveBoardCard(player: TTAPlayer, card: TTACard, index: Int) {
        this.gameMode.cardBoard.takeCard(index)
        this.report.playerTakeCardCache(player, card, index)
        this.sendCardRowRemoveCardResponse(card.id)
    }

    fun playerRemoveCard(player: TTAPlayer, cardId: String) {
        player.allPlayedCard.filter { it.id == cardId }.forEach { this.playerRemoveCard(player, it) }
    }

    fun playerRemoveCard(player: TTAPlayer, card: TTACard) {
        this.playerRemoveCardDirect(player, card)
        this.report.playerRemoveCard(player, card)
    }

    /**
     * 玩家失去已打出的牌
     * @param player
     * @param card
     */
    fun playerRemoveCardDirect(player: TTAPlayer, card: TTACard) {
        val result = player.removeCard(card)
        this.sendPlayerRemoveCardResponse(player, card, null)
        this.playerAddToken(player, result)
        this.playerAddAction(player, result)
        this.playerRefreshProperty(player)
        // 玩家卡牌变化时,刷新所有与玩家有关联属性能力的玩家属性
        this.refreshRelationPlayerProperty(player, card)
    }

    /**
     * 玩家失去手牌
     * @param player
     * @param cards
     */
    fun playerRemoveHand(player: TTAPlayer, cards: List<TTACard>) {
        player.removeHand(cards)
        this.sendPlayerRemoveHandResponse(player, cards, null)
    }

    /**
     * 玩家失去手牌
     * @param player
     * @param card
     */
    fun playerRemoveHand(player: TTAPlayer, card: TTACard) {
        this.playerRemoveHand(player, listOf(card))
    }

    /**
     * 玩家失去未建成的奇迹
     * @param player
     */
    fun playerRemoveUncompleteWonder(player: TTAPlayer) {
        val card = player.uncompletedWonder ?: return
        // 如果该奇迹上有蓝色标志物,则需要回到资源库
        val blues = card.blues
        if (blues > 0) {
            player.tokenPool.addAvailableBlues(blues)
            this.sendPlayerBoardTokens(player, null)
        }
        player.uncompletedWonder = null
        this.sendPlayerRemoveCardResponse(player, card, null)
        this.report.playerRemoveCard(player, card)
    }

    /**
     * 玩家结束请求,关闭窗口
     * @param player
     */
    fun playerRequestEnd(player: TTAPlayer) {
        this.sendPlayerRequestEndResponse(player)
    }

    /**
     * 玩家研发科技时支付科技点,会消耗临时科技,执行科技协作等能力
     * @param player
     * @param cost
     */
    fun playerResearchCost(player: TTAPlayer, cost: TTAProperty) {
        // 自身消耗科技点
        this.playerAddPoint(player, cost, -1)
        this.report.playerCostPoint(player, cost, "花费")
        // 发动科技协作能力
        val pacts = player.abilityManager.getPactAbilitiesWithRelation(CivilAbilityType.PA_SCIENCE_ASSIST)
        for ((ca, p) in pacts) {
            val alian = p.alian ?: continue
            val num = this.playerAddSciencePoint(alian, -ca.amount)
            this.report.playerAddSciencePoint(alian, num)
            this.report.printCache(alian)
        }
    }

    /**
     * 玩家重置行动点数
     * @param player
     */
    fun playerResetActionPoint(player: TTAPlayer) {
        player.resetActionPoint()
        this.sendPlayerCardToken(player, player.government!!, null)
    }

    /**
     * 玩家体面退出游戏
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun playerResign(player: TTAPlayer) {
        CheckUtils.check(gameMode.finalRound && this.isVersion2, "新版最后一轮不能体退!")
        if (this.isTeamMatch || this.isTichuMode) {
            for (p in this.players) {
                if (p.position != player.position && this.isTeammates(p, player)) {
                    val listener = TTAConfirmListener(gameMode, p, "你的队友决定体面退出游戏,你同意吗?")
                    val res = gameMode.insertListener(listener)
                    val confirm = res.getBoolean(p.position)
                    CheckUtils.check(!confirm, "你的队友不同意体面退出游戏!")
                    this.forceResign(p)
                }
            }
            this.report.playerResign(player)
            this.forceResign(player)
        } else {
            player.allPlayedCard.filterIsInstance<AttackCard>().mapNotNull(AttackCard::owner).forEach { p ->
                // 新版体退会给战争的对手加7分
                val point = TTAConstManager.getResignWarCulture(this.isVersion2)
                if (point != 0) {
                    this.playerAddCulturePoint(p, point)
                    this.report.playerAddCulturePoint(p, point)
                    this.report.printCache(p)
                }
            }
            player.allPlayedCard.filterIsInstance<IOvertimeCard>().forEach(this::removeOvertimeCard)
            this.playerRemoveHand(player, player.allHands)
            this.tryDiscardCards(player.allHands)
            this.report.playerResign(player)
            this.forceResign(player)
        }
        if (this.realPlayerNumber <= this.endGamePlayerNumber) this.winGame()
        gameMode.doregroup = false
    }

    /**
     * 玩家回合结束时生产粮食,资源,文明和科技点数
     * @param player
     * @throws BoardGameException
     */
    fun playerRoundScore(player: TTAPlayer) {
        if (!this.checkPlayerUprising(player)) {
            // 生产粮矿、文化和科技
            this.playerRefreshProperty(player)
            this.playerProduce(player, true, true, true, true, true)
            this.playerScoreScience(player)
            this.playerScoreCulture(player)
            this.report.playerRoundScore(player)
        } else {
            // 如果产生暴动,则什么都不会生产
            this.report.playerUprisingWarning(player)
            if (player.abilityManager.hasAbilitiy(CivilAbilityType.PRODUCE_ON_UPRISING)) {
                this.playerProduceResource(player, false)
                this.report.playerActiveCard(player, CivilAbilityType.PRODUCE_ON_UPRISING)
            }
        }
    }

    /**
     * 玩家牺牲部队
     * @param player
     * @param units
     * @throws BoardGameException
     */
    fun playerSacrificeUnit(player: TTAPlayer, units: Map<TechCard, Int>) {
        this.playerDecreasePopulation(player, units)
        this.report.playerSacrificeUnit(player, units)
    }

    /**
     * 玩家生产文明点数
     * @param player
     */
    fun playerScoreCulture(player: TTAPlayer) {
        val res = this.playerAddCulturePoint(player, player.scoreCulturePoint())
        this.report.playerScoreCulturePoint(player, res)
    }

    /**
     * 玩家生产科技点数
     * @param player
     */
    fun playerScoreScience(player: TTAPlayer) {
        val num = this.playerAddSciencePoint(player, player.scoreSciencePoint())
        this.report.playerScoreSciencePoint(player, num)
    }

    /**
     * 从卡排列拿牌
     */
    fun playerTakeCard(player: TTAPlayer, card: TTACard, index: Int) {
        gameMode.cardBoard.takeCard(index)
        when (card.cardType) {
            CardType.WONDER -> // 如果是奇迹牌则直接打出
                this.playerGetWonder(player, card as WonderCard)
            CardType.LEADER -> {
                this.playerAddHand(player, card)
                // 如果拿的是领袖,则设置玩家的领袖参数
                player.setHasLeader(card.level)
            }
            else -> // 否则加入手牌
                this.playerAddHand(player, card)
        }
        this.sendCardRowRemoveCardResponse(card.id)
    }

    /**
     * 玩家升级建筑,部队
     * @param player
     * @param fromCard
     * @param toCard
     */
    fun playerUpgrade(player: TTAPlayer, fromCard: CivilCard, toCard: CivilCard) {
        player.upgrade(fromCard, toCard)
        this.playerRefreshProperty(player)
        this.sendPlayerCardToken(player, listOf(fromCard, toCard), null)
    }

    /**
     * 玩家对目标玩家使用卡牌
     * @param player
     * @param target
     * @param card
     */
    fun playerUseCardOnPlayer(player: TTAPlayer, target: TTAPlayer, card: TTACard) {
        if (card is IOvertimeCard) {
            card.owner = player
            card.target = target
            // 将玩家间持续效果卡牌的信息发送到客户端
            this.sendOvertimeCardInfoResponse(card)
            // 玩家和目标玩家得到卡牌
            this.playerAddCardDirect(player, card)
            this.report.playerAddCard(player, card)
            this.playerAddCardDirect(target, card)
            this.report.playerAddCard(target, card)
        }
    }

    /**
     * @param player
     * @param num
     * @return
     */
    fun refreshDecreasePopulation(player: TTAPlayer, num: Int) {
        TTACmdFactory.createPlayerBoardTokensResponse(player).public(Token.UNUSED_WORKER.toString(), player.tokenPool.unusedWorkers - num).send(this, player)
    }

    /**
     * @param player
     * @param card
     * @param num
     * @return
     */
    fun refreshDecreasePopulation(player: TTAPlayer, card: TechCard, num: Int) {
        this.refreshDecreasePopulation(player, card, num, player)
    }

    /**
     * @param player
     * @param card
     * @param num
     * @param receiver
     * @return
     */
    fun refreshDecreasePopulation(player: TTAPlayer, card: TechCard, num: Int, receiver: TTAPlayer?) {
        val newCard = card.clone()
        newCard.addWorkers(-num)
        this.sendPlayerCardToken(player, listOf(newCard), receiver)

    }

    /**
     * 刷新与玩家有关联属性玩家的属性
     * @param player
     * @param card
     */
    fun refreshRelationPlayerProperty(player: TTAPlayer, card: TTACard) {
        for (p in gameMode.realPlayers) {
            val pacts = p.abilityManager.getPactAbilitiesWithRelation(CivilAbilityType.ADJUST_PROPERTY_BY_ALIAN)
            if (pacts.any { (ca, pact) -> ca.test(card) && pact.alian === player }) {
                this.playerRefreshProperty(p)
            }
        }
    }

    /**
     * 卡牌序列补牌
     */
    fun regroupCardRow(doDiscard: Boolean): Boolean {
        val oldAge = gameMode.currentAge
        gameMode.cardBoard.regroupCardRow(doDiscard)
        this.sendCardRowInfo(null)
        this.sendCardRowReport()
        val newAge = gameMode.currentAge
        return newAge > oldAge
    }

    /**
     * 移除持续效果的卡牌
     * @param card
     */
    fun removeOvertimeCard(card: IOvertimeCard) {
        val cardId = (card as Card).id
        val owner = card.owner ?: return
        val target = card.target ?: return
        this.playerRemoveCard(owner, cardId)
        this.playerRemoveCard(target, cardId)
    }

    /**
     * 移除条约
     * @param targetPlayer
     * @param player
     * @param cardId
     */
    fun removePactCard(player: TTAPlayer, targetPlayer: TTAPlayer, cardId: String) {
        // 因为每个玩家的条约牌都是副本,所以需要找到该条约牌副本对象
        player.allPlayedCard.filter { it.id == cardId }.forEach { this.playerRemoveCard(player, it) }
        targetPlayer.allPlayedCard.filter { it.id == cardId }.forEach { this.playerRemoveCard(targetPlayer, it) }
    }

    /**
     * 移除所有玩家过时的卡牌
     */
    fun removePastCards() {
        if (this.gameMode.currentAge > 0) {
            if (config.isGlobalConflict) {
                val minMilitary = gameMode.realPlayers.map(TTAPlayer::defenceMilitary).min() ?: 0
                gameMode.realPlayers.forEach {
                    val num = it.defenceMilitary - minMilitary
                    this.playerAddCulturePoint(it, num)
                    this.report.playerAddCulturePoint(it, num)
                }
            }
            // 所有玩家需要弃掉前一时代的领袖,未建成奇迹,条约,和手牌
            for (player in gameMode.realPlayers) {
                if (checkPastCard(player.leader)) {
                    val leader = player.leader!!
                    this.playerRemoveCardDirect(player, leader)
                    this.report.playerRemoveCard(player, leader)
                }
                if (checkPastCard(player.uncompletedWonder)) {
                    this.playerRemoveUncompleteWonder(player)
                }
                if (checkPastCard(player.pact)) {
                    this.removeOvertimeCard(player.pact!!)
                }
                // 时代变迁时需要移除2个黄色标记
                val darkAgeToken = TTAConstManager.darkAgeToken.clone()
                if (player.government!!.workers > 0) {
                    player.government!!.addWorkers(-1)
                    darkAgeToken.addProperty(CivilizationProperty.YELLOW_TOKEN, 1)
                }
                this.playerAddToken(player, darkAgeToken)
                this.report.printCache(player)

                // 移除手牌列表
                val discards = player.allHands.filter(this::checkPastCard)
                this.playerRemoveHand(player, discards)
                this.report.playerRemoveHand(player, discards)

                // 刷新玩家文明的属性
                this.sendPlayerCivilizationInfo(player, null)
            }
        }
    }

    /**
     * 发送添加事件牌的信息
     * @param player
     * @param receiver
     */
    fun sendAddEventResponse(player: TTAPlayer?, card: TTACard, futureToCurrent: Boolean, receiver: TTAPlayer?) {
        val isLazyMemory = config.isLazyMemory
        val position = if (isLazyMemory && player != null) player.position else -1
        CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_ADD_EVENT, position).private("cardId", card.id).public("cardLevel", card.level).public("futureToCurrent", futureToCurrent).apply { (if (isLazyMemory) ::public else ::private).call("player", player?.position) }.send(this, receiver)
    }

    /**
     * 添加过去事件
     * @param card
     */
    fun sendAddPastEventResponse(card: TTACard, receiver: TTAPlayer?) {
        this.sendAddPastEventsResponse(listOf(card), receiver)
    }

    /**
     * 添加过去事件
     */
    fun sendAddPastEventsResponse(cards: Collection<TTACard>, receiver: TTAPlayer?) {
        CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_ADD_EVENT, -1).public("past", true).public("cardIds", BgUtils.card2String(cards)).send(this, receiver)
    }

    /**
     * 向目标玩家发送所有持续效果卡牌的信息
     * @param receiver
     */
    fun sendAllOvertimeCardsInfo(receiver: TTAPlayer?) {
        val cards = gameMode.realPlayers.flatMap { listOfNotNull<TTACard>(it.war, it.pact) }
        this.sendOvertimeCardsInfoResponse(cards, receiver)
    }

    /**
     * 发送游戏基本信息
     * @param receiver
     */
    fun sendBaseInfo(receiver: TTAPlayer?) {
        val cardBoard = this.gameMode.cardBoard
        CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_BASE_INFO, -1)
                // 设置基本信息
                .public("isVersion2", this.isVersion2).public("infoType", "base").public(TTACmdString.CURRENT_AGE, this.gameMode.currentAge).public(TTACmdString.CIVIL_REMAIN, cardBoard.civilRemain).public(TTACmdString.MILITARY_REMAIN, cardBoard.militaryRemain)
                // 设置最近一个事件的信息
                .public("lastEventCardId", cardBoard.lastEvent?.id)
                // 设置未来事件的信息
                .public("nextFutureEventLevel", cardBoard.nextFutureEventCard?.level).public("futureDeckNum", cardBoard.futureEvents.size.takeIf { it > 0 })
                // 设置当前事件的信息
                .public("nextCurrentEventLevel", cardBoard.nextCurrentEventCard?.level).public("currentDeckNum", cardBoard.currentEvents.size.takeIf { it > 0 })
                // 设置新版可学阵型的信息
                .public("tactics", BgUtils.card2String(cardBoard.publicTacticsDeck.cards).takeIf(String::isNotEmpty)).send(this, receiver)
    }

    /**
     * 向玩家发送奖励牌堆的信息
     * @param receiver
     */
    fun sendBonusCard(receiver: TTAPlayer?) {
        TTACmdFactory.createBonusCardResponse(this.gameMode.bonusCards).send(this, receiver)
    }

    /**
     * 发送文明牌序列的信息
     * @param receiver
     */
    fun sendCardRowInfo(receiver: TTAPlayer?) {
        CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_CARD_ROW, -1).public("cardIds", this.gameMode.cardBoard.cardRowIds).send(this, receiver)
    }

    /**
     * 发送卡牌序列失去卡牌的消息
     */
    fun sendCardRowRemoveCardResponse(cardId: String) {
        this.sendResponse(TTACmdFactory.createCardRowRemoveCardResponse(cardId))
    }

    /**
     * 发送卡牌列信息
     */
    fun sendCardRowReport() {
        this.report.refreshCardRow(gameMode.cardBoard.getCardRow())
    }

    /**
     * 发送腐败信息
     * @param player
     * @param property
     */
    private fun sendCorruptionAlert(player: TTAPlayer, property: TTAProperty) {
        val sb = StringBuilder("你因为腐败")
        var flag = false
        var p = property.getProperty(CivilizationProperty.FOOD)
        if (p != 0) {
            flag = true
            sb.append(",失去").append(-p).append("食物")
        }
        p = property.getProperty(CivilizationProperty.RESOURCE)
        if (p != 0) {
            flag = true
            sb.append(",失去").append(-p).append("资源")
        }
        if (flag) {
            this.sendAlert(player, sb.toString())
        }
    }

    @Throws(BoardGameException::class)
    override fun sendGameInfo(receiver: TTAPlayer?) {
        // 需要发送以下游戏信息
        // 当前世纪
        // 当前文明牌剩余数量
        // 未来事件牌堆数量
        this.sendBaseInfo(receiver)
        // 发送领袖和奇迹信息
        this.sendLeadersAndWondersInfo(receiver)
        // 当前文明牌序列
        this.sendCardRowInfo(receiver)
        // 如果是额外奖励牌模式,则发送奖励牌堆的信息
    }

    @Throws(BoardGameException::class)
    override fun sendInitInfo(receiver: TTAPlayer?) = Unit

    fun setLeadersAndWondersInfo(res: BgResponse): BgResponse {
        return if (!this.config.isHideAvalable) {
            gameMode.cardBoard.leaders.forEach { (i, cs) -> res.public("leaders_$i", BgUtils.card2String(cs)) }
            gameMode.cardBoard.wonders.forEach { (i, cs) -> res.public("wonders_$i", BgUtils.card2String(cs)) }
            res
        } else {
            res
        }
    }

    /**
     * 发送领袖奇迹信息
     * @param receiver
     */
    fun sendLeadersAndWondersInfo(receiver: TTAPlayer?) {
        CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_BASE_INFO, -1)
                // 设置领袖奇迹显示板
                .public("infoType", "leadersAndWonders").let(this::setLeadersAndWondersInfo).send(this, receiver)
    }

    /**
     * 发送玩家间持续效果卡牌的信息
     */
    fun sendOvertimeCardInfoResponse(card: TTACard) {
        this.sendOvertimeCardsInfoResponse(listOf(card), null)
    }

    /**
     * 发送玩家间持续效果卡牌的信息
     * @param cards
     */
    fun sendOvertimeCardsInfoResponse(cards: List<TTACard>, receiver: TTAPlayer?) {
        TTACmdFactory.createOvertimeCardsInfoResponse(cards).send(this, receiver)
    }

    /**
     * 发送玩家请求行动的信息,card为使用的卡牌
     * @param player
     * @param cmdString 请求的命令字符串
     * @param msg       显示的信息
     * @param showCard  展示的card
     * @param param     其他参数
     */
    fun sendPlayerActionRequestResponse(player: TTAPlayer, cmdString: String, msg: String, showCard: TTACard?, param: Map<String, Any>) {
        // 该请求只需向自己发送
        TTACmdFactory.createPlayerActionRequestResponse(player, cmdString, msg, showCard, param).send(this, player)
    }

    /**
     * 向玩家发送他可激活卡牌的列表
     * @param activeStep
     * @param player
     */
    fun sendPlayerActivableCards(activeStep: RoundStep, player: TTAPlayer) {
        TTACmdFactory.createPlayerActivableCardsResponse(activeStep, player).send(this, player)
    }

    /**
     * 发送玩家添加打出卡牌的信息
     * @param player
     * @param receiver
     */
    fun sendPlayerAddCardResponse(player: TTAPlayer, card: TTACard?, receiver: TTAPlayer?) {
        if (card != null) {
            TTACmdFactory.createPlayerAddCardResponse(player, card).send(this, receiver)
        }
    }

    /**
     * 发送玩家添加打出卡牌的信息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerAddCardsResponse(player: TTAPlayer, cards: List<TTACard>, receiver: TTAPlayer?) {
        TTACmdFactory.createPlayerAddCardsResponse(player, cards).send(this, receiver)
    }

    /**
     * 发送玩家得到手牌的消息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerAddHandResponse(player: TTAPlayer, cards: List<TTACard>, receiver: TTAPlayer?) {
        TTACmdFactory.createPlayerAddHandResponse(player, cards, this.config.isLazyMemory).send(this, receiver)
    }

    /**
     * 发送玩家叠放卡牌的信息
     * @param player
     * @param receiver
     */
    fun sendPlayerAttachCardResponse(player: TTAPlayer, card: TTACard?, destCard: TTACard, receiver: TTAPlayer?) {
        if (card != null) {
            TTACmdFactory.createPlayerAttachCardResponse(player, card, destCard).send(this, receiver)
        }
    }

    /**
     * 发送玩家桌面标志物的数量(该方法在发送玩家文明信息时被调用)
     * @param player
     * @param receiver
     */
    fun sendPlayerBoardTokens(player: TTAPlayer, receiver: TTAPlayer?) {
        TTACmdFactory.createPlayerBoardTokensResponse(player).send(this, receiver)
    }

    /**
     * 发送玩家卡牌的标志物信息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerCardToken(player: TTAPlayer, cards: Collection<TTACard>, receiver: TTAPlayer?) {
        TTACmdFactory.createPlayerCardTokenResponse(player, cards).send(this, receiver)
    }

    /**
     * 发送玩家所有卡牌的标志物信息
     * @param player
     * @param receiver
     */
    fun sendPlayerCardToken(player: TTAPlayer, receiver: TTAPlayer?) {
        val cards = player.allPlayedCard + listOfNotNull(player.uncompletedWonder)
        // 发送玩家未建成奇迹的token信息
        this.sendPlayerCardToken(player, cards, receiver)
    }

    /**
     * 发送玩家卡牌的标志物信息
     * @param player
     * @param card
     * @param receiver
     */
    fun sendPlayerCardToken(player: TTAPlayer, card: TTACard, receiver: TTAPlayer?) {
        this.sendPlayerCardToken(player, listOf(card), receiver)
    }

    /**
     * 发送玩家文明的基本属性信息
     * @param player
     * @param receiver
     */
    fun sendPlayerCivilizationInfo(player: TTAPlayer, receiver: TTAPlayer?) {
        TTACmdFactory.createPlayerCivilizationInfoResponse(player).send(this, receiver)
        // 同时更新一下玩家当前政府牌上token的信息
        if (player.government != null) {
            this.sendPlayerCardToken(player, player.government!!, receiver)
        }
        // 以及玩家面板的情况
        this.sendPlayerBoardTokens(player, receiver)
    }

    /**
     * 发送玩家得到奇迹的信息
     * @param player
     * @param card
     * @param receiver
     */
    fun sendPlayerGetWonderResponse(player: TTAPlayer, card: TTACard, receiver: TTAPlayer?) {
        TTACmdFactory.createPlayerGetWonderResponse(player, card).send(this, receiver)
    }

    @Throws(BoardGameException::class)
    override fun sendPlayerPlayingInfo(receiver: TTAPlayer?) {
        // 发送全局的游戏信息
        this.sendAllOvertimeCardsInfo(receiver)
        // 需要发送以下玩家游戏状态
        for (p in this.players) {
            // 玩家所有已打出牌的信息
            val allCards = p.allPlayedCard + listOfNotNull(p.tactics)
            this.sendPlayerAddCardsResponse(p, allCards, receiver)
            if (p.uncompletedWonder != null) {
                this.sendPlayerGetWonderResponse(p, p.uncompletedWonder!!, receiver)
            }
            // 玩家的手牌信息
            this.sendPlayerAddHandResponse(p, p.allHands, receiver)
            // 玩家文明信息
            this.sendPlayerCivilizationInfo(p, receiver)
            // 玩家所有卡牌和台面上标志物的信息
            this.sendPlayerCardToken(p, receiver)
            // 玩家叠加卡牌的信息
            for ((c, d) in this.gameMode.cardBoard.attachedCards.filterValues(p.allPlayedCard::contains)) {
                this.sendPlayerAttachCardResponse(p, c, d, receiver)
            }
        }
        with(gameMode.cardBoard) {
            if (pastEvents.isNotEmpty()) this@TTA.sendAddPastEventsResponse(gameMode.cardBoard.pastEvents.reversed(), receiver)
            for ((c, r) in currentEventRelation) this@TTA.sendAddEventResponse(r, c, true, receiver)
            for ((c, r) in removedCurrentEventRelation) this@TTA.sendRemoveEventResponse(r, c, false, receiver)
            for ((c, r) in futureEventRelation) this@TTA.sendAddEventResponse(r, c, false, receiver)
        }
    }

    /**
     * 发送玩家结束政治阶段
     * @param player
     * @param currentStep
     */
    fun sendPlayerPoliticalEndResponse(player: TTAPlayer, currentStep: RoundStep) {
        CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_CHANGE_STEP, player.position).public("currentStep", currentStep).send(this)
    }

    /**
     * 发送玩家失去打出卡牌的信息
     * @param player
     * @param receiver
     */
    fun sendPlayerRemoveCardResponse(player: TTAPlayer, card: TTACard, receiver: TTAPlayer?) {
        TTACmdFactory.createPlayerRemoveCardResponse(player, card).send(this, receiver)
    }

    /**
     * 发送玩家失去打出卡牌的信息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerRemoveCardsResponse(player: TTAPlayer, cards: List<TTACard>, receiver: TTAPlayer?) {
        TTACmdFactory.createPlayerRemoveCardsResponse(player, cards).send(this, receiver)
    }

    /**
     * 发送玩家失去手牌的消息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerRemoveHandResponse(player: TTAPlayer, cards: List<TTACard>, receiver: TTAPlayer?) {
        TTACmdFactory.createPlayerRemoveHandResponse(player, cards, this.config.isLazyMemory).send(this, receiver)
    }

    /**
     * 发送玩家请求行动结束,关闭窗口的信息
     * @param player
     */
    fun sendPlayerRequestEndResponse(player: TTAPlayer) {
        CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_REQUEST_END, player.position).send(this, player)
    }

    /**
     * 发送玩家当前选中手牌的消息
     * @param player
     * @param cards
     * @param receiver
     */
    fun sendPlayerSelectHandResponse(player: TTAPlayer, cards: List<TTACard>, receiver: TTAPlayer?) {
        TTACmdFactory.createPlayerSelectHandResponse(player, cards).send(this, receiver)
    }

    /**
     * 发送玩家奇迹建造完成的消息
     * @param player
     */
    fun sendPlayerWonderCompleteResponse(player: TTAPlayer) {
        CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_WONDER_COMPLETE, player.position).send(this)
    }

    /**
     * 发送移除事件牌的信息
     * @param player
     * @param futureToCurrent
     * @param receiver
     */
    fun sendRemoveEventResponse(player: TTAPlayer?, card: TTACard, futureToCurrent: Boolean, receiver: TTAPlayer?) {
        val isLazyMemory = config.isLazyMemory
        val position = if (isLazyMemory && player != null) player.position else -1
        CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_REMOVE_EVENT, position).private("cardId", card.id).private("player", player?.position).public("cardLevel", card.level).public("futureToCurrent", futureToCurrent).send(this, receiver)
    }

    /**
     * 移除过去事件（殖民等）
     * @param card
     */
    fun sendRemovePastEventResponse(card: TTACard, receiver: TTAPlayer?) {
        CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_REMOVE_EVENT, -1).public("past", true).public("cardIds", card.id).send(this, receiver)
    }

    /**
     * 发送第一张当前事件
     * @param receiver
     */
    fun sendTopEvent(receiver: TTAPlayer?) {
        CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_BASE_INFO, -1)
                // 设置领袖奇迹显示板
                .public("infoType", "topEvent")
                // 设置当前事件的信息
                .public("nextCurrentEventCardId", this.gameMode.cardBoard.nextCurrentEventCard?.id)
                .send(this, receiver)
    }

    /**
     * 发送战争的提示信息
     * @param player
     * @param trigPlayer
     * @param card
     */
    fun sendWarAlertInfo(player: TTAPlayer, trigPlayer: TTAPlayer, card: TTACard) {
        this.sendAlert(player, trigPlayer.reportString + "对你宣战了!", mapOf("cardId" to card.id))
    }

    /**
     * 设置当前玩家
     */
    fun setCurrentPlayer(p: TTAPlayer) {
        currentPlayer = p
        gameMode.gameRank.currentPlayer = p
    }

    /**
     * @param player
     */
    fun setTichu(player: TTAPlayer, card: TTACard) {
        this.startPlayer = player
        this.currentPlayer = player
        this.playerAddCardDirect(player, card)
        player.team = 0
        this.players.filter { it.position != player.position }.forEach { it.team = 1 }
    }

    @Throws(BoardGameException::class)
    override fun setupGame() {
        log.info("设置游戏...")
        val num = this.currentPlayerNumber
        log.info("游戏人数: $num")
        // 设置游戏人数
        this.config.playerNumber = num
        this.gameMode = TTAGameMode(this)
    }

    /**
     * 将使用过的牌放入弃牌堆（新版规则）
     * @param cards
     */
    fun tryDiscardCards(cards: List<TTACard>) {
        if (this.isVersion2) {
            gameMode.cardBoard.discardCards(cards)
        }
    }

    /**
     * 将使用过的牌放入弃牌堆（新版规则）
     * @param card
     */
    fun tryDiscardCards(card: TTACard) {
        this.tryDiscardCards(listOf(card))
    }

    fun checkUnitedNation(abilityType: CivilAbilityType, cost: Int) {
        for (p in gameMode.realPlayers.filter { it.abilityManager.hasAbilitiy(abilityType) }) {
            if (p.availableCivilAction < cost) continue
            val card = p.abilityManager.getAbilityCard(abilityType) ?: continue
            if (cost > 0) {
                val listener = TTAConfirmListener(gameMode, p, "你是否使用 ${card.name} 的能力?")
                val res = gameMode.insertListener(listener)
                if (res.getBoolean(p.position)) continue
            }
            this.playerAddCivilAction(p, -cost)
            this.playerAddPoint(p, p.abilityManager.getAbility(abilityType)!!.property)
            this.report.playerActiveCard(p, abilityType)
        }
    }

    /**
     * 玩家宣战
     * @param player
     * @param targetPlayer
     * @param card
     */
    fun playerDeclareWar(player: TTAPlayer, targetPlayer: TTAPlayer, card: AttackCard) {
        // 如果是战争,则将该牌添加到玩家的战争牌序列
        this.playerUseCardOnPlayer(player, targetPlayer, card)
        // 向目标玩家发送提示信息
        this.sendWarAlertInfo(targetPlayer, player, card)
    }

    fun beforePlayerPlayEvent(player: TTAPlayer, card: TTACard) {
        for (it in this.players.filter { it !== player && it.abilityManager.hasAbilitiy(CivilAbilityType.PA_NOSTRADAMUS) }) {
            val listener = TTAShowCardListener(gameMode, it, card)
            gameMode.insertListener(listener)
        }
    }

    fun playerDrawMilitaryCard(player: TTAPlayer, property: TTAProperty) {
        val num = property.getProperty(CivilizationProperty.MILITARY_DRAW)
        this.playerDrawMilitaryCard(player, num)
    }

}
