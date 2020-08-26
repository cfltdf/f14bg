package com.f14.TS.executor

import com.f14.TS.ActiveResult
import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSConsts
import com.f14.TS.consts.TrigType
import com.f14.TS.factory.InitParamFactory
import com.f14.TS.listener.Custom67Listener
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.bg.exception.BoardGameException
import com.f14.utils.CollectionUtils


class Custom67Executor(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam) : TSListenerExecutor(trigPlayer, gameMode, initParam) {

    /**
     * 触发所选牌的事件
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun activeEvent(player: TSPlayer, card: TSCard) {
        gameMode.game.activeCardEvent(player, card)
    }

    /**
     * 使用OP进行行动
     * @param player
     * @param card
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun activeOpAction(player: TSPlayer, card: TSCard) {
        val type = TrigType.ACTION
        val initParam = InitParamFactory.createOpActionParam(gameMode, player, card, type)
        val executor = TSOpActionExecutor(player, gameMode, initParam)
        executor.execute()
    }

    /**
     * @param player
     * @param drawnCard
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doActWithUni(player: TSPlayer, drawnCard: TSCard, uni: TSCard) {
        // 从苏联玩家手上移除被抽掉的牌
        this.removeDrawnCardFromHand(drawnCard)
        gameMode.game.discardCard(drawnCard)
        // 输出战报信息
        val type = TrigType.EVENT
        // mode.getGame().playerPlayCard(player, drawnCard);
        gameMode.game.playerRemoveHand(player, uni)
        gameMode.report.playerPlayCard(player, uni, type)
        gameMode.report.playerDiscardCard(player, drawnCard)
        // 触发前置事件
        gameMode.game.onPlayerAction(player, type, uni)
        // 使用OP进行行动
        this.activeOpAction(player, drawnCard)
        val res = ActiveResult(player, true)
        gameMode.game.processActivedCard(uni, res)
    }

    /**
     * 执行太空竞赛
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doSpaceRace(player: TSPlayer, card: TSCard) {
        // 从苏联玩家手上移除被抽掉的牌
        this.removeDrawnCardFromHand(card)
        // 触发前置事件
        gameMode.game.onPlayerAction(player, null, null)
        // 执行太空竞赛
        gameMode.game.playerSpaceRace(player, card)
    }

    @Throws(BoardGameException::class)
    override fun execute() {
        val player = this.initiativePlayer!!
        // 从苏联手上随机抽出一张牌
        val ussr = gameMode.game.ussrPlayer
        if (!ussr.hands.empty) {
            val drawnCard = CollectionUtils.randomDraw(ussr.hands.cards) ?: throw BoardGameException("No such card!")
            // 输出战报
            gameMode.report.playerRandowDrawCard(gameMode.game.usaPlayer, drawnCard)
            val l = Custom67Listener(trigPlayer, gameMode, initParam, drawnCard)
            val param = gameMode.insertListener(l)
            val action = param.getString("action")
            if (TSCmdString.ACTION_USE_OP == action) {
                // 使用OP
                // 从苏联玩家手上移除被抽掉的牌
                this.removeDrawnCardFromHand(drawnCard)
                // 输出战报信息
                val type = TrigType.ACTION
                gameMode.report.playerPlayCard(player, drawnCard, type)
                // 触发前置事件
                gameMode.game.onPlayerAction(player, type, drawnCard)
                // 使用OP进行行动
                this.activeOpAction(player, drawnCard)
                if (player.superPower.oppositeSuperPower == drawnCard.superPower) {
                    // 执行触发事件
                    this.activeEvent(player, drawnCard)
                } else {
                    val res = ActiveResult(player, false)
                    gameMode.game.processActivedCard(drawnCard, res)
                }
            } else if (TSCmdString.ACTION_ACTIVE_EVENT == action) {
                // 发生事件
                // 从苏联玩家手上移除被抽掉的牌
                this.removeDrawnCardFromHand(drawnCard)
                // 输出战报信息
                val type = TrigType.EVENT
                // mode.getGame().playerPlayCard(player, this.drawnCard);
                gameMode.report.playerPlayCard(player, drawnCard, type)
                // 触发前置事件
                gameMode.game.onPlayerAction(player, type, drawnCard)
                // 执行触发事件
                this.activeEvent(player, drawnCard)
                if (player.superPower.oppositeSuperPower == drawnCard.superPower) {
                    // 使用OP进行行动
                    this.activeOpAction(player, drawnCard)
                }
            } else if (TSCmdString.ACTION_SPACE_RACE == action) {
                // 太空竞赛
                this.doSpaceRace(player, drawnCard)
            } else if (TSCmdString.ACTION_WITH_UNI == action) {
                this.doActWithUni(player, drawnCard, player.getCardByCardNo(TSConsts.UNI_CARD_NO)!!)
            } else if ("return" == action) {
                gameMode.report.action(player, "退回了抽到的牌")
                // 如果选择的是退回或者苏联没有手牌,则可以正常使用本牌的OP
                this.activeOpAction(player, card)
            }
        } else {
            gameMode.report.action(ussr, "没有手牌")
            this.activeOpAction(player, card)
        }
    }

    /**
     * 从苏联玩家手上移除被抽掉的牌
     */
    private fun removeDrawnCardFromHand(drawnCard: TSCard) {
        val ussr = gameMode.game.ussrPlayer
        gameMode.game.playerRemoveHand(ussr, drawnCard)
        gameMode.report.playerRemoveCard(ussr, drawnCard)
    }

}
