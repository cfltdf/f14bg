package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.component.TSCountry
import com.f14.TS.listener.initParam.InitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam

/**
 * 使用初始化参数的TS中断监听器
 * @author F14eagle
 */
abstract class TSParamInterruptListener(trigPlayer: TSPlayer, gameMode: TSGameMode, protected val initParam: InitParam) : TSInterruptListener(gameMode, trigPlayer) {

    protected var confirmString: String? = null

    init {
        this.loadInitParam()
    }

    /**
     * 判断玩家是否可以取消该监听器
     * @param gameMode
     * @param action
     * @return
     */
    protected open fun canCancel(action: GameAction) = this.initParam.isCanCancel

    /**
     * 判断玩家是否可以跳过该监听器
     * @param gameMode
     * @param action
     * @return
     */
    protected open fun cannotPass(action: GameAction) = !this.initParam.isCanPass

    /**
     * 玩家确认时进行的校验
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun confirmCheck(action: GameAction)

    override fun createInterruptParam(): InterruptParam {
        val param = super.createInterruptParam()
        param["confirmString"] = this.confirmString
        param["trigType"] = this.initParam.trigType
        param["card"] = this.initParam.card
        param["validCode"] = this.validCode
        return param
    }

    override fun createStartListenCommand(player: TSPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        // 将关联卡牌的信息设置到监听消息中
        res.public("cardId", this.card?.id)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val confirmString = action.getAsString("confirmString")
        this.confirmString = confirmString
        when (confirmString) {
            ConfirmString.CONFIRM -> {
                this.confirmCheck(action)
                this.doConfirm(action)
            }
            ConfirmString.CANCEL -> this.doCancel(action)
            ConfirmString.PASS -> this.doPass(action)
            ConfirmString.RESET -> this.doReset(action)
            else -> // 否则执行其他行动
                this.doSubact(action)
        }
    }

    /**
     * 玩家取消时进行的操作
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun doCancel(action: GameAction) {
        // 如果玩家选择取消,则需要判断是否可以取消该监听器
        if (!this.canCancel(action)) throw BoardGameException(this.getMsg(action.getPlayer()))
        // 取消时需要先重置
        this.doReset(action)
        // 设置玩家回应
        this.setPlayerResponsed(action.getPlayer<TSPlayer>())
    }

    /**
     * 玩家确认时进行的操作
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun doConfirm(action: GameAction)

    /**
     * 玩家放弃时进行的操作
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun doPass(action: GameAction) {
        // 如果玩家选择跳过,则需要判断是否可以跳过该监听器
        if (this.cannotPass(action)) throw BoardGameException(this.getMsg(action.getPlayer()))
        // 设置玩家回应
        this.setPlayerResponsed(action.getPlayer<TSPlayer>())
    }

    /**
     * 玩家重置时进行的操作
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun doReset(action: GameAction) = Unit

    /**
     * 玩家确认时进行的操作
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun doSubact(action: GameAction)

    /**
     * 返回参数中的卡牌
     * @return
     */
    protected val card: TSCard?
        get() = this.initParam.card

    /**
     * 取得参数中监听的玩家
     * @return
     */
    protected val listeningPlayer: TSPlayer
        get() = this.gameMode.game.getPlayer(this.initParam.listeningPlayer)!!

    override fun getMsg(player: TSPlayer) = this.initParam.realMsg

    /**
     * 取得实际的OP
     * @return
     */
    protected fun getOP(player: TSPlayer, countries: Collection<TSCountry>?): Int {
        val card = this.card ?: return this.initParam.num
        return player.getOp(card, countries)
    }

    /**
     * 装载初始化参数
     * @param initParam
     */
    protected fun loadInitParam() {
        // 设置该监听器监听的玩家
        val player = gameMode.game.getPlayer(this.initParam.listeningPlayer) ?: return
        this.addListeningPlayer(player)
    }
}
