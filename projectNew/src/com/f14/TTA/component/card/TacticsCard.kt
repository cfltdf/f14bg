package com.f14.TTA.component.card

import com.f14.TTA.consts.CardSubType
import com.f14.TTA.consts.CivilAbilityType
import com.f14.TTA.consts.CivilizationProperty
import java.util.*

/**
 * 战术牌

 * @author F14eagle
 */
class TacticsCard : MilitaryCard() {
    var infantry: Int = 0
    var cavalry: Int = 0
    var artillery: Int = 0
    var armyBonus: Int = 0
    var secondArmyBonus: Int = 0

    /**
     * 计算给出的部队可以组成的军队情况
     * @param units
     * @param genghis
     * @return
     */
    fun getTacticsResult(units: Map<TechCard, Int>, type: TacticType): TacticsResult {
        if (TacticType.MULTIPLE == type) {
            return units.keys.map { u ->
                getTacticsResult(units.mapValues { (k, v) -> if (k === u) v + 1 else v }.toMap(), TacticType.NORMAL)
            }.maxBy(TacticsResult::totalMilitaryBonus) ?: TacticsResult()
        }
        val values = LinkedHashMap<CardSubType, UnitValue>()
        // 初始化部队类型map
//        for (type in UNIT_TYPE) {
//            val uv = UnitValue()
//            values[type] = uv
//        }
        values[CardSubType.INFANTRY] = UnitValue(this.infantry)
        values[CardSubType.CAVALRY] = UnitValue(this.cavalry)
        values[CardSubType.ARTILLERY] = UnitValue(this.artillery)
        values[CardSubType.AIR_FORCE] = UnitValue(0)
        if (TacticType.ZIZKA == type) {
            values[CardSubType.FARM] = UnitValue(0)
        }
        // 统计各个部队类型,各个等级的部队数量
        for ((card, num) in units) {
            // 只处理部队类型的牌
            val uv = values[card.cardSubType] ?: continue
            if (card.level < this.level - 1) {
                // 如果部队等级小于战术牌等级1级,则只能作为次要部队数量
                uv.secondaryNum += num
            } else {
                uv.mainNum += num
            }
        }

        if (TacticType.GHENGIS == type) {
            // 临时将阵型及统计的步兵加到骑兵上
            val uc = values.getValue(CardSubType.CAVALRY)
            val ui = values.getValue(CardSubType.INFANTRY)
            uc += ui
//            uc.secondaryNum += ui.secondaryNum
//            uc.mainNum += ui.mainNum
        } else if (TacticType.ZIZKA == type) {
            val ua = values.getValue(CardSubType.ARTILLERY)
            val ui = values.getValue(CardSubType.INFANTRY)
            val uf = values.getValue(CardSubType.FARM)
            ua += uf
            ui += uf
            uf.baseNum = ua.baseNum + ui.baseNum
            uf.mainNum = ua.mainNum + ui.mainNum - uf.mainNum
            uf.secondaryNum = ua.secondaryNum + ui.secondaryNum - uf.secondaryNum
        }

        // 统计军队数量
        val res = TacticsResult()
        res.mainArmyNum = values.values.filter { it.baseNum > 0 }.map(UnitValue::mainCount).min() ?: 0
        res.secondaryArmyNum = (values.values.filter { it.baseNum > 0 }.map(UnitValue::allCount).min()
                ?: 0) - res.mainArmyNum
//        while (true) {
//            var mainArmy = true
//            // 检查步兵数量
//            if (this.infantry > 0) {
//                val uv = values.getValue(CardSubType.INFANTRY)
//                // 检查部队是否够组成mainArmy
//                mainArmy = mainArmy and (uv.mainNum >= this.infantry)
//                if (uv.decreaseNum(this.infantry) > 0) {
//                    // 如果已经不够军队的基本数量,则跳出循环
//                    break
//                }
//            }
//            // 检查骑兵数量
//            if (this.cavalry > 0) {
//                val uv = values[CardSubType.CAVALRY]!!
//                // 检查部队是否够组成mainArmy
//                mainArmy = mainArmy and (uv.mainNum >= this.cavalry)
//                if (uv.decreaseNum(this.cavalry) > 0) {
//                    // 如果已经不够军队的基本数量,则跳出循环
//                    break
//                }
//            }
//            // 检查炮兵数量
//            if (this.artillery > 0) {
//                val uv = values[CardSubType.ARTILLERY]!!
//                // 检查部队是否够组成mainArmy
//                mainArmy = mainArmy and (uv.mainNum >= this.artillery)
//                if (uv.decreaseNum(this.artillery) > 0) {
//                    // 如果已经不够军队的基本数量,则跳出循环
//                    break
//                }
//            }
//            // 增加军队的数量
//            if (mainArmy) {
//                res.mainArmyNum += 1
//            } else {
//                res.secondaryArmyNum += 1
//            }
//        }
        // 设置空军的数量
        val uv = values.getValue(CardSubType.AIR_FORCE)
        res.airForceNum = uv.totalNum

        return res
    }

