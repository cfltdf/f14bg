package com.f14.Eclipse.config;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Group of start position config
 *
 * @author f14eagle
 */
public class StartPositionGroup {
    public int playerNumber;

    public List<StartPositionConfig> startConfig = new ArrayList<>();

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }


    public List<StartPositionConfig> getStartConfig() {
        return startConfig;
    }

    public void setStartConfig(List<StartPositionConfig> startConfig) {
        this.startConfig = new ArrayList<>();
        for (Object o : startConfig) {
            StartPositionConfig a = (StartPositionConfig) JSONObject.toBean(JSONObject.fromObject(o),
                    StartPositionConfig.class);
            this.startConfig.add(a);
        }
    }

    /**
     * 取得玩家位置对应的起始配置
     *
     * @param playerPosition
     * @return
     */
    public StartPositionConfig getStartPositionConfig(int playerPosition) {
        return this.startConfig.get(playerPosition);
    }

}
