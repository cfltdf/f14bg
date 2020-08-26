package com.f14.TS.manager

import com.f14.TS.TSGameMode
import com.f14.TS.action.TSEffect
import com.f14.TS.component.TSCountry
import com.f14.TS.component.condition.TSCountryCondition
import com.f14.TS.consts.*
import com.f14.bg.component.ICondition
import com.f14.bg.exception.BoardGameException
import com.f14.bg.report.Printable
import org.apache.log4j.Logger
import java.util.*
import kotlin.math.max

/**
 * Created by Administrator on 2017-07-22.
 */


class ScoreManager(private var gameMode: TSGameMode) {
    val log = Logger.getLogger(ScoreManager::class.java)!!
    private val groups: MutableMap<ScoreRegion, ScoreGroup> = LinkedHashMap()
    private val finalScoreRegions: MutableSet<ScoreRegion> = LinkedHashSet()

    /**
     * 添加计分组
     * @param group
     */
    private fun addScoreGroup(group: ScoreGroup) {
        this.groups[group.scoreRegion] = group
    }

    /**
     * 计分结束后调用的方法
     * @param scoreRegion
     */
    private fun afterScore(scoreRegion: ScoreRegion) {
        // 如果计分区域是中东或者亚洲,则检查是否存在#73穿梭外交
        if (scoreRegion == ScoreRegion.MIDDLE_EAST || scoreRegion == ScoreRegion.ASIA) {
            // 可能会在遍历的过程中移除卡牌,所以需要创建一个新的list来遍历
            gameMode.eventManager.activedCards.filter { it.tsCardNo == 73 }.forEach {
                // 移除该效果
                gameMode.game.removeActivedCard(it)
                // 将该牌添加到弃牌堆中
                gameMode.game.discardCard(it)
            }
        }
    }

    /**
     * 执行游戏结束时的计分
     * @return
     */
    fun executeFinalScore() = this.finalScoreRegions.map { it to this.executeScore(it.toString(), true) }.toMap()


    /**
     * 执行指定区域的计分,返回计分对象
     * @param scoreRegion
     * @param gameOverScore 是否是游戏结束时计分
     * @return
     */
    fun executeScore(scoreRegion: String, gameOverScore: Boolean): ScoreParam {
        val sr = ScoreRegion.valueOf(scoreRegion)
        val group = this.getScoreGroup(sr)
        val param = group.executeScore(gameOverScore)
        group.checkResult(param)
        this.afterScore(sr)
        return param
    }

    /**
     * 取得支配和控制的区域总数
     * @param power
     * @return
     */
    fun getDominationNumber(power: SuperPower): Int {
        return this.finalScoreRegions.map(this::getScoreGroup).map(ScoreGroup::checkScore).count {
            when (power) {
                SuperPower.USSR -> it.ussr.situation in arrayOf(TSSituation.DOMINATION, TSSituation.CONTROL)
                SuperPower.USA -> it.usa.situation in arrayOf(TSSituation.DOMINATION, TSSituation.CONTROL)
                else -> false
            }
        }
    }

    /**
     * 取得计分组
     * @param scoreRegion
     * @return
     */
    private fun getScoreGroup(scoreRegion: ScoreRegion): ScoreGroup {
        return this.groups[scoreRegion]!!
    }

    /**
     * 初始化计分模块
     */
    private fun init() {
    }

    fun replaceEuropeScore() {
        val group = this.getScoreGroup(ScoreRegion.EUROPE)
        group[TSSituation.DOMINATION] = 6
        group[TSSituation.CONTROL] = 6
    }

    /**
     * 计分区域

     * @author F14eagle
     */
    enum class ScoreRegion {
        /**
         * 亚洲
         */
        ASIA,
        /**
         * 欧洲
         */
        EUROPE,
        /**
         * 中东
         */
        MIDDLE_EAST,
        /**
         * 中美洲
         */
        CENTRAL_AMERICA,
        /**
         * 南美洲
         */
        SOUTH_AMERICA,
        /**
         * 非洲
         */
        AFRICA,
        /**
         * 东南亚
         */
        SOUTHEAST_ASIA;

