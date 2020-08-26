package com.f14.bg.hall;

import com.f14.F14bg.consts.CmdConst;
import com.f14.F14bg.consts.GameType;
import com.f14.F14bg.network.CmdFactory;
import com.f14.F14bg.network.PlayerAsyncHandler;
import com.f14.bg.action.BgResponse;
import com.f14.bg.action.ISendable;
import com.f14.bg.action.ISendableWith;
import com.f14.bg.chat.IChatable;
import com.f14.bg.chat.IChatableWith;
import com.f14.bg.chat.Message;
import com.f14.bg.component.Convertable;
import com.f14.bg.player.Player;
import com.f14.net.socket.cmd.ByteCommand;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class User implements Convertable, ISendable, ISendableWith<Integer>, IChatable, IChatableWith<Integer> {
    public long id;
    public String loginName;
    public String name;
    public boolean isAdmin = false;
    public PlayerAsyncHandler handler;

    public ScheduledFuture<?> future;

    protected Map<Integer, Player> players = new LinkedHashMap<>();
    protected com.f14.f14bgdb.model.User userModel;

    protected Set<GameRoom> rooms = new HashSet<>();

    public User(PlayerAsyncHandler handler) {
        this.handler = handler;
    }

    /**
     * 添加玩家对象
     *
     * @param player
     */
    public void addPlayer(GameRoom gameRoom, Player player) {
        this.players.put(gameRoom.getId(), player);
        this.rooms.add(gameRoom);
    }

    /**
     * 检查是否可以向该用户发送通知
     *
     * @return
     */
    public boolean canSendCreateRoomNotify(GameType gameType) {
        // 如果玩家正在游戏中,则不用发送
        for (GameRoom room : this.rooms) {
            if (room.isPlayingGame(this)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 切断玩家的连接
     */
    public void closeConnection() {
        this.handler.close();
    }

    /**
     * 取得用户在指定房间里的玩家对象
     *
     * @param <P>
     * @param gameRoom
     * @return
     */
    public <P extends Player> P getPlayer(GameRoom gameRoom) {
        return this.getPlayer(gameRoom.getId());
    }

    /**
     * 取得用户在指定房间里的玩家对象
     *
     * @param <P>
     * @param roomId
     * @return
     */
    @SuppressWarnings("unchecked")
    public <P extends Player> P getPlayer(int roomId) {
        return (P) this.players.get(roomId);
    }

    /**
     * 取得用户所在的所有的房间id
     *
     * @return
     */
    public Collection<Integer> getRoomIds() {
        synchronized (this.players) {
            return this.players.keySet();
        }
    }

    public com.f14.f14bgdb.model.User getUserModel() {
        return userModel;
    }

    public void setUserModel(com.f14.f14bgdb.model.User userModel) {
        this.userModel = userModel;
        this.id = userModel.getId();
        this.loginName = userModel.getLoginName();
        this.name = userModel.getUserName();
    }

    /**
     * 判断玩家是否已经进入房间
     *
     * @return
     */
    public boolean hasRoom() {
        return !this.players.isEmpty();
    }

    /**
     * 移除玩家对象
     *
     * @param <P>
     * @return
     */

    @SuppressWarnings("unchecked")
    public <P extends Player> P removePlayer(GameRoom gameRoom) {
        this.rooms.remove(gameRoom);
        return (P) this.players.remove(gameRoom.getId());
    }

    /**
     * 向玩家发送指令
     *
     * @param cmd
     */
    public void sendCommand(ByteCommand cmd) {
        if (!this.handler.isClosed()) {
            this.handler.sendCommand(cmd);
        }
    }

    /**
     * 向玩家发送消息
     *
     * @param message
     */
    @Override
    public void sendMessage(Integer roomId, Message message) {
        BgResponse res = CmdFactory.createChatResponse(message);
        this.sendResponse(roomId, res);
    }
    /**
     * 向玩家发送消息
     *
     * @param message
     */
    @Override
    public void sendMessage(Integer roomId, Message message) {
        BgResponse res = CmdFactory.createChatResponse(message);
        this.sendResponse(roomId, res);
    }

    /**
     * 向玩家发送回应
     *
     * @param roomId 发送回应的房间id
     * @param res
     */
    @Override
    public void sendResponse(Integer roomId, BgResponse res) {
        String content;
        if (roomId == 0) {
            // roomId为0时表示发送大厅的信息
            content = res.getPrivateString();
        } else {
            Player p = this.getPlayer(roomId);
            if (p != null && res.position == p.position) {
                content = res.getPrivateString();
            } else if ((p == null || p.position < 0) && this.isAdmin) {
                // 管理员特权
                content = res.getPrivateString();
            } else {
                content = res.getPublicString();
            }
        }
        ByteCommand cmd;
        if (res.type == CmdConst.CLIENT_CMD) {
            // 如果消息类型是客户端消息,则按照客户端消息的类型发送
            cmd = CmdFactory.createClientCommand(content);
        } else {
            cmd = CmdFactory.createCommand(roomId, content);
        }
        this.sendCommand(cmd);
    }


    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("loginName", this.loginName);
        map.put("chinese", this.name);
        map.put("userId", this.userModel.getId());
        map.put("isAdmin", this.isAdmin);
        if (this.rooms.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (GameRoom room : this.rooms) {
                if (sb.length() > 0) {
                    sb.append(" 与 ");
                }
                sb.append(room.getName());
            }
            map.put("room", sb.toString());
        }
        return map;
    }

    @Override
    public void sendResponse(@NotNull BgResponse res) {
        this.sendResponse(0, res);
    }
}
