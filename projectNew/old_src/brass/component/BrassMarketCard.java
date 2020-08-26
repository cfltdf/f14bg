package com.f14.brass.component;

import com.f14.bg.component.Card;


public class BrassMarketCard extends Card {
    public int value;


    @Override
    public BrassMarketCard clone() {
        return (BrassMarketCard) super.clone();
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
