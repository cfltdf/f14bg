package com.f14.brass.component;

import com.f14.bg.common.ListMap;
import com.f14.bg.component.Card;
import com.f14.brass.consts.BrassConnectType;

import java.util.LinkedHashSet;
import java.util.Set;

public class BrassLocation extends Card {
    public com.f14.brass.consts.BrassLocation location;
    public boolean use2p;
    public boolean isOuterLocation;
    public int goldCircle;
    protected String canalConnection;
    protected String railConnection;
    protected String virtualConnection;
    protected Set<BrassIndustrySpace> industrySpaces = new LinkedHashSet<>();
    protected ListMap<BrassConnectType, BrassConnection> connections = new ListMap<>();


    @Override
    public BrassLocation clone() {
        BrassLocation res = (BrassLocation) super.clone();
        res.industrySpaces = new LinkedHashSet<>();
        for (BrassIndustrySpace o : this.industrySpaces) {
            res.industrySpaces.add(o.clone());
        }
        res.connections = new ListMap<>();
        return res;
    }

    public String getCanalConnection() {
        return canalConnection;
    }

    public void setCanalConnection(String canalConnection) {
        this.canalConnection = canalConnection;
    }

    public ListMap<BrassConnectType, BrassConnection> getConnections() {
        return connections;
    }

    public void setConnections(ListMap<BrassConnectType, BrassConnection> connections) {
        this.connections = connections;
    }

    public int getGoldCircle() {
        return goldCircle;
    }

    public void setGoldCircle(int goldCircle) {
        this.goldCircle = goldCircle;
    }

    public Set<BrassIndustrySpace> getIndustrySpaces() {
        return industrySpaces;
    }

    public void setIndustrySpaces(Set<BrassIndustrySpace> industrySpaces) {
        this.industrySpaces = industrySpaces;
    }

    public com.f14.brass.consts.BrassLocation getLocation() {
        return location;
    }

    public void setLocation(com.f14.brass.consts.BrassLocation location) {
        this.location = location;
    }

    public String getRailConnection() {
        return railConnection;
    }

    public void setRailConnection(String railConnection) {
        this.railConnection = railConnection;
    }

    public String getVirtualConnection() {
        return virtualConnection;
    }

    public void setVirtualConnection(String virtualConnection) {
        this.virtualConnection = virtualConnection;
    }

    public boolean isOuterLocation() {
        return isOuterLocation;
    }

    public void setOuterLocation(boolean isOuterLocation) {
        this.isOuterLocation = isOuterLocation;
    }

    public boolean isUse2p() {
        return use2p;
    }

    public void setUse2p(boolean use2p) {
        this.use2p = use2p;
    }

}
