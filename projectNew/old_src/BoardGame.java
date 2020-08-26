package com.f14.bg;

import com.f14.F14bg.consts.CmdConst;
import com.f14.F14bg.manager.ResourceManager;
import com.f14.F14bg.network.CmdFactory;
import com.f14.F14bg.utils.ResourceUtils;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.anim.AnimParam;
import com.f14.bg.chat.Message;
import com.f14.bg.chat.MessageType;
import com.f14.bg.consts.BgState;
import com.f14.bg.consts.PlayerState;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.hall.GameRoom;
import com.f14.bg.listener.ActionListener;
import com.f14.bg.player.Player;
import com.f14.bg.report.BgReport;
import com.f14.bg.utils.BgUtils;
import com.f14.bg.utils.CheckUtils;
import com.f14.utils.CollectionUtils;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * BG基类
 *
 * @author F14eagle
 */
public abstract class BoardGame<P extends Player, GM extends GameMode<P>> {
    protected Logger log = Logger.getLogger(this.getClass());

    protected List<P> players = new ArrayList<>();
    protected GM gameMode;

    protected Date startTime;
    protected GameRoom room;
    protected BoardGameConfig config;
    protected BgReport<P> report;

    public BoardGame() {

    }

    /**
     * 玩家可否离开（不影响游戏进行）
     *
     * @param player
     * @return
     */
    public boolean canLeave(Player player) {
        return false;
    }

    /**
     * 清空所有玩家
     */
    protected void clearPlayers() {
        this.players.clear();
    }

    /**
     * 按照用户选择的参数创建游戏配置对象
     *
     * @param <C>
     * @return
     * @throws BoardGameException
     */
    protected abstract <C extends BoardGameConfig> C createConfig(JSONObject object) throws BoardGameException;

    /**
     * 创建游戏配置的信息
     *
     * @return
     */

