package com.f14.bg.component

import com.f14.bg.report.Printable

/**
 * 组件 - 卡牌
 * @author F14eagle
 */
abstract class Card : Cloneable, Convertable, Printable {
    lateinit var id: String
    lateinit var cardNo: String
    lateinit var name: String
    lateinit var descr: String
    var imageIndex: Int = 0
    var qty: Int = 0
    lateinit var gameVersion: String
    var cardIndex: Int = 0

    public override fun clone() = super.clone() as Card

    override val reportString: String
        get() = "[${this.name}]"

    override fun toMap(): Map<String, Any> = mapOf("id" to this.id)
}
