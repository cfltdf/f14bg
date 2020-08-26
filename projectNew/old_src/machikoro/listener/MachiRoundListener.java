package com.f14.machikoro.listener;

import com.f14.bg.action.BgAction;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.ListenerType;
import com.f14.machikoro.MachiGameMode;
import com.f14.machikoro.MachiPlayer;
import com.f14.machikoro.card.MachiCard;
import com.f14.machikoro.consts.MachiAbilityType;
import com.f14.machikoro.consts.MachiConstCmd;
import com.f14.machikoro.consts.MachiIcon;
import com.f14.utils.DiceUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MachiRoundListener extends MachiOrderListener {

    public MachiRoundListener(MachiPlayer player) {
        super(player, ListenerType.NORMAL);
    }

    @Override
    protected void beforeStartListen(MachiGameMode gameMode) throws BoardGameException {
        super.beforeStartListen(gameMode);
        // 为所有玩家创建回合参数
        for (MachiPlayer p : gameMode.getGame().getPlayers()) {
            RoundParam param = new RoundParam();
            this.setParam(p.getPosition(), param);
        }
    }

    @Override
    protected void doAction(MachiGameMode gameMode, BgAction action) throws BoardGameException {
        super.doAction(gameMode, action);
        String subact = action.getAsString("subact");
        MachiPlayer player = action.getPlayer();
        RoundParam param = this.getParam(player.getPosition());
        int step = param.step;
        switch (step) {
            case 0:
                // 掷骰
                if ("ONE_DICE".equals(subact)) {
                    List<Integer> dicelist = new ArrayList<>();
                    dicelist.add(DiceUtils.roll("D6"));
                    player.dices = (Integer[]) dicelist.toArray();
                    if (player.hasAbility(MachiAbilityType.REROLL)) {
                        goStep(gameMode, player, 1);
                    } else {
                        gameMode.getGame().executeDice(player);
                        if (player.steps.size() > 0) {
                            goStep(gameMode, player, 6);
                        } else {
                            goStep(gameMode, player, 2);
                        }
                    }
                } else if ("TWO_DICE".equals(subact)) {
                    List<Integer> dicelist = new ArrayList<>();
                    dicelist.add(DiceUtils.roll("D6"));
                    dicelist.add(DiceUtils.roll("D6"));
                    player.dices = (Integer[]) dicelist.toArray();
                    if (player.hasAbility(MachiAbilityType.REROLL)) {
                        goStep(gameMode, player, 1);
                    } else {
                        gameMode.getGame().executeDice(player);
                        if (player.steps.size() > 0) {
                            goStep(gameMode, player, 6);
                        } else {
                            goStep(gameMode, player, 2);
                        }
                    }
                } else {
                    throw new BoardGameException("无效的指令!");
                }
                break;
            case 1:
                // 重掷
                if ("REROLL".equals(subact)) {
                    for (int i = 0; i < player.dices.length; ++i) {
                        player.dices[i] = DiceUtils.roll("D6");
                    }
                    gameMode.getGame().executeDice(player);
                    if (player.steps.size() > 0) {
                        goStep(gameMode, player, 6);
                    } else {
                        goStep(gameMode, player, 2);
                    }
                } else if ("NO_REROLL".equals(subact)) {
                    gameMode.getGame().executeDice(player);
                    if (player.steps.size() > 0) {
                        goStep(gameMode, player, 6);
                    } else {
                        goStep(gameMode, player, 2);
                    }
                } else {
                    throw new BoardGameException("无效的指令!");
                }
                break;
            case 2:
                // 购买
                if ("BUY".equals(subact)) {
                    String cardId = action.getAsString("cardId");
                    MachiCard card = gameMode.getCard(cardId);
                    if (card == null) {
                        throw new BoardGameException("找不到这张卡!");
                    }
                    gameMode.getGame().playerBuyCard(player, card);
                    if (gameMode.getGame().checkWin(player)) {
                        this.setAllPlayerResponsed(gameMode);
                    } else {
                        if (player.canOnceMore()) {
                            goStep(gameMode, player, 0);
                            player.dices = null;
                        } else {
                            this.setPlayerResponsed(gameMode, player);
                        }
                    }
                } else {
                    throw new BoardGameException("无效的指令!");
                }
                break;
            case 3:
                // 商业中心
                if ("SELECT_CARD".equals(subact)) {
                    String cardId = action.getAsString("cardId");
                    MachiCard card = gameMode.getCard(cardId);
                    if (card.icon == MachiIcon.LANDMARK) {
                        throw new BoardGameException("不能选择这张卡!");
                    }
                    Integer pos = action.getAsInt("targetPlayer");
                    MachiPlayer targetPlayer = gameMode.getGame().getPlayer(pos);
                    if (pos == player.getPosition()) {
                        throw new BoardGameException("不能选择自己!");
                    }
                    param.tmpCard = card;
                    param.tmpPlayer = targetPlayer;
                    goStep(gameMode, player, 4);
                } else if ("NO_SELECT".equals(subact)) {
                    if (player.steps.size() > 0) {
                        goStep(gameMode, player, 6);
                    } else {
                        goStep(gameMode, player, 2);
                    }
                } else {
                    throw new BoardGameException("无效的指令!");
                }
                break;
            case 4:
                // 换卡
                if ("SELECT_CARD".equals(subact)) {
                    String cardId = action.getAsString("cardId");
                    MachiCard card = gameMode.getCard(cardId);
                    if (card.icon == MachiIcon.LANDMARK) {
                        throw new BoardGameException("不能选择这张卡!");
                    }
                    Integer pos = action.getAsInt("targetPlayer");
                    MachiPlayer targetPlayer = gameMode.getGame().getPlayer(pos);
                    if (pos != player.getPosition()) {
                        throw new BoardGameException("只能能选择自己的卡!");
                    }
                    gameMode.getGame().exchangeCard(targetPlayer, card, param.tmpPlayer, param.tmpCard);
                    player.steps.remove(MachiAbilityType.EXCHANGE_BUILDING);
                    if (player.steps.size() > 0) {
                        goStep(gameMode, player, 6);
                    } else {
                        goStep(gameMode, player, 2);
                    }
                } else if ("CANCEL".equals(subact)) {
                    goStep(gameMode, player, 3);
                } else {
                    throw new BoardGameException("无效的指令!");
                }
                break;
            case 5:
                // 电视台
                if ("SELECT_PLAYER".equals(subact)) {
                    Integer pos = action.getAsInt("targetPlayer");
                    MachiPlayer targetPlayer = gameMode.getGame().getPlayer(pos);
                    gameMode.getGame().takeMoney(player, targetPlayer, 5);
                    player.steps.remove(MachiAbilityType.TAKE_ONE);
                    if (player.steps.size() > 0) {
                        goStep(gameMode, player, 6);
                    } else {
                        goStep(gameMode, player, 2);
                    }
                } else {
                    throw new BoardGameException("无效的指令!");
                }
                break;
            case 6:
                // 选择
                if ("EXCHANGE_CARD".equals(subact)) {
                    goStep(gameMode, player, 3);
                } else if ("TAKE_ONE_PLAYER".equals(subact)) {
                    goStep(gameMode, player, 5);
                } else if ("TAKE_OTHERS".equals(subact)) {
                    player.steps.remove(MachiAbilityType.TAKE_OTHER);
                    if (player.steps.size() > 0) {
                        goStep(gameMode, player, 6);
                    } else {
                        goStep(gameMode, player, 2);
                    }
                } else {
                    throw new BoardGameException("无效的指令!");
                }
                break;
        }
    }

    @Override
    protected int getValidCode() {
        return MachiConstCmd.GAME_CODE_ROUND;
    }

    private void goStep(MachiGameMode gameMode, MachiPlayer player, int step) {
        RoundParam param = this.getParam(player.getPosition());
        switch (step) {
            case 0:
                // 掷骰
                gameMode.getGame().sendPlayerRollRequest(player);
                return;
            case 1:
                // 重掷
                gameMode.getGame().sendPlayerRerollRequest(player);
                return;
            case 2:
                // 购买
                gameMode.getGame().sendPlayerBuyRequest(player);
                return;
            case 3:
                // 商业中心
                gameMode.getGame().sendPlayerExchangeRequest(player);
                return;
            case 4:
                // 换卡
                gameMode.getGame().sendPlayerExchangeForRequest(player);
                return;
            case 5:
                // 电视台
                gameMode.getGame().sendPlayerSelectPlayerRequest(player);
                return;
            case 6:
                // 选择
                gameMode.getGame().sendPlayerSelectActionRequest(player);
                return;
        }
        param.step = step;
    }

    @Override
    protected void onPlayerTurn(MachiGameMode gameMode, MachiPlayer player) throws BoardGameException {
        player.dices = null;
        player.steps = new HashSet<>();
        this.goStep(gameMode, player, 0);
    }

    class RoundParam {
        int step;
        MachiCard tmpCard;
        MachiPlayer tmpPlayer;

        public RoundParam() {
            step = 0;
        }
    }
}
