package com.f14.bg.listener;

import com.f14.F14bg.consts.CmdConst;
import com.f14.F14bg.network.CmdFactory;
import com.f14.bg.GameMode;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.common.PlayerParamSet;
import com.f14.bg.consts.PlayerState;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.player.Player;
import com.f14.bg.utils.CheckUtils;
import org.apache.log4j.Logger;

import java.util.*;

public abstract class ActionListener<P extends Player, GM extends GameMode<P>> {
    /**
     * 参数key值
     */
    private static final String PARAM_KEY = "PARAM_KEY";
    protected Logger log = Logger.getLogger(this.getClass());
    protected boolean isClosed;
    protected ListenerType listenerType;

    protected Map<Integer, PlayerParamSet> params = new HashMap<>();

    protected Map<P, List<ActionStep<P, GM>>> actionSteps = new LinkedHashMap<>();

    protected Collection<P> listeningPlayers = new LinkedHashSet<>();
    /**
     * 玩家的输入状态
     */

    protected Map<P, PlayerState> playerStates = new HashMap<>();

    /**
     * 构造函数
     */
    public ActionListener() {
        this(ListenerType.NORMAL);
    }

    /**
     * 指定类型的构造函数
     *
     * @param listenerType
     */
    public ActionListener(ListenerType listenerType) {
        this.listenerType = listenerType;
    }

    /**
     * 为玩家添加行动步骤
     *
     * @param gameMode
     * @param player
     * @param step
     * @throws BoardGameException
     */
    protected void addActionStep(GM gameMode, P player, ActionStep<P, GM> step) throws BoardGameException {
        List<ActionStep<P, GM>> steps = this.getActionSteps(player);
        // 如果当前没有步骤,则触发该新增的步骤
        if (steps.isEmpty()) {
            steps.add(step);
            step.onStepStart(gameMode, player);
        } else {
            steps.add(step);
        }
        // 设置该步骤属于的监听器
        step.listener = this;
    }

    /**
     * 添加玩家到监听列表中
     *
     * @param player
     */
    public void addListeningPlayer(P player) {
        this.listeningPlayers.add(player);
    }

    /**
     * 添加玩家到监听列表中
     *
     * @param players
     */
    public void addListeningPlayers(Collection<? extends P> players) {
        this.listeningPlayers.addAll(players);
    }

    /**
     * 在玩家开始监听前的检验方法
     *
     * @param gameMode
     * @param player
     * @return 如果返回true, 则需要玩家回应, false则不需玩家回应
     */
    protected boolean beforeListeningCheck(GM gameMode, P player) {
        return true;
    }

    /**
     * 开始监听前执行的动作
     *
     * @param gameMode
     * @throws BoardGameException
     */
    protected void beforeStartListen(GM gameMode) throws BoardGameException {

    }

    /**
     * 检查该监听器是否完成监听
     *
     * @param gameMode
     * @throws BoardGameException
     */
    public void checkResponsed(GM gameMode) throws BoardGameException {
        if (this.isAllPlayerResponsed()) {
            // 如果所有玩家都回应了,则设置状态为完成
            this.onAllPlayerResponsed(gameMode);
            this.endListen(gameMode);
        }
    }

    /**
     * 关闭并移除监听
     */
    public void close() {
        this.isClosed = true;
    }

    /**
     * 创建中断监听器的回调参数
     *
     * @return
     */
    public InterruptParam createInterruptParam() {
        return new InterruptParam();
    }

    /**
     * 创建阶段结束的指令
     *
     * @param gameMode
     * @return
     */

