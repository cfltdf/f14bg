package com.f14.TS.component

import com.f14.TS.component.ability.TSAbilityGroup
import com.f14.TS.consts.CardType
import com.f14.TS.consts.SuperPower
import com.f14.TS.consts.TSPhase
import com.f14.TS.consts.ability.TSAbilityType
import com.f14.bg.component.Card
import com.google.gson.annotations.SerializedName


open class TSCard : Card() {
    /**
     * 行动点
     */
    var op: Int = 0
    /**
     * 所属超级大国
     */
    var superPower: SuperPower = SuperPower.NONE
    /**
     * 阶段
     */
    var phase: TSPhase = TSPhase.EARLY
    /**
     * 发生后移出游戏
     */
    var removeAfterEvent: Boolean = false
    /**
     * 卡牌类型
     */
    var cardType: CardType = CardType.NORMAL
    /**
     * 是否可以当头条使用
     */
    @SerializedName("headLine")
    var canHeadLine: Boolean = false
    /**
     * TS的卡牌号
     */
    var tsCardNo: Int = 0
    /**
     * 计分区域字符串
     */
    var scoreRegion: String? = null
    /**
     * 能力组合
     */
    var abilityGroup: TSAbilityGroup? = null
    /**
     * 发生事件需要的前置卡牌
     */
    var requireCardNos: IntArray = intArrayOf()
    /**
     * 阻止该事件发生的卡牌
     */
    var preventedCardNos: IntArray = intArrayOf()
    /**
     * 取消该事件发生的卡牌
     */
    var canceledCardNos: IntArray = intArrayOf()
    /**
     * 持续效果类型
     */
    var durationResult: DurationResult? = null
    /**
     * 是否是战争牌
     */
    var isWar: Boolean = false
    /**
     * 事件发生后时候忽略处理该牌
     */
    var ignoreAfterEvent: Boolean = false


    override fun clone(): TSCard {
        return super.clone() as TSCard
    }

    /**
     * 取得拥有指定能力的技能对象
     * @param abilityType
     * @return
     */
    fun getAbility(abilityType: TSAbilityType) = this.abilityGroup?.abilities?.firstOrNull { it.abilityType == abilityType }

    override val reportString: String
        get() = when {
            this.tsCardNo <= 110 -> "[#${this.tsCardNo}-${this.name}]"
            this.tsCardNo <= 200 -> "[#P${this.tsCardNo - 110}-${this.name}]"
            this.tsCardNo > 300 -> "[F14-0${this.tsCardNo - 300}-${this.name}]"
            else -> super.reportString
        }

    /**
     * 判断该卡牌是否拥有指定的能力
     * @param abilityType
     * @return
     */
    fun hasAbility(abilityType: TSAbilityType) = this.abilityGroup?.abilities?.any { it.abilityType == abilityType }
            ?: false

    /**
     * 检查该牌是否会被指定的牌取消
     * @param card
     * @return
     */
    fun isCanceledByCard(card: TSCard) = card.tsCardNo in this.canceledCardNos
}
