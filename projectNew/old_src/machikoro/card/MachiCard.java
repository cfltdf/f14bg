package com.f14.machikoro.card;

import com.f14.bg.component.Card;
import com.f14.machikoro.consts.MachiAbilityType;
import com.f14.machikoro.consts.MachiColor;
import com.f14.machikoro.consts.MachiIcon;

import java.util.ArrayList;
import java.util.List;

public class MachiCard extends Card {
    public MachiColor color;
    public MachiIcon icon;
    public boolean start;
    public MachiAbilityType abilityType;

    public int amount;

    public int minDice;
    public int maxDice;

    public List<MachiIcon> adjustIcons;
    public int cost;
    public String replaceCardNo;

    public MachiAbilityType getAbilityType() {
        return abilityType;
    }

    public void setAbilityType(MachiAbilityType abilityType) {
        this.abilityType = abilityType;
    }


    public List<MachiIcon> getAdjustIcons() {
        return adjustIcons;
    }

    public void setAdjustIcons(List<MachiIcon> adjustIcons) {
        if (adjustIcons != null) {
            this.adjustIcons = new ArrayList<>();
            for (Object o : adjustIcons) {
                MachiIcon i = MachiIcon.valueOf(o.toString());
                this.adjustIcons.add(i);
            }
        } else {
            this.adjustIcons = null;
        }
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public MachiColor getColor() {
        return color;
    }

    public void setColor(MachiColor color) {
        this.color = color;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public MachiIcon getIcon() {
        return icon;
    }

    public void setIcon(MachiIcon icon) {
        this.icon = icon;
    }

    public int getMaxDice() {
        return maxDice;
    }

    public void setMaxDice(int maxDice) {
        this.maxDice = maxDice;
    }

    public int getMinDice() {
        return minDice;
    }

    public void setMinDice(int minDice) {
        this.minDice = minDice;
    }

    public String getReplaceCardNo() {
        return replaceCardNo;
    }

    public void setReplaceCardNo(String replaceCardNo) {
        this.replaceCardNo = replaceCardNo;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }

}
