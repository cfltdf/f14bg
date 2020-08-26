package com.f14.TS.factory

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.action.ActionParam
import com.f14.TS.component.TSCard
import com.f14.TS.component.ability.TSAbility
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.component.condition.TSCountryConditionGroup
import com.f14.TS.consts.ActionType
import com.f14.TS.consts.SubRegion
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TrigType
import com.f14.TS.listener.initParam.*


/**
 * 初始化参数的工厂类

 * @author F14eagle
 */
object InitParamFactory {

    /**
     * 创建行动参数
     * @param player
     * @param card
     * @return
     */
    fun createActionInitParam(player: TSPlayer, card: TSCard?, trigType: TrigType?): ActionInitParam {
        val initParam = ActionInitParam()
        initParam.card = card
        initParam.listeningPlayer = player.superPower
        initParam.trigType = trigType
        return initParam
    }

    /**
     * 按照TS能力创建行动监听器初始化参数
     * @param gameMode
     * @param player
     * @param card
     * @param ability
     * @param trigType
     * @return
     */

    fun createActionInitParam(gameMode: TSGameMode, player: TSPlayer, card: TSCard?, ability: TSAbility, trigType: TrigType?): ActionInitParam {
        val initParam = ActionInitParam()
        initParam.card = card
        initParam.trigType = trigType
        initParam.targetPower = ability.actionParam.targetPower
        initParam.actionType = ability.actionParam.actionType
        initParam.num = ability.actionParam.num
        initParam.countryNum = ability.actionParam.countryNum
        initParam.limitNum = ability.actionParam.limitNum
        initParam.msg = ability.actionParam.descr
        initParam.isCanCancel = ability.actionParam.isCanCancel
        initParam.isCanPass = ability.actionParam.isCanPass
        initParam.isCanLeft = ability.actionParam.isCanLeft
        initParam.clazz = ability.actionParam.clazz
        initParam.conditionGroup = ability.countryCondGroup
        // 设置监听的目标玩家
        if (ability.trigPower != SuperPower.NONE) {
            initParam.listeningPlayer = gameMode.game.convertSuperPower(ability.trigPower, player)
        }
        return initParam
    }

    /**
     * 创建回合行动时放置影响力的初始化参数
     * @param player
     * @param card
     * @param op
     * @param trigType
     * @return
     */

    fun createAddInfluenceParam(player: TSPlayer, card: TSCard, op: Int, trigType: TrigType, isFreeAction: Boolean, conditionGroup: TSCountryConditionGroup): OPActionInitParam = OPActionInitParam().apply {
        this.listeningPlayer = player.superPower
        this.targetPower = player.superPower
        this.actionType = ActionType.ADD_INFLUENCE
        this.card = card
        this.trigType = trigType
        this.isCanCancel = true
        this.isCanPass = false
        this.num = op
        this.msg = "请用 {num}OP 放置影响力!"
        this.isFreeAction = isFreeAction
        this.conditionGroup = conditionGroup
    }

    /**
     * 按照TS能力创建卡牌行动监听器初始化参数
     * @param gameMode
     * @param player
     * @param card
     * @param ability
     * @param trigType
     * @return
     */

    fun createCardActionInitParam(gameMode: TSGameMode, player: TSPlayer, card: TSCard?, ability: TSAbility, trigType: TrigType?): CardActionInitParam {
        val initParam = CardActionInitParam()
        initParam.card = card
        initParam.trigType = trigType
        initParam.targetPower = ability.actionParam.targetPower
        initParam.num = ability.actionParam.num
        initParam.msg = ability.actionParam.descr
        initParam.isCanCancel = (ability.actionParam.isCanCancel)
        initParam.isCanPass = (ability.actionParam.isCanPass)
        initParam.conditionGroup = ability.cardCondGroup
        // 设置监听的目标玩家
        if (ability.trigPower != SuperPower.NONE) {
            initParam.listeningPlayer = gameMode.game.convertSuperPower(ability.trigPower, player)
        }
        return initParam
    }

