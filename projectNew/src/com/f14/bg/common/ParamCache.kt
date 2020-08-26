package com.f14.bg.common


import java.util.*

/**
 * 参数缓存
 * @author F14eagle
 */
class ParamCache {
    private var paramSets: MutableMap<LifeCycle, ParamSet> = EnumMap(com.f14.bg.common.LifeCycle::class.java)

    /**
     * 清除所有的参数
     */
    fun clear() {
        this.paramSets.clear()
    }

    /**
     * 清除游戏全局的参数
     */
    fun clearGameParameters() {
        this.getParamSet(LifeCycle.GAME).clear()
    }

    /**
     * 清除回合的参数
     */
    fun clearRoundParameters() {
        this.getParamSet(LifeCycle.ROUND).clear()
    }

    /**
     * 取得Boolean类型的参数
     * @param key
     * @return
     */
    fun getBoolean(key: Any): Boolean = this.getParameter(key, false)

    /**
     * 取得Double类型的参数
     * @param key
     * @return
     */
    fun getDouble(key: Any): Double = this.getParameter(key, 0.0)

    /**
     * 取得Integer类型的参数
     * @param key
     * @return
     */
    fun getInteger(key: Any): Int = this.getParameter(key, 0)

    /**
     * 取得参数(按照生存周期顺序取得)
     * @param <C>
     * @param key
     * @return
     */
    fun <C : Any> getParameter(key: Any, defaultValue: C): C = getParameter(key) ?: defaultValue

    /**
     * 取得参数(按照生存周期顺序取得)
     * @param <C>
     * @param key
     * @return
     */
    fun <C : Any> getParameter(key: Any): C? = cycleIndex.map(this::getParamSet).mapNotNull { it.get<C>(key) }.firstOrNull()

    /**
     * 取得生存周期对应的参数集
     * @param c
     * @return
     */
    private fun getParamSet(c: LifeCycle): ParamSet = paramSets.computeIfAbsent(c) { ParamSet() }

    /**
     * 取得String类型的参数
     * @param key
     * @return
     */
    fun getString(key: Any): String = this.getParameter(key, "")

    /**
     * 设置游戏全局参数
     * @param key
     * @param value
     */
    fun <C : Any> setGameParameter(key: Any, value: C?) {
        this.getParamSet(LifeCycle.GAME)[key] = value
    }

    /**
     * 设置回合参数
     * @param key
     * @param value
     */
    fun <C : Any> setRoundParameter(key: Any, value: C) {
        this.getParamSet(LifeCycle.ROUND)[key] = value
    }

    companion object {
        /**
         * 从生存周期缓存中读取参数时的顺序
         */
        private val cycleIndex = arrayOf(LifeCycle.ROUND, LifeCycle.GAME)
    }

}
