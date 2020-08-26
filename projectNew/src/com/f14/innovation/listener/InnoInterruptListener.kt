package com.f14.innovation.listener

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.anim.AnimType
import com.f14.bg.consts.ConfirmString
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ListenerType
import com.f14.innovation.InnoGameMode
import com.f14.innovation.InnoPlayer
import com.f14.innovation.command.InnoCommandList
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.consts.InnoActiveType
import com.f14.innovation.consts.InnoPlayerTargetType
import com.f14.innovation.param.InnoInitParam
import com.f14.innovation.param.InnoResultParam


/**
 * Innovation的中断监听器(所有玩家同时执行)
 * @author F14eagle
 */
abstract class InnoInterruptListener(gameMode: InnoGameMode, protected val trigPlayer: InnoPlayer, val initParam: InnoInitParam?, val resultParam: InnoResultParam, val ability: InnoAbility?, val abilityGroup: InnoAbilityGroup?) : InnoActionListener(gameMode, ListenerType.INTERRUPT) {


    protected var confirmString: String? = null

    lateinit var commandList: InnoCommandList

    protected var next: InnoInterruptListener? = null

    init {
        this.addListeningPlayer(trigPlayer)
    }

    /**
     * 判断玩家是否可以取消该监听器
     * @param action
     * @return
     */
    protected open fun canCancel(action: GameAction) = this.initParam?.isCanCancel == true

    /**
     * 判断玩家是否可以跳过该监听器
     * @param action
     * @return
     */
    protected open fun canPass(action: GameAction) = this.initParam?.isCanPass == true

    /**
     * 玩家确认时进行的校验
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun confirmCheck(action: GameAction)

    override fun createInterruptParam() = super.createInterruptParam().also {
        it["confirmString"] = this.confirmString
        it["validCode"] = this.validCode
        it["resultParam"] = this.resultParam
        if (next != null) it["next"] = this.next
    }

    override fun createStartListenCommand(player: InnoPlayer): BgResponse {
        val res = super.createStartListenCommand(player)
        res.public("msg", this.getMsg(player))
        // 设置行动字符串
        res.public("actionString", this.actionString)
        // 将关联卡牌的信息设置到监听消息中
        res.public("cardId", this.commandList.mainCard.id)
        // 设置按钮的显示情况
        val action = GameAction(player.user, player.room, "{}")
        res.public("showConfirmButton", this.showConfirmButton())
        res.public("showCancelButton", this.canCancel(action))
        res.public("showPassButton", this.canPass(action))
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
        this.setPlayerResponsed(action.getPlayer<InnoPlayer>())
    }

    /**
     * 玩家确认时进行的操作
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun doConfirm(action: GameAction)

    /**
     * 玩家放弃时进行的操作
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun doPass(action: GameAction) {
        // 如果玩家选择跳过,则需要判断是否可以跳过该监听器
        if (!this.canPass(action)) throw BoardGameException(this.getMsg(action.getPlayer()))
        // 设置玩家回应
        this.setPlayerResponsed(action.getPlayer<InnoPlayer>())
    }

    /**
     * 玩家重置时进行的操作
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun doReset(action: GameAction) = Unit

    /**
     * 玩家进行的其他操作
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected fun doSubact(action: GameAction) = Unit

    /**
     * 取得选择行动字符串
     * @return
     */
    protected open val actionString: String
        get() = ""

    /**
     * 取得当前针对处理效果的玩家
     * @return
     */
    val currentPlayer: InnoPlayer
        get() = this.commandList.currentPlayer

    /**
     * 取得触发能力的主要玩家
     * @return
     */
    protected val mainPlayer: InnoPlayer
        get() = this.commandList.mainPlayer

    /**
     * 取得提示文本
     * @param player
     * @return
     */
    protected open fun getMsg(player: InnoPlayer) = this.initParam?.realMsg ?: ""

    /**
     * 按照参数配置取得实际的目标玩家
     * @return
     */
    val targetPlayer: InnoPlayer
        get() = when (this.initParam?.targetPlayer) {
            InnoPlayerTargetType.MAIN_PLAYER -> this.commandList.mainPlayer
            InnoPlayerTargetType.CURRENT_PLAYER -> this.currentPlayer
            else -> this.trigPlayer
        }

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        super.onAllPlayerResponsed()
        // 如果是确认行动,则所有玩家都完成行动后,设置执行结果参数
        if (ConfirmString.CONFIRM == this.confirmString) {
            this.setExecuteResult()
        }
    }

    /**
     * 刷新玩家的当前提示信息
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun refreshMsg(player: InnoPlayer) {
        val res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_REFRESH_MSG, player.position)
        res.public("msg", this.getMsg(player))
        gameMode.game.sendResponse(player, res)
    }

    /**
     * 设置返回参数
     */
    protected fun setExecuteResult() {
        this.resultParam.animType = this.initParam?.animType ?: AnimType.DIRECT
        // 只有当正常执行时,才设置是否触发能力的参数
        if (ConfirmString.CONFIRM == this.confirmString) {
            if (this.initParam?.isSetActived == true) {
                when (this.abilityGroup?.activeType) {
                    InnoActiveType.DEMAND // 如果是被要求的能力,则设置为被要求执行过能力
                    -> this.commandList.setPlayerDomanded(this.currentPlayer)
                    else // 否则就设置为触发过能力
                    -> this.commandList.setPlayerActived(this.currentPlayer)
                }
            }
        }
    }

    override fun setListenerInfo(res: BgResponse) = super.setListenerInfo(res).public("trigPlayerPosition", this.trigPlayer.position)

    /**
     * 是否显示确定按钮
     * @return
     */
    protected open fun showConfirmButton() = this.initParam?.isShowConfrimButton ?: true

}