    /**
     * 按照TS能力创建行动监听器初始化参数
     * @param gameMode
     * @param player
     * @param card
     * @param trigType
     * @return
     */
    fun createChoiceInitParam(gameMode: TSGameMode, player: TSPlayer, card: TSCard?, trigType: TrigType): ChoiceInitParam {
        val initParam = ChoiceInitParam()
        initParam.card = card
        initParam.trigType = trigType
        initParam.targetPower = player.superPower
        // 设置监听的目标玩家
        if (card != null && card.abilityGroup!!.trigPower != SuperPower.NONE) {
            initParam.listeningPlayer = gameMode.game.convertSuperPower(card.abilityGroup!!.trigPower, player)
        }
        return initParam
    }

    /**
     * 按照参数创建条件判断类的初始化参数
     * @param gameMode
     * @param player
     * @param card
     * @param actionParam
     * @return
     */

    fun createConditionInitParam(gameMode: TSGameMode, player: TSPlayer, card: TSCard, actionParam: ActionParam): ConditionInitParam {
        val initParam = ConditionInitParam()
        initParam.card = card
        initParam.clazz = actionParam.clazz
        // initParam.targetPower = actionParam.targetPower;
        // initParam.num = actionParam.num;
        // initParam.canCancel = actionParam.canCancel;
        // initParam.canPass = actionParam.canPass;
        // 设置监听的目标玩家
        if (actionParam.trigPower != SuperPower.NONE) {
            initParam.listeningPlayer = gameMode.game.convertSuperPower(actionParam.trigPower, player)
        }
        return initParam
    }

    /**
     * 创建回合行动时政变的初始化参数
     * @param gameMode
     * @param player
     * @param card
     * @param op
     * @param trigType
     * @return
     */
    fun createCoupParam(gameMode: TSGameMode, player: TSPlayer, card: TSCard, op: Int, trigType: TrigType, isFreeAction: Boolean, conditionGroup: TSCountryConditionGroup): OPActionInitParam {
        val initParam = OPActionInitParam()
        initParam.listeningPlayer = player.superPower
        initParam.targetPower = player.superPower.oppositeSuperPower
        initParam.actionType = ActionType.COUP
        initParam.card = card
        initParam.trigType = trigType
        initParam.isCanCancel = true
        initParam.isCanPass = false
        initParam.num = op
        initParam.msg = "请用 {num}OP 进行政变!"
        initParam.isFreeAction = isFreeAction
        initParam.conditionGroup = conditionGroup
        return initParam
    }

    /**
     * 按照TS能力创建行动执行器的初始化参数
     * @param gameMode
     * @param player
     * @param card
     * @param ability
     * @param trigType
     * @return
     */
    fun createExecutorInitParam(gameMode: TSGameMode, player: TSPlayer, card: TSCard?, ability: TSAbility, trigType: TrigType?): ExecutorInitParam {
        val initParam = ExecutorInitParam()
        initParam.card = card
        initParam.trigType = trigType
        initParam.clazz = ability.actionParam.clazz
        initParam.targetPower = ability.actionParam.targetPower
        initParam.num = ability.actionParam.num
        initParam.isCanCancel = (ability.actionParam.isCanCancel)
        initParam.isCanPass = (ability.actionParam.isCanPass)
        // 设置监听的目标玩家
        if (ability.trigPower != SuperPower.NONE) {
            initParam.listeningPlayer = gameMode.game.convertSuperPower(ability.trigPower, player)
        }
        return initParam
    }

    /**
     * 创建游戏开始时苏联让点的初始化参数
     * @return
     */

    fun createGivenPointInfluence(superPower: SuperPower, num: Int): ActionInitParam {
        val initParam = ActionInitParam()
        // 由美国在已有影响力的国家分配点数
        initParam.listeningPlayer = superPower
        initParam.targetPower = superPower
        initParam.actionType = ActionType.ADJUST_INFLUENCE
        initParam.num = num
        initParam.msg = "请在已有自己影响力的国家分配 {num} 点影响力!"
        val c = TSCountryCondition()
        when (superPower) {
            SuperPower.USA -> c.hasUsaInfluence = true
            SuperPower.USSR -> c.hasUssrInfluence = true
            else -> Unit
        }
        initParam.addWc(c)
        return initParam
    }

