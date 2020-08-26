package com.f14.bg;

import com.f14.bg.consts.BgVersion;
import com.f14.bg.consts.TeamMode;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 桌游的配置对象
 *
 * @author F14eagle
 */
public abstract class BoardGameConfig {
    public int playerNumber;
    public Set<String> versions = new HashSet<>();
    public boolean randomSeat = true;
    public boolean teamMatch = false;
    public TeamMode teamMode = TeamMode.RANDOM;

    public boolean getRandomSeat() {
        return randomSeat;
    }

    public void setRandomSeat(boolean randomSeat) {
        this.randomSeat = randomSeat;
    }

    public TeamMode getTeamMode() {
        return teamMode;
    }

    public void setTeamMode(TeamMode teamMode) {
        this.teamMode = teamMode;
    }

    public Collection<String> getVersions() {
        return versions;
    }

    public void setVersions(Set<String> versions) {
        this.versions = versions;
    }

    /**
     * 判断该配置是否拥有指定的扩充
     *
     * @param expName
     * @return
     */
    public boolean hasExpansion(String expName) {
        return this.versions.contains(expName);
    }

    /**
     * 判断该配置是否是基础版游戏
     *
     * @return
     */
    public boolean isBaseGame() {
        return versions.size() == 1 && versions.contains(BgVersion.BASE);
    }

    public boolean isTeamMatch() {
        return teamMatch;
    }

    public void setTeamMatch(boolean teamMatch) {
        this.teamMatch = teamMatch;
    }
}
