package com.f14.TTA.listener;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.TTAProperty;
import com.f14.TTA.component.ability.CivilCardAbility;
import com.f14.TTA.consts.CivilizationProperty;
import com.f14.TTA.consts.TTAGameCmd;
import com.f14.bg.action.BgAction;
import com.f14.bg.action.BgResponse;
import com.f14.bg.exception.BoardGameException;
import com.f14.bg.listener.InterruptParam;
import com.f14.bg.utils.CheckUtils;
import com.f14.utils.CommonUtil;


/**
 * 选择额外生产的监听器
 *
 * @author 吹风奈奈
 */
public class ProductionChoiceListener extends TTAInterruptListener {
    private CivilCardAbility ability;

    private CivilizationProperty propertyA = null;

    private CivilizationProperty propertyB = null;
    private int numA;
    private int numB;
    private int amount = 0;

    public ProductionChoiceListener(TTAPlayer trigPlayer, CivilCardAbility ability) {
        super(trigPlayer);
        this.ability = ability;
        for (CivilizationProperty property : ability.property.getAllProperties().keySet()) {
            if (ability.property.getProperty(property) > 0) {
                if (propertyA == null) {
                    propertyA = property;
                } else {
                    propertyB = property;
                    break;
                }
            }
        }
        this.amount = ability.getAvailableNumber(trigPlayer);
        this.addListeningPlayer(trigPlayer);
    }


    @Override
    public InterruptParam createInterruptParam() {
        InterruptParam param = new InterruptParam();
        TTAProperty property = new TTAProperty();
        property.setProperty(propertyA, ability.property.getProperty(propertyA) * numA);
        property.setProperty(propertyB, ability.property.getProperty(propertyB) * numB);
        param.set("property", property);
        return param;
    }

    @Override
    protected BgResponse createStartListenCommand(TTAGameMode gameMode, TTAPlayer player) {
        BgResponse res = super.createStartListenCommand(gameMode, player);
        // 参数中传递需要选择的资源数量和类型
        res.setPublicParameter("amount", amount);
        res.setPublicParameter("singleSelection", false);
        res.setPublicParameter("maxPropertyA", player.getProperty(propertyA));
        res.setPublicParameter("maxPropertyB", player.getProperty(propertyB));
        res.setPublicParameter("nameA", propertyA.getPropertyName());
        res.setPublicParameter("nameB", propertyB.getPropertyName());
        return res;
    }

    @Override
    protected void doAction(TTAGameMode gameMode, BgAction action) throws BoardGameException {
        numA = action.getAsInt("numA");
        numB = action.getAsInt("numB");
        // 不能选择负数
        CheckUtils.check(numA < 0 || numB < 0, "数量选择错误!");
        // 检查选择数量是否和需求的数量相等
        CheckUtils.check((numA + numB) != Math.abs(amount), "数量选择错误!");
        TTAPlayer player = action.getPlayer();
        // 完成选择
        this.setPlayerResponsed(gameMode, player);
    }

    @Override
    protected String getMsg(TTAPlayer player) {
        String msg = "你获得总数{0}的{1}和{2},请选择!";
        String nameA = propertyA.getPropertyName();
        String nameB = propertyB.getPropertyName();
        msg = CommonUtil.getMsg(msg, amount, nameA, nameB);
        return msg;
    }

    @Override
    protected int getValidCode() {
        return TTAGameCmd.GAME_CODE_CHOOSE_RESOURCE;
    }

    @Override
    public void onAllPlayerResponsed(TTAGameMode gameMode) throws BoardGameException {
        super.onAllPlayerResponsed(gameMode);
    }
}
