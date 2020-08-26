package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.consts.*
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import java.util.*

class TSHeadLineListener(gameMode: TSGameMode) : TSActionListener(gameMode) {

    private var headlines: MutableList<HeadLineParam> = ArrayList()

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 开始时重置背叛者效果
        this.clearCancelHeadlineEffects()
        for (player in gameMode.game.players) {
            val param = HeadLineParam(player)
            this.setParam(player, param)
        }
        // 检查是否有玩家有太空竞赛2级特权-对方先展示头条
        gameMode.game.players.filter { it.hasEffect(EffectType.SR_PRIVILEGE_2) }
                // 如果有,则将对手的展示参数设为true
                .map { gameMode.game.getOppositePlayer(it.superPower) }.map { this.getParam<HeadLineParam>(it) }.forEach { it.revealFirst = true }
    }

    /**
     * 移除所有玩家的取消头条的效果
     * @param gameMode
     */
    private fun clearCancelHeadlineEffects() {
        for (player in gameMode.game.players) {
            // 移除背叛者牌的效果
            player.removeEffect(gameMode.cardManager.defactorCard)
        }
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        if (TSCmdString.RESIGN == action.getAsString("subact")) {
            gameMode.game.playerResigen(player)
            return
        }
        val param = this.getParam<HeadLineParam>(player)
        if (param.card != null) {
            throw BoardGameException("你已经选择过头条了!")
        }
        val cardId = action.getAsString("cardId").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择要头条的卡牌!")
        val card = player.getCard(cardId)
        if (!card.canHeadLine) {
            throw BoardGameException("这张牌不能用做头条!")
        }
        param.card = card
        // 发送头条选择状态
        this.sendHeadLineParam(null)
        this.sendInputStateParam(player)
        // 执行前置事件（目前只有鲜花反战）（不需要，因为后面会执行）
        // mode.getGame().onPlayerHeadline(param.player, null, param.card);
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_HEAD_LINE

    /**
     * 判断是否所有的玩家都选择好了头条
     * @param gameMode
     * @return
     */
    private fun isAllHeadLineSelected(): Boolean {
        return gameMode.game.players.map { this.getParam<HeadLineParam>(it) }.none { it.card == null }
    }

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        // 如果所有玩家都选择完成,则执行头条
        this.headlines.clear()
        // 首先检查是否存在背叛者
        for (player in gameMode.game.players) {
            val param = this.getParam<HeadLineParam>(player)
            // 移除手牌并输出战报
            gameMode.game.playerPlayCard(player, param.card!!)
            gameMode.report.playerHeadLine(player, param.card!!)
            if (param.card!!.tsCardNo == TSConsts.DEFACTOR_CARD_NO) {
                // 如果是背叛者,则优先执行,能力生效
                gameMode.report.playerActiveCard(player, param.card!!)
                gameMode.game.activeCardEvent(player, param.card!!)
            } else {
                // 否则添加到处理列表中
                this.headlines.add(param)
            }
        }
        // 整理头条的触发顺序
        this.headlines.sort()
        // 执行头条
        gameMode.game.executeHeadLine(this.headlines)
        // 结束时也需要重置背叛者效果
        this.clearCancelHeadlineEffects()
    }

    override fun onPlayerStartListen(player: TSPlayer) {
        super.onPlayerStartListen(player)
        this.sendHeadLineParam(player)
        this.sendInputStateParam(player)
    }

    /**
     * 向指定玩家发送头条选择状态的信息

     * @param gameMode

     * @param receiver
     */
    private fun sendHeadLineParam(receiver: TSPlayer?) {
        val res = this.createSubactResponse(receiver, "headLineParam")
        val isAllSelected = this.isAllHeadLineSelected()
        res.public("isAllSelected", isAllSelected)
        val ussr = this.getParam<HeadLineParam>(gameMode.game.ussrPlayer)
        val usa = this.getParam<HeadLineParam>(gameMode.game.usaPlayer)
        if (isAllSelected) {
            // 如果已经全部选择完成,则设置选择的卡牌
            res.public("ussrCardId", ussr.card!!.id)
            res.public("usaCardId", usa.card!!.id)
        } else {
            // 如果没有选择完成,则设置是否选择卡牌
            res.public("isUssrSelected", ussr.card != null)
            res.public("isUsaSelected", usa.card != null)
            // 检查是否有需要先展示的头条
            if (ussr.revealFirst && ussr.card != null) {
                res.public("ussrCardId", ussr.card!!.id)
            }
            if (usa.revealFirst && usa.card != null) {
                res.public("usaCardId", usa.card!!.id)
            }
        }
        gameMode.game.sendResponse(receiver, res)
    }

    /**
     * 向指定玩家发送输入选择状态的信息

     * @param gameMode

     * @param receiver
     */
    private fun sendInputStateParam(receiver: TSPlayer) {
        val res = this.createSubactResponse(receiver, "inputState")
        // 只有没选择过头条的玩家才能进行选择
        val param = this.getParam<HeadLineParam>(receiver)
        res.public("selecting", param.card == null)
        gameMode.game.sendResponse(receiver, res)
    }

    /**
     * 头条参数

     * @author F14eagle
     */
    inner class HeadLineParam internal constructor(var player: TSPlayer) : Comparable<HeadLineParam> {
        var card: TSCard? = null
        /**
         * 是否先展示头条
         */
        internal var revealFirst: Boolean = false

        override fun compareTo(other: HeadLineParam): Int {
            // OP大的先执行,如果OP相同,则美国玩家的头条先执行
            return when {
                this.card!!.op < other.card!!.op -> 1
                this.card!!.op > other.card!!.op -> -1
                this.player.superPower == SuperPower.USSR -> 1
                else -> -1
            }
        }
    }

}
