package com.f14.Eclipse.component;

import com.f14.Eclipse.EclipsePlayer;
import com.f14.Eclipse.consts.ReputationSquareType;
import com.f14.Eclipse.consts.ReputationTileType;
import com.f14.bg.component.Convertable;

import java.util.HashMap;
import java.util.Map;

public class ReputationSquare implements Convertable {
    public int index;
    public ReputationSquareType reputationSquareType;
    public ReputationTileType reputationTileType;
    public int vp;
    public EclipsePlayer targetPlayer;


    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> res = new HashMap<>();
        res.put("index", index);
        res.put("reputationSquareType", reputationSquareType);
        res.put("reputationTileType", reputationTileType);
        res.put("vp", vp);
        if (targetPlayer != null) {
            res.put("targetPlayer", targetPlayer.getPosition());
            res.put("targetRace", targetPlayer.race);
        }
        return res;
    }

}
