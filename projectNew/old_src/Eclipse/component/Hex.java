package com.f14.Eclipse.component;

import com.f14.Eclipse.EclipsePlayer;
import com.f14.Eclipse.consts.RaceType;
import com.f14.Eclipse.consts.ResourceType;
import com.f14.Eclipse.consts.UnitType;
import com.f14.bg.component.Card;
import com.f14.bg.utils.BgUtils;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Hex extends Card {
    public int section;
    public int vp;
    public boolean artifactKey;
    public int[] wormHoles = new int[6];

    public Position position = new Position();
    public int direction;
    public boolean discoveryTile;
    public boolean explored;
    public List<String> initShips = new ArrayList<>();
    public RaceType startRace;
    protected EclipsePlayer owner;

    protected DiscoveryTile discoveryTileObject;

    protected Hex[] adjacentHex = new Hex[6];

    protected List<Planet> planets = new ArrayList<>();

    protected List<Unit> units = new ArrayList<>();

    public void addPlanet(Planet o) {
        o.index = this.planets.size();
        this.planets.add(o);
    }

    /**
     * 添加单位到板块
     *
     * @param o
     */
    public void addUnit(Unit o) {
        this.units.add(o);
    }


    @Override
    public Hex clone() {
        Hex o = (Hex) super.clone();
        o.adjacentHex = new Hex[6];
        o.position = new Position();
        o.units = new ArrayList<>();
        o.planets = new ArrayList<>();
        for (Planet p : this.planets) {
            o.planets.add(p.clone());
        }
        // Clone wormHoles
        int[] wh = new int[6];
        System.arraycopy(this.wormHoles, 0, wh, 0, this.wormHoles.length);
        o.wormHoles = wh;
        o.discoveryTileObject = null;
        return o;
    }

    /**
     * 取得板块上的所有资源方格
     *
     * @return
     */

    public List<ResourceSquare> getAllResourceSquares() {
        List<ResourceSquare> res = new ArrayList<>();
        for (Planet p : this.planets) {
            res.addAll(p.getSquares());
        }
        return res;
    }

    public boolean getDiscoveryTile() {
        return discoveryTile;
    }

    public void setDiscoveryTile(boolean discoveryTile) {
        this.discoveryTile = discoveryTile;
    }


    public DiscoveryTile getDiscoveryTileObject() {
        return discoveryTileObject;
    }

    public void setDiscoveryTileObject(DiscoveryTile discoveryTileObject) {
        this.discoveryTileObject = discoveryTileObject;
    }

    public List<String> getInitShips() {
        return initShips;
    }

    public void setInitShips(List<String> initShips) {
        this.initShips = initShips;
    }

    /**
     * 取得敌对玩家的单位数量
     *
     * @param player
     * @return
     */
    public int getOppositeUnitCount(EclipsePlayer player) {
        int i = 0;
        for (Unit o : this.units) {
            if (o.getOwner() == null || o.getOwner().getTeam() != player.getTeam()) {
                i += 1;
            }
        }
        return i;
    }

    /**
     * 取得板块的拥有者
     *
     * @return
     */
    public EclipsePlayer getOwner() {
        return owner;
    }

    /**
     * 设置板块的拥有者
     *
     * @param player
     */
    public void setOwner(EclipsePlayer player) {
        this.owner = player;
    }

    /**
     * 按照index取得planet
     *
     * @param index
     * @return
     */

    public Planet getPlanet(int index) {
        if (this.planets.size() > index) {
            return this.planets.get(index);
        } else {
            return null;
        }
    }


    public List<Planet> getPlanets() {
        return planets;
    }

    public void setPlanets(List<Planet> planets) {
        this.planets.clear();
        if (planets != null) {
            for (Object o : planets) {
                Planet a = (Planet) JSONObject.toBean(JSONObject.fromObject(o), Planet.class);
                this.addPlanet(a);
            }
        }
    }

    /**
     * 取得玩家的所有单位
     *
     * @param player
     * @return
     */

    public List<Unit> getPlayerUnit(EclipsePlayer player) {
        List<Unit> res = new ArrayList<>();
        for (Unit o : this.units) {
            if (o.owner == player) {
                res.add(o);
            }
        }
        return res;
    }

    /**
     * 取得指定玩家单位的数量
     *
     * @param player
     * @return
     */
    public int getPlayerUnitCount(EclipsePlayer player) {
        int i = 0;
        for (Unit o : this.units) {
            if (o.getOwner() == player) {
                i += 1;
            }
        }
        return i;
    }

    public int getSection() {
        return section;
    }

    public void setSection(int section) {
        this.section = section;
    }

    public RaceType getStartRace() {
        return startRace;
    }

    public void setStartRace(RaceType startRace) {
        this.startRace = startRace;
    }

    /**
     * 取得指定id的单位
     *
     * @param unitId
     * @return
     */

    public Unit getUnit(int unitId) {
        for (Unit o : this.units) {
            if (o.id == unitId) {
                return o;
            }
        }
        return null;
    }

    public int getVp() {
        return vp;
    }

    public void setVp(int vp) {
        this.vp = vp;
    }

    public int[] getWormHoles() {
        return wormHoles;
    }

    public void setWormHoles(int[] wormHoles) {
        this.wormHoles = wormHoles;
    }

    /**
     * 检查玩家在板块上是否有未被牵制的飞船
     *
     * @param player
     * @return
     */
    public boolean hasFreeShip(EclipsePlayer player) {
        if (this.getPlayerUnitCount(player) <= this.getOppositeUnitCount(player)) {
            return false;
        }
        for (Unit o : this.getPlayerUnit(player)) {
            if (UnitType.isShip(o.getUnitType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查板块中是否存在指定类型的星球
     *
     * @param unitType
     * @return
     */
    public boolean hasPlanet(UnitType unitType) {
        ResourceType type = ResourceType.convertFrom(unitType);
        for (Planet o : this.planets) {
            if (o.resourceType == type) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查板块上是否有指定玩家的单位,包括影响力圆片和飞船
     *
     * @param player
     * @return
     */
    public boolean hasPlayerUnitOrInfluence(EclipsePlayer player) {
        if (this.owner == player) {
            return true;
        }
        for (Unit o : this.units) {
            if (o.getOwner() == player) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查板块在指定的方向是否有虫洞
     *
     * @param direction
     * @return
     */
    public boolean hasWormhole(int direction) {
        return this.wormHoles[direction] == 1;
    }

    public boolean isArtifactKey() {
        return artifactKey;
    }

    public void setArtifactKey(boolean artifactKey) {
        this.artifactKey = artifactKey;
    }

    /**
     * 判断玩家是否是板块的owner
     *
     * @param player
     * @return
     */
    public boolean isOwner(EclipsePlayer player) {
        return this.owner == player;
    }

    /**
     * 移除单位
     *
     * @param o
     */
    public void removeUnit(Unit o) {
        this.units.remove(o);
    }

    /**
     * 向左旋转一格
     */
    public void rotateLeft() {
        // 所有虫洞左移一位
        int w = this.wormHoles[0];
        System.arraycopy(this.wormHoles, 1, this.wormHoles, 0, 5);
        this.wormHoles[5] = w;
        this.direction = (this.direction - 1 + 6) % 6;
    }

    /**
     * 向右旋转一格
     */
    public void rotateRight() {
        // 所有虫洞右移一位
        int w = this.wormHoles[5];
        System.arraycopy(this.wormHoles, 0, this.wormHoles, 1, 5);
        this.wormHoles[0] = w;
        this.direction = (this.direction + 1) % 6;
    }

    /**
     * 旋转到指定的方向
     *
     * @param direction
     */
    public void rotateTo(int direction) {
        while (this.direction != direction) {
            this.rotateRight();
        }
    }

    /**
     * 设置hex的index,主要是给玩家起始板块用的
     *
     * @param index
     */
    public void setHexIndex(int index) {
        this.name = index + "";
        this.cardIndex = index;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> res = super.toMap();
        res.put("chinese", this.name);
        res.put("cardIndex", this.cardIndex);
        res.put("position", this.position);
        res.put("section", this.section);

        if (this.explored) {
            res.put("direction", this.direction);
            res.put("wormHoles", this.wormHoles);
            res.put("vp", this.vp);
            res.put("discoveryTile", this.discoveryTileObject != null);
            res.put("artifactKey", this.artifactKey);
            res.put("explored", this.explored);
            res.put("planets", BgUtils.toMapList(this.planets));
            res.put("owner", this.owner == null ? -1 : this.owner.getPosition());
            res.put("units", BgUtils.toMapList(this.units));
        }
        return res;
    }
}
