package com.f14.machikoro;

import com.f14.bg.GameMode;
import com.f14.bg.exception.BoardGameException;
import com.f14.machikoro.card.MachiCard;
import com.f14.machikoro.consts.MachiColor;
import com.f14.machikoro.listener.MachiRoundListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MachiGameMode extends GameMode<MachiPlayer> {
    protected MachiKoro game;
    protected List<MachiCard> allCards;
    protected MachiPlayer startPlayer;

    public MachiGameMode(MachiKoro machikoro) {
        this.game = machikoro;
        this.init();
    }


    public MachiCard getCard(String cardId) {
        for (MachiCard c : this.allCards) {
            if (c.id.equals(cardId)) {
                return c;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MachiKoro getGame() {
        return this.game;
    }

    @Override
    protected boolean isGameOver() {
        return false;
    }

    @Override
    protected void round() throws BoardGameException {
        this.waitForRoundAction();
    }

    @Override
    protected void setupGame() throws BoardGameException {
        MachiResourceManager rm = this.game.getResourceManager();
        for (MachiPlayer p : this.game.getPlayers()) {
            List<MachiCard> landmarks = new ArrayList<>();
            p.buildings = new ArrayList<>();
            for (MachiCard c : rm.startCards.get(p.getPosition())) {
                if (c.color == MachiColor.GRAY) {
                    landmarks.add(c);
                } else {
                    p.buildings.add(c);
                }
            }
            p.landmarks = (MachiCard[]) landmarks.toArray();
        }
        allCards = new ArrayList<>();
        allCards.addAll(rm.cards);
        this.startPlayer = this.game.getPlayers().get(0);
    }

    private void waitForRoundAction() throws BoardGameException {
        this.addListener(new MachiRoundListener(this.startPlayer));
    }


    public MachiCard buyCard(String id) {
        Iterator<MachiCard> it = allCards.iterator();
        while (it.hasNext()) {
            MachiCard res = it.next();
            if (res.id.equals(id)) {
                it.remove();
                return res;
            }
        }
        return null;
    }

}
