package com.f14.TTA

import com.f14.TTA.component.*
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.component.ability.ScoreAbility
import com.f14.TTA.component.card.*
import com.f14.TTA.consts.*
import com.f14.TTA.manager.TTAAbilityManager
import com.f14.TTA.manager.TTAConstManager
import com.f14.TTA.manager.TTATemplateResourceManager
import com.f14.bg.common.ParamSet
import com.f14.bg.component.ICondition
import com.f14.bg.exception.BoardGameException
import com.f14.bg.hall.GameRoom
import com.f14.bg.hall.User
import com.f14.bg.player.Player
import com.f14.bg.utils.BgUtils
import com.f14.bg.utils.CheckUtils
import kotlin.math.max

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

class TTAPlayer(user: User, room: GameRoom) : Player(user, room) {
    /**
     * 玩家下回合发动的临时属性(调整下回合CA,跳过政治阶段等)
     */
    val roundTempParam = ParamSet()
    /**
     * 玩家的得分情况
     */
    val points = TTAProperty()
    /**
     * 玩家属性
     */
    val properties = TTAProperty()
    /**
     * 玩家已打出的农矿,建筑,部队,建成的奇迹,殖民地,特殊科技
     */
    val buildings = TTACardDeck()
    /**
     * 政治手牌
     */
    val civilHands = TTACardDeck()
    /**
     * 军事手牌
     */
    val militaryHands = TTACardDeck()
    /**
     * 玩家指示物
     */
    val tokenPool = TokenPool()
    /**
     * 玩家卡牌能力管理器
     */
    val abilityManager = TTAAbilityManager()
    /**
     * 玩家临时资源管理器
     */
    val tempResManager: TTATemplateResourceManager = TTATemplateResourceManager(this)
    /**
     * 当前的政府
     */
    var government: GovermentCard? = null
    /**
     * 体面退出游戏
     */
    var resigned = false
    /**
     * 时代虚拟卡
     */
    var ageDummyCard: TTACard = TTACard()
    /**
     * 遗言（领袖）
     */
    var willCard: TTACard? = null
    /**
     * 所属游戏模式
     */
    private var gameRank: TTAGameRank? = null
    /**
     * 玩家的领袖
     */
    var leader: CivilCard? = null
        private set
    /**
     * 当前在建的奇迹
     */
    var uncompletedWonder: WonderCard? = null
    /**
     * 当前的战术
     */
    var tactics: TacticsCard? = null
        private set
    /**
     * 玩家打出的战争牌
     */
    var war: AttackCard? = null
        private set
    /**
     * 玩家打出的条约牌
     */
    var pact: PactCard? = null
        private set


    val buildLimit: Int
        get() = this.government!!.buildingLimit + this.properties.getProperty(CivilizationProperty.BUILDING_LIMIT)

//    override val reportString: String
//        get() = when (this.position) {
//            0 -> "蓝家[${this.name}]"
//            1 -> "绿家[${this.name}]"
//            2 -> "红家[${this.name}]"
//            3 -> "黄家[${this.name}]"
//            else -> super.reportString
//        }

    /**
     * 调整玩家可用工人的数量
     * @param num
     * @return
     */
    fun addAvailableWorker(num: Int): Int {
        return this.tokenPool.addAvailableWorker(num).also { this.checkUnhappyWorkers() }
    }

    /**
     * 玩家获得牌
     * @param card
     * @return
     */
    fun addCard(card: TTACard): TTAProperty {
        val usedCa = properties.getProperty(CivilizationProperty.CIVIL_ACTION) - this.availableCivilAction
        val usedMa = properties.getProperty(CivilizationProperty.MILITARY_ACTION) - this.availableMilitaryAction
        var removedCard: TTACard? = null
        when (card.cardType) {
            CardType.PRODUCTION, CardType.BUILDING, CardType.UNIT, CardType.WONDER, CardType.EVENT // EVENT中只可能出现领土牌
            -> {
                // 将该卡牌添加到玩家的已打出建筑牌堆中并排序
                buildings.addCard(card)
                buildings.sortCards()
            }
            CardType.SPECIAL -> {
                // 特殊科技,同种类型的只能存在一张,打出新的时需要将原科技废除
                removedCard = this.getBuildingsBySubType(card.cardSubType).firstOrNull()
                buildings.removeCard(removedCard)
                buildings.addCard(card)
                buildings.sortCards()
            }
            CardType.GOVERMENT -> {
                // 移除原政府,添加新政府
                removedCard = this.government
                this.government = card as GovermentCard
            }
            CardType.LEADER -> {
                // 移除原领袖,添加新领袖
                removedCard = this.leader
                this.leader = card as CivilCard
            }
            CardType.TACTICS -> { // 战术牌
                // 移除原战术牌,应用新的战术牌
                removedCard = this.tactics
                this.tactics = card as TacticsCard
            }
            CardType.WAR -> { // 战争
                val war = card as AttackCard
                if (war.owner === this) {
                    // 如果该战争牌属于当前玩家,则设置当前玩家的war
                    removedCard = this.war
                    this.war = war
                }
                buildings.addCard(war)
            }
            CardType.PACT -> { // 条约
                val pact = card as PactCard
                if (pact.owner === this) {
                    // 如果该条约牌属于当前玩家,则设置当前玩家的pact
                    removedCard = this.pact
                    this.pact = pact
                }
                this.buildings.addCard(pact)
            }
            CardType.TICHU -> buildings.addCard(card)
            else -> Unit
        }
        // 处理添加卡牌时的事件
        val res = this.onCardChange(card, removedCard, usedCa, usedMa)
        // 刷新属性

        if (removedCard != null && removedCard is GovermentCard && card is GovermentCard) {
            card.addReds(removedCard.reds)
            card.addWhites(removedCard.whites)
            card.addWorkers(removedCard.workers)
        }
        return res
    }

    /**
     * 调整当前可用的内政行动点数
     * @param num
     */
    fun addCivilAction(num: Int) {
        this.government?.addWhites(num)
    }

    /**
     * 调整当前的文明点数
     * @param num
     */
    fun addCulturePoint(num: Int) {
        this.points.addProperty(CivilizationProperty.CULTURE, num)
    }

    /**
     * 调整玩家的食物
     * @param num
     * @return
     */

    fun addFood(num: Int) = when {
        num > 0 -> this.getBlueToken(CivilizationProperty.FOOD, num)
        num < 0 -> this.payBlueToken(CivilizationProperty.FOOD, -num)
        else -> ProductionInfo(CivilizationProperty.FOOD)
    }

