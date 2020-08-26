package com.f14.TTA.listener;

import com.f14.TTA.TTA;
import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.param.RoundParam;
import com.f14.TTA.consts.*;
import com.f14.TTA.executor.*;
import com.f14.TTA.executor.active.TTAActiveExecutor;
import com.f14.TTA.factory.TTAExecutorFactory;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.utils.CheckUtils;


/**
 * TTA玩家回合监听器
 *
 * @author F14eagle
 */
public class TTARoundListener extends TTAOrderListener {
    public PoliticalAction politicalAction;

    public TTARoundListener() {
        this.politicalAction = new PoliticalAction();
    }

    /**
     * 玩家使用卡牌的能力
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected void activeCard(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        String cardId = action.getAsString("cardId");
        RoundParam param = this.getParam(player.getPosition());
        TTACard card = player.getPlayedCard(cardId);
        // 检查玩家是否可以使用该卡牌
        CheckUtils.checkNull(card.activeAbility, "该卡牌没有可以使用的能力!");
        card.activeAbility.checkCanActive(param.currentStep, player);

        TTAActiveExecutor executor = TTAExecutorFactory.createActiveExecutor(param, card);
        executor.execute();
        if (executor.actived && card.activeAbility.endPhase) {
            if (card.activeAbility.activeStep == RoundStep.POLITICAL) {
                this.politicalAction.endPoliticalPhase(gameMode, player, false);
            } else {
                this.endActionPhase(gameMode, player);
            }
        }
    }

    @Override
    protected boolean beforeListeningCheck(TTAGameMode gameMode, TTAPlayer player) {
        // 检测是否投降

        // 结束宗教裁判所
        if (gameMode.inquisitionPosition == player.getPosition()) {
            gameMode.inquisitionPosition = -1;
        }

        RoundParam param = this.getParam(player);
        if (player.getResigned()) {
            return false;
        } else if (gameMode.getGame().getConfig().mode == TTAMode.SIMPLE) {
            // 简单模式跳过政治行动阶段
            param.currentStep = RoundStep.NORMAL;
        } else if (gameMode.getRound() == 2) {
            // 第二回合无政治行动阶段
            param.currentStep = RoundStep.NORMAL;
        } else {
            param.currentStep = RoundStep.POLITICAL;
        }
        return gameMode.getGame().getRealPlayerNumber() > gameMode.getGame().getRequiredPlayerNumber();
    }

    @Override
    protected void beforeStartListen(TTAGameMode gameMode) throws BoardGameException {
        super.beforeStartListen(gameMode);
        // 为所有玩家创建回合参数
        for (TTAPlayer p : gameMode.getGame().getPlayers()) {
            RoundParam param = new RoundParam(gameMode, this, p);
            this.setParam(p.getPosition(), param);
        }
    }

    @Override
    protected BgResponse createStartListenCommand(TTAGameMode gameMode, TTAPlayer player) {
        BgResponse res = super.createStartListenCommand(gameMode, player);
        RoundParam param = this.getParam(player.getPosition());
        // 发送当前阶段
        res.setPublicParameter("currentStep", param.currentStep);
        return res;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        super.doAction(gameMode, action);
        RoundParam param = this.getParam(action.getPlayer().getPosition());
        switch (param.currentStep) {
            case POLITICAL: // 政治行动阶段
                this.politicalAction.execute(gameMode, action);
                break;
            case NORMAL: // 内政行动阶段
                this.doRoundAction(gameMode, action);
                break;
            default:
                throw new BoardGameException("阶段错误,不能进行行动!");
        }
    }

    /**
     * 执行玩家行动阶段
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected void doRoundAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        String subact = action.getAsString("subact");
        if (TTACmdString.ACTION_PLAY_CARD.equals(subact)) {
            // 出牌
            this.playCard(gameMode, action);
        } else if (TTACmdString.ACTION_TAKE_CARD.equals(subact)) {
            // 拿牌
            this.takeCard(gameMode, action);
        } else if (TTACmdString.ACTION_POPULATION.equals(subact)) {
            // 扩张人口
            this.increasePopulation(gameMode, action);
        } else if (TTACmdString.REQUEST_BUILD.equals(subact)) {
            // 请求建造界面
            this.requestBuild(gameMode, action);
        } else if (TTACmdString.REQUEST_UPGRADE.equals(subact)) {
            // 请求升级的界面
            this.requestUpgrade(gameMode, action);
        } else if (TTACmdString.REQUEST_DESTORY.equals(subact)) {
            // 请求摧毁建筑的界面
            this.requestDestory(gameMode, action);
        } else if (TTACmdString.REQUEST_COPY_TAC.equals(subact)) {
            // 新版请求拷贝阵型
            this.requestCopyTactics(gameMode, action);
        } else if (TTACmdString.ACTION_ACTIVE_CARD.equals(subact)) {
            // 使用卡牌能力
            this.activeCard(gameMode, action);
        } else if (TTACmdString.ACTION_PASS.equals(subact)) {
            // 结束内政阶段
            this.processEndAction(gameMode, action);
        } else {
            throw new BoardGameException("无效的指令!");
        }
    }

    /**
     * 结束行动阶段
     *
     * @param gameMode
     * @param player
     * @throws BoardGameException
     */
    protected void endActionPhase(TTAGameMode gameMode, TTAPlayer player) throws BoardGameException {
        RoundParam param = this.getParam(player.getPosition());
        gameMode.getReport().playerEndAction(player);
        if (gameMode.getGame().isVersion2() && param.needDiscardMilitary) {
            // 新版,如果玩家的军事手牌超过上限,则需要进行弃牌操作
            new TTADiscardHandLimitExecutor(param).execute();
        }
        new TTAEndTurnExecutor(param).execute();
        this.setPlayerResponsed(gameMode, player.getPosition());
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_ROUND;
    }