        val chinese: String
            get() = when (this) {
                ASIA -> "亚洲"
                EUROPE -> "欧洲"
                MIDDLE_EAST -> "中东"
                CENTRAL_AMERICA -> "中美洲"
                SOUTH_AMERICA -> "南美洲"
                AFRICA -> "非洲"
                SOUTHEAST_ASIA -> "东南亚"
            }
    }

    /**
     * 东南亚计分时的计数器
     * @author F14eagle
     */
    inner class ESScoreCounter : ScoreCounter() {
        override val reportString: String
            get() = "控制战场国数量:" + this.battleNum + "控制非战场国数量:" + this.normalNum
    }

    /**
     * 计分时的计数器
     * @author F14eagle
     */
    open inner class ScoreCounter : Printable {
        /**
         * 战场国数量
         */
        var battleNum: Int = 0
        /**
         * 非战场国数量
         */
        var normalNum: Int = 0
        /**
         * 对方超级大国的邻国数量
         */
        var adjacentNum: Int = 0
        /**
         * 形式
         */
        var situation: TSSituation = TSSituation.NONE
        /**
         * 是否控制了所有的战场国
         */
        var controlAllBattle: Boolean = false


        override val reportString: String
            get() = "局势为 [" + TSSituation.getDescr(situation) + "] 控制国家数量:" + this.totalCountriesNum + " 控制战场国数量:" + this.battleNum + " 控制对方超级大国邻国数量:" + this.adjacentNum

        /**
         * 取得国家总数
         * @return
         */
        val totalCountriesNum: Int
            get() = this.battleNum + this.normalNum
    }

