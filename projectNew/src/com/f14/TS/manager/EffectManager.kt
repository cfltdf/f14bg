package com.f14.TS.manager

import com.f14.TS.action.TSEffect
import com.f14.TS.component.TSCard
import com.f14.TS.consts.EffectType

import java.util.*

/**
 * 持续效果管理器
 * @author F14eagle
 */
class EffectManager {
    private var effects: MutableSet<TSEffect> = LinkedHashSet()
    private var cardEffects: MutableMap<TSCard, EffectContainer> = HashMap()
    private var typeEffects: MutableMap<EffectType, EffectContainer> = HashMap()

    /**
     * 添加效果
     * @param card
     * @param effect
     */
    fun addEffect(card: TSCard, effect: TSEffect) {
        this.effects.add(effect)
        this.getEffectContainer(card).addEffect(effect)
        this.getEffectContainer(effect.effectType).addEffect(effect)
    }

    fun clear() {
        this.effects.clear()
        this.cardEffects.clear()
        this.typeEffects.clear()
    }

    /**
     * 按照效果类型取得卡牌对象
     * @param effectType
     * @return
     */
    fun getCardByEffectType(effectType: EffectType) = this.cardEffects.filterValues { it.hasEffect(effectType) }.keys.firstOrNull()

    /**
     * 按照卡牌取得效果
     * @return
     */
    private fun getEffectContainer(effectType: EffectType) = this.typeEffects.computeIfAbsent(effectType) { EffectContainer() }

    /**
     * 按照卡牌取得效果
     * @param card
     * @return
     */
    private fun getEffectContainer(card: TSCard) = this.cardEffects.computeIfAbsent(card) { EffectContainer() }

    /**
     * 按照效果类型取得效果对象
     * @param effectType
     * @return
     */
    fun getEffects(effectType: EffectType) = this.getEffectContainer(effectType).effects

    /**
     * 是否拥有指定卡牌的能力
     * @param card
     * @return
     */
    fun hasCardEffect(card: TSCard) = this.getEffectContainer(card).effects.isNotEmpty()

    /**
     * 按照效果类型取得效果对象
     * @param effectType
     * @return
     */
    fun hasEffect(effectType: EffectType) = this.getEffectContainer(effectType).effects.isNotEmpty()

    /**
     * 移除卡牌对应的所有效果
     * @param card
     */
    fun removeEffects(card: TSCard) {
        val es = this.getEffectContainer(card)
        for (e in es.effects) {
            this.effects.remove(e)
            this.getEffectContainer(e.effectType).removeEffect(e)
        }
        this.cardEffects.remove(card)
    }

    /**
     * 效果容器
     * @author F14eagle
     */
    inner class EffectContainer {
        var effects: MutableSet<TSEffect> = LinkedHashSet()

        fun addEffect(effect: TSEffect) = this.effects.add(effect)

        /**
         * 检查是否有指定类型的效果
         * @param effectType
         * @return
         */
        fun hasEffect(effectType: EffectType) = this.effects.any { it.effectType === effectType }

        fun removeEffect(effect: TSEffect) = this.effects.remove(effect)
    }
}
