package com.f14.bg;

import com.f14.bg.action.BgAction;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.ActionListener;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 监听器的指令管理
 *
 * @author 吹风柰柰
 */
public class ListenerHandler {
    protected static Logger log = Logger.getLogger(ListenerHandler.class);
    protected GameMode gameMode;

    @SuppressWarnings("rawtypes")
    protected ActionListener listener;

    protected List<BgAction> actions = new ArrayList<>();

    protected Object lock = new Object();
    private boolean isRunning;
    private ListenerHandler prevHandler;

    public ListenerHandler(GameMode gameMode, ListenerHandler prevHandler) {
        this.gameMode = gameMode;
        this.prevHandler = prevHandler;
    }

    /**
     * 添加执行指令,如果不存在监听器则会跑出异常
     *
     * @param action
     * @throws BoardGameException
     */
    public void doAction(BgAction action) throws BoardGameException {
        if (this.listener == null) {
            throw new BoardGameException("现在还不能进行行动!");
        }
        this.actions.add(action);
        if (!this.isRunning) {
            // 指令添加成功后,检查是否需要执行该指令
            synchronized (this.lock) {
                this.isRunning = true;
                this.lock.notifyAll();
            }
        }
    }

    /**
     * 中止线程
     */
    public void done() {
        if (this.listener != null) {
            this.listener.close();
            if (!this.isRunning) {
                // 指令添加成功后,检查是否需要执行该指令
                synchronized (this.lock) {
                    this.isRunning = true;
                    this.lock.notifyAll();
                }
            }
        }
    }

    /**
     * @return
     */
    public ListenerHandler getPrevHandler() {
        return prevHandler;
    }

    /**
     * 检查是否执行完成
     *
     * @return
     */
    public boolean isFinished() {
        return this.listener == null;
    }

    /**
     * 重置监听器线程
     */
    protected void reset() {
        this.listener = null;
        this.actions.clear();
    }

    /**
     * 开始监听
     *
     * @param listener
     * @throws BoardGameException
     */
    @SuppressWarnings("rawtypes")
    public void start(ActionListener listener) throws BoardGameException {
        this.listener = listener;
        this.waitForInput();
        this.reset();
    }

    /**
     * 等待输入
     *
     * @throws BoardGameException
     */
    @SuppressWarnings("unchecked")
    private void waitForInput() throws BoardGameException {
        while (!listener.isClosed()) {
            if (this.actions.isEmpty()) {
                synchronized (this.lock) {
                    try {
                        this.isRunning = false;
                        this.lock.wait();
                    } catch (InterruptedException e) {
                        log.error("游戏过程中发生系统错误: " + e.getMessage(), e);
                        throw new BoardGameException("执行过程发生错误!");
                    }
                }
            }
            BgAction action = this.actions.remove(0);
            try {
                this.listener.execute(gameMode, action);
            } catch (BoardGameException e) {
                // 当发生异常时,将该异常信息发送到指令的所属玩家
                action.getPlayer().sendException(gameMode.getGame().getRoom().getId(), e);
            } catch (Exception e) {
                log.error("游戏过程中发生系统错误: " + e.getMessage(), e);
                action.getPlayer().sendException(gameMode.getGame().getRoom().getId(), e);
            }
        }
    }

}