    /**
     * 玩家得到手牌
     * @param cards
     */
    fun addHand(cards: List<TTACard>) = cards.groupBy(TTACard::actionType).forEach { (t, list) ->
        when (t) {
            ActionType.CIVIL -> this.civilHands.addCards(list)
            ActionType.MILITARY -> this.militaryHands.addCards(list)
        }
    }

//            cards.forEach {
//        when (it.actionType) {
//            ActionType.CIVIL -> this.civilHands.addCard(it)
//            ActionType.MILITARY -> this.militaryHands.addCard(it)
//        }
//    }

    /**
     * 调整当前可用的军事行动点数
     * @param num
     */
    fun addMilitaryAction(num: Int) {
        this.government?.addReds(num)
    }

    /**
     * 玩家添加属性,返回与原先玩家属性的差值
     * @param properties
     * @return
     */
    fun addProperties(properties: TTAProperty): TTAProperty {
        val res = TTAProperty()
        val oldValues = this.properties.allFactProperties
        this.properties += properties
        val keys = properties.allProperties.keys
        // 计算玩家属性更新后与原来属性的差值
        keys.forEach { res.addProperty(it, this.properties.getProperty(it) - oldValues.getOrDefault(it, 0)) }
//        properties.allFactProperties.forEach { (p, v) -> res.setProperty(p, v - oldValues.getOrDefault(p, 0)) }
        return res
    }

    /**
     * 调整玩家的资源
     * @param num
     * @return
     */
    fun addResource(num: Int): ProductionInfo = when {
        num > 0 -> this.getBlueToken(CivilizationProperty.RESOURCE, num)
        num < 0 -> this.payBlueToken(CivilizationProperty.RESOURCE, -num)
        else -> ProductionInfo(CivilizationProperty.RESOURCE)
    }

    /**
     * 调整当前的科技点数
     * @param num
     */
    fun addSciencePoint(num: Int) = this.points.addProperty(CivilizationProperty.SCIENCE, num)

    /**
     * 玩家建造建筑/部队
     * @param card
     * @return
     */
    fun build(card: CivilCard) {
        this.tokenPool.addUnusedWorker(-1)
        card.addWorkers(1)
    }

    /**
     * 玩家建造奇迹
     * @param step
     * @return 返回所有状态变化过的卡牌列表
     */
    fun buildWonder(step: Int) {
        // 在奇迹上放置蓝色指示物表示完成建造的步骤
        this.uncompletedWonder?.buildStep(-tokenPool.addAvailableBlues(-step))
    }

    /**
     * 检查能力是否还能够使用
     * @param ability
     * @return
     */
    fun checkAbility(ability: CivilAbilityType): Boolean = this.abilityManager.hasAbilitiy(ability) && !this.params.getBoolean(ability)

    /**
     * 检查玩家是否拥有足够的内政/军事行动点,如果不够则抛出异常
     * @param actionCost
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkActionPoint(actionType: ActionType, actionCost: Int) {
        CheckUtils.check(actionCost > this.getAvailableActionPoint(actionType), "${actionType.chinese}行动点不够,你还能使用 ${this.getAvailableActionPoint(actionType)} 个${actionType.chinese}行动点!")
    }

    /**
     * 检查玩家各项资源够不够
     * @param property
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkEnoughResource(property: TTAProperty) = this.checkEnoughResource(property, 1)

    /**
     * 检查玩家各项资源够不够
     * @param property
     * @param multi
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkEnoughResource(property: TTAProperty, multi: Int) {
        CheckUtils.check(this.sciencePoint < multi * property.getProperty(CivilizationProperty.SCIENCE), "你没有足够的科技点数!")
        CheckUtils.check(this.culturePoint < multi * property.getProperty(CivilizationProperty.CULTURE), "你没有足够的文化分数!")
        CheckUtils.check(this.totalFood < multi * property.getProperty(CivilizationProperty.FOOD), "你没有足够的食物!")
        CheckUtils.check(this.totalResource < multi * property.getProperty(CivilizationProperty.RESOURCE), "你没有足够的资源!")
    }

    /**
     * 检查玩家是否可以拿取指定的卡牌,如果不能则抛出异常
     * @param card @throws
     */
    @Throws(BoardGameException::class)
    fun checkTakeCard(card: TTACard) {
        // 只有当前手牌数小于总内政行动点数时才能拿,奇迹牌不入手无需判断
        CheckUtils.check(card.cardType != CardType.WONDER && this.civilHands.size >= this.civilHandLimit, "你的内政牌数量已经达到上限!")
        when (card.cardType) {
            CardType.WONDER ->
                // 如果拿的是奇迹牌,并且拥有在建的奇迹,则不能再拿
                CheckUtils.check(this.uncompletedWonder != null, "你的奇迹正在建造中,不能拿取新的奇迹!")
            CardType.LEADER ->
                // 如果是领袖牌,则需要判断是否已经拥有同等级的领袖,有则不能再拿
                CheckUtils.check(this.hasLeader(card.level), "你已经拥有该时代的领袖了!")
            else ->
                // 如果是科技牌,则不能重复拿
                CheckUtils.check(card.isTechnologyCard && this.hasSameCard(card), "你已经拥有该科技了!")
        }
    }

    /**
     * 检查并设置玩家不满意的工人数
     */
    private fun checkUnhappyWorkers() {
        // 需要同时调整玩家不开心的工人数
        val need = TTAConstManager.getNeedHappiness(this.tokenPool.availableWorkers) - this.abilityManager.getAbilitiesByType(CivilAbilityType.PA_IGNORE_DISCONTENT).sumBy(CivilCardAbility::amount)
        val value = maxOf(0, need - this.getProperty(CivilizationProperty.HAPPINESS))
        this.properties.setProperty(CivilizationProperty.DISCONTENT_WORKER, value)
        this.tokenPool.unhappyWorkers = value
    }

    /**
     * 检查玩家是否可以使用card
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkUseCard(card: AttackCard) {
        // 检查是否有能力限制使用卡牌
        for (ca in abilityManager.getAbilitiesByType(CivilAbilityType.PA_USE_CARD_LIMIT)) {
            CheckUtils.check(!ca.test(card), "你不能使用这种类型的卡牌!")
        }
        // 检查使用行动点限制
        if (card.actionCost.adjustType == null) {
            this.checkActionPoint(card.actionCost.actionType, card.actionCost.actionCost)
        }
    }

    /**
     * 清空手牌
     * @return
     */
    fun clearAllHands() {
        this.militaryHands.clear()
        this.civilHands.clear()
    }

