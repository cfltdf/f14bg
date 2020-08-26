package com.f14.f14bgdb.service.impl

import com.f14.bg.VPCounter
import com.f14.bg.VPResult
import com.f14.bg.exception.BoardGameException
import com.f14.f14bgdb.dao.BgInstanceDao
import com.f14.f14bgdb.dao.BoardGameDao
import com.f14.f14bgdb.dao.RankingListDao
import com.f14.f14bgdb.dao.UserDao
import com.f14.f14bgdb.model.*
import com.f14.f14bgdb.service.BgInstanceManager
import com.f14.f14bgdb.util.ScoreUtil
import com.f14.framework.common.dao.BaseDao
import com.f14.framework.common.service.BaseManagerImpl
import com.f14.utils.CommonUtil
import net.sf.json.JSONObject
import java.util.*
import kotlin.math.roundToLong

open class BgInstanceManagerImpl : BaseManagerImpl<BgInstanceModel, Long>(), BgInstanceManager {
    open lateinit var bgInstanceDao: BgInstanceDao
    open lateinit var userDao: UserDao
    open lateinit var boardGameDao: BoardGameDao
    open lateinit var rankingListDao: RankingListDao

    override val dao: BaseDao<BgInstanceModel, Long>
        get() = bgInstanceDao

    /**
     * 保存游戏结果
     *
     * @param result
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun saveGameResult(result: VPResult): BgInstanceModel {
        val bg = boardGameDao[result.boardGame.room.type.toString()] ?: throw BoardGameException("找不到指定的游戏!")

        val o = BgInstanceModel()
        o.config = JSONObject.fromObject(result.boardGame.config).toString()
        o.boardGame = bg
        o.playerNum = result.boardGame.players.size
        o.gameTime = System.currentTimeMillis() - result.boardGame.startTime.time

        val rankMap = HashMap<VPCounter, RankingListModel>()
        val process = ScoreProcess(result.boardGame)
        // 设置明细信息
        for (vpc in result.vpCounters) {
            val u = userDao[vpc.player.user.id] ?: continue

            // 取得玩家当前积分
            val rank: RankingListModel = rankingListDao.getRankingList(u.id!!, bg.id!!) ?: createRankingList(u, bg.id)
            process.addVPCounter(vpc, rank.rankPoint!!)
            rankMap[vpc] = rank
        }

        // 计算积分和排名信息
        process.count()
        for ((vpc, rank) in rankMap) {
            // 计算胜率及积分
            if (vpc.isWinner) {
                rank.numWins = rank.numWins!! + 1
            } else {
                rank.numLoses = rank.numLoses!! + 1
            }
            rank.numTotal = rank.numWins!! + rank.numLoses!!
            if (rank.numTotal != null && rank.numTotal!! > 0) {
                val rate = 1.0 * rank.numWins!! / rank.numTotal!! * 100
                rank.rate = CommonUtil.formatRate(rate)
            }
            rank.score = rank.score!! + vpc.score
            rank.rankPoint = rank.rankPoint!! + vpc.rankPoint
            // 保存积分和排名信息
            rankingListDao.save(rank)

            // 设置游戏记录明细
            val u = userDao[vpc.player.user.id]
            val r = BgInstanceRecordModel()
            r.user = u
            r.rank = vpc.rank
            r.isWinner = vpc.isWinner
            r.vp = vpc.totalVP
            r.score = vpc.score.toInt()
            r.rankPoint = vpc.rankPoint.toInt()
            r.detailStr = vpc.toJSONString()
            o.addBgInstanceRecord(r)
        }
        // 保存游戏记录
        save(o)
        return o
    }

    /**
     * 保存游戏战报
     *
     * @param o
     * @param descr
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun saveGameReport(o: BgInstanceModel, descr: String) {
        val report = BgReportModel()
        report.descr = descr
        o.addBgReport(report)
        update(o)
    }

    /**
     * 创建默认的排行榜对象
     *
     * @param user
     * @return
     */
    private fun createRankingList(user: UserModel, boardGameId: String?): RankingListModel {
        val rank = RankingListModel()
        rank.userId = user.id
        rank.boardGameId = boardGameId
        rank.loginName = user.loginName
        rank.userName = user.userName
        rank.numWins = 0L
        rank.numLoses = 0L
        rank.score = 0L // 初始积分为0
        rank.rankPoint = ScoreUtil.defaultRankPoint // 初始排名点数
        return rank
    }

    class ScoreProcess(var bg: com.f14.bg.BoardGame<*, *, *>) {
        var scoreCounters: MutableCollection<ScoreCounter> = ArrayList()

        /**
         * 得到胜者的数量
         * @return
         */
        val winnerNumber: Int
            get() = scoreCounters.count { sc -> sc.vpCounter.isWinner }

        /**
         * 添加VPCounter
         * @param o
         * @param orgRankPoint
         */
        fun addVPCounter(o: VPCounter, orgRankPoint: Long) {
            val s = ScoreCounter(o)
            scoreCounters.add(s)
        }

        /**
         * 计算所有VPCounter得到的积分和排名
         */
        fun count() {
            val playerNum = scoreCounters.size
            // 单局游戏总排名点数
            val totalRankPoint = ScoreUtil.getRoundRankPoint(playerNum).toDouble()
            // 计算所有玩家在本局游戏中得到的积分和胜利点数
            var rest = 0
            for (e in scoreCounters) {
                e.rate = 1 / playerNum.toDouble()// e.orgRankPoint/total;
                e.factor = ScoreUtil.getRankPointFactor(playerNum, e.vpCounter.rank, this.bg.isTeamMatch)
                e.vpCounter.score = ScoreUtil.getScore(playerNum, e.vpCounter.rank).toLong()
                e.vpCounter.rankPoint = ((e.factor - e.rate) * totalRankPoint).roundToLong()
                if (e.vpCounter.isWinner && e.vpCounter.rankPoint <= 0) {
                    // 胜者至少能得到1分...
                    rest += 1 - e.vpCounter.rankPoint.toInt()
                    e.vpCounter.rankPoint = 1
                }
            }
            // 如果胜者分数有经过调整,则将其被调整的分数从其他玩家的得分中扣除
            if (rest != 0) {
                val winNum = this.winnerNumber
                // 如果全部都是胜者,则不扣分
                if (winNum < playerNum) {
                    val each = rest / (playerNum - winNum)
                    for (e in scoreCounters.filterNot { it.vpCounter.isWinner }) {
                        e.vpCounter.rankPoint = e.vpCounter.rankPoint - each
                    }
                }
            }
        }
    }

    class ScoreCounter(var vpCounter: VPCounter) {
        var rate: Double = 0.0
        var factor: Double = 0.0
    }

}
