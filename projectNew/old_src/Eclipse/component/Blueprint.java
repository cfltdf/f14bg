package com.f14.Eclipse.component;

import com.f14.Eclipse.EclipseGameMode;
import com.f14.Eclipse.consts.BlueprintType;
import com.f14.Eclipse.consts.ShipProperty;
import com.f14.Eclipse.consts.UnitType;
import com.f14.Eclipse.manager.ShipPartManager;
import com.f14.bg.component.Convertable;
import com.f14.bg.utils.BgUtils;
import com.f14.utils.ArrayUtil;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 蓝图
 *
 * @author f14eagle
 */
public class Blueprint implements Cloneable, Convertable {
    public UnitType shipType;
    public BlueprintType blueprintType;

    protected EclipseShipProperty defaultProperty = new EclipseShipProperty();

    protected EclipseShipProperty property = new EclipseShipProperty();
    protected int[] defaultPartIndex = new int[0];
    protected ShipPart[] defaultShipParts = new ShipPart[0];
    protected ShipPart[] shipParts = new ShipPart[0];

    protected List<Weapon> defaultWeapons = new ArrayList<>();

    protected List<Weapon> weapons = new ArrayList<>();

    /**
     * 将飞船配件添加到指定的位置,并重新计算飞船的属性
     *
     * @param o
     * @param position
     */
    public void addShipPart(ShipPart o, int position) {
        this.shipParts[position] = o;
        this.calculateShipProperty();
    }

    /**
     * 设置飞船配件,并重新计算飞船的属性
     *
     * @param parts
     */
    public void addShipParts(ShipPart[] parts) {
        System.arraycopy(parts, 0, this.shipParts, 0, this.shipParts.length);
        this.calculateShipProperty();
    }

    /**
     * 计算飞船的属性
     */
    protected void calculateShipProperty() {
        this.property.clear();
        // 飞船的默认属性
        this.property.addProperties(this.defaultProperty);
        // 飞船的配件属性
        for (ShipPart o : this.shipParts) {
            if (o != null) {
                this.property.addProperties(o.property);
            }
        }
    }


    @Override
    public Blueprint clone() {
        try {
            Blueprint res = (Blueprint) super.clone();
            res.defaultProperty = new EclipseShipProperty();
            res.defaultProperty.addProperties(this.defaultProperty);
            res.property = new EclipseShipProperty();
            res.property.addProperties(this.property);
            res.defaultPartIndex = ArrayUtil.cloneArray(this.defaultPartIndex);
            res.defaultShipParts = new ShipPart[0];
            res.shipParts = new ShipPart[0];
            res.defaultWeapons = new ArrayList<>();
            res.defaultWeapons.addAll(this.defaultWeapons);
            res.weapons = new ArrayList<>();
            res.weapons.addAll(this.weapons);
            return res;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public BlueprintType getBlueprintType() {
        return blueprintType;
    }

    public void setBlueprintType(BlueprintType blueprintType) {
        this.blueprintType = blueprintType;
    }

    public int[] getDefaultPartIndex() {
        return defaultPartIndex;
    }

    public void setDefaultPartIndex(int[] defaultPartIndex) {
        this.defaultPartIndex = defaultPartIndex;
    }


    public EclipseShipProperty getDefaultProperty() {
        return defaultProperty;
    }

    /**
     * 取得指定位置的默认部件
     *
     * @param index
     * @return
     */
    public ShipPart getDefaultShipPart(int index) {
        return this.defaultShipParts[index];
    }

    public ShipPart[] getDefaultShipParts() {
        return defaultShipParts;
    }

    public void setDefaultShipParts(ShipPart[] defaultShipParts) {
        this.defaultShipParts = defaultShipParts;
    }


    public List<Weapon> getDefaultWeapons() {
        return defaultWeapons;
    }

    public void setDefaultWeapons(List<Weapon> defaultWeapons) {
        this.defaultWeapons = new ArrayList<>();
        if (defaultWeapons != null) {
            for (Object o : defaultWeapons) {
                Weapon a = (Weapon) JSONObject.toBean(JSONObject.fromObject(o), Weapon.class);
                this.defaultWeapons.add(a);
            }
        }
    }

    /**
     * 取得单位属性
     *
     * @param property
     * @return
     */
    public int getProperty(ShipProperty property) {
        return this.property.getProperty(property);
    }

    /**
     * 取得指定位置的飞船部件
     *
     * @param positionIndex
     * @return
     */
    public ShipPart getShipPart(int positionIndex) {
        return this.shipParts[positionIndex];
    }

    /**
     * 按照id取得飞船部件
     *
     * @param id
     * @return
     */

    public ShipPart getShipPart(String id) {
        for (ShipPart o : this.shipParts) {
            if (o != null && o.id.equals(id)) {
                return o;
            }
        }
        return null;
    }

    /**
     * 取得飞船部件所在的index
     *
     * @param o
     * @return
     */
    public int getShipPartIndex(ShipPart o) {
        for (int i = 0; i < this.shipParts.length; i++) {
            if (this.shipParts[i] == o) {
                return i;
            }
        }
        return -1;
    }

    public ShipPart[] getShipParts() {
        return shipParts;
    }

    public void setShipParts(ShipPart[] shipParts) {
        this.shipParts = shipParts;
    }

    public UnitType getShipType() {
        return shipType;
    }

    public void setShipType(UnitType shipType) {
        this.shipType = shipType;
    }


    public List<Weapon> getWeapons() {
        return weapons;
    }

    /**
     * 装载默认的飞船配件,将id转换成配件对象
     *
     * @param gameMode
     */
    public void loadDefaultShipParts(EclipseGameMode gameMode) {
        ShipPartManager m = gameMode.getShipPartManager();
        // 创建默认的shipPart
        this.defaultShipParts = new ShipPart[this.defaultPartIndex.length];
        this.shipParts = new ShipPart[this.defaultPartIndex.length];
        for (int i = 0; i < this.defaultPartIndex.length; i++) {
            ShipPart p = m.createShipPart(this.defaultPartIndex[i]);
            this.defaultShipParts[i] = p;
        }
        // 将默认的shipPart添加到蓝图中
        for (int i = 0; i < this.defaultShipParts.length; i++) {
            this.addShipPart(this.defaultShipParts[i], i);
        }
    }

    public void setDefaultShipProperty(Map<String, Integer> defaultProperty) {
        this.defaultProperty = new EclipseShipProperty();
        if (defaultProperty != null) {
            for (String key : defaultProperty.keySet()) {
                ShipProperty p = ShipProperty.valueOf(key);
                this.defaultProperty.setProperty(p, defaultProperty.get(key));
            }
        }
    }


    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> res = new HashMap<>();
        res.put("shipType", this.shipType);
        res.put("blueprintType", this.blueprintType);
        res.put("defaultProperty", this.defaultProperty.getAllProperties());
        res.put("shipParts", BgUtils.toMapList(this.shipParts));
        return res;
    }
}
