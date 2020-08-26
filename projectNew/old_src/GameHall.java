package com.f14.bg.hall;

import com.f14.F14bg.consts.CmdConst;
import com.f14.F14bg.consts.GameType;
import com.f14.F14bg.network.CmdFactory;
import com.f14.F14bg.utils.PrivUtil;
import com.f14.bg.action.BgResponse;
import com.f14.bg.chat.Message;
import com.f14.bg.consts.NotifyType;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.utils.BgUtils;
import com.f14.bg.utils.CheckUtils;
import com.f14.f14bgdb.model.BoardGame;
import com.f14.f14bgdb.util.CodeUtil;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameHall {
    /**
     * 允许的最大房间数量
     */
    public static int MAX_ROOM = 50;
    /**
     * 单个玩家允许的房间最大数量
     */
    public static int PLAYER_ROOM_LIMIT = 3;
    /**
     * 超时时限 - 15分钟
     */
    public static long TIME_OUT = 1000 * 60 * 15;
    /**
     * 通知的发送间隔 - 1分钟
     */
    public static long NOTIFY_GAP = 1000 * 60;
    protected Logger log = Logger.getLogger(this.getClass());
    /**
     * 所有房间
     */
    protected LinkedHashMap<Integer, GameRoom> rooms = new LinkedHashMap<>();
    /**
     * 在线用户
     */
    protected LinkedHashMap<String, User> users = new LinkedHashMap<>();
    /**
     * 掉线用户
     */
    protected LinkedHashMap<String, User> recentUsers = new LinkedHashMap<>();
    /**
     * 掉线管理
     */
    protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    /**
     * 最后一次发送通知的时间（房间）
     */
    protected Map<Integer, Long> lastNotifyTimesRoom = new HashMap<>();
    /**
     * 最后一次发送通知的时间（用户）
     */
    protected Map<Long, Long> lastNotifyTimesUser = new HashMap<>();

    /**
     * 将user添加到最近登录过的用户列表中
     */
    public void addRecentUser(User user) {
        synchronized (recentUsers) {
            this.recentUsers.put(user.loginName, user);
        }
    }

    /**
     * 添加用户到大厅中
     *
     * @param user
     * @throws BoardGameException
     */
    public void addUser(User user) throws BoardGameException {
        synchronized (users) {
            this.users.put(user.loginName, user);
        }
        // 将用户加入大厅的消息发送给所有大厅中的用户
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_JOIN_HALL, -1);
        res.setPublicParameter("user", user.toMap());
        this.sendResponse(res);
    }

    /**
     * 向大厅中的所有玩家广播消息
     *
     * @param user
     * @param message
     */
    public void broadcast(User user, String message) {
        BgResponse res = CmdFactory.createClientResponse(CmdConst.CLIENT_BROADCAST);
        res.setPublicParameter("user", user.name);
        res.setPublicParameter("message", message);
        this.sendResponse(res);
    }

    /**
     * 检查目标是否允许发送通知
     *
     * @return
     */
    public boolean checkSendNotifyTime(GameRoom room) {
        synchronized (lastNotifyTimesRoom) {
            Long time = this.lastNotifyTimesRoom.get(room.id);
            return !(time != null && (System.currentTimeMillis() - time < NOTIFY_GAP));
        }
    }

    /**
     * 检查目标是否允许发送通知
     *
     * @return
     */
    public boolean checkSendNotifyTime(User user) {
        synchronized (lastNotifyTimesUser) {
            Long time = this.lastNotifyTimesUser.get(user.id);
            return !(time != null && (System.currentTimeMillis() - time < NOTIFY_GAP));
        }
    }

    /**
     * 创建游戏房间
     *
     * @param user     创建的玩家
     * @param gameType
     * @param name     房间名称
     * @param descr    房间描述
     * @param password
     */

    public GameRoom createGameRoom(User user, String gameType, String name, String descr, String password)
            throws BoardGameException {
        synchronized (rooms) {
            CheckUtils.check(rooms.size() >= MAX_ROOM, "房间已满,不能创建房间!");
            if (!PrivUtil.hasAdminPriv(user)) {
                CheckUtils.check(user.hasRoom(), "你已经在其他房间中,不能创建房间!");
            }
            GameType type = GameType.valueOf(gameType);
            CheckUtils.checkNull(type, "未知的游戏类型!");
            BoardGame bg = CodeUtil.getBoardGame(gameType);
            CheckUtils.checkNull(bg, "未知找到指定的游戏信息!");
            GameRoom room = new GameRoom(user, type, name, descr, password);
            // 创建房间中的游戏,并设置允许的人数
            room.createGame(bg.getId(), bg.getMaxPlayerNumber(), bg.getMinPlayerNumber());
            rooms.put(room.id, room);
            room.hall = this;
            // 发送创建房间的消息到大厅的玩家
            BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_ADDED, -1);
            res.setPublicParameter("room", room.toMap());
            this.sendResponse(res);
            return room;
        }
    }

    /**
     * 按照id取得游戏实例
     *
     * @param id
     * @return
     */
    public GameRoom getGameRoom(int id) {
        synchronized (rooms) {
            return rooms.get(id);
        }
    }

    /**
     * 取得所有游戏房间的列表
     *
     * @return
     */

    public Collection<GameRoom> getGameRoomList() {
        synchronized (rooms) {
            return this.rooms.values();
        }
    }

    /**
     * 按照loginName取得最近登录过的用户对象
     *
     * @param loginName
     * @return
     */
    public User getRecentUser(String loginName) {
        synchronized (recentUsers) {
            return this.recentUsers.get(loginName);
        }
    }

    /**
     * 按照登录名取得登录的用户
     *
     * @param loginName
     * @return
     */
    public User getUser(String loginName) {
        synchronized (users) {
            return this.users.get(loginName);
        }
    }

    /**
     * 取得所有在大厅中的用户
     *
     * @return
     */

    public Collection<User> getUsers() {
        synchronized (users) {
            return this.users.values();
        }
    }

    /**
     * 用户失去连接
     *
     * @param user
     */
    public void lostConnect(User user) {
        this.addRecentUser(user);
        user.future = executor.schedule(() -> {
            synchronized (recentUsers) {
                if (recentUsers.containsKey(user.loginName)) {
                    GameHall.this.removeRecentUser(user.loginName);
                    // 如果玩家在游戏中,则将其从游戏中移除
                    for (int roomId : user.getRoomIds()) {
                        GameRoom room = GameHall.this.getGameRoom(roomId);
                        if (room != null && room.containUser(user)) {
                            room.leave(user);
                        }
                    }
                }
            }
        }, TIME_OUT, TimeUnit.MILLISECONDS);
    }

    /**
     * 用户重新连接
     *
     * @param user
     */
    public void reconnect(User user) {
        this.removeRecentUser(user.loginName);
        if (user.future != null) {
            user.future.cancel(true);
            user.future = null;
        }
    }

    /**
     * 刷新目标的最近一次发送通知的时间
     */
    public void refreshSendNotifyTime(GameRoom room) {
        synchronized (lastNotifyTimesRoom) {
            this.lastNotifyTimesRoom.put(room.id, System.currentTimeMillis());
        }
    }

    /**
     * 刷新目标的最近一次发送通知的时间
     */
    public void refreshSendNotifyTime(User user) {
        synchronized (lastNotifyTimesUser) {
            this.lastNotifyTimesUser.put(user.id, System.currentTimeMillis());
        }
    }

    /**
     * 刷新大厅中用户的信息
     *
     * @param user
     */
    public void refreshUser(User user) {
        // 将用户加入大厅的消息发送给所有大厅中的用户
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_HALL_REFRESH_USER, -1);
        res.setPublicParameter("user", user.toMap());
        this.sendResponse(res);
    }

    /**
     * 移除游戏房间
     */
    public void removeGameRoom(GameRoom room) {
        synchronized (rooms) {
            rooms.remove(room.id);
        }
        // 发送移除房间的消息到大厅的玩家
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_REMOVED, -1);
        res.setPublicParameter("roomId", room.id);
        this.sendResponse(res);
    }

    /**
     * 按照loginName移除最近登录过的用户对象
     *
     * @param loginName
     */
    public void removeRecentUser(String loginName) {
        synchronized (recentUsers) {
            this.recentUsers.remove(loginName);
        }
    }

    /**
     * 将用户从大厅中移除
     *
     * @param user
     */
    public void removeUser(User user) {
        synchronized (users) {
            this.users.remove(user.loginName);
        }
        // 将用户离开大厅的消息发送给所有大厅中的用户
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_LEAVE_HALL, -1);
        res.setPublicParameter("user", user.toMap());
        this.sendResponse(res);
    }

    /**
     * 发送创建房间的通知
     *
     * @param room
     */
    public void sendCreateRoomNotify(User sender, GameRoom room) {
        String message = "[" + room.type + "][" + room.name + "]等待玩家加入," + room.descr;
        BgResponse res = CmdFactory.createClientNotifyResponse(CmdConst.CLIENT_BUBBLE_NOTIFY, NotifyType.CREATE_ROOM);
        res.setPublicParameter("roomId", room.id);
        res.setPublicParameter("message", message);
        res.setPublicParameter("gameType", room.type);
        // 只能向所有不在进行游戏,并且同意接受该类型通知的玩家发送该通知
        for (User u : this.getUsers()) {
            if (sender != u // 也不用给自己发
                    && !room.containUser(u) // 如果已经在该房间的也不用发
                    && u.canSendCreateRoomNotify(room.type)) {
                u.sendResponse(0, res);
            }
        }
        // 记录最后一次发送通知的时间
        this.refreshSendNotifyTime(sender);
        this.refreshSendNotifyTime(room);
    }

    /**
     * 给在大厅中不在房间里的所有玩家发送消息
     *
     * @param message
     */
    public void sendMessage(Message message) {
        for (User u : this.getUsers()) {
            u.sendMessage(0, message);
        }
    }

    /**
     * 将回应发送给大厅内不在房间里的所有玩家
     *
     * @param res
     */
    public void sendResponse(BgResponse res) {
        for (User u : this.getUsers()) {
            u.sendResponse(0, res);
        }
    }

    /**
     * 向大厅中的用户发送房间属性变化的消息
     *
     * @param room
     */
    public void sendRoomChangeResponse(GameRoom room) {
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_CHANGED, -1);
        res.setPublicParameter("room", room.toMap());
        this.sendResponse(res);
    }

    /**
     * 发送房间列表
     *
     * @throws BoardGameException
     */
    public void sendRoomList(User user) throws BoardGameException {
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_ROOM_LIST, -1);
        res.setPublicParameter("rooms", BgUtils.toMapList(this.getGameRoomList()));
        user.sendResponse(0, res);
    }

    /**
     * 发送大厅中所有用户列表
     *
     * @throws BoardGameException
     */
    public void sendUserList(User user) throws BoardGameException {
        BgResponse res = CmdFactory.createSystemResponse(CmdConst.SYSTEM_CODE_PLAYER_LIST, -1);
        res.setPublicParameter("users", BgUtils.toMapList(this.getUsers()));
        user.sendResponse(0, res);
    }

    /**
     * 发送等待换人的通知
     *
     * @param room
     */
    public void sendWaitReplayRoomNotify(User sender, GameRoom room) {
        String message = "[" + room.type + "][" + room.name + "]等待换人," + room.descr;
        BgResponse res = CmdFactory.createClientNotifyResponse(CmdConst.CLIENT_BUBBLE_NOTIFY, NotifyType.CREATE_ROOM);
        res.setPublicParameter("roomId", room.id);
        res.setPublicParameter("message", message);
        res.setPublicParameter("gameType", room.type);
        // 只能向所有不在进行游戏,并且同意接受该类型通知的玩家发送该通知
        for (User u : this.getUsers()) {
            if (sender != u // 也不用给自己发
                    && !room.containUser(u) // 如果已经在该房间的也不用发
                    && u.canSendCreateRoomNotify(room.type)) {
                u.sendResponse(0, res);
            }
        }
    }

    /**
     * 处理用户掉线
     *
     * @param user
     */
    public void userDisconnect(User user) {
        // 如果用户已经登陆,则处理断开
        Integer[] roomIds = user.getRoomIds().toArray(new Integer[user.getRoomIds().size()]);
        boolean lostConnect = false;
        for (int roomId : roomIds) {
            GameRoom room = this.getGameRoom(roomId);
            if (room != null) {
                if (room.isPlayingGame(user)) {
                    // 如果玩家正在进行游戏,则将用户保存到最近用户列表中
                    lostConnect = true;
                }
                // 将玩家状态设置为断线
                room.lostConnect(user);
            }
        }
        if (lostConnect) {
            this.lostConnect(user);
        }
    }

}