    /**
     * 玩家扩张人口
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected void increasePopulation(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        RoundParam param = this.getParam(player);
        new TTAIncreasePopExecutor(param).execute();
    }

    @Override
    protected void onPlayerTurn(TTAGameMode gameMode, TTAPlayer player) throws BoardGameException {
        super.onPlayerTurn(gameMode, player);
        TTA game = gameMode.getGame();
        RoundParam param = this.getParam(player);

        // 设置当前玩家
        gameMode.getGame().setCurrentPlayer(player);
        // 牌列执行补牌的行动
        if (gameMode.getRound() == 2 && player == game.getStartPlayer()) {
            // 如果是第2回合起始玩家,则不进行补牌的动作,因为在1回合结束时已经补过了
            game.sendCardRowReport();
        } else {
            // 弃牌并补牌
            param.regroupCardRow(gameMode.doregroup);
            // 补牌完成后,如果游戏结束,并且当前玩家是起始玩家,则这是最后一个回合
            if (gameMode.gameOver && player == gameMode.getGame().getStartPlayer()) {
                gameMode.finalRound = true;
                gameMode.getReport().gameOverWarning();
                // 向所有玩家发送游戏即将结束的警告
                game.sendAlertToAll("游戏即将结束!");
            }
        }
        // 第二回合没有政治阶段，所以...
        gameMode.doregroup = gameMode.getGame().getConfig().getMode() == TTAMode.SIMPLE || gameMode.getRound() == 2;
        // 重置临时资源和卡牌能力
        player.getTempResManager().resetTemplateResource();
        player.resetRoundFlags();
        gameMode.getReport().playerRoundStart(player);
        // 重置政治阶段参数
        this.politicalAction.shouldPass = false;
        this.politicalAction.eventCardPlayed = false;

        // 检查玩家下回合中临时的属性调整值
        if (!player.getRoundTempParam().isEmpty()) {
            // 调整CA(老版叛乱)
            Integer num = player.getRoundTempParam().getInteger(CivilizationProperty.CIVIL_ACTION);
            if (num != null) {
                gameMode.getGame().playerAddjustCivilAction(player, num);
            }
            // 是否跳过政治阶段
            String passed = player.getRoundTempParam().getString(RoundStep.POLITICAL);
            if (passed != null && TTACmdString.POLITICAL_PASS.equals(passed)) {
                this.politicalAction.shouldPass = true;
                gameMode.getGame().sendAlert(player, "你必须放弃政治阶段!");
            }
            // 清空
            player.getRoundTempParam().clear();
        }

        // 新版当前阵型要移动到公共阵型区
        if (game.isVersion2()) {
            if (gameMode.getCardBoard().publicTactics(player.getTactics())) {
                gameMode.getReport().publicTactics(player, player.getTactics());
            }
        }

        // 向房间内玩家发送基本信息
        game.sendBaseInfo(null);

        // 新版贞德技能,向当前玩家发送即将触发的事件牌信息
        if (player.getAbilityManager().hasAbilitiy(CivilAbilityType.PA_VIEW_TOP_EVENT)) {
            game.sendTopEvent(player);
        }

        // 最后,检查玩家是否需要结算战争牌,如果有,则解决战争
        if (player.getWar() != null) {
            new TTAWarCardExecutor(this.getParam(player), player.getWar()).execute();
            gameMode.getGame().removeOvertimeCard(player.getWar());
        }

    }

    /**
     * 玩家内政行动阶段从手牌中出牌
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected void playCard(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        String cardId = action.getAsString("cardId");
        RoundParam param = this.getParam(player.getPosition());
        TTACard card = player.getCard(cardId);
        TTAPlayCardExecutor executor = TTAExecutorFactory.createPlayCardExecutor(param, card);
        CheckUtils.check(executor == null, "不能打出这张牌!");
        executor.execute();
    }

    protected void processEndAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        RoundParam param = this.getParam(player);
        TTAEndActionExecutor executor = new TTAEndActionExecutor(param);
        executor.execute();
        if (executor.endPhase) {
            this.endActionPhase(gameMode, player);
        }
    }

    /**
     * 玩家请求建造界面
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected void requestBuild(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        RoundParam param = this.getParam(player.getPosition());
        new TTARequestBuildExecutor(param).execute();
    }

    /**
     * 玩家请求拷贝阵型的界面
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected void requestCopyTactics(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        RoundParam param = this.getParam(player);
        new TTACopyTacticsExecutor(param).execute();
    }

    /**
     * 玩家请求摧毁建筑的界面
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected void requestDestory(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        RoundParam param = this.getParam(player.getPosition());
        new TTARequestDestroyExecutor(param).execute();
    }

    /**
     * 玩家请求升级的界面
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected void requestUpgrade(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        RoundParam param = this.getParam(player.getPosition());
        new TTARequestUpgradeExecutor(param).execute();
    }

    @Override
    public void sendReconnectResponse(TTAGameMode gameMode, TTAPlayer player) throws BoardGameException {
        super.sendReconnectResponse(gameMode, player);
        if (this.listeningPlayer == player) {
            RoundParam param = this.getParam(player);
            // 新版贞德技能,向当前玩家发送即将触发的事件牌信息
            if (param.currentStep == RoundStep.POLITICAL && !this.politicalAction.eventCardPlayed
                    && this.listeningPlayer.getAbilityManager().hasAbilitiy(CivilAbilityType.PA_VIEW_TOP_EVENT)) {
                gameMode.getGame().sendTopEvent(this.listeningPlayer);
            }
        }
    }

    @Override
    protected void sendStartListenCommand(TTAGameMode gameMode, TTAPlayer p, TTAPlayer receiver) {
        super.sendStartListenCommand(gameMode, p, receiver);
        // 发送玩家可激活的卡牌列表
        RoundParam param = this.getParam(p.getPosition());
        gameMode.getGame().sendPlayerActivableCards(param.currentStep, p);
    }

    /**
     * 玩家从卡牌序列中拿牌
     *
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    protected void takeCard(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        TTAPlayer player = action.getPlayer();
        String cardId = action.getAsString("cardId");
        RoundParam param = this.getParam(player.getPosition());
        TTATakeCardExecutor executor = new TTATakeCardExecutor(param, cardId);
        executor.execute();
    }

    /**
     * 政治行动处理对象
     *
     * @author F14eagle
     */
    public class PoliticalAction {
        public boolean shouldPass = false;
        public boolean eventCardPlayed = false;

