package com.f14.TTA;

import com.f14.TTA.component.CardBoard;
import com.f14.TTA.component.Chooser;
import com.f14.TTA.component.ability.CivilCardAbility;
import com.f14.TTA.component.card.EventCard;
import com.f14.TTA.component.card.TTACard;
import com.f14.TTA.component.param.AuctionParam;
import com.f14.TTA.consts.CivilAbilityType;
import com.f14.TTA.consts.CivilizationProperty;
import com.f14.TTA.listener.ChooseArmyTichuListener;
import com.f14.TTA.listener.FirstRoundListener;
import com.f14.TTA.listener.TTARoundListener;
import com.f14.bg.GameMode;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.BgUtils;

import java.util.*;

/**
 * TTA的游戏模式
 *
 * @author F14eagle
 */
public class TTAGameMode extends GameMode<TTAPlayer> {
    /**
     * 是否为新版模式
     */
    public boolean isVersion2 = false;
    /**
     * 游戏最后一回合的标志
     */
    public boolean finalRound = false;
    /**
     * 游戏结束的标志
     */
    public boolean gameOver = false;
    public int inquisitionPosition;
    public boolean doregroup;
    public int tichuBid;
    protected TTAGameRank gameRank;
    /**
     * 当前世纪
     */
    protected int currentAge;
    /**
     * 公共卡牌版
     */
    protected CardBoard cardBoard;
    /**
     * 和平模式下会用到的记分牌
     */

    protected List<EventCard> bonusCards = new ArrayList<>();
    protected Map<TTAPlayer, Integer> resignedPlayerPosition;

    public TTAGameMode(TTA game) {
        super(game);
        this.init();
    }

    /**
     * 增加世纪
     */
    public void addAge() {
        this.currentAge += 1;
        for (TTAPlayer p : gameRank.getPlayers()) {
            p.ageDummyCard.setLevel(currentAge);
            this.getGame().playerRefreshProperty(p);
        }
        getGame().getReport().newAge(this.currentAge);
    }

    @Override
    protected void endGame() throws BoardGameException {
        super.endGame();
        // 结束时算分
        TTAEndPhase endPhase = new TTAEndPhase();
        endPhase.execute(this);
    }

    /**
     * 第一回合结束时进行的动作
     *
     * @throws BoardGameException
     */
    protected void firstRoundEnd() {
        // 为所有玩家生产资源和分数
        for (TTAPlayer p : this.getGame().getPlayers()) {
            this.getGame().playerRoundScore(p);
        }

        // 补牌,并进入I世纪
        this.cardBoard.regroupCardRow(true);
        if (this.getCurrentAge() < 1) {
            // 只有当当前世纪为A时,才会进入I世纪...
            this.cardBoard.newAge();
        }
        this.getGame().sendBaseInfo(null);
        this.getGame().sendCardRowInfo(null);
    }

    /**
     * 取得公共卡牌区
     *
     * @return
     */
    public CardBoard getCardBoard() {
        return this.cardBoard;
    }

    /**
     * 取得当前世纪
     *
     * @return
     */
    public int getCurrentAge() {
        return currentAge;
    }

    @SuppressWarnings("unchecked")
    @Override
    public TTA getGame() {
        return (TTA) super.getBoardGame();
    }

    /**
     * 取得所有玩家的能力
     *
     * @param type
     * @return
     */

    public Map<CivilCardAbility, TTAPlayer> getPlayerAbilities(CivilAbilityType type) {
        Map<CivilCardAbility, TTAPlayer> res = new HashMap<>();
        for (TTAPlayer player : gameRank.getPlayers()) {
            for (CivilCardAbility a : player.getAbilityManager().getAbilitiesByType(type)) {
                res.put(a, player);
            }
        }
        return res;
    }

    /**
     * 按照选择器取得玩家列表
     *
     * @param chooser
     * @return
     */

