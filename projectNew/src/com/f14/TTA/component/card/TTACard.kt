package com.f14.TTA.component.card

import com.f14.TTA.component.TTAPartPool
import com.f14.TTA.component.TTAProperty
import com.f14.TTA.component.ability.ActiveAbility
import com.f14.TTA.component.ability.CivilCardAbility
import com.f14.TTA.consts.ActionType
import com.f14.TTA.consts.CardSubType
import com.f14.TTA.consts.CardType
import com.f14.bg.component.Card
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * TTA的卡牌
 * @author F14eagle
 */
open class TTACard : Card(), Comparable<TTACard> {
    var level: Int = 0
    lateinit var actionType: ActionType
    lateinit var cardType: CardType
    lateinit var cardSubType: CardSubType
    /**
     * 判断该卡牌是否是科技卡
     * @return
     */
    @SerializedName("technologyCard")
    var isTechnologyCard: Boolean = false
    @SerializedName("propertyMap")
    open var property = TTAProperty()
    @SerializedName("tokenMap")
    var tokens = TTAPartPool()

    open var activeAbility: ActiveAbility? = null
    open var abilities: MutableList<CivilCardAbility> = ArrayList(0)
    var replaceNo: String? = null // 替换基础版卡牌

    @Transient
    var attachedCards: MutableList<TTACard>? = null // 新版叠放在下面的增强牌


    override fun clone(): TTACard {
        val c = super.clone() as TTACard
        c.property = this.property.clone()
        c.tokens = this.tokens.clone()
        return c
    }

    /**
     * TTA牌比较(并没有什么卵用)
     * @return
     */
    override fun compareTo(other: TTACard) = this.level.compareTo(other.level)

    val ageString: String
        get() = getAgeString(this.level)

    /**
     * 取得有效基数,不计算其他加成.(需要工人的牌返回当前工人数量,否则返回1)
     * @return
     */
    open val availableCount: Int
        get() = 1

    override val reportString: String
        get() = "[${this.ageString}${this.name}]"

    fun getReportString(idx: Int): String = "[${this.ageString}${this.name}($idx)]"

    /**
     * 判断该卡牌是否需要工人才能生效
     * @return
     */
    val needWorker
        get() = this.cardType in arrayOf(CardType.BUILDING, CardType.PRODUCTION, CardType.UNIT)

    companion object {
        fun getAgeString(age: Int) = arrayOf("Ａ", "Ⅰ", "Ⅱ", "Ⅲ", "Ⅳ").elementAt(age)
    }
}