        public void endPoliticalPhase(TTAGameMode gameMode, TTAPlayer player, boolean forceEnd)
                throws BoardGameException {
            RoundParam param = TTARoundListener.this.getParam(player.getPosition());
            ++param.politicalAction;
            // 有2次政治行动能力还未发动,若不是点结束,这是第1次政治行动结束
            if (!forceEnd && player.checkAbilitiy(CivilAbilityType.PA_DOUBLE_POLITICAL)) {
                if (param.politicalAction == 1) {
                    // 第一次政治行动结束
                    gameMode.getGame().sendAlert(player, "你可以额外执行一次政治行动!");
                    return;
                } else {
                    // 第二次政治行动结束
                    player.getParams().setGameParameter(CivilAbilityType.PA_DOUBLE_POLITICAL, true);
                }
            }
            if (!gameMode.getGame().isVersion2() && param.needDiscardMilitary) {
                // 老版,如果玩家的军事手牌超过上限,则需要进行弃牌操作
                new TTADiscardHandLimitExecutor(param).execute();
            }
            // 结束政治行动阶段
            param.currentStep = RoundStep.NORMAL;
            gameMode.doregroup = true;
            // 发送玩家结束政治行动的信息
            gameMode.getGame().sendPlayerPoliticalEndResponse(player, param.currentStep);
            gameMode.getReport().playerEndPoliticalPhase(player);
            // 发送玩家可激活的卡牌列表
            gameMode.getGame().sendPlayerActivableCards(param.currentStep, player);
        }