    enum class TacticType {
        NORMAL, GHENGIS, ZIZKA, MULTIPLE
    }

    /**
     * 战术军队结果
     * @author F14eagle
     */
    inner class TacticsResult {
        var mainArmyNum: Int = 0
        var secondaryArmyNum: Int = 0
        var airForceNum: Int = 0

        /**
         * 取得军队中最好的单个军队奖励数值(如果存在空军,则该数值加倍)

         * @return
         */
        val bestArmyBonus: Int
            get() {
                val multi = if (this.airForceNum > 0) 2 else 1
                return when {
                    this.mainArmyNum > 0 -> this@TacticsCard.armyBonus * multi
                    this.secondaryArmyNum > 0 -> this@TacticsCard.secondArmyBonus * multi
                    else -> 0
                }
            }

        /**
         * 取得军队和空军所有的军事力加成总值
         * @return
         */
        // 每个空军可以将一个军队的军事力加成加倍
        // 如果空军剩余数量大于等于主力军数量,则加成军队数量取主力军数
        // 否则加成军队数量取空军剩余数
        // 如果空军剩余数量大于等于次级军数量,则加成军队数量取次级军数
        // 否则加成军队数量取空军剩余数
        val totalMilitaryBonus: Int
            get() {
                var res = 0
                val addition = this@TacticsCard.attachedCards?.flatMap(TTACard::abilities)?.filter { it.abilityType == CivilAbilityType.ATTACH_PROPERTY }?.sumBy { it.property.getProperty(CivilizationProperty.MILITARY) }
                        ?: 0
                res += mainArmyNum * (this@TacticsCard.armyBonus + addition)
                res += secondaryArmyNum * (this@TacticsCard.secondArmyBonus + addition)
                var rest = airForceNum
                if (rest >= mainArmyNum) {
                    res += mainArmyNum * (this@TacticsCard.armyBonus + addition)
                    rest -= mainArmyNum
                } else {
                    res += rest * (this@TacticsCard.armyBonus + addition)
                    rest = 0
                }
                res += if (rest >= secondaryArmyNum) {
                    secondaryArmyNum * (this@TacticsCard.secondArmyBonus + addition)
                } else {
                    rest * (this@TacticsCard.secondArmyBonus + addition)
                }
                return res
            }
    }

    internal inner class UnitValue(var baseNum: Int) {
        var mainNum: Int = 0
        var secondaryNum: Int = 0

        fun mainCount(): Int {
            if (baseNum == 0) return Int.MAX_VALUE
            return mainNum / baseNum
        }

        fun allCount(): Int {
            if (baseNum == 0) return Int.MAX_VALUE
            return (mainNum + secondaryNum) / baseNum
        }

        /**
         * 减去数量,从mainNum开始减,不够则继续从secondaryNum里减,返回不够扣除的数量
         * @param num
         * @return
         */
        fun decreaseNum(num: Int): Int {
            var rest = num
            // 如果存在mainNum,则从mainNum中扣除
            if (mainNum > 0) {
                if (mainNum >= rest) {
                    mainNum -= rest
                    rest = 0
                } else {
                    rest -= mainNum
                    mainNum = 0
                }
            }
            // 如果secondaryNum还是不够扣,则返回不够扣的数量
            if (secondaryNum > 0) {
                if (secondaryNum >= rest) {
                    secondaryNum -= rest
                    rest = 0
                } else {
                    rest -= secondaryNum
                    secondaryNum = 0
                }
            }
            return rest
        }

        /**
         * 取得总数
         * @return
         */
        val totalNum: Int
            get() = this.mainNum + this.secondaryNum

        operator fun plusAssign(that: UnitValue) {
            this.mainNum += that.mainNum
            this.secondaryNum += that.secondaryNum
            this.baseNum += that.baseNum
        }
    }

    companion object {
        /**
         * 部队类型常量
         */
        private val UNIT_TYPE = arrayOf(CardSubType.INFANTRY, CardSubType.CAVALRY, CardSubType.ARTILLERY, CardSubType.AIR_FORCE)
    }

}
