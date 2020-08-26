package com.f14.TTA

import com.f14.TTA.consts.TTAMode
import com.f14.bg.BoardGameConfig
import com.google.gson.annotations.SerializedName

/**
 * Created by 吹风奈奈 on 2017/7/19.
 */

class TTAConfig : BoardGameConfig() {
    /**
     * 最大世纪的限制
     */
    var ageCount: Int = 0
    /**
     * 是否会暴动
     */
    @SerializedName("uprising")
    var isUprising: Boolean = true
    /**
     * 游戏模式
     */
    var mode: TTAMode = TTAMode.FULL
    /**
     * 暴动摸牌
     */
    @SerializedName("revoltDraw")
    var isRevoltDraw: Boolean = false
    /**
     * 无上限
     */
    @SerializedName("noLimit")
    var isNoLimit: Boolean = false
    /**
     * BGO扩展
     */
    @SerializedName("expansionUsed")
    var isExpansionUsed: Boolean = false
    /**
     * 奈奈TH扩
     */
    @SerializedName("touhouUsed")
    var isTouhouUsed: Boolean = false
    /**
     * 中国扩
     */
    @SerializedName("expansionCN")
    var isExpansionCN: Boolean = false
    /**
     * DIY大赛
     */
    @SerializedName("expansionDIY")
    var isExpansionDIY: Boolean = false
    /**
     * 隐藏人物
     */
    @SerializedName("expansion14")
    var isExpansion14: Boolean = false
    /**
     * 新版
     */
    @SerializedName("newAgeUsed")
    var isNewAgeUsed: Boolean = false
    /**
     * 新版扩
     */
    var version2Ex: Boolean = false
    /**
     * 2v2平衡扩
     */
    var isBalanced22: Boolean = false
    /**
     * 是否隐藏本局奇迹领袖
     */
    @SerializedName("hideAvalable")
    var isHideAvalable: Boolean = false
    /**
     * 辅助记牌
     */
    @SerializedName("lazyMemory")
    var isLazyMemory: Boolean = false
    /**
     * 全球冲突
     */
    @SerializedName("globalConflict")
    var isGlobalConflict: Boolean = false
}