        /**
         * 处理行动代码
         *
         * @param gameMode
         * @param action
         * @throws BoardGameException
         */
        public void execute(TTAGameMode gameMode, BgAction action) throws BoardGameException {
            TTAPlayer player = action.getPlayer();
            String subact = action.getAsString("subact");
            RoundParam param = TTARoundListener.this.getParam(player.getPosition());
            // 检查当前阶段是否是政治行动阶段
            param.checkPoliticalPhase();
            // 如果玩家不执行政治行动阶段
            if (TTACmdString.POLITICAL_PASS.equals(subact)) {
                // 结束政治行动
                this.endPoliticalPhase(gameMode, player, true);
            } else if (TTACmdString.RESIGN.equals(subact)) {
                // 体面退出游戏
                gameMode.getGame().playerResign(player);
                param.currentStep = RoundStep.RESIGNED;
                TTARoundListener.this.setPlayerResponsed(gameMode, player);
            } else {
                CheckUtils.check(this.shouldPass, "你必须放弃政治阶段!");
                if (TTACmdString.ACTION_PLAY_CARD.equals(subact)) {
                    // 玩家打出手牌
                    this.playCard(gameMode, action);
                } else if (TTACmdString.ACTION_ACTIVE_CARD.equals(subact)) {
                    // 使用卡牌能力
                    TTARoundListener.this.activeCard(gameMode, action);
                } else if (TTACmdString.REQUEST_BREAK_PACT.equals(subact)) {
                    // 请求废除条约
                    TTARequestBreakPactExecutor executor = new TTARequestBreakPactExecutor(param);
                    executor.execute();
                    if (executor.finished) {
                        this.endPoliticalPhase(gameMode, player, false);
                    }
                } else {
                    throw new BoardGameException("无效的指令!");
                }
            }
        }

        /**
         * 玩家从手牌中出黑牌
         *
         * @param gameMode
         * @param action
         * @throws BoardGameException
         */
        protected void playCard(TTAGameMode gameMode, BgAction action) throws BoardGameException {
            TTAPlayer player = action.getPlayer();
            RoundParam param = TTARoundListener.this.getParam(player);
            String cardId = action.getAsString("cardId");
            TTACard card = player.getCard(cardId);
            TTAPoliticalCardExecutor executor = TTAExecutorFactory.createPoliticalExecutor(param, card);
            CheckUtils.check(executor == null, "不能打出这张牌!");
            if (card.cardType == CardType.EVENT) {
                this.eventCardPlayed = true;
            }
            executor.execute();
            if (executor.finished) {
                // 如果处理完毕结束政治阶段
                this.endPoliticalPhase(gameMode, player, false);
            }
        }
    }
}
