package com.f14.bg;

import com.f14.bg.action.BgAction;
import com.f14.bg.consts.BgState;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.ActionListener;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.player.Player;
import com.f14.bg.report.BgReport;
import com.f14.bg.utils.CheckUtils;
import org.apache.log4j.Logger;


public abstract class GameMode<P extends Player> {
    protected Logger log = Logger.getLogger(this.getClass());

    /**
     * 当前回合数
     */
    protected int round;
    protected BoardGame<P, ?, ?, ?> game;
    protected ListenerHandler currentHandler = null;

    public GameMode() {

    }

    public GameMode(BoardGame<P, ?, ?, ?> boardGame) {
        this.boardGame = boardGame;
    }

    /**
     * 添加监听器
     *
     * @param listener
     * @throws BoardGameException
     */
    @SuppressWarnings("unchecked")
    public void addListener(ActionListener listener) throws BoardGameException {
        CheckUtils.check(!this.boardGame.isPlaying(), "游戏异常终止!");
        listener.startListen(this);
        if (listener.isAllPlayerResponsed()) {
            // 如果无需玩家输入,则直接结束,并且不需要唤醒线程
            listener.onAllPlayerResponsed(this);
            listener.endListen(this);
        } else {
            // 否则的话,添加到主线程中,并等待玩家输入
            try {
                this.waitForInput(listener);
            } catch (InterruptedException e) {
                log.error(e, e);
            }
            CheckUtils.check(!this.boardGame.isPlaying(), "游戏异常终止!");
        }
    }

    /**
     * 执行行动
     *
     * @param action
     * @throws BoardGameException
     */
    public void doAction(BgAction action) throws BoardGameException {
        this.currentHandler.doAction(action);
    }

    /**
     * 游戏结束时执行的代码
     *
     * @throws BoardGameException
     */
    protected void endGame() throws BoardGameException {
    }

    /**
     * 回合结束
     */
    protected void endRound() {
        this.round++;
    }

    /**
     * 取得当前的监听器
     *
     * @return
     */

    @SuppressWarnings("rawtypes")
    public ActionListener getCurrentListener() {
        if (this.currentHandler != null) {
            return this.currentHandler.listener;
        } else {
            return null;
        }
    }

    /**
     * 取得游戏对象
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public <BG extends BoardGame<P, ?, ?, ?>> BG getGame() {
        return (BG) this.boardGame;
    }

    /**
     * 取得战报记录对象
     *
     * @return
     */
    public BgReport getReport() {
        return this.getBoardGame().getReport();
    }

    /**
     * 取得当前回合数
     *
     * @return
     */
    public int getRound() {
        return this.round;
    }

    /**
     * 初始化参数
     */
    protected void init() {
        this.round = 1;
    }

    /**
     * 回合初始化
     */
    protected void initRound() {
        this.getReport().system("第 " + round + " 回合开始!");
        // 清除所有玩家的回合参数
        for (Player player : this.getBoardGame().getPlayers()) {
            player.getParams().clearRoundParameters();
        }
    }

    /**
     * 插入监听器,并等待到该监听器执行完成
     *
     * @param listener
     * @throws BoardGameException
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public InterruptParam insertListener(ActionListener listener) throws BoardGameException {
        CheckUtils.check(!this.boardGame.isPlaying(), "游戏异常终止!");
        listener.startListen(this);
        if (listener.isAllPlayerResponsed()) {
            // 如果无需玩家输入,则直接结束,插入的监听器都不需要唤醒线程
            listener.onAllPlayerResponsed(this);
            listener.endListen(this);
        } else {
            // 否则的话,设置次监听器线程,并等待到该线程执行完成
            ListenerHandler handler = new ListenerHandler(this, this.currentHandler);
            this.currentHandler = handler;
            handler.start(listener);
            CheckUtils.check(!this.boardGame.isPlaying(), "游戏异常终止!");
            this.currentHandler = this.currentHandler.getPrevHandler();
        }
        // 刷新当前监听器的监听信息
        if (this.currentHandler != null) {
            this.currentHandler.listener.sendCurrentPlayerListeningResponse(this);
        }
        // 返回中断参数
        return listener.createInterruptParam();
    }

    /**
     * 判断游戏是否可以结束
     *
     * @return
     */
    protected abstract boolean isGameOver();

    /**
     * 回合中的行动
     */
    protected abstract void round() throws BoardGameException;

    /**
     * 执行游戏流程
     *
     * @throws Exception
     */
    public void run() throws Exception {
        this.startGame();
        while (!isGameOver()) {
            this.initRound();
            this.round();
            this.endRound();
            CheckUtils.check(this.round > 10000, "回合数大于限制,游戏中止!");
        }
        this.getBoardGame().setState(BgState.END);
        this.endGame();
    }

    /**
     * 游戏初始化设置
     *
     * @throws BoardGameException
     */
    protected abstract void setupGame() throws BoardGameException;

    /**
     * 游戏开始时执行的代码
     *
     * @throws BoardGameException
     */
    protected void startGame() throws BoardGameException {
        this.setupGame();
        this.getBoardGame().sendPlayingInfo();
    }

    /**
     * 等待玩家输入,输入完成或者等待超时后会检查当前 游戏状态,如果游戏状态被中断则会抛出异常
     *
     * @param listener
     * @throws BoardGameException
     * @throws InterruptedException
     */
    @SuppressWarnings("rawtypes")
    protected void waitForInput(ActionListener listener) throws BoardGameException, InterruptedException {
        ListenerHandler handler = new ListenerHandler(this, this.currentHandler);
        this.currentHandler = handler;
        handler.start(listener);
        if (this.getBoardGame().getState() != null) {
            switch (this.getBoardGame().getState()) {
                case INTERRUPT:
                    throw new BoardGameException("游戏异常中止!");
                case WIN: // 中盘获胜
                    this.endGame();
                    throw new BoardGameException("游戏结束!");
                default:
                    this.currentHandler = this.currentHandler.getPrevHandler();
                    break;
            }
        }
    }

    /**
     * 唤醒
     */
    public void wakeUp() {
        while (this.currentHandler != null) {
            this.currentHandler.done();
            this.currentHandler = this.currentHandler.getPrevHandler();
        }
    }
}