    /**
     * 清空玩家已打在桌面上的所有牌
     * @return
     */
    fun clearAllPlayedCard() {
        this.buildings.clear()
        this.government = null
        this.leader = null
    }

    /**
     * 玩家完成奇迹的建造
     * @return 返回所有状态变化过的卡牌列表
     */

    fun completeWonder(): TTAProperty {
        // 如果建造完成,则需要将转移该建成的奇迹
        // 将奇迹牌上的蓝色标志物返回资源库
        val wonder = this.uncompletedWonder!!
        this.uncompletedWonder = null
        if (!wonder.isCostBlues) {
            val blues = wonder.blues
            wonder.addBlues(-blues)
            tokenPool.addAvailableBlues(blues)
        }
        return this.addCard(wonder)
    }

    /**
     * 减少空闲人口,人口回到资源库
     * @return 实际减少的人口数
     * @throws BoardGameException
     */
    fun decreasePopulation(num: Int): Int {
        val n = minOf(this.tokenPool.unusedWorkers, num)
        this.addAvailableWorker(n)
        this.tokenPool.addUnusedWorker(-n)
        return n
    }

    /**
     * 玩家摧毁建筑/部队
     * @param card
     * @param num
     * @return 返回拆掉的实际数量
     */
    fun destroy(card: CivilCard, num: Int): Int {
        val i = card.addWorkers(-num)
        this.tokenPool.addUnusedWorker(i)
        return i
    }

    /**
     * 取得指定阶段中玩家可用的卡牌列表
     * @param activeStep
     * @return
     */
    fun getActiveCards(activeStep: RoundStep): Collection<TTACard> = this.abilityManager.activeCards.filter { it.activeAbility!!.checkCanActive(activeStep, this) }

    /**
     * 取得所有手牌
     * @return
     */

    val allHands: List<TTACard>
        get() = this.civilHands.cards + this.militaryHands.cards

    /**
     * 取得玩家已打在桌面上的所有牌
     * @return
     */

    val allPlayedCard: List<TTACard>
        get() = getAllPlayedCard(false)

    /**
     * 取得玩家已打在桌面上的所有牌
     * @return
     */
    fun getAllPlayedCard(bUncompletedWonder: Boolean): List<TTACard> = this.buildings.cards + listOfNotNull(this.government, this.leader, if (bUncompletedWonder) this.uncompletedWonder else null)

    /**
     * 玩家为侵略方时的战力调整
     * @param card   侵略或战争卡
     * @param target 目标玩家
     * @return
     */
    fun getAttackerMilitary(card: TTACard, target: TTAPlayer): Int {
        val defensiveMilitary = this.properties.getProperty(CivilizationProperty.DEFENSIVE_MILITARY)
        val isAggression = card.cardSubType == CardSubType.AGGRESSION
        if (isAggression) this.properties.addPropertyBonus(CivilizationProperty.MILITARY, -defensiveMilitary)
        var ret = this.getProperty(CivilizationProperty.MILITARY)
        if (isAggression) this.properties.addPropertyBonus(CivilizationProperty.MILITARY, defensiveMilitary)
        // 攻击盟友时调整战力的能力
        val pacts = abilityManager.getPactAbilitiesWithRelation(CivilAbilityType.PA_ATTACK_ALIAN_ADJUST)
        ret += pacts.filterValues { it.alian === target }.keys.sumBy { it.property.getProperty(CivilizationProperty.MILITARY) }
        if (this.abilityManager.hasAbilitiy(CivilAbilityType.PA_HUBATIAN)) {
            ret /= 2
        }
        return ret
    }

    /**
     * 取得指定类型的行动点数
     * @param actionType
     * @return
     */
    fun getAvailableActionPoint(actionType: ActionType): Int = when (actionType) {
        ActionType.CIVIL -> this.availableCivilAction
        ActionType.MILITARY -> this.availableMilitaryAction
    }

    fun getAvailableActionPoint(property: CivilizationProperty): Int = when (property) {
        CivilizationProperty.CIVIL_ACTION -> this.availableCivilAction
        CivilizationProperty.MILITARY_ACTION -> this.availableMilitaryAction
        else -> 0
    }

    /**
     * 取得当前可用的内政行动点数
     * @return
     */
    val availableCivilAction: Int
        get() = this.government?.whites ?: 0

    /**
     * 取得当前可用的军事行动点数
     * @return
     */
    val availableMilitaryAction: Int
        get() = this.government?.reds ?: 0

    /**
     * 得到指定数量的资源,并将得到的蓝色指示物放在卡牌上
     * @param property
     * @param num
     * @return
     */
    private fun getBlueToken(property: CivilizationProperty, num: Int) = ResourceTaker(property).putResource(num).execute()

    /**
     * 取得玩家所有的防御/殖民地加值卡
     * @return
     */

    val bonusCards: List<BonusCard>
        get() = this.allHands.filter { it.cardType == CardType.DEFENSE_BONUS }.map { it as BonusCard }

    /**
     * 取得玩家指定类型的已建造建筑数量
     * @param cardSubType
     * @return
     */
    fun getBuildingNumber(cardSubType: CardSubType): Int = this.getBuildingsBySubType(cardSubType).sumBy(CivilCard::workers)

    /**
     * 取得玩家 的卡牌,并按等级从大到小排序
     * @param cardSubType
     * @return
     */

    fun getBuildingsBySubType(cardSubType: CardSubType): List<CivilCard> = this.allPlayedCard.filter { it.cardSubType == cardSubType }.map { it as CivilCard }.sorted().reversed().toList()

    /**
     * 取得玩家建造建筑时的费用
     * @param card
     * @return
     */
    fun getBuildResourceCost(card: TechCard): Int {
        var res = card.costResource
        // 计算所有调整建造费用的能力
        res += abilityManager.getAbilitiesByType(CivilAbilityType.PA_BUILD_COST).filter { it.test(card) }.sumBy { it.getAvailableProperty(this, CivilizationProperty.RESOURCE) }
        // 计算新版莎士比亚调整建筑费用的能力
        res += abilityManager.getAbilitiesByType(CivilAbilityType.PA_BUILD_COST_GROUP).filter { it.testGroup(this.allPlayedCard.filter { it.availableCount > 0 }, card) }.sumBy { it.getAvailableProperty(this, CivilizationProperty.RESOURCE) }
        res = max(0, res)
        return res
    }

