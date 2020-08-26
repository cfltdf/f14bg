package com.f14.innovation

import com.f14.bg.report.BgReport
import com.f14.innovation.component.InnoCard
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoSplayDirection


class InnoReport(bg: Innovation) : BgReport<InnoPlayer>(bg) {

    fun playerAddHand(player: InnoPlayer, card: InnoCard) {
        this.action(player, "获得了手牌" + card.reportString)

    }

    fun playerAddHand(player: InnoPlayer, level: Int) {
        this.action(player, "获得了手牌[$level]")
    }

    fun playerAddScore(player: InnoPlayer, card: InnoCard) {
        this.action(player, "记分了" + card.reportString)
    }

    fun playerAddScore(player: InnoPlayer, level: Int) {
        this.action(player, "记分了[$level]")

    }

    fun playerDogmaCard(player: InnoPlayer, card: InnoCard) {
        this.action(player, "触发了" + card.reportString)
    }

    fun playerDrawAchieveCard(player: InnoPlayer, card: InnoCard) {
        this.action(player, "获得了成就" + card.reportString)
    }

    fun playerMeld(player: InnoPlayer, card: InnoCard) {
        this.action(player, "融合了" + card.reportString)
    }

    fun playerRemoveHand(player: InnoPlayer, level: Int) {
        this.action(player, "失去了手牌[$level]")

    }

    fun playerRemoveScore(player: InnoPlayer, level: Int) {
        this.action(player, "失去了分数[$level]")

    }

    fun playerRemoveStackCard(player: InnoPlayer, c: InnoCard) {
        this.action(player, "失去了" + c.reportString)

    }

    fun playerReturnCard(player: InnoPlayer, level: Int) {
        this.action(player, "归还了[$level]")

    }

    fun playerRevealHand(player: InnoPlayer, card: InnoCard) {
        this.action(player, "展示了手牌" + card.reportString)

    }

    fun playerSelectColor(player: InnoPlayer, colors: Array<InnoColor>) {
        val sb = StringBuilder("选择了")
        var sep = ""
        for (c in colors) {
            sb.append(sep).append(InnoColor.getDescr(c))
            sep = "和"
        }
        this.action(player, sb.toString())
    }

    fun playerSplay(player: InnoPlayer, color: InnoColor, splayDirection: InnoSplayDirection) {
        this.action(player, "将" + InnoColor.getDescr(color) + "牌堆向" + InnoSplayDirection.getDescr(splayDirection) + "展开")
    }

    fun playerTuck(player: InnoPlayer, card: InnoCard) {
        this.action(player, "垫底了" + card.reportString)
    }

}
