package com.f14.bg.common


import java.util.*

class ListMap<K, V> {

    private var map: MutableMap<K, MutableList<V>> = LinkedHashMap()

    /**
     * 添加对象
     * @param key
     * @param value
     */
    fun add(key: K, value: V) {
        this.getList(key).add(value)
    }

    fun clear() {
        this.map.clear()
    }

    /**
     * 取得所有包含value的key
     * @param value
     * @return
     */
    fun getKeyByValue(value: V): List<K> {
        return map.filterValues { it.contains(value) }.keys.toList()
    }

    /**
     * 按照key取得list,如果不存在则会自动创建一个list
     * @param key
     * @return
     */
    fun getList(key: K): MutableList<V> {
        return this.map.computeIfAbsent(key) { ArrayList() }
    }

    /**
     * 取得键的合集
     * @return
     */
    fun keySet(): Set<K> {
        return this.map.keys
    }

    /**
     * 取得列表的合集
     * @return
     */
    fun values(): MutableCollection<MutableList<V>> {
        return this.map.values
    }

    /**
     * 移除对象
     * @param value
     */
    fun remove(value: V) {
        this.map.values.forEach { it.remove(value) }
    }

    /**
     * 按照键值移除对象
     * @param key
     */
    fun removeKey(key: K) {
        this.map.remove(key)
    }
}