    /**
     * 从手牌中取得指定的牌,如果不存在则抛出异常
     * @param cardId
     * @return
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun getCard(cardId: String): TTACard {
        return this.getCardFromList(cardId, this.allHands)
    }

    /**
     * 取得指定的牌,如果不存在则抛出异常
     * @param cardId
     * @param cards
     * @return
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    private fun getCardFromList(cardId: String, cards: List<TTACard>): TTACard = cards.find { it.id == cardId }
            ?: throw BoardGameException("没有找到指定的对象!")

    /**
     * 从手牌中取得指定的牌,如果不存在则抛出异常
     * @param cardIds
     * @return
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun getCards(cardIds: String): List<TTACard> {
        val ids = BgUtils.string2Array(cardIds)
        return ids.map(this::getCard)
    }

    /**
     * 取得玩家内政手牌的上限
     * @return
     */
    // 内政点数+内政手牌数量调整
    val civilHandLimit: Int
        get() = this.properties.getProperty(CivilizationProperty.CIVIL_ACTION) + this.properties.getProperty(CivilizationProperty.CIVIL_HANDS)

    /**
     * 取得玩家所有建成奇迹的数量
     * @return
     */
    val completedWonderNumber: Int
        get() = this.getBuildingsBySubType(CardSubType.WONDER).filterNot { it.activeAbility?.abilityType == ActiveAbilityType.PA_RED_CROSS }.size

    /**
     * 玩家粮食消费
     * @return
     */
    val consumption: Int
        get() {
            val foodSupply = TTAConstManager.getFoodSupply(this.tokenPool.availableWorkers) + abilityManager.getAbilitiesByType(CivilAbilityType.ADJUST_CONSUMPTION).sumBy { it.getAvailableNumber(this) }
            return max(foodSupply, 0)
        }

    /**
     * 取得当前的文明点数
     * @return
     */
    val culturePoint: Int
        get() = this.points.getProperty(CivilizationProperty.CULTURE)

    val defenceMilitary: Int
        get() {
            var ret = this.getProperty(CivilizationProperty.MILITARY)
            if (this.abilityManager.hasAbilitiy(CivilAbilityType.PA_HUBATIAN)) {
                ret /= 2
            }
            return ret
        }

    /**
     * 取得双倍产值的卡牌集合,可叠加
     * @param p
     * @return
     */
    private fun getDoubledCards(p: CivilizationProperty): Map<CivilCard, Int> {
        return abilityManager.getAbilitiesByType(CivilAbilityType.DOUBLE_PRODUCE)
                .filter { it.byProperty == p }
                .mapNotNull { it.getBestCard(this.buildings.cards) }
                .filterIsInstance<CivilCard>().groupBy { it }
                .mapValues { (_, v) -> v.map { 2 }.reduce { a, b -> a * b } }
    }

    /**
     * @param card
     * @return
     */
    fun getFinalRankValue(card: ActionCard): TTAProperty {
        val rank = this.getRank(card.actionAbility.rankProperty!!, false)
        return card.getFinalRankValue(rank, realPlayerNumber)
    }

    /**
     * 取得玩家食物的总生产力
     * @return
     */
    // 基础农场产值
    val foodProduction: Int
        get() = this.productionFromFarm.totalValue

    /**
     * 按照条件取得玩家的手牌
     * @param condition
     * @return
     */
    fun getHandCard(condition: ICondition<TTACard?>): List<TTACard> = this.allHands.filter(condition::test)

    /**
     * 取得玩家抓军事牌的上限
     * @return
     */
    val militaryDraw: Int
        get() = this.properties.getProperty(CivilizationProperty.MILITARY_DRAW) + 3

    /**
     * 取得玩家军事手牌的上限
     * @return
     */
    // 军事点数+军事手牌数量调整
    val militaryHandLimit: Int
        get() = this.properties.getProperty(CivilizationProperty.MILITARY_ACTION) + this.properties.getProperty(CivilizationProperty.MILITARY_HANDS)

    /**
     * 条约带来的军力
     * @param pactIds
     * @return
     */
    fun getPactMilitary(pactIds: List<String>): Int = this.allPlayedCard.filter { pactIds.contains(it.id) }.sumBy { it.property.getProperty(CivilizationProperty.MILITARY) }

    /**
     * 按照条件取得玩家所有已打在桌面上的牌
     * @param condition
     * @return
     */
    fun getPlayedCard(condition: ICondition<TTACard>): List<TTACard> = this.allPlayedCard.filter(condition::test)

    /**
     * 按照cardId取得玩家已打在桌面上的牌
     * @param cardId
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    @JvmOverloads
    fun getPlayedCard(cardId: String, bUncompleteWonder: Boolean = false): TTACard {
        val cards = this.getAllPlayedCard(bUncompleteWonder)
        return this.getCardFromList(cardId, cards)
    }

    /**
     * 农场产能统计(计入其他卡牌对其的增益)
     * @return
     */
    val productionFromFarm: ProductionInfo
        get() {
            val doubledCards = getDoubledCards(CivilizationProperty.FOOD)
            val res = ProductionInfo(CivilizationProperty.FOOD)
            res += this.getBuildingsBySubType(CardSubType.FARM).filter { it.workers > 0 }.map { it to doubledCards.getOrDefault(it, 1) + it.workers - 1 }
//                    .forEach { res[it] = doubledCards.getOrDefault(it, 1) + it.workers - 1 }
            return res
        }

    /**
     * 来自实验室的产能(比尔盖茨/特斯拉)
     * @return
     */

    val productionFromLab: ProductionInfo
        get() {
            val res = ProductionInfo(CivilizationProperty.RESOURCE)
            res += this.getBuildingsBySubType(CardSubType.LAB).filter { it.level > 0 && it.workers > 0 }.map { it to it.workers }
//                    .forEach { res[it] = it.workers }
            return res
        }

    /**
     * 矿山产能统计(计入其他卡牌对其的增益)
     * @return
     */

    val productionFromMine: ProductionInfo
        get() {
            val doubledCards = getDoubledCards(CivilizationProperty.RESOURCE)
            val res = ProductionInfo(CivilizationProperty.RESOURCE)
            res += this.getBuildingsBySubType(CardSubType.MINE).filter { it.workers > 0 }.map { it to doubledCards.getOrDefault(it, 1) + it.workers - 1 }
//                    .toMap(res)
//                    .forEach { res[it] = doubledCards.getOrDefault(it, 1) + it.workers - 1 }
            return res
        }

