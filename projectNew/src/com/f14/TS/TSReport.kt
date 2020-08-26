package com.f14.TS

import com.f14.TS.component.AdjustParam
import com.f14.TS.component.TSCard
import com.f14.TS.component.TSCountry
import com.f14.TS.component.TSZeroCard
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TSProperty
import com.f14.TS.consts.TrigType
import com.f14.TS.manager.ScoreManager.ScoreCounter
import com.f14.bg.report.BgReport
import com.f14.bg.utils.BgUtils
import java.util.*
import kotlin.math.max

class TSReport(bg: TS) : BgReport<TSPlayer>(bg) {

    private var records: MutableList<ActionRecord> = ArrayList()

    fun addCardToMid(cards: Collection<TSCard>) {
        if (!cards.isEmpty()) {
            val sb = StringBuilder()
            for (c in cards) {
                sb.append(c.reportString)
            }
            sb.append("被移至中期牌库!")
            this.info(sb.toString())
        }
    }

    /**
     * 输出DEFCON的变化值
     * @param num
     */
    fun adjustDefcon(num: Int) {
        val str: String = when {
            num > 0 -> "DEFCON改善了 " + num + "个等级"
            num < 0 -> "DEFCON恶化了 " + -num + "个等级"
            else -> return
        }
        this.info(str)
    }

    /**
     * 输出VP的变化值
     * @param num
     */
    fun adjustVp(num: Int) {
        val str: String = when {
            num > 0 -> "苏联得到 " + num + "VP"
            num < 0 -> "美国得到 " + -num + "VP"
            else -> return
        }
        this.info(str)
    }

    /**
     * 执行行动提示
     * @param ap 调整的详细参数
     */
    fun doAction(ap: AdjustParam) {
        this.playerDoAction(null, ap)
    }

    /**
     * 取得最近的N条记录
     * @param n
     * @return
     */

    fun getRecentRecords(n: Int): Collection<ActionRecord> {
        return if (this.records.isEmpty()) {
            ArrayList()
        } else {
            var i = this.records.size - n
            i = max(0, i)
            this.records.subList(i, records.size)
        }
    }

    /**
     * 执行头条
     * @param card
     */
    fun headLine(card: TSCard) {
        this.line()
        this.info(card.reportString + "生效")
    }

    /**
     * 玩家卡牌生效
     * @param player
     * @param card
     */
    fun playerActiveCard(player: TSPlayer, card: TSCard) {
        this.action(player, "的" + card.reportString + "生效")
        this.printRecord(player, card, "卡牌生效")
    }

    /**
     * 调整玩家的军事行动力
     * @param player
     * @param num
     */
    fun playerAdjustMilitaryAction(player: TSPlayer, num: Int) {
        this.action(player, "得到 $num 点军事行动")
    }

    /**
     * 玩家调整太空竞赛等级
     * @param player
     * @param num
     */
    fun playerAdvanceSpaceRace(player: TSPlayer, num: Int) {
        this.action(player, "的太空竞赛等级前进了 " + num + " 格,变为 " + player.getProperty(TSProperty.SPACE_RACE))
    }

    /**
     * 取消头条
     * @param player
     * @param power
     */
    fun playerCancelHeadline(player: TSPlayer, power: SuperPower) {
        this.action(player, "取消了 " + power.chinese + " 方的头条")
    }

    /**
     * 玩家弃牌
     * @param player
     * @param card
     */
    fun playerDiscardCard(player: TSPlayer, card: TSCard) {
        this.playerDiscardCards(player, BgUtils.toList(card))
    }

    /**
     * 玩家弃牌
     * @param player
     * @param cards
     */
    fun playerDiscardCards(player: TSPlayer, cards: List<TSCard>) {
        if (cards.isNotEmpty()) {
            val sb = StringBuilder()
            sb.append("弃掉了 ")
            for (card1 in cards) {
                sb.append(card1.reportString)
                // if (i < cards.size() - 1) {
                // sb.append(", ");
                // }
            }
            this.action(player, sb.toString())
            // 记录行动
            for (card in cards) {
                this.printRecord(player, card, "弃掉了")
            }
        }
    }

    /**
     * 玩家执行行动
     * @param player 调整的玩家(可以为空)
     * @param ap     调整的详细参数
     */
    fun playerDoAction(player: TSPlayer?, ap: AdjustParam) {
        if (player != null) {
            this.action(player, ap.reportString)
        } else {
            this.info(ap.reportString)
        }
    }

