package com.f14.TTA.listener.event;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.ability.EventAbility;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.card.TechCard;
import com.f14.TTA.component.param.PopParam;
import com.f14.TTA.consts.CardSubType;
import com.f14.TTA.consts.CardType;
import com.f14.TTA.consts.TTACmdString;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.TTA.listener.TTAEventListener;
import com.f14.bg.action.BgAction;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.CheckUtils;
import com.f14.utils.CommonUtil;

import java.util.HashMap;
import java.util.Map;

public class InquisitionListener extends TTAEventListener {

    public InquisitionListener(EventAbility ability, TTAPlayer trigPlayer) {
        super(ability, trigPlayer);
    }

    @Override
    protected boolean beforeListeningCheck(TTAGameMode gameMode, TTAPlayer p) {
        return !this.canPass(p);
    }

    @Override
    protected void beforeStartListen(TTAGameMode gameMode) throws BoardGameException {
        super.beforeStartListen(gameMode);
        // 为所有玩家创建人口参数
        gameMode.inquisitionPosition = this.trigPlayer.getPosition();
        for (TTAPlayer player : gameMode.getRealPlayers()) {
            int limit = player.goverment.buildingLimit;
            InquisitionParam param = new InquisitionParam();
            for (CardSubType subType : new CardSubType[]{CardSubType.LAB, CardSubType.LIBRARY}) {
                int shouldDestroy = Math.max(0, player.getBuildingNumber(subType) - limit + 1);
                param.shouldDestroys.put(subType, shouldDestroy);
                for (TTACard c : player.getBuildingsBySubType(subType)) {
                    if (c.getAvailableCount() == limit) {
                        try {
                            param.destory((TechCard) c);
                            gameMode.getGame().refreshDecreasePopulation(player, (TechCard) c, 1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            this.setParam(player.getPosition(), param);
        }
    }

    protected boolean canPass(TTAPlayer player) {
        InquisitionParam param = this.getParam(player);
        for (Integer n : param.shouldDestroys.values()) {
            if (n != 0) {
                return false;
            }
        }
        return true;
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam res = new InterruptParam();
        for (TTAPlayer p : this.listeningPlayers) {
            InquisitionParam param = this.getParam(p);
            res.set(p.getPosition(), param.param);
        }
        return res;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        InquisitionParam param = this.getParam(player);
        boolean confirm = action.getAsBoolean("confirm");
        if (confirm) {
            String cardId = action.getAsString("cardId");
            TTACard card = player.getBuildings().getCard(cardId);
            CheckUtils.check(card.cardType != CardType.BUILDING, "不能选择这张牌!");
            CheckUtils.check(card.cardSubType != CardSubType.LIBRARY && card.cardSubType != CardSubType.LAB,
                    "不能选择这张牌!");
            param.destory((TechCard) card);
            gameMode.getGame().refreshDecreasePopulation(player, (TechCard) card, 1);
            if (this.canPass(player)) {
                this.setPlayerResponsed(gameMode, player.getPosition());
            }
        } else {
            // 判断玩家是否可以结束
            CheckUtils.check(!this.canPass(player), this.getMsg(player));
            this.setPlayerResponsed(gameMode, player.getPosition());
        }
    }


    @Override
    protected String getActionString() {
        return TTACmdString.ACTION_DESTORY;
    }

    @Override
    protected String getMsg(TTAPlayer player) {
        InquisitionParam param = this.getParam(player);
        String msg = "你需要摧毁{0}个图书馆和{1}个实验室,请选择!";
        msg = CommonUtil.getMsg(msg, param.shouldDestroys.get(CardSubType.LIBRARY),
                param.shouldDestroys.get(CardSubType.LAB));
        return msg;
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_EVENT_DESTORY;
    }

    class InquisitionParam {

        public Map<CardSubType, Integer> shouldDestroys = new HashMap<>();

        public PopParam param = new PopParam();

        /**
         * 拆除建筑
         *
         * @param card
         * @throws BoardGameException
         */
        void destory(TechCard card) throws BoardGameException {
            CheckUtils.check(card.getWorkers() == 0, "这张牌上没有工人!");
            Integer shouldDestroy = shouldDestroys.get(card.cardSubType);
            if (shouldDestroy != null && shouldDestroy > 0) {
                shouldDestroy -= 1;
                shouldDestroys.put(card.cardSubType, shouldDestroy);
                param.destory(card);
                return;
            }
            throw new BoardGameException("不能选择这张牌!");
        }

    }

}
