package com.f14.bg

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.ISendable
import com.f14.bg.action.ISendableWith
import com.f14.bg.consts.BgState
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.ActionListener
import com.f14.bg.listener.InterruptParam
import com.f14.bg.listener.ListenerHandler
import com.f14.bg.player.Player
import com.f14.bg.report.BgReport
import com.f14.bg.utils.CheckUtils
import kotlinx.coroutines.CoroutineStart.DEFAULT
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.runBlocking
import org.apache.log4j.Logger
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

abstract class GameMode<P : Player, C : BoardGameConfig, R : BgReport<P>>(boardGame: BoardGame<P, C, R>) : ISendable by boardGame, ISendableWith<P?> by boardGame {
    protected val log = Logger.getLogger(this.javaClass)!!

    /**
     * 当前回合数
     */
    var round: Int = 0
    open val game: BoardGame<P, C, R> = boardGame
    protected val stack: Stack<ListenerHandler<P, C, R>> = Stack()
    protected val currentHandler: ListenerHandler<P, C, R>?
        get() = if (stack.isEmpty()) null else stack.peek()

    init {
        this.round = 1
        this.init()
    }

    /**
     * 添加监听器
     * @param listener
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun addListener(listener: ActionListener<P, C, R>) {
        listener.startListen()
        if (listener.isAllPlayerResponsed) {
            // 如果无需玩家输入,则直接结束,并且不需要唤醒线程
            listener.onAllPlayerResponsed()
            listener.endListen()
        } else {
            // 否则的话,添加到主线程中,并等待玩家输入
            runBlocking { waitForInput(listener) }
        }
    }

    /**
     * 游戏结束时执行的代码
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun endGame() {
        game.state = BgState.END
    }

    /**
     * 回合结束
     */
    protected open fun endRound() {
        this.round++
    }

    /**
     * 取得当前的监听器
     * @return
     */
    val currentListener: ActionListener<P, C, R>?
        get() = this.currentHandler?.listener

    /**
     * 取得战报记录对象
     * @return
     */
    val report
        get() = this.game.report

    /**
     * 初始化参数
     */
    protected open fun init() {
    }

    /**
     * 回合初始化
     */
    protected open fun initRound() {
        this.report.system("第 $round 回合开始!")
        // 清除所有玩家的回合参数
        for (player in this.game.players) {
            player.params.clearRoundParameters()
        }
    }

    /**
     * 插入监听器,并等待到该监听器执行完成
     * @param listener
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun insertListener(listener: ActionListener<P, C, R>): InterruptParam {
        listener.startListen()
        if (listener.isAllPlayerResponsed) {
            // 如果无需玩家输入,则直接结束,插入的监听器都不需要唤醒线程
            listener.onAllPlayerResponsed()
            listener.endListen()
        } else {
            // 否则的话,设置次监听器线程,并等待到该线程执行完成
            runBlocking { waitForInput(listener) }
        }
        // 刷新当前监听器的监听信息
        this.currentHandler?.listener?.sendCurrentPlayerListeningResponse()
        // 返回中断参数
        return listener.createInterruptParam()
    }

    /**
     * 判断游戏是否可以结束
     * @return
     */
    protected abstract val isGameOver: Boolean

    /**
     * 回合中的行动
     */
    @Throws(BoardGameException::class)
    protected abstract fun round()

    lateinit var receiver: Channel<GameAction>

    private val roundLimit = 10000

    /**
     * 执行游戏流程
     * @throws Exception
     */
    @Throws(Exception::class)
    fun run(job: Job) = GlobalScope.actor<GameAction>(job, UNLIMITED, DEFAULT) {
        receiver = channel
        startGame()
        while (!isGameOver) {
            initRound()
            round()
            endRound()
            CheckUtils.check(round > roundLimit, "回合数大于限制,游戏中止!")
        }
        endGame()
    }

    /**
     * 游戏初始化设置
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected abstract fun setupGame()

    /**
     * 游戏开始时执行的代码
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    protected open fun startGame() {
        this.setupGame()
        this.game.sendPlayingInfo()
    }

    /**
     * 等待玩家输入,输入完成或者等待超时后会检查当前 游戏状态,如果游戏状态被中断则会抛出异常
     * @param listener
     * @throws BoardGameException
     * @throws InterruptedException
     */
    @Throws(BoardGameException::class)
    private suspend fun waitForInput(listener: ActionListener<P, C, R>) {
        val handler = ListenerHandler(this, listener)
        stack.push(handler)
        handler.run()
        when (this.game.state) {
            BgState.INTERRUPT -> throw BoardGameException("游戏异常中止!")
            BgState.WIN -> {// 中盘获胜
                this.endGame()
                throw BoardGameException("游戏结束!")
            }
            else -> if (stack.isNotEmpty()) stack.pop()
        }
    }

    /**
     * 唤醒
     */
    fun wakeUp() {
        while (stack.isNotEmpty()) stack.pop().done()
        if (this.game.state == BgState.WIN) this.endGame()
    }
}