    /**
     * 创建回合行动时使用OP的初始化参数
     * @param gameMode
     * @param player
     * @param card
     * @param trigType
     * @return
     */
    fun createOpActionParam(gameMode: TSGameMode, player: TSPlayer, card: TSCard, trigType: TrigType): OPActionInitParam {
        val initParam = OPActionInitParam()
        initParam.listeningPlayer = player.superPower
        initParam.targetPower = player.superPower
        initParam.card = card
        initParam.trigType = trigType
        initParam.isCanCancel = false
        initParam.isCanPass = false
        initParam.canAddInfluence = true
        initParam.canCoup = true
        initParam.canRealignment = true
        initParam.isFreeAction = false
        initParam.num = player.getOp(card)
        initParam.msg = "请用 {num}OP 进行行动!"
        return initParam
    }

    /**
     * 按照TS能力创建OP行动监听器初始化参数
     * @param gameMode
     * @param player
     * @param card
     * @param ability
     * @param trigType
     * @return
     */
    fun createOpActionParam(gameMode: TSGameMode, player: TSPlayer, card: TSCard?, ability: TSAbility, trigType: TrigType?): OPActionInitParam {
        val initParam = OPActionInitParam()
        initParam.card = card
        initParam.trigType = trigType
        initParam.targetPower = ability.actionParam.targetPower
        initParam.actionType = ability.actionParam.actionType
        initParam.msg = ability.actionParam.descr
        initParam.isCanCancel = (ability.actionParam.isCanCancel)
        initParam.isCanPass = (ability.actionParam.isCanPass)
        initParam.canAddInfluence = ability.actionParam.isCanAddInfluence
        initParam.canCoup = ability.actionParam.isCanCoup
        initParam.canRealignment = ability.actionParam.isCanRealignment
        initParam.isFreeAction = (ability.actionParam.isFreeAction)
        initParam.conditionGroup = ability.countryCondGroup
        // 设置监听的目标玩家
        if (ability.trigPower != SuperPower.NONE) {
            initParam.listeningPlayer = gameMode.game.convertSuperPower(ability.trigPower, player)
        }
        // 设置可使用的OP数
        if (ability.actionParam.num > 0) {
            // 如果在参数中设置了num,则使用num
            initParam.num = ability.actionParam.num
        } /*
             * else{ TSPlayer p =
			 * mode.getBoardGame().getPlayer(initParam.listeningPlayer);
			 * initParam.num = p.getOp(card); }
			 */
        // 如果num的值和card的OP不同,则clone一个card,并设置card的op为num
        if (card != null && card.op != initParam.num && initParam.num > 0) {
            val tmpCard = card.clone()
            tmpCard.op = initParam.num
            initParam.card = tmpCard
        }
        return initParam
    }

    /**
     * 创建回合行动时调整阵营的初始化参数
     * @param player
     * @param card
     * @param op
     * @param trigType
     * @return
     */
    fun createRealignmentParam(player: TSPlayer, card: TSCard, op: Int, trigType: TrigType, isFreeAction: Boolean, conditionGroup: TSCountryConditionGroup): RealignmentInitParam = RealignmentInitParam().apply {
        this.listeningPlayer = player.superPower
        this.targetPower = player.superPower.oppositeSuperPower
        this.actionType = ActionType.REALIGNMENT
        this.card = card
        this.trigType = trigType
        this.isCanCancel = (true)
        this.isCanPass = (false)
        this.num = op
        this.msg = "请用 {num}OP 调整阵营!"
        this.isFreeAction = (isFreeAction)
        this.conditionGroup = conditionGroup
    }

    /**
     * 创建游戏设置时放置影响力的初始化参数
     * @param power
     * @return
     */
    fun createSetupInfluence(power: SuperPower): ActionInitParam? {
        val initParam = ActionInitParam()
        initParam.listeningPlayer = power
        initParam.targetPower = power
        initParam.actionType = ActionType.ADJUST_INFLUENCE
        when (power) {
            SuperPower.USSR // 苏联在东欧分配6点影响力
            -> {
                initParam.num = 6
                initParam.msg = "请在东欧分配 {num} 点影响力!"
                val c = TSCountryCondition()
                c.subRegion = SubRegion.EAST_EUROPE
                initParam.addWc(c)
            }
            SuperPower.USA // 美国在西欧分配7点影响力
            -> {
                initParam.num = 7
                initParam.msg = "请在西欧分配 {num} 点影响力!"
                val c = TSCountryCondition()
                c.subRegion = SubRegion.WEST_EUROPE
                initParam.addWc(c)
            }
            else -> return null
        }
        return initParam
    }
}
