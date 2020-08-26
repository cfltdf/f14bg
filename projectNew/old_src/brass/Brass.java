package com.f14.brass;

import com.f14.bg.BoardGame;
import com.f14.bg.consts.BgVersion;
import com.f14.bg.exception.BoardGameException;
import net.sf.json.JSONObject;


public class Brass extends BoardGame<BrassPlayer, BrassGameMode> {


    @SuppressWarnings("unchecked")
    @Override
    protected BrassConfig createConfig(JSONObject object) throws BoardGameException {
        BrassConfig config = new BrassConfig();
        config.versions.add(BgVersion.BASE);
        // String versions = object.getString("versions");
        // if(!StringUtils.isEmpty(versions)){
        // String[] vs = versions.split(",");
        // for(String v : vs){
        // config.versions.add(v);
        // }
        // }
        return config;
    }


    @Override
    public BrassConfig getConfig() {
        return (BrassConfig) super.getConfig();
    }


    @Override
    public BrassReport getReport() {
        return (BrassReport) super.getReport();
    }

    @Override
    public void initConfig() {
        BrassConfig config = new BrassConfig();
        config.versions.add(BgVersion.BASE);
        this.config = config;
    }

    @Override
    public void initConst() {
    }

    @Override
    public void initReport() {
        this.report = new BrassReport(this);
    }

    @Override
    protected void sendGameInfo(BrassPlayer receiver) throws BoardGameException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void sendInitInfo(BrassPlayer receiver) throws BoardGameException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void sendPlayerPlayingInfo(BrassPlayer receiver) throws BoardGameException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void setupGame() throws BoardGameException {
        this.config.playerNumber = this.getCurrentPlayerNumber();
        this.gameMode = new BrassGameMode(this);
    }

}
