package com.f14.f14bgdb.util

import org.apache.log4j.Logger
import java.util.*

/**
 * 积分和排名相关辅助类
 * @author F14eagle
 */
object ScoreUtil {
    /**
     * 积分和排名默认参数的系统代码
     */
    private const val SYS_POINT = "SYS_POINT"
    /**
     * 排名点数的系统代码
     */
    private const val SYS_RANK_POINT = "SYS_RANK_POINT"
    /**
     * 积分的系统代码
     */
    private const val SYS_SCORE_POINT = "SYS_SCORE_POINT"
    private var log = Logger.getLogger(ScoreUtil::class.java)
    private var DEFAULT_RANK_POINT: Int = 0
    private var ROUND_RANK_POINT: Int = 0
    private lateinit var rankMap: MutableMap<Int, DoubleArray>
    private lateinit var scoreMap: MutableMap<Int, IntArray>

    /**
     * 取得初始排名点数
     * @return
     */
    val defaultRankPoint: Long
        get() = DEFAULT_RANK_POINT.toLong()

    /**
     * 初始化
     */
    @Throws(Exception::class)
    fun init() {
        ScoreUtil.log.info("初始化积分模块...")
        rankMap = HashMap()
        scoreMap = HashMap()
        DEFAULT_RANK_POINT = CodeUtil.getLabel(SYS_POINT, "DEFAULT_RANK_POINT")!!.toInt()
        ROUND_RANK_POINT = CodeUtil.getLabel(SYS_POINT, "DEFAULT_ROUND_POINT")!!.toInt()

        var codes = CodeUtil.getCodes(SYS_RANK_POINT)
        for (e in codes) {
            val strs = e.label!!.split(",".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            val factors = strs.map(String::toDouble).toDoubleArray()
//            val factors = arrayOfNulls<Double>(strs.size)
//            for (i in strs.indices) {
//                factors[i] = java.lang.Double.valueOf(strs[i])
//            }
            rankMap[strs.size] = factors
        }

        codes = CodeUtil.getCodes(SYS_SCORE_POINT)
        for (e in codes) {
            val strs = e.label!!.split(",".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
            val scores = strs.map(String::toInt).toIntArray()
//            for (i in strs.indices) {
//                scores[i] = Integer.valueOf(strs[i])
//            }
            scoreMap[strs.size] = scores
        }
        ScoreUtil.log.info("积分模块初始化完成!")
    }

    /**
     * 取得每局的排名点数
     * @param playerNum 玩家数
     * @return
     */
    fun getRoundRankPoint(playerNum: Int): Int {
        return ROUND_RANK_POINT * playerNum
    }

    /**
     * 取得排名点数因数
     * @param playerNum 游戏中的玩家数
     * @param roundRank 游戏中的排名
     * @return
     */
    fun getRankPointFactor(playerNum: Int, roundRank: Int, isTeamMatch: Boolean): Double {
        return if (isTeamMatch) {
            when (roundRank) {
                1 -> 0.5
                else -> 0.0
            }
        } else {
            rankMap[playerNum]?.get(roundRank - 1) ?: 0.0
        }
    }

    /**
     * 取得积分
     * @param playerNum 游戏中的玩家数
     * @param roundRank 游戏中的排名
     * @return
     */
    fun getScore(playerNum: Int, roundRank: Int): Int {
        return scoreMap[playerNum]?.get(roundRank - 1) ?: 0
    }
}
