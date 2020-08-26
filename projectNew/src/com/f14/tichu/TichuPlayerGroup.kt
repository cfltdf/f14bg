package com.f14.tichu

import com.f14.bg.component.Convertable
import com.f14.bg.player.PlayerGroup
import com.f14.bg.report.Printable

class TichuPlayerGroup : PlayerGroup<TichuPlayer>(), Convertable, Printable {
    var bothCatchScore: Int = 0
    var addScore: Int = 0
    var formula: String = ""
    var roundScore: Int = 0

    /**
     * 计算回合分数
     */
    fun calculateRoundScore() {
        this.roundScore = this.bothCatchScore + this.addScore + this.groupPlayerScore
        this.formula = this.roundScore.toString() + " + " + this.score
        this.score += this.roundScore
    }

    /**
     * 取得组中所有玩家的得分

     * @return
     */
    val groupPlayerScore: Int
        get() = players.sumBy(TichuPlayer::totalScore)


    override val reportString: String
        get() = this.players.joinToString(" 与 ", transform = TichuPlayer::reportString)

    /**
     * 重置回合分数
     */
    fun resetRoundScore() {
        this.bothCatchScore = 0
        this.addScore = 0
        this.formula = ""
        this.roundScore = 0
    }


    override fun toMap() = mapOf(
            "bothCatchScore" to bothCatchScore,
            "addScore" to addScore,
            "formula" to formula,
            "score" to score)
}
