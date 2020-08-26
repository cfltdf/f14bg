package com.f14.tichu

import com.f14.bg.report.BgReport
import com.f14.tichu.componet.TichuCard
import com.f14.tichu.componet.TichuCardGroup
import com.f14.tichu.consts.CardValueUtil
import com.f14.tichu.consts.Combination
import com.f14.tichu.consts.TichuType

class TichuReport(bg: Tichu) : BgReport<TichuPlayer>(bg) {

    fun giveCards(player: TichuPlayer, cards: Map<TichuPlayer, TichuCard>) {
        val strPublic = cards.entries.joinToString(",") { "给 ${it.key.reportString} 一张牌"}
        val strPrivate = cards.entries.joinToString(",") {  "给 ${it.key.reportString} ${it.value.reportString}" }
        this.action(player, strPublic, strPrivate)
    }

    /**
     * 玩家叫地主
     * @param player
     * @param tichuType
     */
    fun playerCallTichu(player: TichuPlayer, tichuType: TichuType) {
        this.action(player, " 叫了 ${TichuType.getChinese(tichuType)}", true)
    }

    fun playerGetCards(player: TichuPlayer, cards: List<TichuCard>) {
        val strPublic = "摸了 ${cards.size} 张牌"
        val strPrivate= "摸了 ${cards.joinToString(transform = TichuCard::reportString)}"
        this.action(player, strPublic, strPrivate)
    }

    fun playerGetRank(player: TichuPlayer, rank: Int) {
        this.action(player, "第 $rank 个出完牌,分数为 ${player.score}")
    }

    /**
     * 玩家得到分数
     * @param player
     * @param score
     */
    fun playerGetScore(player: TichuPlayer, score: Int) {
        this.action(player, " 得到 $score 分", true)
    }

    fun playerPass(player: TichuPlayer) {
        this.action(player, "PASS")
    }

    fun playerPlayCards(player: TichuPlayer, group: TichuCardGroup) {
        val str = "打出了 ${Combination.getChinese(group.combination!!)} ${group.cards.joinToString(transform = TichuCard::reportString)}"
        this.action(player, str)
    }

    /**
     * 玩家许愿
     * @param player
     * @param point
     */
    fun playerWishPoint(player: TichuPlayer, point: Double) {
        this.action(player, " 许愿了 ${CardValueUtil.getCardValue(point)}", true)
    }
}
