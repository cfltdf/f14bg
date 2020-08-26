package com.f14.machikoro;

import com.f14.RFTG.network.CmdFactory;
import com.f14.bg.BoardGame;
import com.f14.bg.action.BgResponse;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.report.BgReport;
import com.f14.bg.utils.BgUtils;
import com.f14.machikoro.card.MachiCard;
import com.f14.machikoro.consts.MachiAbilityType;
import com.f14.machikoro.consts.MachiConstCmd;
import com.f14.machikoro.consts.MachiIcon;
import net.sf.json.JSONObject;

import java.util.List;

public class MachiKoro extends BoardGame<MachiPlayer, MachiGameMode> {

    public boolean checkWin(MachiPlayer player) {
        if (player.getScore() == player.getMaxScore()) {
            this.winGame();
            return true;
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    @Override
    protected MachiConfig createConfig(JSONObject object) throws BoardGameException {
        return new MachiConfig();
    }

    public void exchangeCard(MachiPlayer player1, MachiCard card1, MachiPlayer player2, MachiCard card2) {
        this.playerRemoveCard(player2, card1);
        this.playerRemoveCard(player2, card2);
        this.playerAddCard(player1, card2);
        this.playerAddCard(player2, card1);
    }

    public void executeDice(MachiPlayer player) {
    }

    @Override
    public void initConfig() {
        this.config = new MachiConfig();
    }

    @Override
    public void initConst() {
    }

    @Override
    public void initReport() {
        this.report = new BgReport(this);
    }

    private void playerAddCard(MachiPlayer player, MachiCard card) {
        player.addCard(card);
        this.sendPlayerAddCardResponse(player, BgUtils.toList(card), null);
    }

    private int playerAddCoin(MachiPlayer player, int num) {
        int coin = player.addCoin(num);
        this.sendPlayerPointResponse(player, null);
        return coin;
    }

    public void playerBuyCard(MachiPlayer player, MachiCard card) throws BoardGameException {
        if (player.coins < card.cost) {
            throw new BoardGameException("你没有足够的金币!");
        }
        if (card.icon == MachiIcon.LANDMARK && player.hasCard(card)) {
            throw new BoardGameException("你已经有这张卡!");
        }
        MachiCard c = this.gameMode.buyCard(card.id);
        if (c == null) {
            throw new BoardGameException("不存在的卡!");
        }
        this.playerAddCoin(player, -card.cost);
        this.playerAddCard(player, card);
    }

    private void playerRemoveCard(MachiPlayer player, MachiCard card) {
        player.removeCard(card.id);
        this.sendPlayerRemoveCardResponse(player, card, null);
    }

    @Override
    protected void sendGameInfo(MachiPlayer receiver) throws BoardGameException {
        BgResponse res = CmdFactory.createGameResponse(MachiConstCmd.GAME_CODE_BASE_INFO, -1);
        res.setPublicParameter("cards", BgUtils.card2String(gameMode.allCards));
    }

    @Override
    protected void sendInitInfo(MachiPlayer receiver) throws BoardGameException {
        // TODO Auto-generated method stub
    }

    private void sendPlayerAddCardResponse(MachiPlayer player, List<MachiCard> cards, MachiPlayer receiver) {
        BgResponse res = CmdFactory.createGameResponse(MachiConstCmd.GAME_CODE_ADD_CARD, player.getPosition());
        res.setPublicParameter("cards", BgUtils.card2String(cards));
        this.sendResponse(receiver, res);
    }

    public void sendPlayerBuyRequest(MachiPlayer player) {
        BgResponse res = CmdFactory.createGameResponse(MachiConstCmd.GAME_CODE_ACTION_REQUEST, player.getPosition());
        res.setPublicParameter("cmdString", "BUY_REQUEST");
        this.sendResponse(player, res);
    }

    public void sendPlayerExchangeForRequest(MachiPlayer player) {
        BgResponse res = CmdFactory.createGameResponse(MachiConstCmd.GAME_CODE_ACTION_REQUEST, player.getPosition());
        res.setPublicParameter("cmdString", "EXCHANGE_FOR_REQUEST");
        this.sendResponse(player, res);
    }

    public void sendPlayerExchangeRequest(MachiPlayer player) {
        BgResponse res = CmdFactory.createGameResponse(MachiConstCmd.GAME_CODE_ACTION_REQUEST, player.getPosition());
        res.setPublicParameter("cmdString", "EXCHANGE_REQUEST");
        this.sendResponse(player, res);
    }

    @Override
    protected void sendPlayerPlayingInfo(MachiPlayer receiver) throws BoardGameException {
        for (MachiPlayer p : this.getPlayers()) {
            this.sendPlayerAddCardResponse(p, p.allCards(), receiver);
        }
    }

    private void sendPlayerPointResponse(MachiPlayer player, MachiPlayer receiver) {
        BgResponse res = CmdFactory.createGameResponse(MachiConstCmd.GAME_CODE_PLAYER_INFO, player.getPosition());
        res.setPublicParameter("coin", player.coins);
        this.sendResponse(receiver, res);

    }

    private void sendPlayerRemoveCardResponse(MachiPlayer player, MachiCard card, MachiPlayer receiver) {
        BgResponse res = CmdFactory.createGameResponse(MachiConstCmd.GAME_CODE_REMOVE_CARD, player.getPosition());
        res.setPublicParameter("card", card.id);
        this.sendResponse(receiver, res);
    }

    public void sendPlayerRerollRequest(MachiPlayer player) {
        BgResponse res = CmdFactory.createGameResponse(MachiConstCmd.GAME_CODE_ACTION_REQUEST, player.getPosition());
        res.setPublicParameter("cmdString", "REROLL_REQUEST");
        res.setPublicParameter("dices", player.dices);
        this.sendResponse(player, res);
    }

    public void sendPlayerRollRequest(MachiPlayer player) {
        BgResponse res = CmdFactory.createGameResponse(MachiConstCmd.GAME_CODE_ACTION_REQUEST, player.getPosition());
        res.setPublicParameter("cmdString", "ROLL_REQUEST");
        res.setPublicParameter("twoDice", player.hasAbility(MachiAbilityType.TWO_DICE));
        this.sendResponse(player, res);
    }

    public void sendPlayerSelectActionRequest(MachiPlayer player) {
        BgResponse res = CmdFactory.createGameResponse(MachiConstCmd.GAME_CODE_ACTION_REQUEST, player.getPosition());
        res.setPublicParameter("cmdString", "SELECT_ACTION_REQUEST");
        res.setPublicParameter("actions", player.steps.toArray());
        this.sendResponse(player, res);
    }

    public void sendPlayerSelectPlayerRequest(MachiPlayer player) {
        BgResponse res = CmdFactory.createGameResponse(MachiConstCmd.GAME_CODE_ACTION_REQUEST, player.getPosition());
        res.setPublicParameter("cmdString", "SELECT_PLAYER_REQUEST");
        this.sendResponse(player, res);
    }

    @Override
    protected void setupGame() throws BoardGameException {
        getLog().info("设置游戏...");
        int num = this.getCurrentPlayerNumber();
        getLog().info("游戏人数: " + num);
        this.config.playerNumber = num;
        this.gameMode = new MachiGameMode(this);
    }

    public void takeMoney(MachiPlayer player, MachiPlayer targetPlayer, int i) {
        int num = this.playerAddCoin(targetPlayer, -i);
        this.playerAddCoin(player, num);
    }

}
