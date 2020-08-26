package com.f14.bg.component


import java.util.*

/**
 * 属性计数器

 * @author F14eagle
 */
abstract class PropertyCounter<Key> : IProperty<Key>, Cloneable {

    protected var values: MutableMap<Key, ValueObject> = HashMap()

    /**
     * 加上所有附加属性值x倍数(不调整因数)
     * @param properties
     */
    override fun addBonusProperties(properties: IProperty<Key>, multiple: Int) = properties.allProperties
            .forEach { (k, v) -> this.addPropertyBonus(k, multiple * v) }

    /**
     * 加上所有属性值x倍数(不调整因数)
     * @param properties
     */
    override fun addProperties(properties: IProperty<Key>, multiple: Int) = properties.allProperties
            .forEach { (k, v) -> this.addProperty(k, multiple * v) }

    /**
     * 调整属性的值
     * @param property
     * @param value
     */
    override fun addProperty(property: Key, value: Int) {
        this.getValueObject(property).addValue(value)
    }

    /**
     * 添加属性的附加值
     * @param property
     * @param bonus
     */
    override fun addPropertyBonus(property: Key, bonus: Int) {
        this.getValueObject(property).addBonus(bonus)
    }

    /**
     * 移除所有属性值(所有属性置0)
     */
    override fun clear() {
        this.values.values.forEach { it.setValue(0) }
        this.clearAllBonus()
    }

    /**
     * 移除所有属性的附加值
     */
    override fun clearAllBonus() {
        this.values.values.forEach { it.setBonus(0) }
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): PropertyCounter<Key> {
        val res = super.clone() as PropertyCounter<Key>
        res.values = HashMap()
        this.values.mapValuesTo(res.values) { it.value.clone() }
        return res
    }

    /**
     * 取得所有实际的属性值(不受最大值最小值限制)
     * @return
     */
    override val allFactProperties: Map<Key, Int>
        get() = this.values.mapValues { it.value.factValue }

    /**
     * 取得所有属性值(受最大值最小值限制)
     * @return
     */
    override val allProperties: Map<Key, Int>
        get() = this.values.mapValues { it.value.finalValue }

    /**
     * 取得属性的值(受最大值最小值限制)
     * @param property
     * @return
     */
    override fun getProperty(property: Key): Int {
        val o = this.getValueObject(property)
        return o.finalValue
    }

    /**
     * 取得属性的实际值(不受最大值最小值限制)
     * @param property
     * @return
     */
    override fun getPropertyFactValue(property: Key): Int {
        val o = this.getValueObject(property)
        return o.factValue
    }

    /**
     * 取得属性对象
     * @param property
     * @return
     */
    protected fun getValueObject(property: Key): ValueObject {
        return this.values.computeIfAbsent(property, { ValueObject() })
    }

    /**
     * 将所有属性值乘上i
     * @param i
     */
    override fun multi(i: Int) {
        this.values.values.forEach { it.setValue(it.finalValue * i) }
    }

    /**
     * 设置属性的因数
     * @param property
     * @param factor
     */
    override fun setFactor(property: Key, factor: Number) {
        this.getValueObject(property).factor = factor.toDouble()
    }

    /**
     * 设置属性的最大值
     * @param property
     * @param max
     */
    override fun setMaxValue(property: Key, max: Int) {
        this.getValueObject(property).maxValue = max
    }

    /**
     * 设置属性的最小值
     * @param property
     * @param min
     */
    override fun setMinValue(property: Key, min: Int) {
        this.getValueObject(property).minValue = min
    }

    /**
     * 设置属性是否允许超出最大值和最小值
     * @param property
     * @param overflow
     */
    override fun setOverflow(property: Key, overflow: Boolean) {
        this.getValueObject(property).overflow = overflow
    }

    /**
     * 设置属性的值
     * @param property
     * @param value
     */
    override fun setProperty(property: Key, value: Int) {
        this.getValueObject(property).setValue(value)
    }

    /**
     * 设置属性的值和因数
     * @param property
     * @param value
     * @param factor
     */
    override fun setProperty(property: Key, value: Int, factor: Number) {
        val o = this.getValueObject(property)
        o.setValue(value)
        o.factor = factor.toDouble()
    }

    /**
     * 设置属性的附加值
     * @param property
     * @param bonus
     */
    override fun setPropertyBonus(property: Key, bonus: Int) {
        this.getValueObject(property).setBonus(bonus)
    }

    override fun toString() = this.values.toString()

    /**
     * 属性容器
     * @author F14eagle
     */
    protected class ValueObject : Cloneable {
        /**
         * 实际值
         */
        private var value = 0
        /**
         * 附加值
         */
        private var bonus = 0
        /**
         * 因数,默认为1
         */
        var factor = 1.0
        /**
         * 最大值,默认为最大int
         */
        var maxValue = Integer.MAX_VALUE
        /**
         * 最小值,默认为最小int
         */
        var minValue = Integer.MIN_VALUE
        /**
         * 是否允许溢出最大最小值,默认为false
         */
        var overflow = false

        /**
         * 调整附加值
         * @param bonus
         */
        fun addBonus(bonus: Int) {
            this.setBonus(this.bonus + bonus)
        }

        /**
         * 调整属性值
         * @param value
         */
        fun addValue(value: Int) {
            this.setValue(this.value + value)
        }


        @Throws(CloneNotSupportedException::class)
        public override fun clone() = super.clone() as ValueObject

        /**
         * 取得实际的属性值(不受最大最小值限制)
         * @return
         */
        val factValue: Int
            get() = (value * factor).toInt()

        /**
         * 取得属性值(加上附加值,并受最大值最小值限制)
         * @return
         */
        val finalValue: Int
            get() {
                val value = ((this.value + this.bonus) * factor).toInt()
                return maxOf(minOf(value, this.maxValue), this.minValue)
            }

        /**
         * 设置属性值
         * @param value
         */
        fun setValue(value: Int) {
            when {
                this.overflow -> this.value = value
                else -> this.value = maxOf(minOf(value, this.maxValue), this.minValue)
            }
        }

        /**
         * 设置附加值
         * @param bonus
         */
        fun setBonus(bonus: Int) {
            this.bonus = bonus
        }

        override fun toString() = this.finalValue.toString()
    }
}
