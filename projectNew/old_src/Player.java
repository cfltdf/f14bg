package com.f14.bg.player;

import com.f14.F14bg.consts.CmdConst;
import com.f14.bg.BGConst;
import com.f14.bg.action.BgResponse;
import com.f14.bg.chat.Message;
import com.f14.bg.common.ParamCache;
import com.f14.bg.component.Convertable;
import com.f14.bg.hall.User;
import com.f14.bg.report.Printable;

import java.util.HashMap;
import java.util.Map;

public abstract class Player implements Convertable, Printable {

    public User user;
    public int position = BGConst.INT_NULL;

    private ParamCache params = new ParamCache();
    private int team;

    public String getName() {
        return this.user.getName();
    }


    public ParamCache getParams() {
        return params;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String getReportString() {
        return String.format("玩家%d[%s]", this.position + 1, this.getName());
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    /**
     * 重置玩家的游戏信息
     */
    public void reset() {
        this.params.clear();
    }

    /**
     * 向玩家发送异常信息
     *
     * @param roomId
     * @param e
     */
    public void sendException(int roomId, Exception e) {
        user.getHandler().sendCommand(CmdConst.EXCEPTION_CMD, roomId, e.getMessage());
    }

    /**
     * 发送消息
     *
     * @param roomId
     * @param message
     */
    public void sendMessage(int roomId, Message message) {
        user.sendMessage(roomId, message);
    }

    /**
     * 发送回应
     *
     * @param roomId
     * @param res
     */
    public void sendResponse(int roomId, BgResponse res) {
        user.sendResponse(roomId, res);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> res = new HashMap<>();
        res.put("userId", this.user.getId());
        res.put("chinese", this.getName());
        res.put("position", this.getPosition());
        res.put("team", this.getTeam());
        return res;
    }
}