    /**
     * 计分组
     * @author F14eagle
     */
    open inner class ScoreGroup(var scoreRegion: ScoreRegion, protected var condition: ICondition<TSCountry>) {

        val countries: Map<Country, TSCountry>
        /**
         * 战场国数量
         */
        val battleNum: Int
        /**
         * 局势的得分情况
         */
        var situationVp: MutableMap<TSSituation, Int> = LinkedHashMap()

        init {
            // 按照条件读取国家信息
            val countries = gameMode.countryManager.getCountriesByCondition(this.condition)
            // 添加国家
            this.countries = countries.map { it.country to it }.toMap()
            // 计算战场国数量
            this.battleNum = countries.count(TSCountry::isBattleField)
        }

        /**
         * 检查power是否控制了所有战场国
         * @param power
         * @return
         */
        private fun checkControlAllBattle(power: SuperPower): Boolean {
            return this.countries.values.none { it.isBattleField && it.controlledPower !== power }
        }

        /**
         * 设置最终得分,负分为美国,正分为苏联
         * @param param
         */
        open fun checkResult(param: ScoreParam) {
            val ussr = this[param.ussr.situation] + param.ussr.battleNum + param.ussr.adjacentNum
            val usa = this[param.usa.situation] + param.usa.battleNum + param.usa.adjacentNum
            param.vp = ussr - usa
        }

        /**
         * 检查区域控制的形式
         * @return
         */
        fun checkScore() = ScoreParam(
                battleNum,
                this.createScoreCounter(SuperPower.USSR, checkEffect = false, gameOverScore = false),
                this.createScoreCounter(SuperPower.USA, false, false)
        ).also(ScoreParam::checkTSSituation)

        /**
         * 创建超级大国的计分计数器
         * @param power
         * @param checkEffect   是否检查效果
         * @param gameOverScore 是否是结束时的计分
         * @return
         */
        protected open fun createScoreCounter(power: SuperPower, checkEffect: Boolean, gameOverScore: Boolean): ScoreCounter {
            val res = ScoreCounter()
            val player = gameMode.game.getPlayer(power)!!
            for (c in this.countries.values) {
                // 只计算控制的国家
                if (c.controlledPower === power) {
                    if (c.isBattleField) {
                        res.battleNum += 1
                    } else {
                        res.normalNum += 1
                    }
                    // 检查是否对方超级大国的邻国
                    if (c.adjacentPowers.contains(power.oppositeSuperPower)) {
                        res.adjacentNum += 1
                    }
                }
            }
            // 检查是否控制了所有战场国
            res.controlAllBattle = this.checkControlAllBattle(power)
            // 检查对方超级大国邻国的占领情况
            if (checkEffect) {
                // 游戏结束时计分不计算穿梭外交
                if (!gameOverScore) {
                    // 如果计分区域是中东或者亚洲,则检查#73穿梭外交的效果
                    if (this.scoreRegion in arrayOf(ScoreRegion.MIDDLE_EAST, ScoreRegion.ASIA)) {
                        // 在下一次中东或者亚洲计分时,战场国数量-1
                        res.battleNum += player.getEffects(EffectType.EFFECT_73).sumBy(TSEffect::num)
                        // 最小不能是负数
                        res.battleNum = max(0, res.battleNum)
                    }
                }
                // 如果计分区域是亚洲,则检查#101-台湾决议的效果
                if (this.scoreRegion == ScoreRegion.ASIA) {
                    // 如果控制台湾,则台湾算作战场国
                    if (player.hasEffect(EffectType.EFFECT_101)) {
                        try {
                            val taiwan = gameMode.countryManager.getCountry(Country.TW)
                            if (taiwan.controlledPower === player.superPower) {
                                res.battleNum += 1
                                res.normalNum -= 1
                            }
                        } catch (e: BoardGameException) {
                            log.error("怎么能没找到台湾呢!!", e)
                        }
                    }
                }

            }
            return res
        }

        /**
         * 执行计分
         * @return
         */
        fun executeScore(gameOverScore: Boolean) = ScoreParam(battleNum, this.createScoreCounter(SuperPower.USSR, true, gameOverScore), this.createScoreCounter(SuperPower.USA, true, gameOverScore)).also(ScoreParam::checkTSSituation) // 检查局势

        /**
         * 取得局势的得分
         * @param situation
         * @return
         */
        operator fun get(situation: TSSituation) = this.situationVp[situation] ?: 0

        /**
         * 设置局势的得分
         * @param situation
         * @param vp
         */
        operator fun set(situation: TSSituation, vp: Int) {
            this.situationVp[situation] = vp
        }
    }

    /**
     * 计分参数
     * @author F14eagle
     */
    inner class ScoreParam(val battleNum: Int, val ussr: ScoreCounter, val usa: ScoreCounter) {
        var vp: Int = 0

        /**
         * 检查局势
         */
        fun checkTSSituation() {
            // 先判断是否在场
            if (ussr.totalCountriesNum > 0) ussr.situation = TSSituation.PRESENCE
            if (usa.totalCountriesNum > 0) usa.situation = TSSituation.PRESENCE
            when {
            // 判断是否为控制,控制所有战场国并且国家数比对方多
                ussr.battleNum >= battleNum && ussr.controlAllBattle && ussr.totalCountriesNum > usa.totalCountriesNum -> ussr.situation = TSSituation.CONTROL
                usa.battleNum >= battleNum && usa.controlAllBattle && usa.totalCountriesNum > ussr.totalCountriesNum -> usa.situation = TSSituation.CONTROL
            // 如果控制的国家和战场国都比对方多,并且控制至少1个非战场国,则为支配
                ussr.totalCountriesNum > usa.totalCountriesNum && ussr.battleNum > usa.battleNum && ussr.normalNum > 0 -> ussr.situation = TSSituation.DOMINATION
                usa.totalCountriesNum > ussr.totalCountriesNum && usa.battleNum > ussr.battleNum && usa.normalNum > 0 -> usa.situation = TSSituation.DOMINATION
            }
        }

    }

