package com.f14.TTA.listener.event;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.ability.EventAbility;
import com.f14.TTA.component.card.CivilCard;
import com.f14.TTA.component.card.GovermentCard;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.param.RoundParam;
import com.f14.TTA.consts.CardType;
import com.f14.TTA.consts.TTACmdString;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.TTA.executor.*;
import com.f14.TTA.listener.TTAEventListener;
import com.f14.bg.action.BgAction;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.ActionStep;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.CheckUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 新版文明发展监听器
 *
 * @author F14eagle
 */
public class DevOfCivListener extends TTAEventListener {

    private Map<TTAPlayer, TTAActionExecutor> choices = new HashMap<>();

    public DevOfCivListener(EventAbility ability, TTAPlayer trigPlayer) {
        super(ability, trigPlayer);
    }

    @Override
    protected void beforeStartListen(TTAGameMode gameMode) throws BoardGameException {
        super.beforeStartListen(gameMode);
    }

    private void build(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        RoundParam param = this.getParam(player.getPosition());
        // 省1资源建造1个新的城市建筑
        String cardId = action.getAsString("cardId");
        CivilCard card = (CivilCard) player.getPlayedCard(cardId);
        CheckUtils.check(card.cardType != CardType.BUILDING && card.cardType != CardType.PRODUCTION, "只能建造农场、矿山或城市建筑!");
        TTABuildCivilExecutor executor = new TTABuildCivilExecutor(param, card);
        executor.actionCost = 0;
        executor.costModify = -1;
        executor.check();
        choices.put(player, executor);
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        for (TTAPlayer player : this.listeningPlayers) {
            TTAActionExecutor executor = choices.getOrDefault(player, null);
            param.set(player.getPosition(), executor);
        }
        return param;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        boolean confirm = action.getAsBoolean("confirm");
        if (confirm) {
            String subact = action.getAsString("subact");
            if (TTACmdString.ACTION_POPULATION.equals(subact)) {
                this.increasePop(gameMode, action);
            } else if (TTACmdString.ACTION_PLAY_CARD.equals(subact)) {
                if (this.playTechCard(gameMode, action)) {
                    return;
                }
            } else if (TTACmdString.ACTION_DEV_OF_CIV.equals(subact)) {
                this.build(gameMode, action);
            } else if ("cancel".equals(subact)) {
                // Do nothing
            } else {
                throw new BoardGameException("不能执行此操作!");
            }
        }
        // 成功或取消后,设置玩家完成回应
        this.setPlayerResponsed(gameMode, player.getPosition());
    }


    @Override
    protected String getActionString() {
        return TTACmdString.ACTION_DEV_OF_CIV;
    }


    @Override
    protected String getMsg(TTAPlayer player) {
        return "你可以: 省1科技打出1张科技牌 / 省1粮食扩张人口 / 省1资源建造1个新的农场、矿山或城市建筑";
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_EVENT_DESTORY;
    }

    private void increasePop(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        RoundParam param = this.getParam(player.getPosition());
        TTAIncreasePopExecutor executor = new TTAIncreasePopExecutor(param);
        executor.actionCost = 0;
        executor.costModify = -1;
        executor.check();
        choices.put(player, executor);
    }

    private boolean playTechCard(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        RoundParam param = this.getParam(player.getPosition());
        // 省1科技打出1张科技牌
        String cardId = action.getAsString("cardId");
        TTACard card = player.getCard(cardId);
        CheckUtils.check(!card.isTechnologyCard, "只能打出科技牌!");
        if (card.cardType == CardType.GOVERMENT) {
            // 政体可以此时革命或和平演变
            ChangeGovernmentStep step = new ChangeGovernmentStep((GovermentCard) card);
            this.addActionStep(gameMode, player, step);
            return true;
        }
        TTAPlayTechCardExecutor executor = new TTAPlayTechCardExecutor(param, card);
        executor.actionCost = 0;
        executor.costModify = -1;
        executor.check();
        choices.put(player, executor);
        return false;
    }

    class ChangeGovernmentStep extends ActionStep<TTAPlayer, TTAGameMode> {
        private GovermentCard card;
        private boolean fullfilled = false;

        public ChangeGovernmentStep(GovermentCard card) {
            super();
            this.card = card;
        }

        private void changeGovernment(TTAGameMode gameMode, BgAction action) throws BoardGameException {
            TTAPlayer player = action.getPlayer();
            RoundParam param = DevOfCivListener.this.getParam(player.getPosition());
            String cardId = action.getAsString("showCardId");
            GovermentCard card = (GovermentCard) player.getCard(cardId);
            int sel = action.getAsInt("sel");
            // 是否以革命的方式改变政府
            if (sel < 0) {
                return;
            }
            TTAChangeGovermentExecutor executor = new TTAChangeGovermentExecutor(param, card, sel);
            executor.actionCost = 0;
            executor.costModify = -1;
            executor.check();
            choices.put(player, executor);
            this.fullfilled = true;
        }

        @Override
        protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
            boolean confirm = action.getAsBoolean("confirm");
            if (confirm) {
                String subact = action.getAsString("subact");
                if (TTACmdString.REQUEST_SELECT.equals(subact)) {
                    this.changeGovernment(gameMode, action);
                } else if ("cancel".equals(subact)) {
                    // Do nothing
                } else {
                    throw new BoardGameException("不能执行此操作!");
                }
            }
        }

        @Override
        public int getActionCode() {
            return TTAGameCmd.GAME_CODE_CHANGE_GOVERMENT;
        }


        @Override
        public String getStepCode() {
            return TTACmdString.ACTION_CHANGE_GOVERMENT;
        }

        @Override
        protected void onStepOver(TTAGameMode gameMode, TTAPlayer player) throws BoardGameException {
            super.onStepOver(gameMode, player);
            gameMode.getGame().playerRequestEnd(player);
            if (this.fullfilled) {
                DevOfCivListener.this.setPlayerResponsed(gameMode, player);
            }
        }

        @Override
        protected void onStepStart(TTAGameMode gameMode, TTAPlayer player) throws BoardGameException {
            super.onStepStart(gameMode, player);
            Map<String, Object> param = new HashMap<>();
            param.put("subact", TTACmdString.ACTION_CHANGE_GOVERMENT);
            param.put("sel", "革命,和平演变");
            gameMode.getGame().sendPlayerActionRequestResponse(player, TTACmdString.REQUEST_SELECT,
                    "请选择要更换政府的方式", card, param);
        }
    }
}