    /**
     * 取得属性值
     * @param property
     * @return
     */
    fun getProperty(property: CivilizationProperty) = when (property) {
        CivilizationProperty.DISCONTENT_WORKER -> tokenPool.unhappyWorkers
        CivilizationProperty.BLUE_TOKEN -> tokenPool.availableBlues
        CivilizationProperty.FOOD -> this.totalFood
        CivilizationProperty.RESOURCE -> this.totalResource
        else -> properties.getProperty(property)
    }

    /**
     * 取得玩家的某属性排名
     * @return
     */
    fun getRank(byProperty: CivilizationProperty, weekest: Boolean): Int = gameRank!!.getPlayerRank(this, byProperty, weekest)

    /**
     * 取得实际玩家数,影响因人数而异的卡牌能力
     * @return
     */
    val realPlayerNumber: Int
        get() = gameRank!!.playerNumber

    fun getResearchCost(card: TechCard, bSecondary: Boolean): Int {
        var res = if (bSecondary && card is GovermentCard) card.secondaryCostScience else card.costScience
        // 计算一般调整科技费用的能力(巴赫,老版丘吉尔等)
        res += abilityManager.getAbilitiesByType(CivilAbilityType.PA_PLAY_CARD_COST).filter { it.test(card) }.sumBy { it.getAvailableProperty(this, CivilizationProperty.SCIENCE) }
        // 计算新版莎士比亚调整科技费用的能力
        res += abilityManager.getAbilitiesByType(CivilAbilityType.PA_PLAY_CARD_COST_GROUP).filter { it.testGroup(this.allPlayedCard.filter { it.availableCount > 0 }, card) }.sumBy { it.getAvailableProperty(this, CivilizationProperty.SCIENCE) }
        if (this.getBuildingsBySubType(card.cardSubType).isEmpty()) {
            res += abilityManager.getAbilitiesByType(CivilAbilityType.PA_PLAY_NEW_CARD_COST).filter { it test card }.sumBy { it.getAvailableProperty(this, CivilizationProperty.SCIENCE) }
        }
        return res
    }

    /**
     * 取得玩家资源的总生产力
     * @return
     */
    val resourceProduction: Int
        get() {
            // 基础矿山产值
            var res = this.productionFromMine.totalValue
            // 特斯拉实验室产值
            if (this.abilityManager.hasAbilitiy(CivilAbilityType.PA_TESLA_ABILITY)) {
                res += this.productionFromLab.totalValue
            }
            return res
        }

    /**
     * 取得拿取资源后,将返回资源库的蓝色指示物个数
     * @param num
     * @return
     */
    fun getReturnedBlues(property: TTAProperty): Int {
        return arrayOf(CivilizationProperty.RESOURCE, CivilizationProperty.FOOD).sumBy { ResourceTaker(it).takeResource(property.getProperty(it)).returnedBlues }
    }

    /**
     * 取得当前的科技点数
     * @return
     */
    val sciencePoint: Int
        get() = this.points.getProperty(CivilizationProperty.SCIENCE)

    /**
     * 取得得分能力能够带给玩家的分数
     * @return
     */
    fun getScoreCulturePoint(scoreAbilities: List<ScoreAbility>): Int = scoreAbilities.sumBy { it.getScoreCulturePoint(this) }

    /**
     * 玩家算得的纯阵型加成值
     */
    fun getTacticsResult(units: Map<TechCard, Int>): TacticsCard.TacticsResult {
        var type = TacticsCard.TacticType.NORMAL
        when {
            this.abilityManager.hasAbilitiy(CivilAbilityType.PA_GENGHIS) -> type = TacticsCard.TacticType.GHENGIS
            this.abilityManager.hasAbilitiy(CivilAbilityType.PA_ZIZKA) -> type = TacticsCard.TacticType.ZIZKA
            this.abilityManager.hasAbilitiy(CivilAbilityType.MULTIPLE_UNIT) -> type = TacticsCard.TacticType.MULTIPLE
        }
        // 新版成吉思汗能力
        // 新版成吉思汗能力
        return this.tactics!!.getTacticsResult(units, type)
    }

    /**
     * 取得玩家的粮食总数
     * @return
     */
    val totalFood: Int
        get() = this.getBuildingsBySubType(CardSubType.FARM).sumBy { it.blues * it.property.getProperty(CivilizationProperty.FOOD) }

    /**
     * 取得玩家的资源总数
     * @return
     */
    // 每个矿场上的资源等于其上蓝色指示物的数量 x 其资源产量
    val totalResource: Int
        get() = this.getBuildingsBySubType(CardSubType.MINE).sumBy { it.blues * it.property.getProperty(CivilizationProperty.RESOURCE) } + this.getBuildingsBySubType(CardSubType.LAB).sumBy { it.blues * it.property.getProperty(CivilizationProperty.RESOURCE) }

    /**
     * 取得玩家所有的部队信息
     * @return
     */
    val unitsInfo: List<Map<String, Any>>
        get() {
            // 只返回部队的cardId和拥有的工人数量
            val con = Condition()
            con.cardType = CardType.UNIT
            return this.getPlayedCard(con).map { mapOf("cardId" to it.id, "num" to it.availableCount) }
        }

    /**
     * 取得所有的工人数,包括空闲的工人
     * @return
     */
    val workers: Int
        get() = this.tokenPool.unusedWorkers + this.allPlayedCard.filter(TTACard::needWorker).sumBy { (it as CivilCard).workers }

    /**
     * 判断玩家是否拥有指定等级的领袖
     * @param level
     * @return
     */
    private fun hasLeader(level: Int) = this.params.getBoolean(PARAM_LEADER + level)

    /**
     * 判断玩家的手牌和打出的牌中是否拥有同名的牌
     * @param card
     * @return
     */
    fun hasSameCard(card: TTACard) = this.allHands.any { it.cardNo == card.cardNo } || this.allPlayedCard.any { it.cardNo == card.cardNo }

    /**
     * 是否拥有部队
     * @return
     */
    fun hasUnit(): Boolean {
        val con = Condition()
        con.cardType = CardType.UNIT
        return this.getPlayedCard(con).any { it.availableCount > 0 }
    }

    /**
     * 扩张人口
     * @throws BoardGameException
     */
    fun increasePopulation(num: Int): Int {
        // 只有当存在可用工人时才能执行扩张人口
        val workers = -this.addAvailableWorker(-num)
        this.tokenPool.addUnusedWorker(workers)
        return workers
    }

