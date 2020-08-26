package com.f14.f14bgdb.service.impl

import com.f14.f14bgdb.dao.RankingListDao
import com.f14.f14bgdb.model.RankingListModel
import com.f14.f14bgdb.service.RankingManager
import com.f14.framework.common.dao.BaseDao
import com.f14.framework.common.service.BaseManagerImpl

open class RankingManagerImpl : BaseManagerImpl<RankingListModel, Long>(), RankingManager {
    open lateinit var rankingListDao: RankingListDao

    override val dao: BaseDao<RankingListModel, Long>
        get() = rankingListDao

    /**
     * 查询排行榜信息,并以积分倒叙排列
     *
     * @param condition
     * @return
     */
    override fun queryRankingList(condition: RankingListModel): List<RankingListModel> {
        return rankingListDao.queryRankingList(condition, "rankPoint desc,score desc")
    }

    /**
     * 查询用户所有游戏的积分,以游戏总数倒叙排列
     *
     * @param userId
     * @return
     */
    override fun queryUserRanking(userId: Long?): List<RankingListModel> {
        val condition = RankingListModel()
        condition.userId = userId
        return rankingListDao.query(condition, "numTotal desc")
    }
}