    public BgResponse createConfigResponse() {
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_LOAD_CONFIG, -1);
        res.setPublicParameter("config", this.getConfig());
        return res;
    }

    /**
     * 执行行动
     *
     * @param act
     * @throws BoardGameException
     */
    public void doAction(BgAction act) throws BoardGameException {
        this.mode.doAction(act);
    }

    /**
     * 作弊代码
     *
     * @return
     * @throws BoardGameException
     */
    public boolean doCheat(String msg) throws BoardGameException {
        if (msg.startsWith("kill")) {
            int position = Integer.valueOf(msg.substring(5));
            P p = this.getPlayer(position);
            this.getRoom().replace(p.user);
            return true;
        }
        return false;
    }

    /**
     * 结束游戏
     *
     * @throws BoardGameException
     */
    public void endGame() {
        this.startTime = null;
        // 结束时清空所有玩家
        this.clearPlayers();
        // 将结束游戏的信息发送到客户端
        this.sendGameEndResponse();
    }

    public BoardGameConfig getConfig() {
        return this.config;
    }

    public void setConfig(BoardGameConfig config) {
        this.config = config;
    }

    /**
     * 设置游戏配置
     *
     * @param action
     * @throws BoardGameException
     */
    public void setConfig(BgAction action) throws BoardGameException {
        try {
            if (this.room.getState() != BgState.WAITING) {
                throw new BoardGameException("游戏状态错误,不能改变游戏设置!");
            }
            JSONObject config = action.getParameters().getJSONObject("config");
            this.setConfig(this.createConfig(config));
            // 设置完成后,需要将玩家更改设置的消息,返回到客户端
            Message message = new Message();
            message.messageType = MessageType.GAME;
            message.msg = "玩家 [" + BgUtils.escapeHtml(action.getPlayer().getName()) + "] 更改了游戏设置!";
            this.sendMessage(message);
        } catch (BoardGameException e) {
            // 如果改变设置时发生错误,则重新发送游戏设置信息到客户端
            this.room.sendConfig(action.getPlayer().user);
            throw e;
        }
    }

    /**
     * 取得当前玩家数量
     *
     * @return
     */
    public int getCurrentPlayerNumber() {
        synchronized (this.players) {
            return this.players.size();
        }
    }

    /**
     * 取得指定玩家的下一位玩家
     *
     * @return
     */
    public P getNextPlayersByOrder(P player) {
        return this.getPlayersByOrder(player).get(1);
    }

    /**
     * 取得指定位置上的玩家
     *
     * @param position
     * @return
     */

    public P getPlayer(int position) {
        synchronized (this.players) {
            if (position < 0 || position >= this.players.size()) {
                return null;
            } else {
                return this.players.get(position);
            }
        }
    }

    /**
     * 从当前回合玩家开始取得所有玩家的序列
     *
     * @return
     */
    public List<P> getPlayersByOrder() {
        return this.getPlayersByOrder(0);
    }

    /**
     * 从指定位置开始,按顺序取得所有玩家的序列
     *
     * @param position
     * @return
     */

    private List<P> getPlayersByOrder(int position) {
        synchronized (this.players) {
            int i = position;
            List<P> res = new ArrayList<>();
            do {
                res.add(this.players.get(i));
                ++i;
                if (i >= this.players.size()) {
                    i -= this.players.size();
                }
            } while (i != position);
            return res;
        }
    }

    /**
     * 从指定玩家开始,按顺序取得所有玩家的序列
     *
     * @return
     */

    public List<P> getPlayersByOrder(P player) {
        return this.getPlayersByOrder(player.getPosition());
    }

    public BgReport<P> getReport() {
        return this.report;
    }

    /**
     * 取得资源管理器
     *
     * @param <RM>
     * @return
     */
    public <RM extends ResourceManager> RM getResourceManager() {
        return ResourceUtils.getResourceManager(this.getRoom().getType());
    }

    public GameRoom getRoom() {
        return room;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public BgState getState() {
        return this.room.getState();
    }

    public void setState(BgState state) {
        this.room.setState(state);
    }

    /**
     * 取得所有有效的玩家
     *
     * @return
     */

    public List<P> getPlayers() {
        synchronized (players) {
            return this.players;
        }
    }

    /**
     * 初始化
     */
    public void init(GameRoom room) {
        this.room = room;
        initConst();
        initConfig();
    }

    /**
     * 初始化游戏配置
     */
    public abstract void initConfig();

    /**
     * 初始化常量
     */
    public abstract void initConst();

    /**
     * 初始化玩家的游戏信息
     */
    protected void initPlayers() {
        for (P player : this.getPlayers()) {
            player.reset();
        }
    }

    /**
     * 初始化玩家的座位信息
     */
    protected void initPlayersSeat() {
        // 如果是随机座位,则打乱玩家的顺序
        if (this.getConfig().randomSeat) {
            this.regroupPlayers();
        }
    }

    /**
     * 初始化玩家的组队情况
     */
    protected void initPlayerTeams() {
        // 按座位号设置队伍,则所有人都是敌对的
        for (P p : this.getPlayers()) {
            p.setTeam(p.getPosition());
        }
    }

    /**
     * 初始化战报模块
     */
    public abstract void initReport();

    /**
     * 中断游戏
     */
    public void interruptGame() {
        this.setState(BgState.INTERRUPT);
        wakeAll();
    }

    /**
     * 判断游戏是否在进行中
     *
     * @return
     */
    public boolean isPlaying() {
        return this.getRoom().isPlaying();
    }

    /**
     * 判断玩家是否在游戏中
     *
     * @param player
     * @return
     */
    public boolean isPlayingGame(P player) {
        return this.getPlayers().contains(player);
    }

    /**
     * 判断是否是组队赛
     *
     * @return
     */
    public boolean isTeamMatch() {
        return this.getConfig().isTeamMatch();
    }

    /**
     * 判断这些玩家是否是队友
     *
     * @param players
     * @return
     */
    @SafeVarargs
    public final boolean isTeammates(P... players) {
        if (this.isTeamMatch()) {
            int team = players[0].getTeam();
            for (P p : players) {
                if (p.getTeam() != team) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 按照指定的位置加入游戏, 如果游戏已经开始,或者游戏人数已满,或者未能 取到空位,则不能加入游戏并抛出异常
     *
     * @param player
     * @throws BoardGameException
     */
    public void joinGame(Player player) throws BoardGameException {
        synchronized (this.players) {
            CheckUtils.check(players.size() > this.room.getMaxPlayerNumber(), "未取得空位,不能加入游戏!");
            player.reset();

            this.players.add((P) player);
        }
    }

    /**
     * 退出游戏 只有在游戏状态为等待,中断或结束时才能退出
     *
     * @param player
     * @throws BoardGameException
     */
    public void leaveGame(P player) throws BoardGameException {
        this.removePlayer(player);
    }

    /**
     * 游戏开始时执行的方法
     *
     * @throws BoardGameException
     */
    protected void onStartGame() throws BoardGameException {
        // 初始化游戏信息
        this.initReport();
        this.getReport().start();
        this.startTime = new Date();
        // 设置玩家的座位顺序
        this.initPlayersSeat();
        this.initPlayerTeams();
        // 发送游戏开始的指令
        this.sendGameStartResponse();
        this.initPlayers();
        this.sendLocalPlayerInfo(null);
        this.room.sendPlayerSitInfo();
        this.setupGame();
    }

    /**
     * 重新排列玩家的位置
     */
    public void regroupPlayers() {
        synchronized (this.players) {
            // 打乱玩家的顺序
            CollectionUtils.shuffle(this.players);
            for (int i = 0; i < this.players.size(); i++) {
                this.players.get(i).setPosition(i);
            }
        }
    }

    /**
     * 从游戏中移除玩家
     *
     * @param player
     * @throws BoardGameException
     */
    public void removePlayer(P player) throws BoardGameException {
        if (this.room.isPlaying()) {
            if (this.canLeave(player)) {
                return;
            } else {
                throw new BoardGameException("游戏进行中,不能退出游戏!");
            }
        }
        if (!this.getPlayers().contains(player)) {
            throw new BoardGameException("玩家不在游戏中,不能移除游戏!");
        }
        this.removePlayerForce(player);
    }

    /**
     * 玩家强行离开游戏
     *
     * @param player
     * @return
     */
    public boolean removePlayerForce(Player player) {
        if (this.room.isPlaying() && this.canLeave(player)) {
            return false;
        } else if (this.getPlayers().contains(player)) {
            synchronized (this.players) {
                this.players.remove(player);
                player.reset();
                // 将玩家退出的信息发送到客户端
                BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_REMOVE_PLAYER, player.getPosition());
                res.setPublicParameter("userId", player.user.getId());
                res.setPublicParameter("sitPosition", player.getPosition());
                this.sendResponse(res);
                // 游戏属性发生变化
                this.room.onGamePropertyChange();
                // 如果玩家在游戏进行中退出,则中断游戏
                if (this.room.isPlaying()) {
                    this.interruptGame();
                    return true;
                }
            }
        }
        return false;
    }

    public void run() throws Exception {
        try {
            log.info("游戏开始!");
            this.onStartGame();
            this.mode.run();
            log.info("游戏结束!");
        } catch (BoardGameException e) {
            log.info("游戏中止!", e);
            throw e;
        } catch (Exception e) {
            log.error("游戏过程中发生错误!", e);
            throw e;
        } finally {
            this.endGame();
        }
    }

    /**
     * 向玩家发送提示信息
     *
     * @param player
     * @param msg
     */
    public void sendAlert(P player, String msg) {
        this.sendAlert(player, msg, null);
    }

    /**
     * 向玩家发送提示信息
     *
     * @param player
     * @param msg
     * @param param
     */
    public void sendAlert(P player, String msg, Object param) {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_TIP_ALERT, player.getPosition());
        res.setPrivateParameter("msg", msg);
        if (param != null) {
            res.setPrivateParameter("param", param);
        }
        this.sendResponse(player, res);
    }

    /**
     * 向所有玩家发送提示信息
     *
     * @param msg
     */
    public void sendAlertToAll(String msg) {
        this.sendAlertToAll(msg, null);
    }

    /**
     * 向所有玩家发送提示信息
     *
     * @param msg
     * @param param
     */
    public void sendAlertToAll(String msg, Object param) {
        for (P p : this.getPlayers()) {
            this.sendAlert(p, msg, param);
        }
    }

    /**
     * 发送动画效果的指令
     *
     * @param param
     */
    public void sendAnimationResponse(AnimParam param) {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_ANIM_CMD, -1);
        res.setPublicParameter("animParam", param);
        this.sendResponse(res);
    }

    /**
     * 发送游戏结束的信息
     *
     * @throws BoardGameException
     */
    public void sendGameEndResponse() {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_END, -1);
        this.sendResponse(res);
    }

    /**
     * 将游戏当前内容发送给玩家
     *
     * @throws BoardGameException
     */
    protected abstract void sendGameInfo(P receiver) throws BoardGameException;

    /**
     * 发送游戏开始的信息
     *
     * @throws BoardGameException
     */
    public void sendGameStartResponse() throws BoardGameException {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_START, -1);
        this.sendResponse(res);
    }

    /**
     * 向客户端发送当前游戏的时间
     *
     * @param receiver
     */
    public void sendGameTime(P receiver) {
        long sec = System.currentTimeMillis() - this.getStartTime().getTime();
        long totalMinute = sec / (1000 * 60);
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_GAME_TIME, -1);
        res.setPublicParameter("hour", totalMinute / 60);
        res.setPublicParameter("minute", totalMinute % 60);
        this.sendResponse(receiver, res);
    }

    /**
     * 将初始信息传送给玩家
     *
     * @throws BoardGameException
     */
    protected abstract void sendInitInfo(P receiver) throws BoardGameException;

    /**
     * 向所有游戏中的玩家发送本地玩家的信息
     */
    public void sendLocalPlayerInfo(P receiver) {
        if (receiver == null) {
            // 向所有游戏中的玩家发送本地玩家信息
            for (P p : this.getPlayers()) {
                this.sendLocalPlayerInfoResponse(p);
            }
        } else {
            // 向指定玩家发送本地玩家信息
            this.sendLocalPlayerInfoResponse(receiver);
        }
    }

    /**
     * 向指定玩家发送本地玩家的信息,只有在游戏中的玩家才会发送该信息
     *
     * @param player
     */
    private void sendLocalPlayerInfoResponse(P player) {
        if (player != null && this.isPlayingGame(player)) {
            BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_LOCAL_PLAYER, player.getPosition());
            res.setPublicParameter("localPlayer", player.toMap());
            this.sendResponse(player, res);
        }
    }

    /**
     * 将信息发送给所有玩家
     *
     * @param message
     */
    public void sendMessage(Message message) {
        this.room.sendMessage(message);
    }

    /**
     * 将玩家当前信息发送给玩家
     *
     * @throws BoardGameException
     */
    protected abstract void sendPlayerPlayingInfo(P receiver) throws BoardGameException;

    /**
     * 向指定玩家发送玩家状态的消息
     *
     * @param playerStates
     * @param receiver
     */
    public void sendPlayerState(Map<P, PlayerState> playerStates, P receiver) {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_PLAYER_STATE, -1);
        List<Map<String, Object>> list = new ArrayList<>();
        for (P p : playerStates.keySet()) {
            Map<String, Object> map = new HashMap<>();
            map.put("userId", p.user.getId());
            map.put("playerState", playerStates.get(p));
            list.add(map);
        }
        res.setPublicParameter("states", list);
        this.sendResponse(res);
    }

    /**
     * 发送玩家状态的消息
     *
     * @param player
     * @param playerState
     * @param receiver
     */
    public void sendPlayerState(P player, PlayerState playerState, P receiver) {
        Map<P, PlayerState> playerStates = new HashMap<>();
        playerStates.put(player, playerState);
        this.sendPlayerState(playerStates, receiver);
    }

    /**
     * 发送游戏当前玩家信息给玩家
     *
     * @throws BoardGameException
     */
    public void sendPlayingInfo() throws BoardGameException {
        this.sendGameTime(null);
        // 发送游戏的基本设置信息
        this.sendInitInfo(null);
        // 发送游戏的当前信息
        this.sendGameInfo(null);
        // 发送玩家的当前信息
        this.sendPlayerPlayingInfo(null);
    }

    /**
     * 发送游戏当前玩家信息给玩家
     *
     * @param player
     * @throws BoardGameException
     */
    @SuppressWarnings("unchecked")
    public void sendPlayingInfo(Player player) throws BoardGameException {
        // 发送玩家的座位信息
        P p = (P) player;
        this.sendLocalPlayerInfo(p);
        this.room.sendPlayerSitInfo(p.user);
        this.sendGameTime(p);
        // 发送游戏的基本设置信息
        this.sendInitInfo(p);
        // 发送游戏的当前信息
        this.sendGameInfo(p);
        // 发送玩家的当前信息
        this.sendPlayerPlayingInfo(p);
    }

    /**
     * 在重新连接游戏时发送的消息
     *
     * @param player
     * @throws BoardGameException
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void sendReconnectInfo(Player player) throws BoardGameException {
        ActionListener al = this.mode.getCurrentListener();
        if (al != null) {
            al.sendReconnectResponse(this.mode, player);
        }
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public void sendRefreshListeningInfo() {
        this.mode.getCurrentListener().refreshListeningState(this.mode);
    }

    /**
     * 将回应发送给所有玩家
     *
     * @param res
     */
    public void sendResponse(BgResponse res) {
        this.room.sendResponse(res);
    }

    /**
     * 将回应发送给所有玩家
     *
     * @param res
     */
    public void sendResponse(List<BgResponse> res) {
        for (BgResponse r : res) {
            this.sendResponse(r);
        }
    }

    /**
     * 向玩家发送信息,如果玩家为空,则向所有玩家发送(包括旁观者)
     *
     * @param receiver
     * @param res
     */
    public void sendResponse(P receiver, BgResponse res) {
        if (receiver != null) {
            this.room.sendResponse(receiver.user, res);
        } else {
            this.sendResponse(res);
        }
    }

    /**
     * 向指定玩家发送简单指令
     *
     * @param param
     * @param receiver
     */
    public void sendSimpleResponse(Map<String, Object> param, P receiver) {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_SIMPLE_CMD, -1);
        for (String key : param.keySet()) {
            res.setPublicParameter(key, param.get(key));
        }
        this.sendResponse(receiver, res);
    }

    /**
     * 向指定玩家发送简单指令
     *
     * @param key
     * @param value
     * @param receiver
     */
    public void sendSimpleResponse(String key, Object value, P receiver) {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_SIMPLE_CMD, -1);
        res.setPublicParameter(key, value);
        this.sendResponse(receiver, res);
    }

    /**
     * 向指定玩家发送简单指令
     *
     * @param subact
     * @param receiver
     */
    public void sendSimpleResponse(String subact, P receiver) {
        this.sendSimpleResponse("subact", subact, receiver);
    }

    /**
     * 设置游戏
     *
     * @throws BoardGameException
     */
    protected abstract void setupGame() throws BoardGameException;

    /**
     * 唤醒所有等待中的线程
     */
    public void wakeAll() {
        if (mode != null) {
            mode.wakeUp();
        }
    }

    /**
     * 中盘结束游戏
     */
    public void winGame() {
        this.setState(BgState.WIN);
        wakeAll();
    }

}
