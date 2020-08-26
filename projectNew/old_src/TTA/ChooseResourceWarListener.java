package com.f14.TTA.listener.war;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.TTAProperty;
import com.f14.TTA.component.ability.EventAbility;
import com.f14.TTA.component.card.AttackCard;
import com.f14.TTA.consts.CivilizationProperty;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.TTA.listener.TTAAttackResolutionListener;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.common.ParamSet;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.CheckUtils;
import com.f14.utils.CommonUtil;


/**
 * 选择资源的侵略/战争监听器
 *
 * @author F14eagle
 */
public class ChooseResourceWarListener extends TTAAttackResolutionListener {

    private CivilizationProperty propertyA = null;

    private CivilizationProperty propertyB = null;

    public ChooseResourceWarListener(AttackCard warCard, TTAPlayer trigPlayer, TTAPlayer winner, TTAPlayer loser,
                                     ParamSet warParam) {
        super(warCard, trigPlayer, winner, loser, warParam);
        for (CivilizationProperty property : attackCard.loserEffect.property.getAllProperties().keySet()) {
            if (attackCard.loserEffect.property.getProperty(property) > 0) {
                if (propertyA == null) {
                    propertyA = property;
                } else {
                    propertyB = property;
                    break;
                }
            }
        }
    }

    @Override
    protected boolean beforeListeningCheck(TTAGameMode gameMode, TTAPlayer p) {
        // 该事件总是检查战败方的资源损失情况
        // 检查玩家是否可以自动选择资源
        TTAPlayer player = this.loser;
        EventAbility ability = this.attackCard.loserEffect;
        // 如果无需选择资源,则不需玩家回应
        int amount = this.getNeedAmount();
        if (amount == 0) {
            return false;
        }
        // 如果是玩家失去资源,则需要进行一些判断
        if (amount != 0) {
            int maxA = player.getProperty(propertyA);
            int maxB = player.getProperty(propertyB);
            if (maxA + maxB == 0) {
                // 如果玩家没有任何资源,则不需回应
                return false;
            }
            ChooseParam param = this.getParam(player.getPosition());
            if (ability.singleSelection) {
                // 在只能选择一种资源的情况下
                // 如果玩家任一资源总数为0,则自动扣除另一种资源
                if (maxA == 0) {
                    param.property.setProperty(propertyB, Math.min(maxB, Math.abs(amount)),
                            ability.property.getProperty(propertyB));
                    return false;
                }
                if (maxB == 0) {
                    param.property.setProperty(propertyA, Math.min(maxA, Math.abs(amount)),
                            ability.property.getProperty(propertyA));
                    return false;
                }
            } else {
                // 在多选的情况下
                // 如果玩家的资源总数少于等于要扣除的总数,则自动扣光全部资源
                if ((maxA + maxB) <= Math.abs(amount)) {
                    param.property.setProperty(propertyA, maxA, ability.property.getProperty(propertyA));
                    param.property.setProperty(propertyB, maxB, ability.property.getProperty(propertyB));
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void beforeStartListen(TTAGameMode gameMode) throws BoardGameException {
        super.beforeStartListen(gameMode);
        // 为所有玩家创建选择参数
        for (TTAPlayer player : gameMode.getGame().getPlayers()) {
            ChooseParam param = new ChooseParam();
            this.setParam(player.getPosition(), param);
        }
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        ChooseParam cp = this.getParam(loser.getPosition());
        param.set(loser.getPosition(), cp.property);
        return param;
    }

    @Override
    protected BgResponse createStartListenCommand(TTAGameMode gameMode, TTAPlayer player) {
        BgResponse res = super.createStartListenCommand(gameMode, player);
        // GAME_CODE_CHOOSE_RESOURCE 需要的参数
        // 由战胜方选择战败方的
        res.setPublicParameter("amount", this.getNeedAmount());
        res.setPublicParameter("singleSelection", false);
        res.setPublicParameter("maxPropertyA", this.loser.getProperty(propertyA));
        res.setPublicParameter("maxPropertyB", this.loser.getProperty(propertyB));
        res.setPublicParameter("nameA", propertyA.getPropertyName());
        res.setPublicParameter("nameB", propertyB.getPropertyName());
        return res;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        int numA = action.getAsInt("numA");
        int numB = action.getAsInt("numB");
        // 不能选择负数
        CheckUtils.check(numA < 0 || numB < 0, "数量选择错误!");
        EventAbility ability = this.attackCard.loserEffect;
        int amount = this.getNeedAmount();
        if (ability.singleSelection) {
            // 判断是否只选择一种资源
            if (numA != 0 && numB != 0) {
                throw new BoardGameException(
                        "不能同时选择" + propertyA.getPropertyName() + "和" + propertyB.getPropertyName() + "!");
            }
        }
        // 检查选择数量是否和需求的数量相等
        CheckUtils.check((numA + numB) != Math.abs(amount), "数量选择错误!");
        // 需要检查战败方玩家是否拥有足够的食物或资源
        TTAPlayer player = this.loser;
        if (amount < 0) {
            CheckUtils.check(numA > player.getProperty(propertyA), propertyA.getPropertyName() + "数量不足!");
            CheckUtils.check(numB > player.getProperty(propertyB), propertyB.getPropertyName() + "数量不足!");
        }
        // 设置选择的食物和资源
        ChooseParam param = this.getParam(player.getPosition());
        param.property.setProperty(propertyA, numA, ability.property.getProperty(propertyA));
        param.property.setProperty(propertyB, numB, ability.property.getProperty(propertyB));
        // 完成选择
        this.setPlayerResponsed(gameMode, action.getPlayer().getPosition());
    }

    @Override
    protected String getMsg(TTAPlayer player) {
        EventAbility ability = this.attackCard.loserEffect;
        String msg = "你能夺取玩家{0}总数{1}的{3}{2}{4},请选择!";
        int amount = Math.abs(this.getNeedAmount());
        String nameA = propertyA.getPropertyName();
        String nameB = propertyB.getPropertyName();
        msg = CommonUtil.getMsg(msg, this.loser.getReportString(), amount, (ability.singleSelection) ? "或" : "和", nameA,
                nameB);
        return msg;
    }

    /**
     * 取得实际需要选择的数量
     *
     * @return
     */
    protected int getNeedAmount() {
        return this.attackCard.loserEffect.getRealAmount(warParam);
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_CHOOSE_RESOURCE;
    }

    @Override
    public void onAllPlayerResponsed(TTAGameMode gameMode) throws BoardGameException {
        super.onAllPlayerResponsed(gameMode);
    }

    /**
     * 选择的参数
     *
     * @author F14eagle
     */
    class ChooseParam {
        TTAProperty property = new TTAProperty();
    }

}