    /**
     * 初始化
     */
    fun init(gameMode: TTAGameMode) {
        this.gameRank = gameMode.gameRank
        TTAConstManager.initPlayerPoints(this.points, gameMode.game.config.isNoLimit)
        TTAConstManager.initPlayerProperties(this.properties, gameMode.game.config.isNoLimit)
        if (gameMode.isVersion2) {
            this.tokenPool.init(18, 16, 1, 0)
        } else {
            this.tokenPool.init(18, 18, 1, 0)
        }
    }

    /**
     * 判断玩家是否会引起暴动
     * @return
     */
    val isUprising: Boolean
        get() = this.tokenPool.unhappyWorkers > this.tokenPool.unusedWorkers

    /**
     * 判断玩家是否被宣战
     * @return
     */
    // 检查所有战争卡,是否有被作为目标的,如果有,则玩家被宣战中
    val isWarTarget: Boolean
        get() = this.allPlayedCard.any { it.cardType == CardType.WAR && (it as AttackCard).target === this }

    /**
     * 判断玩家当前人口的状态,是否在暴动的临界点
     * @return
     */
    val isWillUprising: Boolean
        get() = this.tokenPool.unhappyWorkers > 0 && this.tokenPool.unhappyWorkers >= this.tokenPool.unusedWorkers

    /**
     * 添加/移除 卡牌时处理的事件
     * @param cardAdd
     * @param cardRemove
     * @param usedma
     * @param usedca
     */
    private fun onCardChange(cardAdd: TTACard?, cardRemove: TTACard?, usedca: Int, usedma: Int): TTAProperty {
        val propertyChange = TTAProperty()
        // 将添加的牌的能力添加到玩家的能力管理器中
        if (cardAdd != null) {
            this.abilityManager.addCardAbilities(cardAdd)
            cardAdd.abilities.forEach {
                when {
                    it.abilityType == CivilAbilityType.PA_TEMPLATE_RESOURCE -> // 添加其提供的临时资源(荷马等)
                        if (it.byProperty == null) this.tempResManager.addTemplateResource(it)
                    it.isFlag -> {
                        this.tempResManager.addAlternateTemplateResource(it)
                        this.params.setRoundParameter(it.abilityType, it.limit)
                    }
                    it.abilityType == CivilAbilityType.PA_TESLA_ABILITY -> this.params.setGameParameter(CivilAbilityType.PA_TESLA_ABILITY, true)
                }
            }
            if (cardAdd.activeAbility != null) {
                this.abilityManager.addActiveCard(cardAdd)
            }
        }
        // 将移除的牌的能力从玩家的能力管理器中移除
        if (cardRemove != null) {
            this.abilityManager.removeCardAbilities(cardRemove)
            cardRemove.abilities.forEach {
                when {
                    it.abilityType == CivilAbilityType.PA_TEMPLATE_RESOURCE -> // 同时移除其提供的临时资源
                        this.tempResManager.removeTemplateResource(it)
                    it.isFlag -> {
                        if (!it.property.isEmpty) {
                            this.tempResManager.removeAlternateTemplateResource(it)
                        }
                        this.params.setRoundParameter(it.abilityType, 0)
                    }
                }
            }
            if (cardRemove.activeAbility != null) {
                this.abilityManager.removeActiveCard(cardRemove)
                if (cardRemove.activeAbility!!.activeStep == RoundStep.RESIGNED) {
                    this.willCard = cardRemove
                }
            }
        }

        // 如果卡牌拥有调整行动点数的能力,则直接调整玩家当前的行动点数
        var ia = cardAdd?.property?.getProperty(CivilizationProperty.CIVIL_ACTION) ?: 0
        var ir = cardRemove?.property?.getProperty(CivilizationProperty.CIVIL_ACTION) ?: 0
        var i = ia - ir
        when {
            i > 0 -> // 如果i>0,则是添加点数,直接加上该点数
                propertyChange.addProperty(CivilizationProperty.CIVIL_ACTION, i)
            i < 0 -> {
                // 如果i<0,则是移除点数,则可以从已使用的点数中扣除该点
                i = minOf(0, i + usedca) // i为扣除点数的数量,总是应该小于等于0
                propertyChange.addProperty(CivilizationProperty.CIVIL_ACTION, i)
            }
        }
        ia = cardAdd?.property?.getProperty(CivilizationProperty.MILITARY_ACTION) ?: 0
        ir = cardRemove?.property?.getProperty(CivilizationProperty.MILITARY_ACTION) ?: 0
        i = ia - ir
        when {
            i > 0 -> // 如果i>0,则是添加点数,直接加上该点数
                propertyChange.addProperty(CivilizationProperty.MILITARY_ACTION, i)
            i < 0 -> {
                // 如果i<0,则是移除点数,则可以从已使用的点数中扣除该点
                i = minOf(0, i + usedma) // i为扣除点数的数量,总是应该小于等于0
                propertyChange.addProperty(CivilizationProperty.MILITARY_ACTION, i)
            }
        }

        // 如果卡牌拥有调整指示物的能力,则直接调整
        ia = cardAdd?.property?.getProperty(CivilizationProperty.YELLOW_TOKEN) ?: 0
        ir = cardRemove?.property?.getProperty(CivilizationProperty.YELLOW_TOKEN) ?: 0
        propertyChange.addProperty(CivilizationProperty.YELLOW_TOKEN, ia - ir)

        ia = cardAdd?.property?.getProperty(CivilizationProperty.BLUE_TOKEN) ?: 0
        ir = cardRemove?.property?.getProperty(CivilizationProperty.BLUE_TOKEN) ?: 0
        propertyChange.addProperty(CivilizationProperty.BLUE_TOKEN, ia - ir)

        return propertyChange
    }

    /**
     * 支付指定数量的蓝色指示物,并将支付的蓝色指示物放回玩家的配件池
     * @param property
     * @param num
     * @return
     */
    private fun payBlueToken(property: CivilizationProperty, num: Int) = ResourceTaker(property).takeResource(num).execute()