    /**
     * 玩家调整影响力
     * @param player 调整的玩家
     * @param aps    调整的详细参数
     */
    fun playerDoAction(player: TSPlayer, aps: Collection<AdjustParam>) {
        for (ap in aps) {
            this.playerDoAction(player, ap)
        }
    }

    /**
     * 玩家摸牌
     * @param player
     */
    fun playerDrawCards(player: TSPlayer, cards: List<TSCard>) {
        val sb = StringBuilder("摸了")
        for (c in cards) {
            sb.append(c.reportString)
        }
        this.action(player, "摸了 " + cards.size + " 张牌", sb.toString())
    }

    /**
     * 玩家得到牌
     * @param player
     * @param card
     */
    fun playerGetCard(player: TSPlayer, card: TSCard) {
        this.action(player, "得到 " + card.reportString)
        this.printRecord(player, card, "得到")
    }

    /**
     * 玩家得到中国牌
     * @param player
     * @param canUse
     */
    fun playerGetChinaCard(player: TSPlayer?, canUse: Boolean) {
        when (player) {
            null -> this.info("中国牌从游戏中移除")
            else -> this.action(player, (if (canUse) "正面向上" else "背面向上") + "得到了中国牌")
        }
    }

    /**
     * 玩家选择头条
     * @param player
     * @param card
     */
    fun playerHeadLine(player: TSPlayer, card: TSCard) {
        this.action(player, "选择" + card.reportString + "作为头条")
        this.printRecord(player, card, "选择头条")
    }

    /**
     * 玩家拥有中国牌
     * @param player
     */
    fun playerOwnChinaCard(player: TSPlayer) {
        this.action(player, "拥有中国牌")
    }

    /**
     * 玩家打出牌
     * @param player
     * @param card
     */
    fun playerPlayCard(player: TSPlayer, card: TSCard, type: TrigType?) {
        val sb = StringBuilder()
        var act = ""
        if (type != null) {
            act += "以" + type.chinese + "方式"
            // sb.append("以").append(TrigType.chinese(type)).append("方式");
        }
        act += "打出"
        sb.append(act).append(card.reportString)
        this.action(player, sb.toString())
        // 记录行动
        this.printRecord(player, card, act)
    }

    /**
     * 玩家随机抽牌
     * @param player
     * @param card
     */
    fun playerRandowDrawCard(player: TSPlayer, card: TSCard) {
        this.action(player, "抽到了 " + card.reportString)
        this.printRecord(player, card, "抽到了")
    }

    /**
     * 玩家区域得分
     * @param player
     * @param counter
     */
    fun playerRegionScore(player: TSPlayer, counter: ScoreCounter) {
        this.action(player, counter.reportString)
    }

    /**
     * 玩家移除生效的卡牌
     * @param player
     * @param card
     */
    fun playerRemoveActiveCard(player: TSPlayer, card: TSCard) {
        this.action(player, "移除了" + card.reportString + "的效果")
    }

    /**
     * 玩家失去牌
     * @param player
     * @param card
     */
    fun playerRemoveCard(player: TSPlayer, card: TSCard) {
        this.playerRemoveCards(player, BgUtils.toList(card))
    }

    /**
     * 玩家失去牌
     * @param player
     * @param cards
     */
    fun playerRemoveCards(player: TSPlayer, cards: List<TSCard>) {
        if (cards.isNotEmpty()) {
//            val sb = StringBuilder()
//            sb.append("失去了 ")
//            for (card1 in cards) {
//                sb.append(card1.reportString)
//                // if (i < cards.size() - 1) {
//                // sb.append(", ");
//                // }
//            }
            this.action(player, cards.joinToString(",", "失去了", transform = TSCard::reportString))
            // 记录行动
            for (card in cards) this.printRecord(player, card, "失去了")
        }
    }

    /**
     * 重新发牌
     * @param player
     */
    fun playerResetHand(player: TSPlayer) {
        this.action(player, "摸到3张计分牌，重新发放手牌!")
    }

