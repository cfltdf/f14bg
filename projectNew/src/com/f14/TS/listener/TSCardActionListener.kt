package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.CardActionInitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam


/**
 * 选择卡牌并执行相应行动的监听器

 * @author F14eagle
 */
class TSCardActionListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: CardActionInitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为监听的玩家创建参数
        for (p in this.listeningPlayers) {
            val param = CardParam(p)
            param.reset()
            this.setParam(p, param)
        }
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {

    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        // 将玩家实际用掉的点数设置在返回参数中
        val player = this.listeningPlayer
        val param = this.getParam<CardParam>(player)
        res["card"] = param.card
        return res
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
        val player = action.getPlayer<TSPlayer>()
        val cardId = action.getAsString("cardId").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择卡牌!")
        val card = player.getCard(cardId)
        // 取得实际的OP值,创建临时的卡牌对象
        val temp = card.clone()
        temp.op = player.getOp(card)
        // 取得实际的条件组
        val cg = this.cardActionInitParam.convertConditionGroup(gameMode, player)
        if (!cg.test(temp)) {
            throw BoardGameException(this.getMsg(player))
        }
        val param = this.getParam<CardParam>(player)
        param.card = card
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doPass(action: GameAction) {
        // 如果玩家选择跳过,则需要判断是否可以跳过该监听器
        if (this.cannotPass(action)) {
            throw BoardGameException(this.getMsg(action.getPlayer()))
        }

        // 设置玩家回应
        this.setPlayerResponsed(action.getPlayer<TSPlayer>())
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {

    }


    override val actionString: String
        get() = TSCmdString.ACTION_SELECT_CARD

    val cardActionInitParam: CardActionInitParam
        get() = super.initParam as CardActionInitParam

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_CARD_ACTION

    /**
     * 选择卡牌的临时参数

     * @author F14eagle
     */
    internal inner class CardParam(var player: TSPlayer) {
        var card: TSCard? = null

        init {
            this.init()
        }

        /**
         * 初始化参数
         */
        fun init() {}

        /**
         * 重置调整参数
         */
        fun reset() {
            this.card = null
        }

    }

}