    /**
     * 刷新军队提供的军事力奖励值
     */
    private fun refreshMilitaryBonus() {
        // 首先清空军力增加值
        this.properties.setPropertyBonus(CivilizationProperty.MILITARY, 0)
        // 如果玩家拥有无视战术牌效果的能力,则不计算任何附加能力
        if (!this.abilityManager.hasAbilitiy(CivilAbilityType.PA_IGNORE_TACTICS)) {
            this.tactics?.let { tactics ->
                val zizka = this.abilityManager.hasAbilitiy(CivilAbilityType.PA_ZIZKA)
                // 整理所有部队卡的数量
                val units = this.allPlayedCard.filter { it.cardType == CardType.UNIT || (zizka && it.cardSubType == CardSubType.FARM) }.map { (it as TechCard) to it.availableCount }.toMap()
                // 得到战术卡组成的军队结果
                val result = this.getTacticsResult(units)
                // 添加额外军力到玩家的军事力结果
                val militaryBonus = result.totalMilitaryBonus + this.abilityManager.getAbilitiesByType(CivilAbilityType.PA_TATIC_BONUS).sumBy {
                    val totalNum = result.mainArmyNum + result.secondaryArmyNum
                    (totalNum + minOf(totalNum, result.airForceNum)) * it.getAvailableNumber(listOf(tactics), this)
                }
                this.properties.addPropertyBonus(CivilizationProperty.MILITARY, militaryBonus)
                if (this.abilityManager.hasAbilitiy(CivilAbilityType.PA_ZIZKA)) {
                    this.properties.addProperty(CivilizationProperty.CULTURE, result.mainArmyNum + result.secondaryArmyNum)
                }

                // 检查玩家是否有获得额外军队军事力奖励的能力
                if (this.abilityManager.hasAbilitiy(CivilAbilityType.PA_ADDITIONAL_TACTICS_BONUS)) {
                    // 有则添加军队中最高的军事奖励值
                    this.properties.addPropertyBonus(CivilizationProperty.MILITARY, result.bestArmyBonus)
                }
            }
        }
        val defensiveMilitary = this.properties.getProperty(CivilizationProperty.DEFENSIVE_MILITARY)
        this.properties.addPropertyBonus(CivilizationProperty.MILITARY, defensiveMilitary)
    }

    /**
     * 刷新玩家的属性值
     * @return
     */
    fun refreshProperties() {
        properties.clear()

        for (card in this.allPlayedCard.filterNot { it.cardType == CardType.TACTICS }) {
            // 1.清空奖励
            card.property.clearAllBonus()

            // 2.计算叠加牌能力(荷马,孙子等)
            card.attachedCards?.flatMap(TTACard::abilities)?.filter { it.abilityType == CivilAbilityType.ATTACH_PROPERTY }?.forEach { card.property.addBonusProperties(it.property, it.getAvailableNumber(this)) }

            // 3.计算调整已有牌单张属性的能力(老版大汗,大教堂)
            abilityManager.getAbilitiesByType(CivilAbilityType.ADJUST_UNIT_PROPERTY).filter { it test card }.forEach { card.property.addBonusProperties(it.property, it.getAvailableNumber(listOf(card), this)) }

            // 4.累加所有属性到玩家属性
            properties.addProperties(card.property, card.availableCount)
        }

        // 计算所有因自己其他卡牌而调整玩家属性的能力(爱因斯坦,莎士比亚,拿破仑,帕特农神庙,等等)
        abilityManager.getAbilitiesByType(CivilAbilityType.ADJUST_PROPERTY).forEach { properties.addProperties(it.property, it.getAvailableNumber(this)) }

        // 计算所有按照其他玩家的属性调整玩家属性的能力
        abilityManager.getPactAbilitiesWithRelation(CivilAbilityType.ADJUST_PROPERTY_BY_ALIAN).forEach { (ca, p) -> properties.addProperties(ca.property, ca.getAvailableNumber(p.alian!!)) }

        if (abilityManager.hasAbilitiy(CivilAbilityType.PA_SALADINE)) properties.addProperties(this.params.getParameter<TTAProperty>(CivilAbilityType.PA_SALADINE)!!)

        // 计算幸福度
        val happiness = this.getProperty(CivilizationProperty.HAPPY_FACE) - this.getProperty(CivilizationProperty.UNHAPPY_FACE)
        properties.setProperty(CivilizationProperty.HAPPINESS, happiness)

        // 调整玩家的不满意工人数
        this.checkUnhappyWorkers()

        // 结算额外的战术牌效果和军事力奖励值
        this.refreshMilitaryBonus()
    }

    /**
     * 玩家移除牌
     * @param card
     * @return
     */
    fun removeCard(card: TTACard): TTAProperty {
        var res = TTAProperty()
        val usedca = properties.getProperty(CivilizationProperty.CIVIL_ACTION) - this.availableCivilAction
        val usedma = properties.getProperty(CivilizationProperty.MILITARY_ACTION) - this.availableMilitaryAction
        when (card.cardType) {
            CardType.PRODUCTION, CardType.BUILDING, CardType.UNIT, CardType.WONDER, CardType.SPECIAL, CardType.EVENT ->// EVENT中只可能出现领土牌
                // 将该卡牌从玩家的已打出建筑牌堆中移除
                buildings.removeCard(card)
            CardType.GOVERMENT ->
                // 如果卡牌是政府牌,则只有是当前政府时,才会移除该政府
                if (this.government === card) {
                    // 将当前政府添加到过去政府列表中
                    this.government = null
                } else {
                    return res
                }
            CardType.LEADER ->
                // 如果卡牌是领袖牌,则只有是当前领袖时,才会移除该领袖
                if (this.leader === card) {
                    this.leader = null
                } else {
                    return res
                }
            CardType.TACTICS ->
                // 如果卡牌是战术牌,则只有是当前战术时,才会移除该战术
                if (this.tactics === card) {
                    this.tactics = null
                } else {
                    return res
                }
            CardType.WAR -> {
                // 如果是战争,则检查是否当前玩家打出的战争,如果是则将其置空
                if (this.war === card) {
                    this.war = null
                }
                buildings.removeCard(card)
            }
            CardType.PACT -> {
                // 如果是条约,则检查是否当前玩家打出的条约,如果是则将其置空
                if (this.pact === card) {
                    this.pact = null
                }
                buildings.removeCard(card)
            }
            else -> return res
        }
        // 处理添加卡牌时的事件
        res = this.onCardChange(null, card, usedca, usedma)
        // 刷新属性

        return res
    }

    /**
     * 玩家失去手牌
     * @param cards
     */
    fun removeHand(cards: List<TTACard>) {
        val group = cards.groupBy(TTACard::actionType)
        group[ActionType.CIVIL]?.let(this.civilHands::removeCards)
        group[ActionType.MILITARY]?.let(this.militaryHands::removeCards)
    }

