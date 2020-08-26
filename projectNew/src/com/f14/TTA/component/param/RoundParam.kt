package com.f14.TTA.component.param

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.TTAResourceManager
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.CardAbility
import com.f14.TTA.component.card.*
import com.f14.TTA.consts.*
import com.f14.TTA.executor.TTAGetAndPlayExecutor
import com.f14.TTA.executor.TTAZhugeliangExecutor
import com.f14.TTA.executor.event.TTAInstantAbilityExecutor
import com.f14.TTA.factory.TTAExecutorFactory
import com.f14.TTA.listener.GetTerritoryListener
import com.f14.TTA.listener.TTAConfirmListener
import com.f14.TTA.listener.TTARequestSelectListener
import com.f14.TTA.listener.TTARoundListener
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

class RoundParam(var gameMode: TTAGameMode, var listener: TTARoundListener, var player: TTAPlayer) {
    /**
     * 回合新拿到的卡牌列表
     */
    var newcards: MutableList<TTACard> = ArrayList()
    /**
     * 判断是否是第一个内政行动
     */
    var isFirstCivilAction = true
    /**
     * 判断是否是第一个军事行动
     */
    var isFirstMilitaryAction = true
    /**
     * 当前步骤
     */
    var currentStep = RoundStep.NONE
    /**
     * 已进行的政治行动数量
     */
    var politicalAction = 0
    /**
     * 是否需要弃军事牌,默认为需要
     */
    var needDiscardMilitary = true
    /**
     * 建造时是否警告过
     */
    var buildAlert = false

    /**
     * 添加临时资源的能力
     * @param ability
     */
    fun addTemplateResource(ability: CardAbility) {
        gameMode.report.playerExecuteTemporaryResource(player, ability.property)
//        var num = ability.property.getProperty(CivilizationProperty.RESOURCE)
//        gameMode.report.playerAddTemporaryResource(player, num)
//        num = ability.property.getProperty(CivilizationProperty.SCIENCE)
//        gameMode.report.playerAddTemporaryScience(player, num)
        player.tempResManager.addTemplateResource(ability)
    }

    /**
     * 完成建造后触发的方法
     * @param card
     */
    fun afterBuild(card: TTACard) {
        if (card.cardType == CardType.WONDER) {
            val wonder = card as WonderCard
            if (wonder.isComplete) {
                gameMode.game.playerCompleteWonder(player)
                (wonder.abilities.filter { it.abilityType == CivilAbilityType.PA_GET_TERRITORY }).forEach {
                    val listener = GetTerritoryListener(this, it)
                    val res = gameMode.insertListener(listener)

                    val cardId = res.getString("cardId")
                    if (cardId.isNotEmpty()) {
                        val territory = player.militaryHands.getCard(cardId)
                        // 玩家打出殖民地
                        gameMode.game.playerRemoveHand(player, territory)
                        gameMode.game.playerAddCardDirect(player, territory)
                        gameMode.report.playerAddCardCache(player, territory)

                        // 处理殖民地的INSTANT类型的事件能力
                        for (ability in (territory as EventCard).eventAbilities) {
                            val executor = TTAInstantAbilityExecutor(this, ability)
                            executor.trigPlayer = player
                            executor.execute()
                        }
                    }
                }
            }
        }
    }

