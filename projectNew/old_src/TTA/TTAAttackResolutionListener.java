package com.f14.TTA.listener;

import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.card.AttackCard;
import com.f14.bg.common.ParamSet;

/**
 * TTA的攻击结算监听器
 *
 * @author F14eagle
 */
public abstract class TTAAttackResolutionListener extends TTAInterruptListener {
    protected AttackCard attackCard;
    protected TTAPlayer winner;
    protected TTAPlayer loser;
    protected ParamSet warParam;

    /**
     * 构造函数
     *
     * @param trigPlayer
     * @param winner
     * @param loser
     * @param warParam
     */
    public TTAAttackResolutionListener(AttackCard attackCard, TTAPlayer trigPlayer, TTAPlayer winner, TTAPlayer loser,
                                       ParamSet warParam) {
        super(trigPlayer);
        this.attackCard = attackCard;
        this.winner = winner;
        this.loser = loser;
        this.warParam = warParam;
        if (this.attackCard.loserEffect.winnerSelect) {
            this.addListeningPlayer(winner);
        } else {
            this.addListeningPlayer(loser);
        }
    }
}
