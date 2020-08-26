package com.f14.TTA.listener.active

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.listener.TTAActiveCardListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils


/**
 * 扩张人口并建造部队的监听器

 * @author F14eagle
 */
class ActiveIncreaesUnitListener(param: RoundParam, card: TTACard) : TTAActiveCardListener(param, card) {

    private var buildCard: TechCard? = null

    init {
        this.buildCard = null
    }


    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param[trigPlayer.position] = this.buildCard
        return param
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val confirm = action.getAsBoolean("confirm")
        val player = action.getPlayer<TTAPlayer>()
        if (confirm) {
            // 检查选择的卡牌是否可以应用到该能力
            val cardId = action.getAsString("cardId")
            val card = player.getPlayedCard(cardId)
            CheckUtils.check(!activeCard.activeAbility!!.test(card), "该能力不能在这张牌上使用!")

            this.buildCard = card as TechCard
        }
        this.setPlayerResponsed(player)
    }


    override val actionString: String
        get() = TTACmdString.ACTION_BUILD


    override fun getMsg(player: TTAPlayer): String {
        return "请选择要建造的部队!"
    }
}
