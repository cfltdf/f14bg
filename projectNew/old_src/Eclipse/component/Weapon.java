package com.f14.Eclipse.component;

import com.f14.Eclipse.consts.DamageDice;
import com.f14.Eclipse.consts.WeaponType;

import java.util.ArrayList;
import java.util.List;

/**
 * 武器
 *
 * @author f14eagle
 */
public class Weapon {
    public WeaponType weaponType;
    public List<DamageDice> damageDice = new ArrayList<>();

    public List<DamageDice> getDamageDice() {
        return damageDice;
    }

    public void setDamageDice(List<DamageDice> damageDice) {
        this.damageDice = damageDice;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public void setWeaponType(WeaponType weaponType) {
        this.weaponType = weaponType;
    }
}
