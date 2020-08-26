package com.f14.TTA;

import com.f14.TTA.consts.TTAMode;
import com.f14.bg.BoardGameConfig;

/**
 * TTA的配置对象
 *
 * @author F14eagle
 */
public class TTAConfig extends BoardGameConfig {
    /**
     * 最大世纪的限制
     */
    public int ageCount;
    /**
     * 是否会腐败
     */
    public boolean corruption;
    /**
     * 是否会暴动
     */
    public boolean uprising;
    /**
     * 时代变化时是否会减少人口
     */
    public boolean darkAge;
    /**
     * 额外奖励分数模式开关
     */
    public boolean bonusCardFlag = false;
    /**
     * 额外奖励分数模式下计分牌的数量
     */
    public int bonusCardNumber = 4;
    /**
     * 游戏模式
     */
    public TTAMode mode;
    /**
     * 暴动摸牌
     */
    public boolean revoltDraw;
    /**
     * 无上限
     */
    public boolean noLimit;
    /**
     * BGO扩展
     */
    public boolean expansionUsed;
    /**
     * 奈奈TH扩
     */
    public boolean touhouUsed;
    /**
     * 中国扩
     */
    public boolean expansionCN;
    /**
     * 隐藏人物
     */
    public boolean expansion14;
    /**
     * 新版
     */
    public boolean version2;
    /**
     * 新版扩
     */
    public boolean version2Ex;
    /**
     * 2v2平衡扩
     */
    public boolean balanced22;
    /**
     * 是否隐藏本局奇迹领袖
     */
    public boolean hideAvalable;
    /**
     * 辅助记牌
     */
    public boolean lazyMemory;

    public int getAgeCount() {
        return ageCount;
    }

    public void setAgeCount(int ageCount) {
        this.ageCount = ageCount;
    }

    public int getBonusCardNumber() {
        return bonusCardNumber;
    }

    public void setBonusCardNumber(int bonusCardNumber) {
    }

    public TTAMode getMode() {
        return mode;
    }

    public void setMode(TTAMode mode) {
        this.mode = mode;
    }

    public boolean isBalanced22() {
        return balanced22;
    }

    public void setBalanced22(boolean balanced22) {
        this.balanced22 = balanced22;
    }

    public boolean isBonusCardFlag() {
        return bonusCardFlag;
    }

    public void setBonusCardFlag(boolean bonusCardFlag) {
        this.bonusCardFlag = bonusCardFlag;
    }

    public boolean isCorruption() {
        return corruption;
    }

    public void setCorruption(boolean corruption) {
        this.corruption = corruption;
    }

    public boolean isDarkAge() {
        return darkAge;
    }

    public void setDarkAge(boolean darkAge) {
        this.darkAge = darkAge;
    }

    public boolean isExpansion14() {
        return expansion14;
    }

    public void setExpansion14(boolean expansion14) {
        this.expansion14 = expansion14;
    }

    public boolean isExpansionCN() {
        return expansionCN;
    }

    public void setExpansionCN(boolean expansionCN) {
        this.expansionCN = expansionCN;
    }

    public boolean isExpansionUsed() {
        return expansionUsed;
    }

    public void setExpansionUsed(boolean expansionUsed) {
        this.expansionUsed = expansionUsed;
    }

    public boolean isHideAvalable() {
        return hideAvalable;
    }

    public void setHideAvalable(boolean hideAvalable) {
        this.hideAvalable = hideAvalable;
    }

    public boolean isLazyMemory() {
        return lazyMemory;
    }

    public void setLazyMemory(boolean lazyMemory) {
        this.lazyMemory = lazyMemory;
    }

    public boolean isNewAgeUsed() {
        return version2;
    }

    public void setNewAgeUsed(boolean newAgeUsed) {
        this.version2 = newAgeUsed;
    }

    public boolean isNoLimit() {
        return noLimit;
    }

    public void setNoLimit(boolean noLimit) {
        this.noLimit = noLimit;
    }

    public boolean isRevoltDraw() {
        return revoltDraw;
    }

    public void setRevoltDraw(boolean revoltDraw) {
        this.revoltDraw = revoltDraw;
    }

    public boolean isTouhouUsed() {
        return touhouUsed;
    }

    public void setTouhouUsed(boolean touhouUsed) {
        this.touhouUsed = touhouUsed;
    }

    public boolean isUprising() {
        return uprising;
    }

    public void setUprising(boolean uprising) {
        this.uprising = uprising;
    }

}