    /**
     * 需要客户端回应的行动牌处理完成后触发的方法
     */
    fun afterPlayActionCard(actionCard: ActionCard?) {
        if (actionCard != null) {
            if (newcards.contains(actionCard)) {
                gameMode.report.playerActiveCardCache(player, CivilAbilityType.PLAY_NEW_ACTION_CARD)
                newcards.remove(actionCard)
                player.params.setRoundParameter(CivilAbilityType.PLAY_NEW_ACTION_CARD, 0)
            }
            if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_SILK_ROAD) || this.checkSilkAbility()) {
                player.params.setRoundParameter(CivilAbilityType.PA_SILK_ROAD, true)
            }
            gameMode.game.playerAddAction(player, actionCard.actionAbility.property)
            gameMode.game.playerRemoveHand(player, actionCard)
            gameMode.game.playerRequestEnd(player)
            gameMode.game.sendPlayerActivableCards(RoundStep.NORMAL, player)
        }
    }

    private fun checkSilkAbility(): Boolean {
        val wonder = player.uncompletedWonder ?: return false
        if (wonder.currentStep == 0) return false
        return wonder.abilities.any { it.abilityType == CivilAbilityType.PA_SILK_ROAD }
    }

    /**
     * 出牌完成后触发的方法
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun afterPlayCard(card: TTACard) {
        // 处理所有出牌后触发的能力
        for (ability in player.abilityManager.getAbilitiesByType(CivilAbilityType.PA_PLAY_CARD)) {
            if (ability.test(card)) {
                gameMode.game.playerAddPoint(player, ability.property)
                gameMode.game.playerAddToken(player, ability.property)
                gameMode.game.playerAddAction(player, ability.property)
                gameMode.game.playerDrawMilitaryCard(player, ability.property)
            }
        }
        for (ability in player.abilityManager.getAbilitiesByType(CivilAbilityType.PA_PLAY_CARD_ALTERNATE)) {
            if (ability.test(card)) {
                if (player.totalResource < 1) continue
                val listener = TTAConfirmListener(gameMode, player, "你是否支付1资源获得4文化分数？")
                val res = gameMode.insertListener(listener)
                if (res.getBoolean("confirm")) {
                    gameMode.game.playerAddPoint(player, ability.property)
                }
            }
        }
        for (ability in card.abilities) when (ability.abilityType) {
            CivilAbilityType.PA_GET_AND_PLAY -> TTAGetAndPlayExecutor(this, ability).execute()
            CivilAbilityType.PA_ZHUGELIANG -> TTAZhugeliangExecutor(this, ability).execute()
            else -> Unit
        }
        if (gameMode.cardBoard.extraCards.any { it.activeAbility?.abilityType == ActiveAbilityType.PA_NOBEL }) {
            val num = player.params.getInteger(ActiveAbilityType.PA_NOBEL)
            if (num == 1) {
                gameMode.game.playerAddCulturePoint(player, 4)
                gameMode.report.playerAddCulturePoint(player, 4)
            }
            player.params.setRoundParameter(ActiveAbilityType.PA_NOBEL, num + 1)
        }
//        card.abilities
//                .filter { it.abilityType == CivilAbilityType.PA_GET_AND_PLAY }
//                .map { TTAGetAndPlayExecutor(this, it) }
//                .forEach(TTAGetAndPlayExecutor::execute)
//        card.abilities
//                .filter { it.abilityType == CivilAbilityType.PA_ZHUGELIANG }
//                .map { TTAZhugeliangExecutor(this, it) }
//                .forEach(TTAZhugeliangExecutor::execute)
//        if (player.uncompletedWonder?.isComplete ?: false) {
//            gameMode.game.playerCompleteWonder(player)
//        }
    }

    /**
     * 拿牌后触发的方法
     * @param card
     */
    fun afterTakeCard(card: TTACard) {
        // 检查所有拿牌后触发的方法
        player.abilityManager.getAbilitiesByType(CivilAbilityType.PA_TAKE_CARD).filter { it.test(card) }.forEach { gameMode.game.playerAddPoint(player, it.property) }
        card.abilities.firstOrNull { it.abilityType == CivilAbilityType.PA_RED_CROSS }?.let { ability ->
            val destCard = gameMode.game.getResourceManager<TTAResourceManager>().getCardByNo("V2WEX303ADD")?.singleOrNull()
            if (destCard is WonderCard) {
                destCard.tokens = card.tokens
                gameMode.realPlayers.filter { it !== this.player }.forEach {
                    gameMode.game.playerAddCardDirect(it, destCard)
                }
            }
            gameMode.report.info("现在大家都可以建造红十字会!", true)
        }
    }

    /**
     * 完成升级后触发的方法
     * @param toCard
     * @return
     */
    fun afterUpgrade(toCard: TTACard) {
        // 如果使用了actionCard则从手牌中移除该卡,并关闭请求窗口
    }

    fun checkActionCardEnhance(actionCard: ActionCard) {
        checkActionCardEnhance(actionCard.actionAbility.property)
    }

    fun checkActionCardEnhance(property: TTAProperty) {
        property.clearAllBonus()
        for (a in player.abilityManager.getAbilitiesByType(CivilAbilityType.ADJUST_ACTION_CARD)) {
            property.allFactProperties.forEach { (k, v) ->
                val num = a.property.getProperty(k)
                if (num > 0) {
                    when {
                        v > 0 -> property.addPropertyBonus(k, num)
                        v < 0 -> property.addPropertyBonus(k, -num)
                    }
                }
            }
        }
        if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_SILK_ROAD) || this.checkSilkAbility()) {
            if (!player.params.getBoolean(CivilAbilityType.PA_SILK_ROAD)) {
                property.allFactProperties.forEach { (k, v) ->
                    when {
                        v > 0 -> property.addPropertyBonus(k, 1)
                        v < 0 -> property.addPropertyBonus(k, -1)
                    }
                }
            }
        }
    }

    fun checkAlert(card: TTACard) = if (buildAlert || !card.needWorker || !player.isWillUprising) true
    else {
        // 每次建造只会警告一次
        buildAlert = true
        gameMode.game.sendAlert(player, "你的人民生活在水深火热之中,如果再让他们干活,你就死定了!")
        false
    }

    /**
     * @param card
     * @return
     */
    fun checkBach(card: TechCard) = if (player.params.getInteger(CivilAbilityType.PA_UPGRADE_TO_THEATER) <= 0) false
    else player.abilityManager.getAbility(CivilAbilityType.PA_UPGRADE_TO_THEATER)!!.test(card)

    /**
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkBuildStep(leftStep: Int): Int {
        val ability = player.abilityManager.getAbility(CivilAbilityType.PA_WONDER_STEP)
        if (leftStep > 1 && ability != null) {
            val stepCard = player.abilityManager.getAbilityCard(CivilAbilityType.PA_WONDER_STEP)!!
            val maxStep = min(ability.buildStep, leftStep)
            val sels = (1..maxStep).joinToString(",")
            val param = gameMode.insertListener(TTARequestSelectListener(gameMode, player, stepCard, TTACmdString.REQUEST_SELECT, sels, "你可以建造奇迹的最多" + ability.buildStep + "步"))
            val sel = param.getInteger("sel")
            val buildStep = sel + 1
            CheckUtils.check(buildStep <= 0, "你至少要建造奇迹的 1 步!")
            CheckUtils.check(buildStep > ability.buildStep, "你最多可以建造奇迹的 " + ability.buildStep + " 步!")
            return buildStep
        }
        return 1
    }

    /**
     * 检查玩家是否可以革命
     * @param isVersion2
     * @return
     * @throws BoardGameException
     */

    @Throws(BoardGameException::class)
    fun checkCanRevolution(isVersion2: Boolean): ActionType {
        return if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_MILITARY_REVOLUTION)) {
            // 以军事行动点进行革命
            // 检查军事行动点是否已经使用过
            // 老版革命必须是该回合的首个内政/军事行动
            CheckUtils.check(!isVersion2 && !isFirstMilitaryAction, "以革命的方式改变政府,必须是当前回合唯一的军事行动!")
            CheckUtils.check(player.availableMilitaryAction < player.getProperty(CivilizationProperty.MILITARY_ACTION), "以革命的方式改变政府,必须保留所有的军事行动点!")
            ActionType.MILITARY
        } else {
            // 以内政行动点进行革命
            // 检查内政行动点是否已经使用过
            // 老版革命必须是该回合的首个内政/军事行动
            CheckUtils.check(!isVersion2 && !isFirstCivilAction, "以革命的方式改变政府,必须是当前回合唯一的内政行动!")
            CheckUtils.check(player.availableCivilAction < player.getProperty(CivilizationProperty.CIVIL_ACTION), "以革命的方式改变政府,必须保留所有的内政行动点!")
            ActionType.CIVIL
        }
    }

    /**
     * 检查牌是否是当前回合拿的,如果是,则抛出异常
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkNewlyCard(card: TTACard) {
        if (player.params.getInteger(CivilAbilityType.PLAY_NEW_ACTION_CARD) > 0) return
        CheckUtils.check(this.newcards.contains(card), "你不能在当前回合打这张刚拿的牌!")
    }

    /**
     * 判断当前是否是政治行动阶段
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkPoliticalPhase() {
        CheckUtils.check(currentStep != RoundStep.POLITICAL, "当前不是政治行动阶段,不能进行该行动!")
    }

    /**
     * 检查玩家是否拥有足够的科技点数,如果不够则抛出异常
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkResearchCost(card: TechCard, param: CostParam) {
        // 玩家最终检查
        player.checkEnoughResource(param.cost)
        // 科技协作检查
        player.abilityManager.getPactAbilitiesWithRelation(CivilAbilityType.PA_SCIENCE_ASSIST).filterKeys { it.test(card) }.mapNotNull { (ca, p) -> p.alian?.to(ca.amount) }.forEach { (p, i) -> CheckUtils.check(p.sciencePoint < i, "你的盟友没有足够的科技点数!") }
    }

    /**
     * 检查玩家是否可以对card进行升级的行动
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkUpgrade(card: TTACard) {
        when (card.cardType) {
            CardType.BUILDING, CardType.PRODUCTION, CardType.UNIT -> Unit
            else -> throw BoardGameException("你不能在这张卡牌上进行升级行动!")
        }
        val c = card as CivilCard
        CheckUtils.check(c.workers <= 0, "这个张卡牌上没有工人,不能升级!")
    }

    fun checkWillCard() {
        val card = player.willCard
        val ability = card?.activeAbility ?: return
        player.willCard = null
        if (ability.checkCanActive(RoundStep.RESIGNED, player)) {
            try {
                TTAExecutorFactory.createWillExecutor(this, card, ability).execute()
            } catch (e: BoardGameException) {
                e.printStackTrace()
                // player.sendException(mode.getGame().getRoom().id, e);
            }

        }

    }

    fun checkWillCard(player: TTAPlayer) {
        val param = this.listener.getParam<RoundParam>(player.position)
        param.checkWillCard()
    }

    /**
     * 新版1回合只能更换1次阵型
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkChangeTactic() {
        CheckUtils.check(gameMode.isVersion2 && player.params.getBoolean(CardType.TACTICS), "你这回合已经更换过阵型!")
    }

    /**
     * 取得玩家打特定科技牌时的费用
     * @param card
     * @return
     */
    fun getResearchCost(card: TechCard, bSecondary: Boolean, costModify: Int): CostParam {
        val param = CostParam()
        var cost = player.getResearchCost(card, bSecondary)
        // 科技协作能力
        cost += player.abilityManager.getPactAbilitiesWithRelation(CivilAbilityType.PA_SCIENCE_ASSIST).keys.sumBy { it.property.getProperty(CivilizationProperty.SCIENCE) }
        if (card.cardType == CardType.GOVERMENT && !bSecondary) {
            if (player.allPlayedCard.filter { it.cardType == CardType.BUILDING }.groupBy(TTACard::cardSubType).any { it.value.sumBy(TTACard::availableCount) == player.buildLimit }) {
                cost += player.abilityManager.getAbilitiesByType(CivilAbilityType.PA_PEACE_CHANGE_COST).sumBy { it.property.getProperty(CivilizationProperty.SCIENCE) }
            }
        }
        // 科技消耗最低为0
        cost = max(0, cost + costModify)
        param.cost.addProperty(CivilizationProperty.SCIENCE, cost)
        // 计算临时科技
        if (cost > 0 && this.currentStep == RoundStep.NORMAL) {
            gameMode.game.checkTemplateResource(player, card, param, CivilizationProperty.SCIENCE)
        }
        return param
    }

    /**
     * 取得玩家打特定科技牌时的费用
     * @param card
     * @return
     */

    fun getResearchCost(card: TechCard, costModify: Int): CostParam {
        return getResearchCost(card, false, costModify)
    }

    /**
     * 取得建造 建筑/农矿/部队 所用的实际费用
     * @param card
     * @param costModify 直接的资源修正值
     * @return
     */

    fun getResourceCost(card: TechCard, costModify: Int): CostParam {
        val param = CostParam()
        var cost = player.getBuildResourceCost(card)
        // 计算影响玩家建筑费用的全局能力[PA_BUILD_COST_GLOBAL]
        cost += gameMode.getPlayerAbilities(CivilAbilityType.PA_BUILD_COST_GLOBAL).filter { (k, v) -> k.test(card) && (k.effectSelf || player !== v) }.keys.sumBy { it.property.getProperty(CivilizationProperty.RESOURCE) }
        // 加上修正值
        cost = max(0, cost + costModify)
        param.cost.addProperty(CivilizationProperty.RESOURCE, cost)
        // 计算临时资源
        if (cost > 0 && this.currentStep == RoundStep.NORMAL) {
            gameMode.game.checkTemplateResource(player, card, param, CivilizationProperty.RESOURCE)
        }
        return param
    }

    /**
     * 取得建造奇迹所用的实际费用
     * @param card
     * @param step 建造步骤
     * @return
     */

    fun getResourceCost(card: WonderCard, step: Int, costModify: Int): CostParam {
        val param = CostParam()
        var cost = card.stepCostResource(step)
        if (card.abilities.any { it.abilityType == CivilAbilityType.PA_RED_CROSS }) {
            // 红十字会用粮食建造
            param.cost.addProperty(CivilizationProperty.FOOD, cost)
        } else {
            // 加上修正值
            cost = max(0, cost + costModify)
            // 如果费用已经等于0则不用再计算临时资源
            param.cost.addProperty(CivilizationProperty.RESOURCE, cost)
            if (cost > 0 && this.currentStep == RoundStep.NORMAL) {
                gameMode.game.checkTemplateResource(player, card, param, CivilizationProperty.RESOURCE)
            }
        }
        return param
    }

    /**
     * 取得升级 建筑/农矿/部队 所用的实际费用
     * @param fromCard
     * @param toCard
     * @return
     */

    fun getUpgradeResourceCost(fromCard: TechCard, toCard: TechCard, costModify: Int): CostParam {
        val param = CostParam()
        val fromCost = player.getBuildResourceCost(fromCard)
        val toCost = player.getBuildResourceCost(toCard)
        // 升级费用为两者建造费用的差
        var cost = toCost - fromCost
        // 计算影响玩家升级费用的全局能力
        cost += gameMode.getPlayerAbilities(CivilAbilityType.PA_UPGRADE_COST_GLOBAL).filter { (k, v) -> k.test(toCard) && (k.effectSelf || player !== v) }.keys.sumBy { it.property.getProperty(CivilizationProperty.RESOURCE) }
        // 加上修正值
        cost = max(0, cost + costModify)
        param.cost.addProperty(CivilizationProperty.RESOURCE, cost)
        // 如果费用已经等于0则不用再计算临时资源
        if (cost > 0) {
            gameMode.game.checkTemplateResource(player, toCard, param, CivilizationProperty.RESOURCE)
        }
        return param
    }

    /**
     * @param doDiscard
     */
    fun regroupCardRow(doDiscard: Boolean) {
        if (gameMode.game.regroupCardRow(doDiscard)) {
            for (player in gameMode.realPlayers) {
                this.checkWillCard(player)
            }
        }
    }

    /**
     * 玩家使用行动点
     * @param actionType 内政/军事
     * @param actionCost
     */
    fun useActionPoint(actionType: ActionType, actionCost: Int) {
        if (actionCost > 0) {
            if (actionType == ActionType.CIVIL) {
                gameMode.game.playerAddCivilAction(player, -actionCost, false)
                this.isFirstCivilAction = false
            } else {
                gameMode.game.playerAddMilitaryAction(player, -actionCost, false)
                this.isFirstMilitaryAction = false
            }
        }
    }

    /**
     * @param useBach
     */
    fun useBach(useBach: Boolean) {
        if (useBach) {
            val num = player.params.getInteger(CivilAbilityType.PA_UPGRADE_TO_THEATER)
            player.params.setRoundParameter(CivilAbilityType.PA_UPGRADE_TO_THEATER, num - 1)
            gameMode.report.playerActiveCardCache(player, CivilAbilityType.PA_UPGRADE_TO_THEATER)
        }
    }

    fun afterRrevolution(revolution: Int) {
        if (revolution == 0) {
            if (player.abilityManager.hasAbilitiy(CivilAbilityType.PA_NO_REVOLUTION)) {
                val leader = player.leader ?: return
                gameMode.game.playerRemoveCardDirect(player, leader)
                gameMode.report.playerRemoveCardCache(player, leader)
            }
        }
    }
}
