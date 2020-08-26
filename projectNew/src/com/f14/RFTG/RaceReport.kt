package com.f14.RFTG

import com.f14.RFTG.card.Goal
import com.f14.RFTG.card.RaceCard
import com.f14.RFTG.consts.RaceActionType
import com.f14.bg.report.BgCacheReport

class RaceReport(bg: RFTG) : BgCacheReport<RacePlayer>(bg) {

    private fun cards2Msg(cards: List<RaceCard>): String {
        return cards.joinToString(",", transform = RaceCard::getChinese)
    }

    fun playerAddCard(player: RacePlayer, drawnCards: List<RaceCard>) {
        val msgPublic = "抓了" + drawnCards.size + "张卡"
        val msgPrivate = "抓了" + cards2Msg(drawnCards)
        this.addAction(player, msgPublic, msgPrivate)
    }

    fun playerChooseAction(player: RacePlayer, types: List<RaceActionType>) {
        this.action(player, "选择了" + types.joinToString(transform = RaceActionType::chinese))
    }

    fun playerConsume(player: RacePlayer, cards: List<RaceCard>) {
        this.addAction(player,  "消费了" + cards2Msg(cards))
    }

    fun playerDiscard(player: RacePlayer, cards: List<RaceCard>) {
        val msgPublic = "弃了" + cards.size + "张卡"
        val msgPrivate = "弃了" + cards2Msg(cards)
        this.addAction(player, msgPublic, msgPrivate)
    }

    fun playerDiscardPlayed(player: RacePlayer, cards: List<RaceCard>) {
        this.addAction(player, "失去" + cards2Msg(cards))
    }

    fun playerGamble(player: RacePlayer, card: RaceCard) {
        this.addAction(player, "将" + card.getChinese() + "作为赌注")

    }

    fun playerGambleResult(player: RacePlayer, cards: List<RaceCard>) {
        val msgPublic = "翻开了" + cards.size + "张卡"
        val msgPrivate = "翻开了" + cards2Msg(cards)
        this.addAction(player, msgPublic, msgPrivate)
    }

    fun playerGetGoal(player: RacePlayer, goal: Goal) {
        this.action(player, "获得" + goal.reportString)
    }

    fun playerGetVp(player: RacePlayer, i: Int) {
        this.addAction(player, "获得 $i VP")
    }

    fun playerPass(player: RacePlayer) {
        this.addAction(player, "放弃")
    }

    fun playerPlayCards(player: RacePlayer, playedCards: List<RaceCard>) {
        this.addAction(player, "打出了" + cards2Msg(playedCards))
    }

    fun playerProduce(player: RacePlayer, cards: List<RaceCard>) {
        this.addAction(player, "生产了" + cards2Msg(cards))
    }

    fun playerTrade(player: RacePlayer, card: RaceCard) {
        this.addAction(player, "卖出" + card.getChinese())
    }

    fun playerUseCard(player: RacePlayer, card: RaceCard) {
        this.addAction(player, "使用" + card.getChinese())
    }

    fun returnGoal(player: RacePlayer, goal: Goal) {
        this.action(player, "失去" + goal.reportString)
    }


}
