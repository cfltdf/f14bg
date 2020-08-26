package com.f14.f14bgdb.service

import com.f14.f14bgdb.model.RankingListModel
import com.f14.framework.common.service.BaseManager

interface RankingManager : BaseManager<RankingListModel, Long> {

    /**
     * 查询排行榜信息,并以积分倒叙排列
     *
     * @param condition
     * @return
     */
    fun queryRankingList(condition: RankingListModel): List<RankingListModel>

    /**
     * 查询用户所有游戏的积分,以游戏总数倒叙排列
     *
     * @param userId
     * @return
     */
    fun queryUserRanking(userId: Long?): List<RankingListModel>
}
