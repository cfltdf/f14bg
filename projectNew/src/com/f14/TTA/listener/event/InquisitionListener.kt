package com.f14.TTA.listener.event

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.TechCard
import com.f14.TTA.component.param.PopParam
import com.f14.TTA.consts.CardSubType
import com.f14.TTA.consts.CardType
import com.f14.TTA.consts.TTACmdString
import com.f14.TTA.consts.TTAGameCmd
import com.f14.TTA.listener.TTAEventListener
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils
import com.f14.utils.CommonUtil
import java.util.*
import kotlin.math.max

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class InquisitionListener(gameMode: TTAGameMode, ability: EventAbility, trigPlayer: TTAPlayer) : TTAEventListener(gameMode, ability, trigPlayer) {

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        return !this.canPass(player)
    }

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为所有玩家创建人口参数
        for (player in gameMode.realPlayers) {
            val limit = player.government!!.buildingLimit
            val param = InquisitionParam()
            for (subType in arrayOf(CardSubType.LAB, CardSubType.LIBRARY)) {
                val shouldDestroy = max(0, player.getBuildingNumber(subType) - limit + 1)
                param.shouldDestroys[subType] = shouldDestroy
                player.getBuildingsBySubType(subType).filter { it.availableCount == limit }.map { param.destory(it as TechCard) }.forEach { gameMode.game.refreshDecreasePopulation(player, it, 1) }
            }
            this.setParam(player.position, param)
        }
    }

    private fun canPass(player: TTAPlayer): Boolean {
        val param = this.getParam<InquisitionParam>(player)
        return param.shouldDestroys.values.none { it != 0 }
    }


    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        for (p in this.listeningPlayers) {
            val param = this.getParam<InquisitionParam>(p)
            res[p.position] = param.param
        }
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<InquisitionParam>(player)
        val confirm = action.getAsBoolean("confirm")
        if (confirm) {
            val cardId = action.getAsString("cardId")
            val card = player.buildings.getCard(cardId)
            CheckUtils.check(card.cardType != CardType.BUILDING, "不能选择这张牌!")
            CheckUtils.check(card.cardSubType != CardSubType.LIBRARY && card.cardSubType != CardSubType.LAB, "不能选择这张牌!")
            param.destory(card as TechCard)
            gameMode.game.refreshDecreasePopulation(player, card, 1)
            if (this.canPass(player)) {
                this.setPlayerResponsed(player)
            }
        } else {
            // 判断玩家是否可以结束
            CheckUtils.check(!this.canPass(player), this.getMsg(player))
            this.setPlayerResponsed(player)
        }
    }


    override val actionString: String
        get() = TTACmdString.ACTION_DESTORY

    override fun getMsg(player: TTAPlayer): String {
        val param = this.getParam<InquisitionParam>(player)
        var msg = "你需要摧毁{0}个图书馆和{1}个实验室,请选择!"
        msg = CommonUtil.getMsg(msg, param.shouldDestroys[CardSubType.LIBRARY]
                ?: 0, param.shouldDestroys[CardSubType.LAB] ?: 0)
        return msg
    }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_EVENT_DESTORY

    internal inner class InquisitionParam {
        var shouldDestroys: MutableMap<CardSubType, Int> = HashMap()
        var param = PopParam()

        /**
         * 拆除建筑
         * @param card
         * @throws BoardGameException
         */
        @Throws(BoardGameException::class)
        fun destory(card: TechCard): TechCard {
            CheckUtils.check(card.workers == 0, "这张牌上没有工人!")
            var shouldDestroy: Int = shouldDestroys[card.cardSubType] ?: throw BoardGameException("不能选择这张牌!")
            if (shouldDestroy == 0) throw BoardGameException("不能选择这张牌!")
            shouldDestroy -= 1
            shouldDestroys[card.cardSubType] = shouldDestroy
            param.destory(card)
            return card
        }

    }

}
