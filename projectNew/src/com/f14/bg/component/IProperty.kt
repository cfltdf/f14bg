package com.f14.bg.component

/**
 * 拥有属性的对象接口

 * @author F14eagle
 */
interface IProperty<Key> {

    /**
     * 加上所有附加属性值(不调整因数)
     * @param properties
     */
    fun addBonusProperties(properties: IProperty<Key>) = this.addBonusProperties(properties, 1)

    /**
     * 加上所有附加属性值x倍数(不调整因数)
     * @param properties
     */
    fun addBonusProperties(properties: IProperty<Key>, multiple: Int)

    operator fun plusAssign(properties: IProperty<Key>) = this.addProperties(properties)

    /**
     * 加上所有属性值(不调整因数)
     * @param properties
     */
    fun addProperties(properties: IProperty<Key>) = this.addProperties(properties, 1)

    /**
     * 加上所有属性值x倍数(不调整因数)
     * @param properties
     */
    fun addProperties(properties: IProperty<Key>, multiple: Int)

    operator fun plusAssign(property: Key) = this.addProperty(property)

    /**
     * 调整属性的值
     * @param property
     */
    fun addProperty(property: Key) = this.addProperty(property, 1)

    /**
     * 调整属性的值
     * @param property
     * @param value
     */
    fun addProperty(property: Key, value: Int)

    /**
     * 添加属性的附加值
     * @param property
     */
    fun addPropertyBonus(property: Key) = this.addPropertyBonus(property, 1)

    /**
     * 添加属性的附加值
     * @param property
     * @param bonus
     */
    fun addPropertyBonus(property: Key, bonus: Int)

    /**
     * 移除所有属性值
     */
    fun clear()

    /**
     * 移除所有属性的附加值
     */
    fun clearAllBonus()

    /**
     * 取得所有实际的属性值(不受最大值最小值限制)
     * @return
     */
    val allFactProperties: Map<Key, Int>

    /**
     * 取得所有属性值(受最大值最小值限制)
     * @return
     */
    val allProperties: Map<Key, Int>

    /**
     * 取得属性的值(受最大值最小值限制)
     * @param property
     * @return
     */
    fun getProperty(property: Key): Int

    /**
     * 取得属性的实际值(不受最大值最小值限制)
     * @param property
     * @return
     */
    fun getPropertyFactValue(property: Key): Int

    /**
     * 判断该属性集是否为空
     * @return
     */
    val isEmpty: Boolean
        get() = this.allProperties.values.all { it == 0 }

    operator fun timesAssign(i: Int) = this.multi(i)

    /**
     * 将所有属性值乘上i
     * @param i
     */
    fun multi(i: Int)

    operator fun minusAssign(properties: IProperty<Key>) = this.removeProperties(properties)

    /**
     * 减去所有属性值(不调整因数)
     * @param properties
     */
    fun removeProperties(properties: IProperty<Key>) = this.addProperties(properties, -1)

    /**
     * 设置属性的因数
     * @param property
     * @param factor
     */
    fun setFactor(property: Key, factor: Number)

    /**
     * 设置属性的最大值
     * @param property
     * @param max
     */
    fun setMaxValue(property: Key, max: Int)

    /**
     * 设置属性的最小值
     * @param property
     * @param min
     */
    fun setMinValue(property: Key, min: Int)

    /**
     * 设置属性是否允许超出最大值和最小值
     * @param property
     * @param overflow
     */
    fun setOverflow(property: Key, overflow: Boolean)

    /**
     * 设置属性的值
     * @param property
     * @param value
     */
    fun setProperty(property: Key, value: Int)

    /**
     * 设置属性的值和因数
     * @param property
     * @param value
     * @param factor
     */
    fun setProperty(property: Key, value: Int, factor: Number)

    /**
     * 设置属性的附加值
     * @param property
     * @param bonus
     */
    fun setPropertyBonus(property: Key, bonus: Int)
}
