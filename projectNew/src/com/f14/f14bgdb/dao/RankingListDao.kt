package com.f14.f14bgdb.dao

import com.f14.f14bgdb.model.RankingListModel
import com.f14.framework.common.dao.BaseDao

interface RankingListDao : BaseDao<RankingListModel, Long> {

    /**
     * 按照用户id和游戏id取得排行榜对象
     *
     * @param userId
     * @param boardGameId
     * @return
     */
    fun getRankingList(userId: Long, boardGameId: String): RankingListModel?

    /**
     * 按照条件查询排行榜, 游戏数低于20的不做排行
     *
     * @param condition
     * @param order
     * @return
     */
    fun queryRankingList(condition: RankingListModel, order: String): List<RankingListModel>

}
