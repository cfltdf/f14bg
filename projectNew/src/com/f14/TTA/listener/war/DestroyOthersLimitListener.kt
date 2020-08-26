package com.f14.TTA.listener.war

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.AttackCard
import com.f14.TTA.component.card.TechCard
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.common.ParamSet
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil


/**
 * 新版突袭

 * @author F14eagle
 */
class DestroyOthersLimitListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, warCard: AttackCard, winner: TTAPlayer, loser: TTAPlayer, warParam: ParamSet) : DestroyOthersWarListener(gameMode, trigPlayer, warCard, winner, loser, warParam) {
    private var level: Int = 0
    private var count: Int = 0

    init {
        this.level = this.attackCard.loserEffect.level
        this.count = this.attackCard.loserEffect.amount
    }


    override fun cannotPass(): Boolean {
        // 如果战败方有可选建筑,则需要继续监听
        return loser.buildings.cards.any { this.level >= it.level && this.attackCard.loserEffect.test(it) && it.availableCount > 0 }
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val confirm = action.getAsBoolean("confirm")
        if (confirm) {
            val targetPosition = action.getAsInt("targetPosition")
            CheckUtils.check(targetPosition != this.loser.position, "不能选择指定的玩家!")
            val target = gameMode.game.getPlayer(targetPosition)
            val cardId = action.getAsString("cardId")
            val card = target.getPlayedCard(cardId)
            CheckUtils.check(this.level < card.level, "牌的等级超过限制!")
            CheckUtils.check(!this.attackCard.loserEffect.test(card), "不能选择这张牌!")
            CheckUtils.check(!card.needWorker || card.availableCount <= 0, "这张牌上没有工人!")
            CheckUtils.check(popParam.detail.containsKey(card) && card.availableCount <= popParam.detail[card]!!, "这张牌上没有工人!")

            popParam.destory(card as TechCard)
            gameMode.game.refreshDecreasePopulation(target, card, popParam.detail[card]!!, player)

            // 可摧毁建筑等级递减,然后继续监听
            --count
            --level
            if (count == 0 || !this.cannotPass()) {
                this.setPlayerResponsed(player)
            } else {
                refreshMsg(player)
            }
        } else {
            // 狼奆说了可以不拆
            // if (this.needResolution()) {
            // throw new BoardGameException(this.getMsg(player));
            // }
            this.setPlayerResponsed(player)
        }
    }

    override fun getMsg(player: TTAPlayer): String {
        var msg = "你可以摧毁玩家{0}一个不高于{1}级的城市建筑,请选择!"
        msg = CommonUtil.getMsg(msg, this.loser.reportString, this.level)
        return msg
    }
}
