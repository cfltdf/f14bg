package com.f14.bg.component


open class PartPool : Cloneable {

    private var pool: MutableMap<Any, Part> = LinkedHashMap()

    /**
     * 清理所有的配件
     */
    fun clear() {
        pool.clear()
    }


    @Throws(CloneNotSupportedException::class)
    public override fun clone(): PartPool {
        val res = super.clone() as PartPool
        res.pool = LinkedHashMap()
        this.pool.mapValues { it.value.clone() }.toMap(res.pool)
        return res
    }

    /**
     * 取得所有配件的数量
     * @return
     */
    val allPartsNumber: Map<Any, Int>
        get() = this.parts.map { it to this.getAvailableNum(it) }.toMap()

    /**
     * 取得配件的可用数量
     * @return
     */
    fun getAvailableNum(part: Any): Int {
        val p = this.getPart(part)
        return when {
            p.num <= 0 -> 0
            else -> p.num
        }
    }

    /**
     * 取得配件对象
     * @return
     */
    private fun getPart(part: Any): Part {
        return pool.computeIfAbsent(part) { Part(0) }
    }

    /**
     * 取得所有非空配件类型的总数
     * @return
     */
    val partNum: Int
        get() = this.pool.keys.count { this.getAvailableNum(it) > 0 }

    /**
     * 取得配件池中所有配件类型
     * @return
     */
    val parts: Set<Any>
        get() = this.pool.keys

    /**
     * 取得所有配件的总数
     * @return
     */
    val totalNum: Int
        get() = this.pool.keys.sumBy(this::getAvailableNum)

    /**
     * 判断配件池中是否拥有parts中的所有配件
     * @param parts
     * @return
     */
    fun hasParts(parts: PartPool) = parts.parts.all { this.getAvailableNum(it) >= parts.getAvailableNum(it) }

    /**
     * 判断配件池是否为空
     * @return
     */
    val isEmpty: Boolean
        get() = this.pool.keys.none { this.getAvailableNum(it) > 0 }

    /**
     * 放入配件
     * @param num
     */
    fun putPart(part: Any, num: Int = 1): Int {
        this.getPart(part).num += num
        return num
    }

    /**
     * 将parts中的配件放入配件堆中
     * @param parts
     */
    fun putParts(parts: PartPool) {
        parts.allPartsNumber.forEach { (p, n) -> this.putPart(p, n) }
//        parts.parts.forEach { this.putPart(it, parts.getAvailableNum(it)) }
    }

    /**
     * 设置配件数量
     * @param num
     */
    fun setPart(part: Any, num: Int) {
        this.getPart(part).num = num
    }

    /**
     * 拿取配件
     * @param num
     * @return
     */
    fun takePart(part: Any, num: Int = 1): Int {
        val p = this.getPart(part)
        return when {
            p.num <= 0 -> 0
            else -> minOf(num, p.num).also { p.num -= it }
        }
    }

    /**
     * 取出所有指定的配件
     * @param part
     * @return
     */
    fun takePartAll(part: Any): Int {
        val p = this.getPart(part)
        return when {
            p.num <= 0 -> 0
            else -> p.num.also { p.num = 0 }
        }
    }

    /**
     * 从配件堆中取出parts中的配件
     * @param parts
     * @return
     */
    fun takeParts(parts: PartPool): PartPool {
        val res = PartPool()
        parts.allPartsNumber.forEach { (p, n) -> res.putPart(this.takePart(p, n)) }
//        parts.parts.forEach { res.putPart(it, this.takePart(it, parts.getAvailableNum(it))) }
        return res
    }

    private class Part(var num: Int) : Cloneable {
        @Throws(CloneNotSupportedException::class)
        public override fun clone(): Part {
            return super.clone() as Part
        }
    }
}