    override fun reset() {
        super.reset()
        this.gameRank = null
        this.tokenPool.clear()
        this.abilityManager.clear()
        this.tempResManager.clear()
        this.civilHands.clear()
        this.militaryHands.clear()
        this.properties.clear()
        this.roundTempParam.clear()
        this.points.clear()
        this.buildings.clear()
        this.government = null
        this.leader = null
        this.uncompletedWonder = null
        this.tactics = null
        this.war = null
        this.pact = null
        this.resigned = false
        this.ageDummyCard = TTACard()
    }

    /**
     * 重置玩家的行动点
     */
    fun resetActionPoint() {
        this.government!!.whites = this.getProperty(CivilizationProperty.CIVIL_ACTION)
        this.government!!.reds = this.getProperty(CivilizationProperty.MILITARY_ACTION)
    }

    /**
     * @return
     */
    fun resetRoundFlags() {
        for (ca in abilityManager.allFlaggedAbilities) {
            this.params.setRoundParameter(ca.abilityType, ca.limit)
        }
    }

    /**
     * 按照当前文明取得文明点数
     * @return
     */
    fun scoreCulturePoint(): Int {
        var res = this.properties.getProperty(CivilizationProperty.CULTURE)
        if (this.abilityManager.hasAbilitiy(CivilAbilityType.PA_HUBATIAN)) res *= 4
        return res
    }

    /**
     * 按照当前科技取得科技点数
     * @return
     */
    fun scoreSciencePoint(): Int {
        return this.properties.getProperty(CivilizationProperty.SCIENCE)
    }

    /**
     * 设置玩家已经拿过指定等级领袖的参数
     * @param level
     */
    fun setHasLeader(level: Int) {
        // 设置该时代leader已经拿过的参数
        this.params.setGameParameter(PARAM_LEADER + level, true)
    }

    /**
     * @param limit
     * @return
     */
    fun testRank(property: CivilizationProperty, weekest: Boolean, limit: Int): Boolean {
        val rank = this.getRank(property, weekest)
        return rank <= if (limit == 2 && this.realPlayerNumber == 2) 1 else limit
    }

    fun testAboveAll(property: CivilizationProperty): Boolean {
        return gameRank!!.getPlayers().filter { it !== this }.all { it.properties.getProperty(property) < this.properties.getProperty(property) }
    }

    /**
     * 玩家升级建筑/部队
     * @param fromCard
     * @param toCard
     * @return 返回所有状态变化过的卡牌列表
     */
    fun upgrade(fromCard: CivilCard, toCard: CivilCard) {
        fromCard.addWorkers(-1)
        toCard.addWorkers(1)
    }

    /**
     * 提取资源和粮食用的类
     * @author F14eagle
     */
    inner class ResourceTaker(private val property: CivilizationProperty) {
        private val cs: MutableList<ResourceCounter>

        init {
            cs = buildings.cards.asSequence().filterIsInstance<CivilCard>().filter { it.property.getProperty(this.property) > 0 }.map(this::ResourceCounter).sorted().toMutableList()
        }

        /**
         * 实际操作配件数量和情况,并返回所有变化过的卡牌列表
         * @return
         */
        fun execute(): ProductionInfo {
            val res = ProductionInfo(this.property)
            // 先处理支付资源的情况
            this.cs.filter { it.payNum > 0 }
                    // 将卡牌上的蓝色指示物移除,并添加到资源池
                    .forEach { tokenPool.addAvailableBlues(it.card.addBlues(-it.payNum)) }
            // 再处理找零的情况,从大找到小
            this.cs.reversed().filter { it.retNum > 0 }.forEach { it.retNum = it.card.addBlues(-tokenPool.addAvailableBlues(-it.retNum)) }
            this.cs.map { it.card to it.retNum - it.payNum }.toMap(res)
            return res
        }

        /**
         * 取得拿取资源后,将返回资源库的蓝色指示物个数
         * @return
         */
        val returnedBlues: Int
            get() = this.cs.sumBy { it.payNum - it.retNum }

        /**
         * 按照算法得到资源
         * @param num
         */
        fun putResource(num: Int): ResourceTaker {
            // 处理特斯拉死亡后不能在实验室上添加蓝点（可以减少）
            if (this@TTAPlayer.params.getBoolean(CivilAbilityType.PA_TESLA_ABILITY) && !this@TTAPlayer.abilityManager.hasAbilitiy(CivilAbilityType.PA_TESLA_ABILITY)) {
                cs.removeIf { rc -> rc.card.cardSubType == CardSubType.LAB }
            }
            var rest = num
            this.cs.reversed().takeWhile { c ->
                val take = rest / c.value
                if (take > 0) {
                    c.retNum += take
                    rest -= take * c.value
                }
                rest > 0
            }
            return this
        }

        /**
         * 按照算法拿取资源
         * @param num
         */
        fun takeResource(num: Int): ResourceTaker {
            var rest = num
            // 循环计算使用资源的数量
            this.cs.takeWhile { c ->
                val take = minOf((rest - 1) / c.value + 1, c.availableNum)
                c.payNum += take
                rest -= take * c.value
                rest > 0
            }.reversed().takeWhile { c ->
                val take = minOf(-rest / c.value, c.payNum)
                c.payNum -= take
                rest += take * c.value
                rest < 0
            }
            if (rest < 0) {
                // 需要处理找零的情况
                return this.putResource(-rest)
            }
            return this
        }


        override fun toString(): String = this.cs.joinToString { "${it.value} 可用:${it.availableNum} 使用:${it.payNum} 找回:${it.retNum}\n" }

        inner class ResourceCounter(val card: CivilCard) : Comparable<ResourceCounter> {
            /**
             * 每个单位表示的值
             */
            val value: Int = this.card.property.getProperty(property)
            /**
             * 可用的数量
             */
            val availableNum: Int = this.card.blues
            /**
             * 使用的数量
             */
            var payNum: Int = 0
            /**
             * 找回的数量
             */
            var retNum: Int = 0

            override fun compareTo(other: ResourceCounter) = when {
                value > other.value -> 1
                value < other.value -> -1
                card.level < other.card.level -> 1
                card.level > other.card.level -> -1
                else -> 0
            }
        }
    }

    companion object {
        private const val PARAM_LEADER = "LEADER_"
    }

    fun getFieldCards(con: ICondition<TTACard?>): List<TTACard> {
        return gameRank!!.players.flatMap(TTAPlayer::allPlayedCard).filter { con test it }
    }


}