package com.f14.Eclipse.component;

import com.f14.Eclipse.EclipsePlayer;
import com.f14.Eclipse.consts.ShipProperty;
import com.f14.Eclipse.consts.UnitType;
import com.f14.bg.component.Convertable;

import java.util.HashMap;
import java.util.Map;

public class Unit implements Convertable, Cloneable {
    protected int id;
    protected String unitCode;
    protected EclipsePlayer owner;
    protected UnitType unitType;
    protected Blueprint blueprint;

    /**
     * 检查该unit是否可以移动
     *
     * @return
     */
    public boolean canMove() {
        return this.blueprint.getProperty(ShipProperty.MOVEMENT) > 0;
    }


    @Override
    public Unit clone() {
        try {
            Unit res = (Unit) super.clone();
            res.blueprint = this.blueprint.clone();
            return res;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public Blueprint getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(Blueprint blueprint) {
        this.blueprint = blueprint;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EclipsePlayer getOwner() {
        return owner;
    }

    public void setOwner(EclipsePlayer owner) {
        this.owner = owner;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }


    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> res = new HashMap<>();
        res.put("id", this.id);
        res.put("owner", owner == null ? -1 : owner.getPosition());
        res.put("unitCode", this.unitCode);
        res.put("unitType", this.unitType);
        return res;
    }
}
