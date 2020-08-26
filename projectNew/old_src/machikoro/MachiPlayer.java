package com.f14.machikoro;

import com.f14.bg.player.Player;
import com.f14.machikoro.card.MachiCard;
import com.f14.machikoro.consts.MachiAbilityType;
import com.f14.machikoro.consts.MachiColor;

import java.util.*;

public class MachiPlayer extends Player {
    public int coins;
    public MachiCard[] landmarks;
    public List<MachiCard> buildings;

    public Integer[] dices;
    public Set<MachiAbilityType> steps;

    public void addCard(MachiCard card) {
        if (card.color == MachiColor.ORANGE) {
            for (int i = 0; i < landmarks.length; ++i) {
                if (landmarks[i].cardNo.equals(card.replaceCardNo)) {
                    landmarks[i] = card;
                }
            }
        } else {
            buildings.add(card);
        }
    }

    public int addCoin(int num) {
        if (coins + num > 0) {
            coins += num;
        } else {
            num = -coins;
            coins = 0;
        }
        return num;
    }

    public boolean canOnceMore() {
        return this.hasAbility(MachiAbilityType.ONE_MORE_TURN) && this.dices.length == 2
                && Objects.equals(this.dices[0], this.dices[1]);
    }

    public int getMaxScore() {
        return landmarks.length;
    }

    public int getScore() {
        int score = 0;
        for (MachiCard c : landmarks) {
            if (c.color == MachiColor.ORANGE) {
                score++;
            }
        }
        return score;
    }

    public boolean hasAbility(MachiAbilityType ability) {
        for (MachiCard c : landmarks) {
            if (c.abilityType == ability) {
                return true;
            }
        }
        for (MachiCard c : buildings) {
            if (c.abilityType == ability) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCard(MachiCard card) {
        for (MachiCard c : landmarks) {
            if (c.cardNo.equals(card.cardNo)) {
                return true;
            }
        }
        for (MachiCard c : buildings) {
            if (c.cardNo.equals(card.cardNo)) {
                return true;
            }
        }
        return false;
    }


    public MachiCard removeCard(String cardId) {
        Iterator<MachiCard> it = buildings.iterator();
        while (it.hasNext()) {
            MachiCard c = it.next();
            if (Objects.equals(c.id, cardId)) {
                it.remove();
                return c;
            }
        }
        return null;
    }


    public List<MachiCard> allCards() {
        List<MachiCard> res = new ArrayList<>();
        Collections.addAll(res, landmarks);
        res.addAll(buildings);
        return res;
    }
}