    /**
     * 东南亚的计分组
     * @author F14eagle
     */
    private inner class SEAsiaScoreGroup(scoreRegion: ScoreRegion, condition: ICondition<TSCountry>)// 东南亚永远不会在结束时计分...
        : ScoreGroup(scoreRegion, condition) {

        /**
         * 设置最终得分,负分为美国,正分为苏联
         * @param param
         */
        override fun checkResult(param: ScoreParam) {
            // 每个非战场国1VP,每个战场国2VP
            val ussr = param.ussr.normalNum + param.ussr.battleNum * 2
            val usa = param.usa.normalNum + param.usa.battleNum * 2
            param.vp = ussr - usa
        }

        /**
         * 创建超级大国的计分计数器
         * @param power
         * @return
         */

        override fun createScoreCounter(power: SuperPower, checkEffect: Boolean, gameOverScore: Boolean): ESScoreCounter {
            val res = ESScoreCounter()
            this.countries.values
                    // 只计算控制的国家
                    .filter { it.controlledPower === power }.forEach {
                        if (it.isBattleField) {
                            res.battleNum += 1
                        } else {
                            res.normalNum += 1
                        }
                    }
            return res
        }

    }

    init {
        // 亚洲计分
        var c = TSCountryCondition()
        c.region = Region.ASIA
        var group = ScoreGroup(ScoreRegion.ASIA, c)
        group[TSSituation.PRESENCE] = 3
        group[TSSituation.DOMINATION] = 7
        group[TSSituation.CONTROL] = 9
        this.addScoreGroup(group)
        // 欧洲计分
        c = TSCountryCondition()
        c.region = Region.EUROPE
        group = ScoreGroup(ScoreRegion.EUROPE, c)
        group[TSSituation.PRESENCE] = 3
        group[TSSituation.DOMINATION] = 7
        group[TSSituation.CONTROL] = 999
        this.addScoreGroup(group)
        // 中东计分
        c = TSCountryCondition()
        c.region = Region.MIDDLE_EAST
        group = ScoreGroup(ScoreRegion.MIDDLE_EAST, c)
        group[TSSituation.PRESENCE] = 3
        group[TSSituation.DOMINATION] = 5
        group[TSSituation.CONTROL] = 7
        this.addScoreGroup(group)
        // 南美洲计分
        c = TSCountryCondition()
        c.region = Region.SOUTH_AMERICA
        group = ScoreGroup(ScoreRegion.SOUTH_AMERICA, c)
        group[TSSituation.PRESENCE] = 2
        group[TSSituation.DOMINATION] = 5
        group[TSSituation.CONTROL] = 6
        this.addScoreGroup(group)
        // 中美洲计分
        c = TSCountryCondition()
        c.region = Region.CENTRAL_AMERICA
        group = ScoreGroup(ScoreRegion.CENTRAL_AMERICA, c)
        group[TSSituation.PRESENCE] = 1
        group[TSSituation.DOMINATION] = 3
        group[TSSituation.CONTROL] = 5
        this.addScoreGroup(group)
        // 非洲计分
        c = TSCountryCondition()
        c.region = Region.AFRICA
        group = ScoreGroup(ScoreRegion.AFRICA, c)
        group[TSSituation.PRESENCE] = 1
        group[TSSituation.DOMINATION] = 4
        group[TSSituation.CONTROL] = 6
        this.addScoreGroup(group)
        // 东南亚计分
        c = TSCountryCondition()
        c.subRegion = SubRegion.SOUTHEAST_ASIA
        group = SEAsiaScoreGroup(ScoreRegion.SOUTHEAST_ASIA, c)
        this.addScoreGroup(group)

        // 设置游戏结束时计分的区域
        this.finalScoreRegions.add(ScoreRegion.ASIA)
        this.finalScoreRegions.add(ScoreRegion.EUROPE)
        this.finalScoreRegions.add(ScoreRegion.MIDDLE_EAST)
        this.finalScoreRegions.add(ScoreRegion.SOUTH_AMERICA)
        this.finalScoreRegions.add(ScoreRegion.CENTRAL_AMERICA)
        this.finalScoreRegions.add(ScoreRegion.AFRICA)
    }

}