    protected BgResponse createPhaseEndCommand(GM gameMode) {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_PHASE_END, -1);
        this.setListenerInfo(res);
        return res;
    }

    /**
     * 创建阶段开始的指令
     *
     * @param gameMode
     * @return
     */
    protected BgResponse createPhaseStartCommand(GM gameMode) {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_PHASE_START, -1);
        this.setListenerInfo(res);
        return res;
    }

    /**
     * 创建玩家回应的指令
     *
     * @param player
     * @param gameMode
     * @return
     */

    protected BgResponse createPlayerResponsedCommand(GM gameMode, P player) {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_PLAYER_RESPONSED, player.getPosition());
        this.setListenerInfo(res);
        return res;
    }

    /**
     * 创建开始监听的指令
     *
     * @param player
     * @param gameMode
     * @return
     */
    protected BgResponse createStartListenCommand(GM gameMode, P player) {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_START_LISTEN, player.getPosition());
        this.setListenerInfo(res);
        return res;
    }

    /**
     * 创建监听器的行动指令
     *
     * @param player
     * @param subact
     * @return
     */

    protected BgResponse createSubactResponse(P player, String subact) {
        BgResponse res = CmdFactory.createGameResponse(this.getValidCode(), player == null ? -1 : player.getPosition());
        res.setPublicParameter("subact", subact);
        return res;
    }

    /**
     * 执行行动
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected abstract void doAction(GM gameMode, BgAction action) throws BoardGameException;

    /**
     * 结束监听
     *
     * @param gameMode
     * @throws BoardGameException
     */
    public void endListen(GM gameMode) throws BoardGameException {
        gameMode.getGame().sendResponse(this.createPhaseEndCommand(gameMode));
        this.close();
    }

    /**
     * 执行行动
     *
     * @param gameMode
     * @param action
     * @throws
     */
    public void execute(GM gameMode, BgAction action) throws BoardGameException {
        // 正常处理该指定
        CheckUtils.check(!isActionPositionValid(action), "你不能进行这个行动!");
        CheckUtils.check(isPlayerResponsed(action.getPlayer().getPosition()), "不能重复进行行动!");
        CheckUtils.check(action.getCode() != this.getValidCode(), "不能处理该行动代码!");
        P player = action.getPlayer();
        ActionStep<P, GM> step = this.getCurrentActionStep(player);
        if (step != null) {
            // 如果存在步骤则需要处理步骤
            step.execute(gameMode, action);
            if (step.isOver) {
                // 如果步骤结束,则从步骤序列中移除
                this.removeCurrentActionStep(gameMode, player);
                step.onStepOver(gameMode, player);
            }
        } else {
            // 如果不存在步骤,则执行以下代码
            this.doAction(gameMode, action);
        }
        this.checkResponsed(gameMode);
    }

    /**
     * 取得玩家的所有行动步骤
     *
     * @param player
     * @return
     */
    protected List<ActionStep<P, GM>> getActionSteps(P player) {
        return this.actionSteps.computeIfAbsent(player, k -> new LinkedList<>());
    }

    /**
     * 取得玩家当前的行动步骤
     *
     * @param player
     * @return
     */

    protected ActionStep<P, GM> getCurrentActionStep(P player) {
        List<ActionStep<P, GM>> steps = this.getActionSteps(player);
        if (!steps.isEmpty()) {
            return steps.get(0);
        } else {
            return null;
        }
    }

    /**
     * 取得监听器的类型
     *
     * @return
     */
    public ListenerType getListenerType() {
        return this.listenerType;
    }

    /**
     * 取得监听玩家的列表
     *
     * @return
     */

    public Collection<P> getListeningPlayers() {
        return this.listeningPlayers;
    }

    /**
     * 取得玩家的参数
     *
     * @param position
     * @return
     */

    @SuppressWarnings("unchecked")
    public <R> R getParam(int position) {
        return (R) this.getPlayerParamSet(position).get(PARAM_KEY);
    }

    /**
     * 取得玩家的参数
     *
     * @return
     */

    public <R> R getParam(P player) {
        return this.getParam(player.getPosition());
    }

    /**
     * 取得玩家的参数集
     *
     * @param position
     * @return
     */
    protected PlayerParamSet getPlayerParamSet(int position) {
        return this.params.computeIfAbsent(position, k -> new PlayerParamSet());
    }

    /**
     * 取得玩家的状态
     *
     * @param player
     * @return
     */
    public PlayerState getPlayerState(P player) {
        return this.playerStates.get(player);
    }

    /**
     * 取得可以处理的指令code
     *
     * @return
     */
    protected abstract int getValidCode();

    /**
     * 初始化监听玩家
     *
     * @param gameMode
     */
    protected void initListeningPlayers(GM gameMode) {
        Collection<? extends P> players;
        if (this.listeningPlayers.isEmpty()) {
            // 如果监听玩家列表为空,则允许所有玩家输入
            players = gameMode.getGame().getPlayers();
        } else {
            // 否则只允许在监听列表中的玩家输入
            players = this.listeningPlayers;
        }
        for (P p : players) {
            this.setNeedPlayerResponse(p.getPosition(), true);
        }
    }

    /**
     * 判断该行动的位置是否可以进行
     *
     * @param action
     * @return
     */
    protected boolean isActionPositionValid(BgAction action) {
        return this.isActionPositionValid(action.getPlayer().getPosition());
    }

    /**
     * 判断该行动的位置是否可以进行
     *
     * @return
     */
    protected boolean isActionPositionValid(int position) {
        return this.getPlayerParamSet(position).needResponse;
    }

    /**
     * 判断是否所有玩家都已经回应
     *
     * @return
     */
    public boolean isAllPlayerResponsed() {
        for (PlayerParamSet p : this.params.values()) {
            if (!p.responsed) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将所有玩家都设为已经回应
     *
     * @param gameMode
     */
    protected void setAllPlayerResponsed(GM gameMode) {
        for (Integer pos : this.params.keySet()) {
            this.setPlayerResponsed(pos);
        }
    }

    /**
     * 判断是否关闭监听
     *
     * @return
     */
    public boolean isClosed() {
        return this.isClosed;
    }

    /**
     * 判断是否需要玩家回应
     *
     * @param position
     * @return
     */
    public boolean isNeedPlayerResponse(int position) {
        return this.getPlayerParamSet(position).needResponse;
    }

    /**
     * 判断玩家是否回应
     *
     * @param position
     * @return
     */
    public boolean isPlayerResponsed(int position) {
        return this.getPlayerParamSet(position).responsed;
    }

    /**
     * 当所有玩家都回应后执行的动作
     *
     * @param gameMode
     * @throws BoardGameException
     */
    public void onAllPlayerResponsed(GM gameMode) throws BoardGameException {

    }

    /**
     * 玩家回应后执行的动作
     *
     * @param gameMode
     * @param player
     * @throws BoardGameException
     */
    protected void onPlayerResponsed(GM gameMode, P player) throws BoardGameException {

    }

    /**
     * 玩家开始监听时触发的方法
     *
     * @param gameMode
     * @param player
     */
    protected void onPlayerStartListen(GM gameMode, P player) {

    }

    /**
     * 重新连接时处理的一些事情
     *
     * @param gameMode
     * @param player
     * @throws BoardGameException
     */
    protected void onReconnect(GM gameMode, P player) throws BoardGameException {
        // 取得当前玩家的步骤
        ActionStep<P, GM> step = this.getCurrentActionStep(player);
        if (step != null) {
            // 如果需要执行步骤,则发送对应的信息
            step.onStepStart(gameMode, player);
        }
    }

    /**
     * 开始监听时执行的动作
     *
     * @param gameMode
     * @throws BoardGameException
     */
    protected void onStartListen(GM gameMode) throws BoardGameException {

    }

    /**
     * 重新发送玩家监听信息
     *
     * @param gameMode
     */
    public void refreshListeningState(GM gameMode) {
        // 向所有不在旁观,并且需要回应的玩家,发送监听指令
        for (P player : gameMode.getGame().getPlayers()) {
            // 如果玩家不是旁观状态,则可能需要向其发送监听指令
            if (gameMode.getGame().isPlayingGame(player)) {
                // if(player.playingState!=PlayingState.AUDIENCE){
                // 需要回应的话则发送监听指令
                boolean responsed = this.isPlayerResponsed(player.getPosition());
                if (!responsed) {
                    this.sendStartListenCommand(gameMode, player, player);
                    this.onPlayerStartListen(gameMode, player);
                    try {
                        this.onReconnect(gameMode, player);
                    } catch (BoardGameException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 移除玩家的所有行动步骤
     *
     * @param player
     */
    protected void removeAllActionSteps(P player) {
        List<ActionStep<P, GM>> res = this.actionSteps.get(player);
        if (res != null) {
            res.clear();
        }
    }

    /**
     * 移除当前步骤,开始下一步骤
     *
     * @param gameMode
     * @param player
     * @throws BoardGameException
     */
    protected void removeCurrentActionStep(GM gameMode, P player) throws BoardGameException {
        List<ActionStep<P, GM>> steps = this.getActionSteps(player);
        if (!steps.isEmpty()) {
            ActionStep<P, GM> s = steps.remove(0);
            if (s.clearOtherStep) {
                // 如果该步骤需要移除所有剩余步骤,则移除
                this.removeAllActionSteps(player);
            } else {
                // 移除后如果还有步骤,则触发该步骤
                if (!steps.isEmpty()) {
                    steps.get(0).onStepStart(gameMode, player);
                }
            }
        }
    }

    /**
     * 向指定玩家发送所有玩家的状态
     *
     * @param gameMode
     * @param receiver
     */
    public void sendAllPlayersState(GM gameMode, P receiver) {
        Map<P, PlayerState> map = new LinkedHashMap<>();
        for (P p : gameMode.getGame().getPlayers()) {
            map.put(p, this.getPlayerState(p));
            // mode.getBoardGame().sendPlayerState(p, this.getPlayerState(p),
            // receiver);
        }
        gameMode.getGame().sendPlayerState(map, receiver);
    }

    /**
     * 发送当前监听器中,所有玩家的监听状态
     *
     * @param gameMode
     * @throws BoardGameException
     */
    public void sendCurrentPlayerListeningResponse(GM gameMode) throws BoardGameException {
        // 重新连接时,先向玩家发送回合开始的指令
        gameMode.getGame().sendResponse(null, this.createPhaseStartCommand(gameMode));
        // 向玩家发送监听器的一些额外信息
        this.sendPlayerListeningInfo(gameMode, null);
        // 发送所有玩家的监听状态
        this.sendAllPlayersState(gameMode, null);

        this.refreshListeningState(gameMode);
    }

    /**
     * 向所有玩家发送开始监听的指令
     *
     * @param gameMode
     */
    public void sendListenerCommand(GM gameMode) {
        // 将所有需要返回输入的玩家的回应状态设为false,不需要返回的设为true
        for (P p : gameMode.getGame().getPlayers()) {
            boolean valid = this.isActionPositionValid(p.getPosition());
            this.setPlayerResponsed(p.getPosition(), !valid);
            if (valid) {
                if (this.beforeListeningCheck(gameMode, p)) {
                    // 需要回应的话则发送监听指令
                    this.sendStartListenCommand(gameMode, p, null);
                    this.onPlayerStartListen(gameMode, p);
                    this.setPlayerState(gameMode, p, PlayerState.INPUTING);
                } else {
                    // 不需要则直接设置为回应完成
                    this.setResponsed(gameMode, p);
                }
            }
        }
    }

    /**
     * 向指定玩家发送监听器提供的一些额外信息,如果receiver为空,则向所有玩家发送
     *
     * @param gameMode
     * @param receiver
     */
    protected void sendPlayerListeningInfo(GM gameMode, P receiver) {

    }

    /**
     * 发送重新连接时发送给玩家的指令
     *
     * @param gameMode
     * @param player
     * @throws BoardGameException
     */
    public void sendReconnectResponse(GM gameMode, P player) throws BoardGameException {
        // 重新连接时,先向玩家发送回合开始的指令
        gameMode.getGame().sendResponse(player, this.createPhaseStartCommand(gameMode));
        // 向玩家发送监听器的一些额外信息
        this.sendPlayerListeningInfo(gameMode, player);
        // 发送所有玩家的监听状态
        this.sendAllPlayersState(gameMode, player);
        // 如果玩家不是旁观状态,则可能需要向其发送监听指令
        if (gameMode.getGame().isPlayingGame(player)) {
            // 需要回应的话则发送监听指令
            boolean responsed = this.isPlayerResponsed(player.getPosition());
            if (!responsed) {
                this.sendStartListenCommand(gameMode, player, player);
                this.onPlayerStartListen(gameMode, player);
                this.onReconnect(gameMode, player);
            }
        }
    }

    /**
     * 向receiver发送player开始回合的指令,如果receiver为空,则向所有玩家发送
     *
     * @param gameMode
     * @param player
     * @param receiver
     */
    protected void sendStartListenCommand(GM gameMode, P player, P receiver) {
        BgResponse res = this.createStartListenCommand(gameMode, player);
        gameMode.getGame().sendResponse(receiver, res);
    }

    /**
     * 设置所有玩家的状态
     *
     * @param gameMode
     * @param state
     */
    public void setAllPlayersState(GM gameMode, PlayerState state) {
        for (P p : gameMode.getGame().getPlayers()) {
            this.setPlayerState(gameMode, p, state);
        }
    }

    /**
     * 为消息设置监听器的信息
     *
     * @param res
     */
    protected void setListenerInfo(BgResponse res) {
        res.setPublicParameter("listenerType", this.getListenerType());
        res.setPublicParameter("validCode", this.getValidCode());
    }

    /**
     * 设置是否需要玩家回应
     *
     * @param position
     * @param needResponse
     */
    protected void setNeedPlayerResponse(int position, boolean needResponse) {
        this.getPlayerParamSet(position).needResponse = needResponse;
        // 需要回应的玩家设置响应状态为false,不需要的,设为true
        this.setPlayerResponsed(position, !needResponse);
    }

    /**
     * 设置玩家的参数
     *
     * @param position
     * @param param
     */
    public <R> void setParam(int position, R param) {
        this.setPlayerParam(position, PARAM_KEY, param);
    }

    /**
     * 设置玩家的参数
     *
     * @param player
     * @param param
     */
    public <R> void setParam(P player, R param) {
        this.setParam(player.getPosition(), param);
    }

    /**
     * 设置玩家的参数
     *
     * @param position
     * @param key
     * @param value
     */
    protected void setPlayerParam(int position, Object key, Object value) {
        this.getPlayerParamSet(position).set(key, value);
    }

    /**
     * 设置玩家的回应状态为完成回应并将该信息返回到客户端
     *
     * @param position
     */
    public void setPlayerResponsed(GM gameMode, int position) {
        P player = gameMode.getGame().getPlayer(position);
        // 不知道为啥这边player会取得为NULL,输出日志跟踪一下吧
        if (player == null) {
            log.error("设置玩家行动完成时发生错误,目标玩家为空!! position=" + position + " roomId=" + gameMode.getGame().getRoom().getId());
        } else {
            this.setPlayerResponsed(gameMode, player);
        }
    }

    /**
     * 设置玩家的回应状态为完成回应并将该信息返回到客户端
     *
     * @param gameMode
     * @param player
     */
    public void setPlayerResponsed(GM gameMode, P player) {
        BgResponse res = this.createPlayerResponsedCommand(gameMode, player);
        gameMode.getGame().sendResponse(res);
        this.setResponsed(gameMode, player);
    }

    /**
     * 设置玩家的回应状态为完成回应
     *
     * @param position
     */
    protected void setPlayerResponsed(int position) {
        this.setPlayerResponsed(position, true);
    }

    /**
     * 设置玩家的回应状态
     *
     * @param position
     * @param responsed
     */
    protected void setPlayerResponsed(int position, boolean responsed) {
        this.getPlayerParamSet(position).responsed = responsed;
    }

    /**
     * 设置玩家的状态
     *
     * @param gameMode
     * @param player
     * @param state
     */
    public void setPlayerState(GM gameMode, P player, PlayerState state) {
        this.playerStates.put(player, state);
        gameMode.getGame().sendPlayerState(player, state, null);
    }

    /**
     * 设置玩家的回应状态为完成回应
     *
     * @param gameMode
     * @param player
     */
    protected void setResponsed(GM gameMode, P player) {
        this.setPlayerResponsed(player.getPosition());
        this.setPlayerState(gameMode, player, PlayerState.RESPONSED);
        try {
            this.onPlayerResponsed(gameMode, player);
        } catch (BoardGameException e) {
            log.error(e, e);
        }
    }

    /**
     * 开始监听
     *
     * @param gameMode
     * @throws BoardGameException
     */
    public void startListen(GM gameMode) throws BoardGameException {
        // 开始监听指令前,先设置监听玩家和监听类型的参数
        this.initListeningPlayers(gameMode);
        // 发送阶段开始的指令
        gameMode.getGame().sendResponse(this.createPhaseStartCommand(gameMode));
        this.setAllPlayersState(gameMode, PlayerState.NONE);
        this.beforeStartListen(gameMode);
        // 发送监听器的一些额外信息
        this.sendPlayerListeningInfo(gameMode, null);
        // 发送开始监听的指令
        this.sendListenerCommand(gameMode);
        // 执行一些需要的行动
        this.onStartListen(gameMode);
    }

}