    /**
     * 玩家掷骰
     * @param player
     * @param roll
     * @param bonus
     */
    fun playerRoll(player: TSPlayer, roll: Int, bonus: Int) {
        val sb = StringBuilder()
        sb.append("掷骰结果为 ").append(roll)
        when {
            bonus > 0 -> sb.append(" + ").append(bonus).append(" = ").append(roll + bonus)
            bonus < 0 -> sb.append(" - ").append(-bonus).append(" = ").append(roll + bonus)
        }
        this.action(player, sb.toString())
    }

    /**
     * 玩家进行选择
     * @param player
     * @param descr
     */
    fun playerSelectChoice(player: TSPlayer, descr: String) {
        this.action(player, "选择了 $descr")
    }

    /**
     * 设置玩家的军事行动力
     * @param player
     */
    fun playerSetMilitaryAction(player: TSPlayer) {
        this.action(player, "的军事行动变为 ${player.getProperty(TSProperty.MILITARY_ACTION)}")
    }

    /**
     * 玩家进行太空竞赛
     * @param player
     * @param card
     * @param roll
     * @param success
     */
    fun playerSpaceRace(player: TSPlayer, card: TSCard, roll: Int, success: Boolean) {
        this.action(player, "用${card.reportString}进行太空竞赛,掷骰结果为 $roll,尝试${if (success) "成功" else "失败"}")
        this.printRecord(player, card, "进行太空竞赛")
    }

    /**
     * 玩家发送战争
     * @param player
     * @param country
     * @param roll
     * @param correctedValue
     * @param success
     */
    fun playerWar(player: TSPlayer, country: TSCountry, roll: Int, correctedValue: Int, success: Boolean) {
        val sb = "在${country.reportString}发动了战争,掷骰结果为 $roll - $correctedValue = ${roll - correctedValue} , 战争${if (success) "胜利" else "失败"}"
        this.action(player, sb)
    }

    /**
     * 输出DEFCON等级
     * @param defcon
     */
    fun printDefcon(defcon: Int) {
        this.info("DEFCON变为 $defcon")
    }

    private val tsGame: TS
        get() = this.game as TS

    /**
     * 打印行动记录
     * @param player
     * @param card
     * @param message
     */
    fun printRecord(player: TSPlayer, card: TSCard, message: String) {
        val rec = ActionRecord(player, card, message)
        this.records.add(rec)
        this.tsGame.sendActionRecord(rec)
    }

    /**
     * 输出VP
     * @param vp
     */
    fun printVp(vp: Int) {
        val str: String = when {
            vp > 0 -> "苏联 ${vp}VP"
            vp < 0 -> "美国 ${-vp}VP"
            else -> "0"
        }
        this.info("VP变为 $str")
    }

    fun replaceCard(card: TSCard, newCard: TSCard) {
        this.info("${card.reportString}被替换为${newCard.reportString}")
    }

    /**
     * 玩家掷骰
     * @param player
     * @param roll
     * @param modify
     */
    fun roll(player: TSPlayer, roll: Int, modify: Int) {
        this.action(player, "掷骰结果: $roll+$modify=${roll + modify}")
    }

    fun showZeroCardResult(card: TSZeroCard) {
        this.info("${card.reportString}生效!")
        this.info(card.descr)
    }

    /**
     * 把卡牌移除出游戏
     * @param cards
     */
    fun trashCard(cards: Collection<TSCard>) {
        if (!cards.isEmpty()) {
            val str = cards.joinToString(",", prefix = "移除出游戏!", transform = TSCard::reportString)
//            val sb = StringBuilder()
//            for (c in cards) {
//                sb.append(c.reportString)
//            }
//            sb.append("")
            this.info(str)
        }
    }

    /**
     * 把卡牌移除出游戏
     * @param card
     */
    fun trashCard(card: TSCard) {
        this.info(card.reportString + "移除出游戏!")
    }

    fun zeroRoll(card: TSZeroCard, roll: Int, ussrModify: Int, usaModify: Int, result: Int, cancelEffect: Boolean) {
        val sb = StringBuilder()
        sb.append(card.reportString).append("投掷结果为 ").append(roll)
        if (!cancelEffect) {
            sb.append(" - ").append(ussrModify).append(" + ").append(usaModify).append(" = ").append(result)
        }
        this.info(sb.toString())
        this.info(card.descr)
        this.info(card.effect)
    }

    fun playerCannotUseTurn(player: TSPlayer, drawnCard: TSCard) {
        this.action(player, "这回合不能够把${drawnCard.reportString}作为事件打出")
    }
}
