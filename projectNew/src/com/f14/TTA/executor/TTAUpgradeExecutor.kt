package com.f14.TTA.executor

import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.CostParam
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.CardSubType
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.manager.TTAConstManager
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils


/**
 * 升级的处理器

 * @author 吹风奈奈
 */
class TTAUpgradeExecutor(param: RoundParam, private var fromCard: TechCard, private var toCard: TechCard) : TTAActionExecutor(param) {
    var actionType: ActionType = if (fromCard.cardType == CardType.UNIT) ActionType.MILITARY else ActionType.CIVIL
    var actionCost: Int = 0
    var costModify = 0
    var cached = false
    var checkType = true
    private var useBach: Boolean = false
    private var cp: CostParam = CostParam()

    init {
        // 部队卡的行动类型为军事,其他为内政
        this.actionCost = TTAConstManager.getActionCost(player, TTACmdString.ACTION_UPGRADE)
    }

    @Throws(BoardGameException::class)
    override fun check() {
        // 检查玩家是否拥有行动点数
        player.checkActionPoint(actionType, actionCost)
        param.checkUpgrade(fromCard)
        if (checkType) {
            if (fromCard.cardSubType == toCard.cardSubType) {
                CheckUtils.check(fromCard.level >= toCard.level, "升级的目标等级必须高于原卡牌等级!")
            } else {
                // 新版巴赫:建筑升级为剧院,且能力未发动
                if (toCard.cardSubType == CardSubType.THEATER && param.checkBach(fromCard)) {
                    // 会受建筑数量的限制
                    gameMode.game.playerBuildLimitCheck(player, toCard)
                    CheckUtils.check(fromCard.level > toCard.level, "升级的目标等级必须不低于原卡牌等级!")
                    // 巴赫生效
                    this.useBach = true
                } else {
                    throw BoardGameException("升级的目标必须和原卡牌是相同的类型!")
                }
            }
        } else {
            if (fromCard == toCard) {
                throw BoardGameException("不能选择相同的牌!")
            }
        }

        cp = param.getUpgradeResourceCost(fromCard, toCard, costModify)
        player.checkEnoughResource(cp.cost)
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        if (!this.checked) {
            this.check()
        }
        // 巴赫生效
        param.useBach(this.useBach)
        // 玩家消耗资源,行动点,升级建筑
        param.useActionPoint(actionType, actionCost)
        // 执行消耗
        gameMode.game.playerAddPoint(player, cp.cost, -1)
        gameMode.report.playerCostPoint(player, cp.cost, "花费")
        // 调用主函数升级
        gameMode.game.playerUpgrade(player, fromCard, toCard)
        // 调整临时资源的值
        gameMode.game.executeTemplateResource(player, cp)
        // 完成升级后触发的方法
        param.afterUpgrade(toCard)
        if (cached) {
            gameMode.report.playerUpgradeCache(player, fromCard, toCard, 1)
        } else {
            gameMode.report.playerUpgrade(player, actionType, actionCost, fromCard, toCard, 1)
        }
    }


}
