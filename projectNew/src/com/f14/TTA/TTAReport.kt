package com.f14.TTA

import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.card.*
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.CivilizationProperty
import com.f14.bg.report.BgCacheReport

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class TTAReport(bg: TTA) : BgCacheReport<TTAPlayer>(bg) {
    val costActions: MutableMap<TTAPlayer, String> = HashMap()

    override fun action(player: TTAPlayer, message: String) {
        if (costActions.containsKey(player)) {
            val str = costActions.getValue(player)
            costActions.remove(player)
            super.action(player, str + message)
        } else {
            super.action(player, message)
        }
    }

//    override fun action(player: TTAPlayer, msgPublic: String, msgPrivate: String) {
//        if (costActions.containsKey(player)){
//            val str = costActions.getValue(player)
//            costActions.remove(player)
//            super.action(player, str + msgPublic, str + msgPrivate)
//        }else {
//            super.action(player, msgPublic, msgPrivate)
//        }
//    }

    /**
     * 玩家竞得地主
     * @param player
     * @param totalValue
     */
    fun bidTichu(player: TTAPlayer, totalValue: Int) {
        this.action(player, "以 $totalValue 分为代价成为地主", true)
    }

    /**
     * 时代更迭
     * @param currentAge
     */
    fun newAge(currentAge: Int) {
        this.line()
        this.info("${TTACard.getAgeString(currentAge)}时代 开始", true)
        this.line()
    }

    /**
     * 玩家选择是否接受条约
     * @param player
     * @param card
     * @param accept
     */
    fun playerAcceptPact(player: TTAPlayer, card: TTACard, accept: Boolean) {
        val act = if (accept) "接受" else "拒绝"
        val str = "${act}了条约${card.reportString}的签订"
        this.action(player, str)
    }

    /**
     * 玩家使用卡牌能力
     * @param player
     * @param abilityType
     */
    fun playerActiveCard(player: TTAPlayer, abilityType: CivilAbilityType) {
        val card = player.abilityManager.getAbilityCard(abilityType)
        if (card != null) this.playerActiveCard(player, card)
    }

    /**
     * 玩家使用卡牌能力
     * @param player
     * @param card
     */
    fun playerActiveCard(player: TTAPlayer, card: TTACard) {
        card.activeAbility?.let {
            if (it.isUseActionPoint) this.playerCostAction(player, it.actionType, it.actionCost)
        }
        this.action(player, "使用了" + card.reportString + "的能力")
    }

    /**
     * 玩家使用卡牌能力
     * @param player
     * @param target
     * @param card
     * @param actionType
     * @param actionCost
     */
    fun playerActiveCard(player: TTAPlayer, target: TTAPlayer, card: TTACard, actionType: ActionType?, actionCost: Int) {
        if (actionType != null) {
            this.playerCostAction(player, actionType, actionCost)
        }
        this.action(player, "对" + target.reportString + "使用了" + card.reportString)
    }

    /**
     * 玩家使用卡牌能力(缓存)
     * @param player
     * @param abilityType
     */
    fun playerActiveCardCache(player: TTAPlayer, abilityType: CivilAbilityType) {
        val card = player.abilityManager.getAbilityCard(abilityType)
        if (card != null) this.playerActiveCardCache(player, card)
    }

    /**
     * 玩家使用卡牌能力(缓存)
     * @param player
     * @param card
     */
    fun playerActiveCardCache(player: TTAPlayer, card: TTACard) {
        card.activeAbility?.let {
            if (it.isUseActionPoint) this.playerCostAction(player, it.actionType, it.actionCost)
        }
        this.addAction(player, "使用了" + card.reportString + "的能力")
    }

    /**
     * 玩家调整蓝色标志物
     * @param player
     * @param num
     */
    fun playerAddBlueToken(player: TTAPlayer, num: Int) {
        when {
            num > 0 -> this.addAction(player, "得到" + num + "个蓝色标志物")
            num < 0 -> this.addAction(player, "失去" + -num + "个蓝色标志物")
        }
    }

    /**
     * 玩家得到打出的牌
     * @param player
     * @param card
     */
    fun playerAddCard(player: TTAPlayer, card: TTACard) {
        this.action(player, "得到" + card.reportString)
    }

    /**
     * 玩家得到打出的牌
     * @param player
     * @param card
     */
    fun playerAddCardCache(player: TTAPlayer, card: TTACard) {
        this.addAction(player, "得到" + card.reportString)
    }

    /**
     * 玩家调整内政行动点
     * @param player
     * @param num
     */
    fun playerAddCivilAction(player: TTAPlayer, num: Int) {
        when {
            num > 0 -> this.addAction(player, "得到" + num + "个内政行动点")
            num < 0 -> this.addAction(player, "失去" + -num + "个内政行动点")
        }
    }

    /**
     * 玩家调整文明点数
     * @param player
     * @param num
     */
    fun playerAddCulturePoint(player: TTAPlayer, num: Int) {
        when {
            num > 0 -> this.addAction(player, "得到" + num + "个文明点数")
            num < 0 -> this.addAction(player, "失去" + -num + "个文明点数")
        }
    }

    /**
     * 玩家添加事件卡
     * @param player
     * @param addedCard
     * @param eventCard
     */
    fun playerAddEvent(player: TTAPlayer, addedCard: TTACard, eventCard: MilitaryCard) {
        val msgPublic = "将一张${TTACard.getAgeString(addedCard.level)}时代的牌放入未来事件牌堆,然后从当前事件牌堆中翻开了${eventCard.reportString}"
        val msgPrivate = "将${addedCard.reportString}放入未来事件牌堆,然后从当前事件牌堆中翻开了${eventCard.reportString}"
        this.action(player, msgPublic, msgPrivate)
    }

    /**
     * 玩家调整食物
     * @param player
     * @param num
     */
    fun playerAddFood(player: TTAPlayer, num: Int) {
        when {
            num > 0 -> this.addAction(player, "得到" + num + "个食物")
            num < 0 -> this.addAction(player, "失去" + -num + "个食物")
        }
    }

    /**
     * 玩家调整军事行动点
     * @param player
     * @param num
     */
    fun playerAddMilitaryAction(player: TTAPlayer, num: Int) {
        when {
            num > 0 -> this.addAction(player, "得到" + num + "个军事行动点")
            num < 0 -> this.addAction(player, "失去" + -num + "个军事行动点")
        }
    }

    /**
     * 玩家按照property中的资源/食物/文明点数/科技点数调整对应的数值
     * @param player
     * @param property
     */
    fun playerAddPoint(player: TTAPlayer, property: TTAProperty) {
        var p = property.getProperty(CivilizationProperty.SCIENCE)
        if (p != 0) {
            this.playerAddSciencePoint(player, p)
        }
        p = property.getProperty(CivilizationProperty.CULTURE)
        if (p != 0) {
            this.playerAddCulturePoint(player, p)
        }
        p = property.getProperty(CivilizationProperty.FOOD)
        if (p != 0) {
            this.playerAddFood(player, p)
        }
        p = property.getProperty(CivilizationProperty.RESOURCE)
        if (p != 0) {
            this.playerAddResource(player, p)
        }
    }

    /**
     * 玩家调整资源
     * @param player
     * @param num
     */
    fun playerAddResource(player: TTAPlayer, num: Int) {
        when {
            num > 0 -> this.addAction(player, "得到" + num + "个资源")
            num < 0 -> this.addAction(player, "失去" + -num + "个资源")
        }
    }

    /**
     * 玩家调整科技点数
     * @param player
     * @param num
     */
    fun playerAddSciencePoint(player: TTAPlayer, num: Int) {
        when {
            num > 0 -> this.addAction(player, "得到" + num + "个科技点数")
            num < 0 -> this.addAction(player, "失去" + -num + "个科技点数")
        }
    }

    /**
     * 玩家调整黄色标志物
     * @param player
     * @param num
     */
    fun playerAddYellowToken(player: TTAPlayer, num: Int) {
        if (num > 0) {
            this.addAction(player, "得到" + num + "个黄色标志物")
        } else if (num < 0) {
            this.addAction(player, "失去" + -num + "个黄色标志物")
        }
    }

    /**
     * 玩家埋牌到目标牌下
     * @param player
     * @param card
     * @param destCard 目标牌
     */
    fun playerAttachCard(player: TTAPlayer, card: TTACard, destCard: TTACard) {
        this.addAction(player, "把" + card.reportString + "叠放在" + destCard.reportString + "下方")
    }

    /**
     * 玩家殖民出价
     * @param player
     * @param totalValue
     */
    fun playerBid(player: TTAPlayer, totalValue: Int) {
//        val sb = StringBuilder()
        if (totalValue > 0) {
            this.action(player, "出价$totalValue")
        } else {
            this.action(player, "放弃")
        }
//        this.action(player, sb.toString())
    }

    /**
     * 玩家打出奖励牌
     */
    fun playerBonusCardPlayed(player: TTAPlayer, cards: List<TTACard>) {
        if (cards.isNotEmpty()) {
//            val sbPublic = StringBuilder("使用了")
//            val sbPrivate = StringBuilder("使用了")
            val group = cards.groupBy { it.cardType == CardType.DEFENSE_BONUS }
            val strPublic = group.entries.joinToString("和") { (k, v) ->
                if (k) {
                    v.joinToString(",", transform = TTACard::reportString)
                } else {
                    "${v.count()}张军事牌"
                }
            }
            val strPrivate = cards.joinToString(",", transform = TTACard::reportString)
            this.addAction(player, strPublic, strPrivate)
//            var num = 0
//            for (c in cards) {
//                if (c.cardType != CardType.DEFENSE_BONUS) {
//                    num += 1
//                } else {
//                    sbPublic.append(c.reportString)
//                }
//                sbPrivate.append(c.reportString)
//            }
//            if (num > 0) {
//                if (num < cards.size) {
//                    sbPublic.append("和")
//                }
//                sbPublic.append(num).append("张军事牌")
//            }
//            this.addAction(player, sbPublic.toString(), sbPrivate.toString())
        }
    }

    /**
     * 玩家废除条约
     * @param player
     * @param card
     */
    fun playerBreakPact(player: TTAPlayer, card: TTACard) {
        this.action(player, "废除了条约" + card.reportString)
    }

    /**
     * 玩家建造建筑/部队
     * @param player
     * @param actionType
     * @param actionCost
     * @param card
     * @param num
     */
    fun playerBuild(player: TTAPlayer, actionType: ActionType, actionCost: Int, card: TTACard, num: Int) {
        if (actionCost != 0) {
            this.playerCostAction(player, actionType, actionCost)
//            this.action(player, "消耗了${actionCost}个${actionType.chinese}行动点建造了${num}个${card.reportString}")
        }
        this.playerBuild(player, card, num)
    }

    /**
     * 玩家建造建筑/部队
     * @param player
     * @param card
     * @param num
     */
    fun playerBuild(player: TTAPlayer, card: TTACard, num: Int) {
        this.action(player, "建造了${num}个${card.reportString}")
    }

    /**
     * 玩家建造建筑/部队(记录在缓存)
     * @param player
     * @param card
     * @param num
     */
    fun playerBuildCache(player: TTAPlayer, card: TTACard, num: Int) {
        this.insertAction(player, "建造了${num}个${card.reportString}")
    }

    /**
     * 玩家建造奇迹
     * @param player
     * @param actionType
     * @param actionCost
     * @param card
     * @param buildStep
     */
    fun playerBuildWonder(player: TTAPlayer, actionType: ActionType, actionCost: Int, card: WonderCard, buildStep: Int) {
        if (actionCost != 0) {
            this.playerCostAction(player, actionType, actionCost)
//            "消耗了${actionCost}个${actionType.chinese}行动点建造了${card.reportString}的${buildStep}个步骤"
        }

        if (card.isComplete) {
            this.addAction(player, "完成了${card.reportString}的建造!")
        }
        this.action(player, "建造了${card.reportString}的${buildStep}个步骤")
    }

    /**
     * 玩家建造奇迹(记录在缓存)
     * @param player
     * @param card
     * @param buildStep
     */
    fun playerBuildWonderCache(player: TTAPlayer, card: WonderCard, buildStep: Int) {
        this.insertAction(player, "建造了${card.reportString}的${buildStep}个步骤")
        if (card.isComplete) {
            this.addAction(player, "完成了${card.reportString}的建造!")
        }
    }

    /**
     * 玩家暴动提示
     * @param player
     */
    fun playerCannotDrawWarning(player: TTAPlayer) {
        this.action(player, "发生暴动,不能摸军事牌!")
    }

    /**
     * 玩家改变政府
     * @param player
     * @param revolution
     * @param card
     */
    fun playerChangeGoverment(player: TTAPlayer, revolution: Boolean, card: GovermentCard) {
        val changeStr = if (revolution) "使用革命的方式" else "使用和平演变的方式"
        val str = "${changeStr}将政府更换成了${card.reportString}"
        this.action(player, str)
    }

    /**
     * 玩家改变政府
     * @param player
     * @param revolution
     * @param card
     */
    fun playerChangeGovermentCache(player: TTAPlayer, revolution: Boolean, card: GovermentCard) {
        val changeStr = if (revolution) "使用革命的方式" else "使用和平演变的方式"
        val str = "${changeStr}将政府更换成了${card.reportString}"
        this.insertAction(player, str)
    }

    /**
     * 玩家选择条约方
     * @param player
     * @param card
     * @param pactSide
     */
    fun playerChoosePactSide(player: TTAPlayer, card: TTACard, pactSide: String) {
        this.action(player, "选择成为条约${card.reportString}的 $pactSide 方")
    }

    /**
     * 学习阵型
     * @param player
     * @param actionType
     * @param actionCost
     * @param card
     */
    fun playerCopyTatics(player: TTAPlayer, actionType: ActionType, actionCost: Int, card: TTACard) {
        if (actionCost != 0) {
            this.playerCostAction(player, actionType, actionCost)
//            sb.append("消耗了").append(actionCost).append("个").append(actionType.chinese).append("行动点")
        }
        this.action(player, "学习了" + card.reportString)
//        this.action(player, sb.toString())
    }

    /**
     * 玩家消耗行动点数
     * @param player
     * @param actionType
     * @param actionCost
     * @param revolution
     */
    fun playerCostAction(player: TTAPlayer, actionType: ActionType, actionCost: Int, revolution: Boolean = false) {
        val str = when {
            revolution -> "消耗了所有的${actionType.chinese}行动点"
            actionCost != 0 -> "消耗了${actionCost}个${actionType.chinese}行动点"
            else -> return
        }
//        this.addAction(player, str)
        this.costActions[player] = str
    }

    /**
     * 玩家按照property中的资源/食物/文明点数/科技点数支付对应的数值
     * @param player
     * @param text
     */
    fun playerCostPoint(player: TTAPlayer, cost: TTAProperty, text: String) {
        arrayOf(CivilizationProperty.SCIENCE, CivilizationProperty.CULTURE, CivilizationProperty.FOOD, CivilizationProperty.RESOURCE).forEach { property ->
            val p = cost.getProperty(property)
            if (p > 0) {
                this.addAction(player, "$text${p}个${property.propertyName}")
            }
        }
    }

    /**
     * 玩家失去人口
     * @param player
     * @param num
     */
    fun playerDecreasePopulation(player: TTAPlayer, num: Int) {
        if (num > 0) {
            this.action(player, "失去了${num}个人口")
        }
    }

    /**
     * 玩家失去人口(缓存输出)
     * @param player
     * @param num
     */
    fun playerDecreasePopulationCache(player: TTAPlayer, num: Int) {
        if (num > 0) {
            this.addAction(player, "失去了${num}个人口")
        }
    }

    /**
     * 玩家摧毁建筑/部队
     * @param player
     * @param actionType
     * @param actionCost
     * @param card
     * @param num
     */
    fun playerDestory(player: TTAPlayer, actionType: ActionType, actionCost: Int, card: TTACard, num: Int) {
        if (actionCost != 0) {
            this.playerCostAction(player, actionType, actionCost)
//            sb.append("消耗了").append(actionCost).append("个").append(actionType.chinese).append("行动点")
        }
        this.action(player, "摧毁了${num}个${card.reportString}")
    }

    /**
     * 玩家摧毁建筑/部队
     * @param player
     * @param detail
     */
    fun playerDestroy(player: TTAPlayer, detail: Map<TechCard, Int>) {
        if (detail.isNotEmpty()) {
            val str = detail.filterValues { it > 0 }.entries.joinToString(",", "摧毁了") { (c, n) -> "${n}个${c.reportString}" }
            this.addAction(player, str)
        }
    }

    /**
     * 玩家摧毁建筑/部队
     * @param player
     * @param card
     * @param num
     */
    fun playerDestroy(player: TTAPlayer, card: TechCard, num: Int) {
        this.addAction(player, "摧毁了${num}个${card.reportString}")
    }

    /**
     * 玩家弃军事手牌
     * @param player
     */
    fun playerDiscardMilitaryHand(player: TTAPlayer, cards: List<TTACard>) {
        val strPublic = "弃了" + cards.size + "张军事牌"
        val strPrivate = cards.joinToString(",", "弃了", transform = TTACard::reportString)
        this.action(player, strPublic, strPrivate)
    }

    /**
     * 玩家摸军事牌
     * @param player
     */
    fun playerDrawMilitary(player: TTAPlayer, cards: List<TTACard>) {
        val strPublic = "摸了${cards.size}张军事牌"
        val strPrivate = cards.joinToString(",", "摸了", transform = TTACard::reportString)
        this.addAction(player, strPublic, strPrivate)
    }

    /**
     * 玩家结束行动阶段
     * @param player
     */
    fun playerEndAction(player: TTAPlayer) {
        this.action(player, "结束了行动阶段")
    }

    /**
     * 玩家结束政治行动阶段
     * @param player
     */
    fun playerEndPoliticalPhase(player: TTAPlayer) {
        this.action(player, "结束了政治行动阶段")
    }

    /**
     * 处理临时资源和临时科技点数
     * @param player
     * @param property
     */
    fun playerExecuteTemporaryResource(player: TTAPlayer, property: TTAProperty) {
        arrayOf(CivilizationProperty.SCIENCE, CivilizationProperty.RESOURCE).forEach { prop ->
            val p = property.getProperty(prop)
            when {
                p > 0 -> this.addAction(player, "得到" + p + "个临时" + prop.propertyName)
                p < 0 -> this.addAction(player, "花费" + -p + "个临时" + prop.propertyName)
            }
        }
    }

    /**
     * 和平城交换事件
     * @param player
     * @param card
     * @param exCard
     */
    fun playerExtrangeEvent(player: TTAPlayer, card: TTACard, exCard: TTACard) {
        val msgPublic = "将一张${card.ageString}时代的牌交换了一张${exCard.ageString}时代的牌"
        val msgPrivate = "将${card.reportString}交换了${exCard.reportString}"
        this.addAction(player, msgPublic, msgPrivate)
    }

    /**
     * 玩家得到殖民地
     * @param player
     * @param territory
     * @param totalValue
     */
    fun playerGetColony(player: TTAPlayer, territory: EventCard, totalValue: Int) {
        if (totalValue != 0) {
            this.action(player, "以总数${totalValue}的殖民点数夺得了${territory.reportString}")
        } else {
            this.action(player, "夺得了${territory.reportString}")
        }
    }

    /**
     * 玩家扩张人口
     * @param player
     * @param actionCost
     * @param num
     */
    fun playerIncreasePopulation(player: TTAPlayer, actionCost: Int, num: Int) {
        if (num > 0) {
            if (actionCost != 0) {
                this.playerCostAction(player, ActionType.CIVIL, actionCost)
            }
            this.action(player, "扩张了${num}个人口")
        }
    }

    /**
     * 玩家扩张人口(缓存输出)
     * @param player
     * @param num
     */
    fun playerIncreasePopulationCache(player: TTAPlayer, num: Int) {
        if (num > 0) {
            this.insertAction(player, "扩张了${num}个人口")
        }
    }

    /**
     * 玩家出牌
     * @param player
     * @param actionType
     * @param actionCost
     * @param card
     */
    fun playerPlayCard(player: TTAPlayer, actionType: ActionType?, actionCost: Int, card: TTACard) {
        if (actionCost != 0) {
            this.playerCostAction(player, actionType!!, actionCost)
        }
        this.action(player, "打出了${card.reportString}")
    }

    /**
     * 玩家出牌
     * @param player
     * @param card
     */
    fun playerPlayCardCache(player: TTAPlayer, card: TTACard) {
        this.insertAction(player, "打出了${card.reportString}")
    }

    /**
     * 玩家生产食物
     * @param player
     * @param num
     */
    fun playerProduceFood(player: TTAPlayer, num: Int) {
        if (num > 0) {
            this.addAction(player, "生产${num}个食物")
        }
    }

    /**
     * 玩家生产资源
     * @param player
     * @param num
     */
    fun playerProduceResource(player: TTAPlayer, num: Int) {
        if (num > 0) {
            this.addAction(player, "生产${num}个资源")
        }
    }

    /**
     * 玩家失去打出的牌
     * @param player
     * @param card
     */
    fun playerRemoveCard(player: TTAPlayer, card: TTACard) {
        this.action(player, "失去了${card.reportString}")
    }

    /**
     * 玩家失去打出的牌
     * @param player
     * @param card
     */
    fun playerRemoveCardCache(player: TTAPlayer, card: TTACard) {
        this.addAction(player, "失去了${card.reportString}")
    }

    /**
     * 玩家弃手牌
     * @param player
     * @param cards
     */
    fun playerRemoveHand(player: TTAPlayer, cards: List<TTACard>) {
        cards.groupBy(TTACard::actionType).forEach { (t, list) ->
            if (t == ActionType.MILITARY) {
                this.playerDiscardMilitaryHand(player, list)
            } else {
                this.action(player, cards.joinToString(",", "弃了", transform = TTACard::reportString))
            }
        }
//        if (cards.isNotEmpty()) {
//            val sbPrivate = StringBuilder("弃了")
//            val sbPublic = StringBuilder("弃了")
//            var num = 0
//            for (c in cards) {
//                if (c.actionType == ActionType.MILITARY) {
//                    num += 1
//                } else {
//                    sbPublic.append(c.reportString)
//                }
//                sbPrivate.append(c.reportString)
//            }
//            if (num > 0) {
//                if (num < cards.size) {
//                    sbPublic.append("和")
//                }
//                sbPublic.append(num).append("张军事牌")
//            }
//            this.action(player, sbPublic.toString(), sbPrivate.toString())
//        }
    }

    /**
     * 玩家体面退出游戏
     */
    fun playerResign(player: TTAPlayer) {
        this.action(player, "选择了体面退出游戏!", true)
    }

    /**
     * 玩家生产回合
     * @param player
     */
    fun playerRoundScore(player: TTAPlayer) {
        this.action(player, "进行了生产阶段")
    }

    /**
     * 玩家牺牲部队
     * @param units
     */
    fun playerSacrificeUnit(player: TTAPlayer, units: Map<TechCard, Int>) {
        val str = units.filterValues { it > 0 }.entries.joinToString(",", "牺牲了") { (c, n) -> "${n}个${c.reportString}" }
        this.addAction(player, str)
    }

    /**
     * 玩家当前文明点数
     * @param player
     * @param num
     */
    fun playerScoreCulturePoint(player: TTAPlayer, num: Int) {
        if (num > 0) {
            this.addAction(player, "生产${num}个文明点数")
        }
    }

    /**
     * 玩家当前科技点数
     * @param player
     * @param num
     */
    fun playerScoreSciencePoint(player: TTAPlayer, num: Int) {
        if (num > 0) {
            this.addAction(player, "生产${num}个科技点数")
        }
    }

    /**
     * 玩家从摸牌区得到卡牌
     * @param player
     * @param actionCost
     * @param card
     */
    fun playerTakeCard(player: TTAPlayer, actionCost: Int, idx: Int, card: TTACard) {
        if (actionCost != 0) {
            this.playerCostAction(player, ActionType.CIVIL, actionCost)
//            sb.append("消耗了").append(actionCost).append("个内政行动点")
        }
        this.action(player, "拿取了${card.getReportString(idx + 1)}")
    }

    /**
     * 玩家失去卡牌
     * @param player
     * @param card
     */
    fun playerTakeCardCache(player: TTAPlayer, card: TTACard, idx: Int) {
        this.addAction(player, "从巨轮上移除了${card.getReportString(idx + 1)}")
    }

    /**
     * 玩家升级建筑/部队(记录在缓存)
     * @param player
     * @param actionType
     * @param actionCost
     * @param fromCard
     * @param toCard
     * @param num
     */
    fun playerUpgrade(player: TTAPlayer, actionType: ActionType, actionCost: Int, fromCard: TTACard, toCard: TTACard, num: Int) {
        if (actionCost != 0) {
            this.playerCostAction(player, actionType, actionCost)
//            sb.append("消耗了").append(actionCost).append("个").append(actionType.chinese).append("行动点")
        }
        this.action(player, "将${num}个${fromCard.reportString}升级为${toCard.reportString}")
    }

    /**
     * 玩家升级建筑/部队
     * @param player
     * @param fromCard
     * @param toCard
     * @param num
     */
    fun playerUpgradeCache(player: TTAPlayer, fromCard: TTACard, toCard: TTACard, num: Int) {
        this.insertAction(player, "将${num}个${fromCard.reportString}升级为${toCard.reportString}")
    }

    /**
     * 玩家暴动提示
     * @param player
     */
    fun playerUprisingWarning(player: TTAPlayer) {
        this.action(player, "发生暴动,跳过生产阶段!")
    }

    /**
     * 玩家的殖民奖励
     * @param player
     * @param colonyBonus
     */
    fun playerUseColoBonus(player: TTAPlayer, colonyBonus: Int) {
        this.addAction(player, "使用了${colonyBonus}点殖民奖励")
    }

    /**
     * 打印战争结果
     * @param player
     * @param target
     * @param card
     * @param playerTotal
     * @param targetTotal
     */
    fun printWarResult(player: TTAPlayer, target: TTAPlayer, card: TTACard, playerTotal: Int, targetTotal: Int, result: Int) {
        val kw = if (result > 0) "战胜了" else if (result < 0) "战败于" else "战平"
        this.action(player, "在${card.reportString}中以总军力 $playerTotal:$targetTotal$kw${target.reportString}")
    }

    /**
     * 公共阵型
     * @param player
     */
    fun publicTactics(player: TTAPlayer, card: TTACard) {
        this.action(player, "的${card.reportString}已可被学习")
    }

    /**
     * 提示卡牌列上的卡牌
     * @param cards
     */
    fun refreshCardRow(cards: Map<Int, List<Pair<TTACard, Int>>>) {
        if (cards.any()) {
            for (i in cards.keys.sorted()) {
                val list = cards.getValue(i)
                val cardsString = list.joinToString { (c, i) -> c.getReportString(i + 1) }
                this.info("巨轮上有卡牌($i):$cardsString")
            }
        } else {
            this.info("巨轮上没有卡牌")
        }
    }

    fun playerAddToken(player: TTAPlayer, property: TTAProperty) {
        mapOf(CivilizationProperty.YELLOW_TOKEN to this::playerAddYellowToken, CivilizationProperty.BLUE_TOKEN to this::playerAddBlueToken).forEach { (p, f) -> f(player, property.getProperty(p)) }
    }

    fun playerAddSelect(player: TTAPlayer, property: TTAProperty) {
        property.allFactProperties.forEach { (p, n) ->
            if (n > 0) {
                this.addAction(player, "选择了$n${p.propertyName}")
            }
        }
    }

    fun playerAddHand(player: TTAPlayer, card: TTACard) {
        this.action(player, "得到手牌${card.reportString}")
    }

    fun playerRemoveHand(player: TTAPlayer, card: TTACard) {
        this.action(player, "失去手牌${card.reportString}")
    }

}
