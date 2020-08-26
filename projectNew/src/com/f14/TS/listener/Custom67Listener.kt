package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.consts.CardType
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSConsts
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam


/**
 * #67-向苏联出售谷物的监听器

 * @author F14eagle
 */
class Custom67Listener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: InitParam,
                       /**
                        * 随机抽的牌,可能为空
                        */
                       private var drawnCard: TSCard?) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {
    private var action: String? = null

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
    }

    /**
     * 检查抽的牌是否存在
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun checkDrawnCard() {
        this.drawnCard ?: throw BoardGameException("没有抽到牌!")
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        res["action"] = action
        return res
    }

    override fun createStartListenCommand(player: TSPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        if (this.drawnCard != null) {
            res.public("drawnCardId", this.drawnCard!!.id)
            res.public("uniEnabled", this.getUniEnabled(player))
        }
        res.public("spaceRaceChance", player.availableSpaceRaceTimes)
        return res
    }

    /**
     * 触发事件
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doActiveEvent(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        this.checkDrawnCard()
        // 检查是否可以发生事件
        if (!gameMode.eventManager.canActiveCard(this.drawnCard!!)) {
            throw BoardGameException("所选牌的事件不能发生!")
        }
        this.action = TSCmdString.ACTION_ACTIVE_EVENT
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    private fun doActWithUni(player: TSPlayer, drawnCard: TSCard?) {
        val uni = player.getCardByCardNo(TSConsts.UNI_CARD_NO)
        this.checkDrawnCard()
        if (uni == null) {
            throw BoardGameException("你手牌没有联合国干涉!")
        } else if (drawnCard!!.superPower != gameMode.game.getOppositePlayer(player.superPower).superPower) {
            throw BoardGameException("联合国干涉需要同对方事件一起打出!")
        }
        this.action = TSCmdString.ACTION_WITH_UNI
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {

    }

    /**
     * 使用OP进行行动
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doOpAction(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        this.checkDrawnCard()
        // 检查是否可以发生OP
        if (!gameMode.eventManager.canPlayOpCard(this.drawnCard!!)) {
            throw BoardGameException("所选牌的卡牌不能用作行动点!")
        }
        if (drawnCard!!.cardType == CardType.SCORING) {
            throw BoardGameException("计分牌只能以 发生事件 的方式打出!")
        }
        this.action = TSCmdString.ACTION_USE_OP
        this.setPlayerResponsed(player)
    }

    /**
     * 执行太空竞赛
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doSpaceRace(player: TSPlayer, card: TSCard) {
        // 检查玩家是否可以进行太空竞赛
        gameMode.spaceRaceManager.checkSpaceRace(player, card)
        this.action = TSCmdString.ACTION_SPACE_RACE
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        when (action.getAsString("subact")) {
            TSCmdString.ACTION_USE_OP -> // 使用OP
                this.doOpAction(action)
            TSCmdString.ACTION_ACTIVE_EVENT -> // 发生事件
                this.doActiveEvent(action)
            TSCmdString.ACTION_SPACE_RACE -> {
                // 太空竞赛
                this.checkDrawnCard()
                this.doSpaceRace(player, drawnCard!!)
            }
            TSCmdString.ACTION_WITH_UNI -> this.doActWithUni(player, drawnCard)
            "return" -> this.returnCard(player)
            else -> throw BoardGameException("无效的行动指令!")
        }
    }

    private fun getUniEnabled(player: TSPlayer): Boolean {
        return drawnCard!!.superPower == gameMode.game.getOppositePlayer(player.superPower).superPower && player.getCardByCardNo(TSConsts.UNI_CARD_NO) != null && gameMode.eventManager.canPlayOpCard(player.getCardByCardNo(TSConsts.UNI_CARD_NO)!!)
    }

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_67

    private fun returnCard(player: TSPlayer) {
        // 如果选择的是退回或者苏联没有手牌,则可以正常使用本牌的OP
        this.action = "return"
        this.setPlayerResponsed(player)
    }
}
