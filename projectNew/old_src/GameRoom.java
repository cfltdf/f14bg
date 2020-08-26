package com.f14.bg.hall;

import com.f14.F14bg.consts.CmdConst;
import com.f14.F14bg.consts.GameType;
import com.f14.F14bg.network.CmdFactory;
import com.f14.F14bg.utils.PrivUtil;
import com.f14.bg.BGConst;
import com.f14.bg.BoardGame;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.chat.Message;
import com.f14.bg.chat.MessageType;
import com.f14.bg.component.Convertable;
import com.f14.bg.consts.BgState;
import com.f14.bg.consts.PlayingState;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.player.Player;
import com.f14.bg.utils.BgUtils;
import com.f14.bg.utils.CheckUtils;
import com.f14.f14bgdb.util.CodeUtil;
import com.f14.utils.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class GameRoom implements Convertable, Runnable {
    public int id;
    public String name;
    public GameType type;
    public String descr;
    public User owner;
    public BoardGame<?, ?> game;
    public GameHall hall;
    public String reportString;
    protected Logger log = Logger.getLogger(this.getClass());
    protected int minPlayerNumber;
    protected int maxPlayerNumber;
    private String password;
    private String bgId;

    private Set<User> users = new LinkedHashSet<>();

    private Set<User> joinUsers = new LinkedHashSet<>();

    private Map<User, UserRoomParam> userParams = new HashMap<>();
    private BgState state;

    private User replaceUser;

    public GameRoom(User owner, GameType type, String name, String descr, String password) {
        this.id = RoomManager.generateRoomId();
        this.type = type;
        this.name = name;
        this.password = password;
        this.descr = descr;
        this.owner = owner;
        this.state = BgState.WAITING;
    }

    /**
     * 判断用户是否可以重连到该房间
     *
     * @param user
     * @return
     */
    public boolean canReconnect(User user) {
        // 如果房间中的游戏存在,并且在进行中,并且玩家的状态为断线重,则可以重连
        return this.game != null && this.isPlaying() && this.getJoinUsers().contains(user)
                && this.getUserState(user) == PlayingState.LOST_CONNECTION;
    }

    /**
     * 检查玩家是否可以执行动作
     *
     * @param user
     * @param playing
     * @throws BoardGameException
     */
    public void checkCanAction(User user, boolean playing) throws BoardGameException {
        CheckUtils.check(this.getUserState(user) != PlayingState.PLAYING, "你不能执行动作!");
        if (playing) {
            CheckUtils.check(this.getState() != BgState.PLAYING, "游戏不在进行,不能执行此动作!");
        } else {
            CheckUtils.check(this.getState() == BgState.PLAYING, "游戏正在进行,不能执行此动作!");
        }
    }

    /**
     * 检查密码是否匹配
     *
     * @param pwd
     * @return
     */
    public boolean invalidPassword(String pwd) {
        return this.hasPassword() && !this.password.equals(pwd);
    }

    /**
     * 检查是否可以开始游戏
     *
     * @throws BoardGameException
     */
    protected void checkStart() throws BoardGameException {
        CheckUtils.check(this.getState() != BgState.WAITING, "游戏状态错误,不能开始游戏!");
        CheckUtils.check(!this.isPlayersSuit(), "玩家数量不正确,不能开始游戏!");
        CheckUtils.check(!this.isAllPlayersReady(), "还有玩家没有准备好,不能开始游戏!");
    }

    /**
     * 判断用户是否在该房间中
     *
     * @param user
     * @return
     */
    public boolean containUser(User user) {
        return this.getUsers().contains(user);
    }

    /**
     * 创建游戏实例
     *
     * @param minPlayerNumber
     * @param maxPlayerNumber
     * @return
     */
    public void createGame(String bgId, Integer maxPlayerNumber, Integer minPlayerNumber) {
        this.bgId = bgId;
        this.minPlayerNumber = minPlayerNumber;
        this.maxPlayerNumber = maxPlayerNumber;
        try {
            this.game = CodeUtil.getBoardGameClass(bgId).newInstance();
            this.game.init(this);
        } catch (Exception e) {
            log.error("创建游戏实例时发生错误!", e);
        }
    }

    /**
     * 创建玩家对象
     *
     * @return
     * @throws BoardGameException
     */

    private Player createPlayer(User user) throws BoardGameException {
        Player res = null;
        try {
            res = CodeUtil.getPlayerClass(bgId).newInstance();
            res.user = user;
        } catch (Exception e) {
            log.error("创建玩家实例时发生错误!", e);
        }
        return res;
    }

    /**
     * 为用户创建房间参数
     *
     * @param user
     */
    protected void createUserParam(User user) {
        synchronized (userParams) {
            this.userParams.put(user, new UserRoomParam(user));
        }
    }

    /**
     * 执行换人操作
     *
     * @param user
     * @throws BoardGameException
     */
    private void doReplace(User user) throws BoardGameException {
        CheckUtils.checkNull(this.replaceUser, "没有玩家在等待换人!");
        if (this.replaceUser == user) {
            this.replaceUser = null;
            this.setUserState(user, PlayingState.PLAYING);
            this.setUserReady(user, false);
            this.sendReplaceEndResponse();
            for (User u : this.getUsers()) {
                // 向玩家发送游戏中的信息
                this.game.sendPlayingInfo(u.getPlayer(this));
            }
            this.game.sendReconnectInfo(user.getPlayer(this));
            this.game.sendRefreshListeningInfo();
        } else {
            CheckUtils.check(this.isJoinGame(user), "你已经加入了游戏!");
            CheckUtils.check(this.getUserState(user) != PlayingState.AUDIENCE, "你不在旁观状态,不能加入游戏!");
            Player player = this.replaceUser.getPlayer(this);
            Player replacePlayer = user.getPlayer(this);
            replacePlayer.user = this.replaceUser;
            player.user = user;
            user.addPlayer(this, player);
            replaceUser.addPlayer(this, replacePlayer);
            this.setUserState(this.replaceUser, PlayingState.AUDIENCE);
            this.setUserState(user, PlayingState.PLAYING);
            this.setUserReady(user, false);
            this.getJoinUsers().remove(this.replaceUser);
            this.getJoinUsers().add(user);
            this.sendPlayerReplaceResponse(user, this.replaceUser);
            this.onGamePropertyChange();
            this.refreshUser(user);
            this.refreshUser(this.replaceUser);
            this.sendReplaceEndResponse();

            for (User u : this.getUsers()) {
                // 向玩家发送游戏中的信息
                this.game.sendPlayingInfo(u.getPlayer(this));
            }
            this.game.sendReconnectInfo(player);
            this.game.sendRefreshListeningInfo();

            this.game.getReport().info("[" + player.getName() + "] 替换 [" + replacePlayer.getName() + "]");
            this.sendReplaceMessage(this.replaceUser, user);
        }

        this.setState(BgState.PLAYING);
        this.sendUserButtonResponse();

        this.replaceUser = null;
    }

    /**
     * 结束游戏
     */
    public void endGame() {
        // 将房间的状态设为等待中
        this.setState(BgState.WAITING);
        this.sendUserButtonResponse();
        for (User u : this.getJoinUsers().toArray(new User[this.getJoinUserNumber()])) {
            if (this.getUserState(u) == PlayingState.LOST_CONNECTION) {
                this.leave(u);
            } else {
                Player p = u.getPlayer(this);
                if (p != null) {
                    p.position = BGConst.INT_NULL;
                }
            }
        }
    }

    /**
     * 取得加入游戏中的玩家数
     *
     * @return
     */
    public int getJoinUserNumber() {
        return this.getJoinUsers().size();
    }

    /**
     * 取得房间内加入游戏的所有用户
     *
     * @return
     */

    public Collection<User> getJoinUsers() {
        synchronized (this.joinUsers) {
            return this.joinUsers;
        }
    }

    public int getMaxPlayerNumber() {
        return maxPlayerNumber;
    }

    public int getMinPlayerNumber() {
        return minPlayerNumber;
    }

    /**
     * 房间名字
     *
     * @return
     */

    public String getName() {
        return this.name + "(" + this.type.toString() + "_" + this.id + ")";
    }

    public BgState getState() {
        synchronized (state) {
            return state;
        }
    }

    public void setState(BgState state) {
        synchronized (this.state) {
            this.state = state;
        }
        this.onGamePropertyChange();
    }

    /**
     * 取得用户对应的map对象,包括用户状态信息
     *
     * @param user
     * @return
     */

    protected Map<String, Object> getUserMap(User user) {
        Map<String, Object> o = user.toMap();
        if (this.containUser(user)) {
            o.put("userState", this.getUserState(user));
        }
        return o;
    }

    /**
     * 取得用户的房间参数
     *
     * @param user
     * @return
     */
    protected UserRoomParam getUserParam(User user) {
        synchronized (userParams) {
            return this.userParams.get(user);
        }
    }

    /**
     * 取得房间内的所有用户
     *
     * @return
     */

    public Collection<User> getUsers() {
        synchronized (this.users) {
            return this.users;
        }
    }

    /**
     * 取得用户在房间中的状态
     *
     * @param user
     */
    protected PlayingState getUserState(User user) {
        return this.getUserParam(user).playingState;
    }

    /**
     * 判断房间是否有密码
     *
     * @return
     */
    public boolean hasPassword() {
        return !StringUtils.isEmpty(this.password);
    }

    /**
     * 判断是否所有玩家都准备好进行游戏
     *
     * @return
     */
    public boolean isAllPlayersReady() {
        for (User u : this.getJoinUsers()) {
            if (!this.isUserReady(u)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断玩家是否加入房间中的游戏
     *
     * @param user
     * @return
     */
    public boolean isInGame(User user) {
        PlayingState state = this.getUserState(user);
        return state == PlayingState.PLAYING || state == PlayingState.WAITING || state == PlayingState.LOST_CONNECTION;
    }

    /**
     * 判断玩家是否加入了房间中的游戏
     *
     * @param user
     * @return
     */
    public boolean isJoinGame(User user) {
        return this.getJoinUsers().contains(user);
    }

    /**
     * 判断玩家数量是否已满
     *
     * @return
     */
    public boolean isPlayerFull() {
        return (this.getJoinUserNumber() >= this.maxPlayerNumber);
    }

    /**
     * 判断玩家数量是否适合游戏
     *
     * @return
     */
    public boolean isPlayersSuit() {
        int i = this.getJoinUserNumber();
        return (i <= this.maxPlayerNumber && i >= this.minPlayerNumber);
    }

    /**
     * 判断房间里的游戏是否在进行中
     *
     * @return
     */
    public boolean isPlaying() {
        return this.getState() == BgState.PLAYING || this.getState() == BgState.PAUSE;
    }

    /**
     * 判断玩家是否正在进行游戏
     *
     * @param user
     * @return
     */
    public boolean isPlayingGame(User user) {
        return this.isPlaying() && this.isInGame(user);
    }

    /**
     * 判断玩家是否已经准备
     *
     * @param user
     * @return
     */
    protected boolean isUserReady(User user) {
        return this.getUserParam(user).ready;
    }

    /**
     * 用户进入房间
     *
     * @param user
     * @param password
     * @throws BoardGameException
     */
    public void join(User user, String password) throws BoardGameException {
        CheckUtils.check(this.containUser(user), "你已经在这个房间里了!");
        if (!PrivUtil.hasAdminPriv(user)) {
            CheckUtils.check(user.hasRoom(), "你已经在其他房间里了!");
            CheckUtils.check(this.invalidPassword(password), "密码错误,不能加入房间!");
        }
        this.getUsers().add(user);
        // 为用户创建玩家对象
        Player player = this.createPlayer(user);
        user.addPlayer(this, player);
        // 为用户创建房间参数,设置用户的状态为旁观
        this.createUserParam(user);

        this.onGamePropertyChange();
        this.hall.refreshUser(user);
        this.sendJoinRoomResponse(user);
        this.sendInfo(user, "进入了房间!");
    }

    /**
     * 用户进入房间前的检查
     *
     * @param user
     * @param password
     * @throws BoardGameException
     */
    public void joinCheck(User user, String password) throws BoardGameException {
        CheckUtils.check(this.containUser(user), "你已经在这个房间里了!");
        if (!PrivUtil.hasAdminPriv(user)) {
            CheckUtils.check(user.hasRoom(), "你已经在其他房间里了!");
            CheckUtils.check(this.invalidPassword(password), "密码错误,不能加入房间!");
        }
        // 检查通过后向客户端发送打开房间窗口的指令
        user.handler.sendOpenRoomResponse(this);
    }

    /**
     * 用户加入游戏
     *
     * @param user
     * @throws BoardGameException
     */
    public void joinPlay(User user) throws BoardGameException {
        CheckUtils.check(!this.containUser(user), "你不在这个房间里了!");
        if (this.getState() == BgState.PAUSE) {
            this.doReplace(user);
        } else {
            CheckUtils.check(this.isJoinGame(user), "你已经加入了游戏!");
            CheckUtils.check(this.getUserState(user) != PlayingState.AUDIENCE, "你不在旁观状态,不能加入游戏!");
            CheckUtils.check(this.isPlayerFull(), "游戏中的玩家数量已满,不能加入游戏!");
            CheckUtils.check(this.getState() != BgState.WAITING, "房间状态错误,不能加入游戏!");
            // 设置用户的状态为进入游戏
            this.setUserState(user, PlayingState.PLAYING);
            this.setUserReady(user, false);
            this.getJoinUsers().add(user);

            this.onGamePropertyChange();
            this.refreshUser(user);
            this.sendJoinPlayResponse(user);
            this.sendUserButtonResponse(user);
            this.sendInfo(user, "加入了游戏!");
        }
    }

    /**
     * 用户离开房间
     *
     * @param user
     * @throws BoardGameException
     */
    public void leave(User user) {
        if (!this.containUser(user)) {
            return;
        }
        if (this.isPlayingGame(user)) {
            // 如果玩家在游戏进行时退出,则需要将其移出游戏
            if (this.game.removePlayerForce(user.getPlayer(this))) {
                this.sendInfo(user, "强行离开了游戏!");
            } else {
                this.sendInfo(user, "离开了游戏!");
            }
        }
        // 将玩家移出房间
        user.removePlayer(this);
        if (this.isInGame(user)) {
            this.getJoinUsers().remove(user);
            this.sendLeavePlayResponse(user);
        }
        this.getUsers().remove(user);
        synchronized (userParams) {
            this.userParams.remove(user);
        }

        this.onGamePropertyChange();
        this.hall.refreshUser(user);
        this.sendLeaveRoomResponse(user);

        // 如果移除玩家后房间里没有其他的用户了,则从大厅移除该房间
        if (this.getUsers().isEmpty()) {
            this.hall.removeGameRoom(this);
        } else {
            this.sendInfo(user, "离开了房间!");
        }
    }

    /**
     * 玩家强制退出房间
     *
     * @param user
     * @throws BoardGameException
     */
    private void leaveForce(User user) {
        if (!this.containUser(user)) {
            user.handler.closeRoomShell(this.id);
        }
        this.leave(user);
        // 并关闭用户的房间窗口
        user.handler.closeRoomShell(this.id);
    }

    /**
     * 用户离开游戏
     *
     * @param user
     * @throws BoardGameException
     */
    public void leavePlay(User user) throws BoardGameException {
        CheckUtils.check(!this.containUser(user), "你不在这个房间里了!");
        CheckUtils.check(this.isPlaying(), "游戏正在进行中,不能离开游戏!");
        CheckUtils.check(!this.isJoinGame(user), "你还没有加入游戏!");
        // 设置用户的状态为进入游戏
        this.setUserState(user, PlayingState.AUDIENCE);
        this.getJoinUsers().remove(user);

        this.onGamePropertyChange();
        this.refreshUser(user);
        this.sendLeavePlayResponse(user);
        this.sendUserButtonResponse(user);
        this.sendInfo(user, "离开了游戏!");
    }

    /**
     * 玩家退出房间的请求
     *
     * @throws BoardGameException
     */
    private void leaveRequest(User user) {
        if (!this.containUser(user)) {
            user.handler.closeRoomShell(this.id);
        }
        if (this.isPlayingGame(user) && !this.game.canLeave(user.getPlayer(this))) {
            // 如果玩家正在进行游戏中,则提示用户是否强制退出游戏
            BgResponse res = CmdFactory.createClientResponse(CmdConst.CLIENT_LEAVE_ROOM_CONFIRM);
            res.setPublicParameter("roomId", this.id);
            this.sendResponse(user, res);
        } else {
            // 否则就直接从房间移除玩家
            this.leave(user);
            // 并关闭用户的房间窗口
            user.handler.closeRoomShell(this.id);
        }
    }

    /**
     * 用户读取房间信息
     *
     * @param user
     * @throws BoardGameException
     */
    protected void loadRoomInfo(User user) throws BoardGameException {
        if (!this.containUser(user)) {
            // 读取时,如果用户还未加入房间,则自动加入
            this.join(user, this.password);
        }
        this.sendRoomInfo(user);
        this.sendUserInfo(user);
        // 向玩家发送游戏设置
        this.sendConfig(user);
        if (this.isPlaying()) {
            Player player = user.getPlayer(this);
            // 如果游戏正在进行中,则需要向玩家发送游戏中的信息
            this.game.sendPlayingInfo(player);
            // 发送最近的战报信息
            this.game.getReport().sendRecentMessages(player);
            // 如果玩家是断线重连的,则发送断线重连的信息
            if (this.canReconnect(user)) {
                this.game.sendReconnectInfo(player);
                // 并刷新用户的状态
                if (this.replaceUser == user) {
                    this.setUserState(user, PlayingState.WAITING);
                    this.setUserReady(user, true);
                } else {
                    this.setUserState(user, PlayingState.PLAYING);
                    this.setUserReady(user, false);
                }
                this.refreshUser(user);
            }
            this.sendUserButtonResponse(user);
        }
    }

    /**
     * 用户断线,返回是否被移出游戏
     *
     * @param user
     * @throws BoardGameException
     */
    public boolean lostConnect(User user) {
        if (this.containUser(user)) {
            if (this.isPlayingGame(user)) {
                // 如果游戏正在进行中,并且玩家在游戏中
                // 则将其状态设置为断线
                this.setUserState(user, PlayingState.LOST_CONNECTION);
                // 刷新用户的状态
                this.refreshUser(user);
                return true;
            } else {
                this.leave(user);
            }
        }
        return false;
    }

    /**
     * 游戏状态变化时触发的事件
     */
    public void onGamePropertyChange() {
        // 将变化后的房间属性发送到大厅的玩家
        this.hall.sendRoomChangeResponse(this);
    }

    /**
     * 处理聊天类型的行动
     *
     * @param user
     * @param act
     * @throws BoardGameException
     */
    protected void processChatAction(User user, BgAction act) throws BoardGameException {
        String msg = act.getAsString("msg");
        if (!StringUtils.isEmpty(msg)) {
            if (msg.startsWith("$#")) {
                if (this.game.doCheat(msg.substring(2))) {
                    return;
                }
            }
            // 现阶段只按照玩家所处的位置来发送消息
            Message message = new Message();
            message.name = BgUtils.escapeHtml(user.name);
            message.loginName = user.loginName;
            message.msg = msg;
            // 否则将消息发送到所在的房间
            message.messageType = MessageType.ROOM;
            this.sendMessage(message);
        }
    }

    /**
     * 处理房间内的指令
     *
     * @param act
     * @throws IOException
     */
    public void processCommand(User user, BgAction act) {
        try {
            switch (act.getType()) {
                case CmdConst.SYSTEM_CMD:
                    this.processSystemAction(user, act);
                    break;
                case CmdConst.CHAT_CMD:
                    this.processChatAction(user, act);
                    break;
                case CmdConst.GAME_CMD:
                    this.processGameAction(user, act);
                    break;
                default:
                    // log.warn("无效的指令来自于 " + user.handler.socket);
            }
        } catch (BoardGameException e) {
            log.warn(e.getMessage());
            act.getPlayer().sendException(this.id, e);
            // user.handler.sendCommand(CmdConst.EXCEPTION_CMD, this.id,
            // e.getMessage());
        } catch (Exception e) {
            // 如果在处理指令时发生了异常,则记录日志并发送到客户端
            log.error(e.getMessage(), e);
            act.getPlayer().sendException(this.id, e);
            // user.handler.sendCommand(CmdConst.EXCEPTION_CMD, this.id,
            // e.getMessage());
        }
    }

    /**
     * 处理游戏类型的行动
     *
     * @param user
     * @param act
     * @throws BoardGameException
     */
    protected void processGameAction(User user, BgAction act) throws BoardGameException {
        // Player player = act.getPlayer();
        switch (act.getCode()) {
            case CmdConst.GAME_CODE_SET_CONFIG:
                // 设置游戏配置
                this.checkCanAction(user, false) {
                this.game.setConfig(act);
                this.sendConfig();
                break;
            case CmdConst.GAME_CODE_LOAD_REPORT:
                this.sendReport();
                break;
            default:
                this.checkCanAction(user, true);
                this.game.doAction(act);
                break;
        }
    }

    /**
     * 处理系统类型的行动
     *
     * @param user
     * @param act
     * @throws BoardGameException
     */
    protected void processSystemAction(User user, BgAction act) throws BoardGameException {
        switch (act.getCode()) {
            case CmdConst.SYSTEM_CODE_LOAD_ROOM_INFO: // 用户读取房间信息
                this.loadRoomInfo(user);
                break;
            case CmdConst.SYSTEM_CODE_PLAYER_LIST: // 刷新玩家列表
                this.sendUserList(user);
                break;
            case CmdConst.SYSTEM_CODE_ROOM_JOIN_PLAY: // 加入游戏
                this.joinPlay(user);
                break;
            case CmdConst.SYSTEM_CODE_ROOM_LEAVE_PLAY: // 离开游戏
                this.leavePlay(user);
                break;
            case CmdConst.SYSTEM_CODE_USER_READY: // 用户准备
                this.ready(user);
                break;
            case CmdConst.SYSTEM_CODE_USER_START: // 用户开始游戏
                this.startGame(user);
                break;
            case CmdConst.SYSTEM_CODE_ROOM_INVITE_NOTIFY: // 发送房间邀请通知
                this.sendRoomInviteNotify(user);
                break;
            case CmdConst.SYSTEM_CODE_ROOM_LEAVE_REQUEST: // 退出房间的请求
                this.leaveRequest(user);
                break;
            case CmdConst.SYSTEM_CODE_ROOM_REPLACE_REQUEST: // 退出房间的请求
                this.replace(user);
                break;
            case CmdConst.SYSTEM_CODE_ROOM_LEAVE: // 用户强制退出房间
                this.leaveForce(user);
                break;
            case CmdConst.SYSTEM_CODE_JOIN_CHECK: // 进入房间前的检查
                this.joinCheck(user, act.getAsString("password"));
                break;
            case CmdConst.SYSTEM_CODE_SAVE_REPLAY:
                // TODO
                break;
            default:
                throw new BoardGameException("无效的指令代码!");
        }
    }

    /**
     * 玩家准备
     *
     * @param user
     * @throws BoardGameException
     */
    protected void ready(User user) throws BoardGameException {
        this.checkCanAction(user, false);
        this.setUserReady(user, !this.isUserReady(user));
        this.sendUserReadyResponse(user);
        // 检查是否所有的玩家都准备了,如果是,则尝试直接开始游戏
        try {
            this.checkStart();
            this.startGame(user);
        } catch (Exception ignored) {
        }
    }

    /**
     * 刷新房间中用户的状态
     *
     * @param user
     */
    protected void refreshUser(User user) {
        this.sendRoomUserResponse(user, CmdConst.SYSTEM_CODE_ROOM_REFRESH_USER);
    }

    /**
     * 换人
     *
     * @param user
     * @throws BoardGameException
     */
    public void replace(User user) throws BoardGameException {
        CheckUtils.check(!this.containUser(user), "用户不在这个房间内!");
        CheckUtils.check(this.replaceUser != null, "已经有玩家在等待换人!");
        CheckUtils.check(this.getState() == BgState.PAUSE, "已经有玩家在等待换人");
        this.replaceUser = user;
        this.setUserState(user, PlayingState.WAITING);
        this.setUserReady(user, true);
        this.sendUserReadyResponse(user);
        this.setState(BgState.PAUSE);
        this.sendUserButtonResponse();
        this.sendReplaceStartResponse();
        if (this.hall.checkSendNotifyTime(this)) {
            this.hall.sendWaitReplayRoomNotify(user, this);
        }
    }

    @Override
    public void run() {
        try {
            this.game.run();
        } catch (BoardGameException e) {
            log.info(e.getMessage(), e);
        } catch (Exception e) {
            log.error("游戏过程中发生异常!", e);
        } finally {
            this.endGame();
        }
    }

    /**
     * 向所有玩家发送游戏配置
     */
    public void sendConfig() {
        for (User u : this.getUsers()) {
            this.sendConfig(u);
        }
    }

    /**
     * 向玩家发送游戏配置
     *
     * @param user
     */
    public void sendConfig(User user) {
        BgResponse res = this.game.createConfigResponse();
        this.sendResponse(user, res);
    }

    /**
     * @param user
     * @param string
     */
    private void sendInfo(User user, String string) {
        Message message = new Message();
        message.messageType = MessageType.GAME;
        message.msg = " [" + BgUtils.escapeHtml(user.name) + "] " + string;
        this.sendMessage(message);
    }

    /**
     * 发送玩家加入游戏的信息
     *
     * @param user
     */
    protected void sendJoinPlayResponse(User user) {
        this.sendRoomUserResponse(user, CmdConst.SYSTEM_CODE_ROOM_JOIN_PLAY);
    }

    /**
     * 发送玩家加入房间的信息
     *
     * @param user
     */
    protected void sendJoinRoomResponse(User user) {
        this.sendRoomUserResponse(user, CmdConst.SYSTEM_CODE_ROOM_JOIN);
    }

    /**
     * 发送玩家离开游戏的信息
     *
     * @param user
     */
    protected void sendLeavePlayResponse(User user) {
        this.sendRoomUserResponse(user, CmdConst.SYSTEM_CODE_ROOM_LEAVE_PLAY);
    }

    /**
     * 发送玩家离开房间的信息
     *
     * @param user
     */
    protected void sendLeaveRoomResponse(User user) {
        this.sendRoomUserResponse(user, CmdConst.SYSTEM_CODE_ROOM_LEAVE);
    }

    /**
     * 给房间内的所有用户发送消息
     *
     * @param message
     */
    public void sendMessage(Message message) {
        for (User u : this.getUsers()) {
            u.sendMessage(this.id, message);
        }
    }

    /**
     * 发送玩家换人的信息
     *
     * @param user
     */
    private void sendPlayerReplaceResponse(User user, User replaceUser) {
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_REPLACE, -1);
        res.setPublicParameter("user", this.getUserMap(user));
        res.setPublicParameter("replaceUser", this.getUserMap(replaceUser));
        this.sendResponse(res);
    }

    /**
     * 将游戏中的玩家的座位信息发送给客户端
     */
    public void sendPlayerSitInfo() {
        for (User u : this.getUsers()) {
            this.sendPlayerSitInfo(u);
        }
    }

    /**
     * 将游戏中的玩家的座位信息发送给user
     *
     * @param user
     */
    public void sendPlayerSitInfo(User user) {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_LOAD_PLAYER, -1);
        List<Map<String, Object>> players = new ArrayList<>();
        for (Player p : this.game.getPlayers()) {
            Map<String, Object> o = p.toMap();
            o.put("localPlayer", p.user == user);
            players.add(o);
        }
        res.setPublicParameter("players", players);
        this.sendResponse(user, res);
    }

    /**
     * 发送换人结束的信息
     */
    public void sendReplaceEndResponse() {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_REPLACE_END, -1);
        this.sendResponse(res);
    }

    /**
     * @param replaceUser
     * @param user
     */
    private void sendReplaceMessage(User replaceUser, User user) {
        Message message = new Message();
        message.messageType = MessageType.GAME;
        message.msg = " [" + BgUtils.escapeHtml(user.name) + "] 替换 [" + BgUtils.escapeHtml(replaceUser.name) + "]";
        this.sendMessage(message);

    }

    /**
     * 发送换人开始的信息
     */
    public void sendReplaceStartResponse() {
        BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_REPLACE_START, -1);
        this.sendResponse(res);
    }

    /**
     * 发送录像
     *
     * @param user
     */
    @SuppressWarnings("unused")
    private void sendReplay(User user) {
        // TODO
    }

    /**
     * 发送战报信息
     */
    public void sendReport() {
        if (this.reportString != null) {
            BgResponse res = CmdFactory.createGameResponse(CmdConst.GAME_CODE_LOAD_REPORT, -1);
            res.setPublicParameter("reportString", this.reportString);
            this.sendResponse(res);
        }
    }

    /**
     * 给房间内的所有用户发送指令
     *
     * @param res
     */
    public void sendResponse(BgResponse res) {
        for (User u : this.getUsers()) {
            u.sendResponse(this.id, res);
        }
    }

    /**
     * 向用户发送指令,如果u为空,则向所有用户发送
     *
     * @param u
     * @param res
     */
    public void sendResponse(User u, BgResponse res) {
        if (u == null) {
            this.sendResponse(res);
        } else {
            u.sendResponse(this.id, res);
        }
    }

    /**
     * 向用户发送用户状态和房间的基本信息
     *
     * @param user
     */
    protected void sendRoomInfo(User user) {
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_USER_ROOM_INFO, -1);
        res.setPublicParameter("userState", this.getUserState(user));
        res.setPublicParameter("room", this.toMap());
        this.sendResponse(user, res);
    }

    /**
     * 发送房间邀请的通知
     *
     * @throws BoardGameException
     */
    private void sendRoomInviteNotify(User user) throws BoardGameException {
        CheckUtils.check(!this.containUser(user), "用户不在指定的房间内!");
        CheckUtils.check(this.isPlaying(), "游戏已经开始,不能发送邀请!");
        CheckUtils.check(!this.hall.checkSendNotifyTime(user) || !this.hall.checkSendNotifyTime(this), "每分钟只允许发送1次通知!");
        this.hall.sendCreateRoomNotify(user, this);
    }

    /**
     * 发送房间中用户操作相关的信息
     *
     * @param user
     * @param code 操作代码
     */
    protected void sendRoomUserResponse(User user, int code) {
        BgResponse res = CmdFactory.createSystemResponse(code, -1);
        res.setPublicParameter("user", this.getUserMap(user));
        this.sendResponse(res);
    }

    /**
     * 向所有用户发送按键状态变化的信息
     */
    public void sendUserButtonResponse() {
        for (User u : this.getUsers()) {
            this.sendUserButtonResponse(u);
        }
    }

    /**
     * 向指定用户发送按键状态变化的信息
     *
     * @param user
     */
    public void sendUserButtonResponse(User user) {
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_USER_BUTTON, -1);
        res.setPublicParameter("userState", this.getUserState(user));
        res.setPublicParameter("roomState", this.getState());
        this.sendResponse(user, res);
    }

    /**
     * 向用户发送房间中所有用户的列表,以及加入游戏的用户和准备状态
     *
     * @param user
     */
    protected void sendUserInfo(User user) {
        List<Map<String, Object>> joinUsers = new ArrayList<>();
        List<Map<String, Object>> users = new ArrayList<>();
        for (User u : this.getJoinUsers()) {
            Map<String, Object> o = u.toMap();
            o.put("ready", this.isUserReady(u));
            joinUsers.add(o);
        }
        for (User u : this.getUsers()) {
            Map<String, Object> o = u.toMap();
            o.put("userState", this.getUserState(u));
            users.add(o);
        }
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_USER_LIST_INFO, -1);
        res.setPublicParameter("joinUsers", joinUsers);
        res.setPublicParameter("users", users);
        this.sendResponse(user, res);
    }

    /**
     * 发送房间中所有用户列表
     *
     * @param user
     * @throws BoardGameException
     */
    public void sendUserList(User user) throws BoardGameException {
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_PLAYER_LIST, -1);
        res.setPublicParameter("users", BgUtils.toMapList(this.getUsers()));
        user.sendResponse(this.id, res);
    }

    /**
     * 向所有用户发送用户准备状态变化的信息
     *
     * @param user
     */
    protected void sendUserReadyResponse(User user) {
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_USER_READY, -1);
        res.setPublicParameter("user", this.getUserMap(user));
        res.setPublicParameter("ready", this.isUserReady(user));
        this.sendResponse(res);
    }

    /**
     * 保存战报信息
     */
    public void setReport() {
        this.reportString = this.game.getReport().toJSONString();
    }

    /**
     * 设置玩家的准备状态
     *
     * @param user
     */
    protected void setUserReady(User user, boolean ready) {
        this.getUserParam(user).ready = ready;
    }

    /**
     * 设置用户在房间中的状态
     *
     * @param user
     * @param state
     */
    protected void setUserState(User user, PlayingState state) {
        this.getUserParam(user).playingState = state;
    }

    /**
     * 开始游戏
     *
     * @param user
     * @throws BoardGameException
     */
    protected void startGame(User user) throws BoardGameException {
        this.checkCanAction(user, false);
        this.checkStart();
        // 将所有玩家的准备状态设为未准备
        // 将所有玩家添加到游戏中,并且开始游戏
        for (User u : this.getJoinUsers()) {
            this.game.joinGame(u.getPlayer(this));
            this.setUserReady(u, false);
            this.sendUserReadyResponse(u);
        }
        // 将房间的状态设为游戏中
        this.setState(BgState.PLAYING);
        this.sendUserButtonResponse();
        // 创建游戏线程
        Thread gameThread = new Thread(this);
        gameThread.setName(this.getLogName());
        gameThread.start();
    }


    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("chinese", this.name);
        map.put("gameType", this.type);
        map.put("state", this.getState());
        map.put("bgState", this.getState());
        map.put("players", this.getJoinUserNumber() + "/" + this.getUsers().size());
        map.put("password", this.hasPassword());
        map.put("descr", this.descr);
        return map;
    }

    /**
     * 用户在房间中的参数
     *
     * @author F14eagle
     */
    protected class UserRoomParam {
        User user;
        boolean ready = false;
        PlayingState playingState = PlayingState.AUDIENCE;

        UserRoomParam(User user) {
            this.user = user;
        }
    }

}