    public List<TTAPlayer> getPlayersByChooser(Chooser chooser) {
        int count = gameRank.getPlayerNumber();
        int num = count <= 2 ? 1 : chooser.num;
        switch (chooser.type) {
            case ALL: { // 所有玩家
                return new ArrayList<>(gameRank.getPlayers());
            }
            case RANK: { // 按照排名取得玩家
                List<TTAPlayer> ranklist = gameRank.getPlayersByRank(chooser.byProperty);
                // 如果是2人游戏,则只会取一名玩家
                if (chooser.weakest) {
                    // 如果要求选择最弱的,则取最后的num名玩家
                    return ranklist.subList(ranklist.size() - num, ranklist.size());
                } else {
                    // 否则就取前面的num名玩家
                    return ranklist.subList(0, num);
                }
            }
            case MOST: { // 最多属性,允许并列
                List<TTAPlayer> ranklist = gameRank.getPlayersByRank(chooser.byProperty);
                int most = Math.max(chooser.num, ranklist.get(0).getProperty(chooser.byProperty));
                ranklist.removeIf(p -> p.getProperty(chooser.byProperty) != most);
                return ranklist;
            }
            case FOR_BARBARIAN: { // 野蛮人入侵事件专用选择器
                // 如果文明点数最高的玩家是军事力最弱的前2名,则返回该玩家
                // 如果是2人游戏,则是最弱的一名
                List<TTAPlayer> ranklist = gameRank.getPlayersByRank(CivilizationProperty.CULTURE);
                if (ranklist.get(0).getRank(CivilizationProperty.MILITARY, true) <= num) {
                    return ranklist.subList(0, 1);
                } else {
                    return ranklist.subList(0, 0);
                }
            }
            case CURRENT_PLAYER: { // 当前玩家
                return BgUtils.toList(getGame().getCurrentPlayer());
            }
        }
        return null;
    }

    /**
     * 还没体退的玩家
     *
     * @return
     */
    public Collection<TTAPlayer> getRealPlayers() {
        return gameRank.getPlayers();
    }


    @Override
    public TTAReport getReport() {
        return this.getGame().getReport();
    }

    /**
     * 体退的玩家数量
     *
     * @return
     */
    public int getResignedPlayerNumber() {
        return this.resignedPlayerPosition.size();
    }

    /**
     * 体退的玩家顺序
     *
     * @param player
     * @return
     */
    public int getResignedPlayerPosition(TTAPlayer player) {
        return this.resignedPlayerPosition.get(player);
    }

    @Override
    protected void init() {
        super.init();

        this.gameRank = new TTAGameRank(this);
        // 起始世纪为0
        this.currentAge = 0;
        // 初始化摸牌区面板
        this.cardBoard = new CardBoard(this);
        this.bonusCards = new ArrayList<>();
        this.resignedPlayerPosition = new HashMap<>();
    }

    @Override
    protected boolean isGameOver() {
        // 必须是最后一回合,而且游戏结束,才真的结束游戏
        return this.gameOver && this.finalRound;
    }

    @Override
    protected void round() throws BoardGameException {
        this.waitForPlayerRound();
    }

    @Override
    protected void setupGame() throws BoardGameException {
        TTAConfig config = this.getGame().getConfig();
        this.isVersion2 = config.version2;
        TTAResourceManager rm = this.getGame().getResourceManager();
        this.cardBoard = new CardBoard(this);
        this.inquisitionPosition = -1;
        this.doregroup = false;

        // 初始化玩家信息
        for (TTAPlayer player : this.getGame().getPlayers()) {
            player.reset();
            player.init(this);
            List<TTACard> cards = rm.getStartDeck(config, player);
            for (TTACard card : cards) {
                player.addCard(card);
            }
            player.refreshProperties();
            // 重置玩家的行动点
            player.resetActionPoint();
        }
    }

    @Override
    protected void startGame() throws BoardGameException {
        super.startGame();
        this.getGame().sendCardRowReport();
        // 地主竞价
        if (getGame().isTichuMode()) {
            this.waitForTichu();
            this.getReport().system("地主竞价结束!");
        }
        // 开始游戏
        this.getReport().system("第 " + round + " 回合开始!");
        this.waitForFirstRound();
        this.firstRoundEnd();
        round++;
    }

    /**
     * 第一回合
     *
     * @throws BoardGameException
     */
    protected void waitForFirstRound() throws BoardGameException {
        this.addListener(new FirstRoundListener());
    }

    /**
     * 等待执行玩家
     *
     * @throws BoardGameException
     */
    protected void waitForPlayerRound() throws BoardGameException {
        this.addListener(new TTARoundListener());
    }

    /**
     * 地主竞价
     *
     * @throws BoardGameException
     */
    protected void waitForTichu() throws BoardGameException {
        this.getReport().system("地主竞价开始!");
        ChooseArmyTichuListener l = new ChooseArmyTichuListener(getGame().getStartPlayer(), cardBoard.tichuCard);
        InterruptParam res = this.insertListener(l);
        // 结算拍卖结果
        TTAPlayer topPlayer = res.get("topPlayer");
        if (topPlayer != null) {
            AuctionParam ap = res.get(topPlayer.getPosition());
            this.getReport().bidTichu(topPlayer, ap.totalValue);
            this.tichuBid = ap.totalValue;
            this.getGame().setTichu(topPlayer, cardBoard.tichuCard);
        } else {
            throw new BoardGameException("没有人愿意做地主!");
        }

    }
}
