package com.f14.TS.factory

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.TSResourceManager
import com.f14.TS.condition.*
import com.f14.TS.executor.*
import com.f14.TS.listener.initParam.ActionInitParam
import com.f14.TS.listener.initParam.ConditionInitParam
import com.f14.TS.listener.initParam.ExecutorInitParam
import com.f14.bg.exception.BoardGameException
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * 行动执行器的工厂类

 * @author F14eagle
 */
object ActionFactory {
    private var classMap: MutableMap<String, KClass<*>> = HashMap()

    init {
        classMap["CheckHandCondition"] = CheckHandCondition::class
        classMap["Custom76Condition"] = Custom76Condition::class
        classMap["Custom80Condition"] = Custom80Condition::class
        classMap["Custom85Condition"] = Custom85Condition::class
        classMap["Custom87Condition"] = Custom87Condition::class
        classMap["Custom89Condition"] = Custom89Condition::class
        classMap["Custom90Condition"] = Custom90Condition::class
        classMap["Custom100Condition"] = Custom100Condition::class
        classMap["Custom100Condition"] = Custom100Condition::class
        classMap["Custom104Condition"] = Custom104Condition::class
        classMap["Custom108Condition"] = Custom108Condition::class
        classMap["Custom113Condition"] = Custom113Condition::class
        classMap["Custom116Condition"] = Custom116Condition::class
        classMap["CustomDiy03Condition"] = CustomDiy03Condition::class
        classMap["KitchenCondition"] = KitchenCondition::class
        classMap["Custom43Executor"] = Custom43Executor::class
        classMap["Custom45Executor"] = Custom45Executor::class
        classMap["Custom46Executor"] = Custom46Executor::class
        classMap["Custom49Executor"] = Custom49Executor::class
        classMap["Custom53Executor"] = Custom53Executor::class
        classMap["Custom58Executor"] = Custom58Executor::class
        classMap["Custom61Executor"] = Custom61Executor::class
        classMap["Custom67Executor"] = Custom67Executor::class
        classMap["Custom71Executor"] = Custom71Executor::class
        classMap["Custom76Executor"] = Custom76Executor::class
        classMap["Custom77Executor"] = Custom77Executor::class
        classMap["Custom78Executor"] = Custom78Executor::class
        classMap["Custom80Executor"] = Custom80Executor::class
        classMap["Custom84Executor"] = Custom84Executor::class
        classMap["Custom85Executor"] = Custom85Executor::class
        classMap["Custom92Executor"] = Custom92Executor::class
        classMap["Custom94Executor"] = Custom94Executor::class
        classMap["Custom98Executor"] = Custom98Executor::class
        classMap["Custom100Executor"] = Custom100Executor::class
        classMap["Custom103Executor"] = Custom103Executor::class
        classMap["Custom104Executor"] = Custom104Executor::class
        classMap["Custom105Executor"] = Custom105Executor::class
        classMap["Custom108Executor"] = Custom108Executor::class
        classMap["Custom112Executor"] = Custom112Executor::class
        classMap["Custom113Executor"] = Custom113Executor::class
        classMap["Custom114Executor"] = Custom114Executor::class
        classMap["Custom117Executor"] = Custom117Executor::class
        classMap["Custom131Executor"] = Custom131Executor::class
        classMap["CustomDiy01Executor"] = CustomDiy01Executor::class
        classMap["CustomDiy02Executor"] = CustomDiy02Executor::class
        classMap["CustomDiy04Executor"] = CustomDiy04Executor::class
        classMap["CustomDiy05Executor"] = CustomDiy05Executor::class
        classMap["CustomDiy06Executor"] = CustomDiy06Executor::class
        classMap["CustomDiy07Executor"] = CustomDiy07Executor::class
        classMap["MilitaryCompareExecutor"] = MilitaryCompareExecutor::class
        classMap["MilitaryCompareExecutor2"] = MilitaryCompareExecutor2::class
        classMap["OlympicExecutor"] = OlympicExecutor::class
        classMap["USSRRelocateExecutor"] = USSRRelocateExecutor::class
    }

    private fun getClassCache(abilityClass: String): KClass<*>? {
        return classMap[abilityClass]
    }

    /**
     * 取得类
     * @param abilityClass
     * @param gameMode
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    private fun <C : KClass<*>> getClass(abilityClass: String, gameMode: TSGameMode): C {
        val res = getClassCache(abilityClass)
        return try {
            if (res != null) {
                res as C
            } else gameMode.game.getResourceManager<TSResourceManager>().loader.loadClass(abilityClass).kotlin as C
        } catch (e: Exception) {
            throw BoardGameException(e.toString())
        }
    }

    fun createActionCondition(player: TSPlayer, gameMode: TSGameMode, initParam: ConditionInitParam): TSActionCondition {
        return getClass<KClass<TSActionCondition>>(initParam.clazz!!, gameMode).primaryConstructor?.call(player, gameMode, initParam)
                ?: throw BoardGameException("创建实例失败!")
//        return getClass<Class<TSActionCondition>>(initParam.clazz!!, gameMode)
//                .getConstructor(TSPlayer::class.java, TSGameMode::class.java, ConditionInitParam::class.java)
//                .newInstance(player, gameMode, initParam)
    }


    fun createActionExecutor(player: TSPlayer, gameMode: TSGameMode, initParam: ExecutorInitParam): TSActionExecutor {
        return getClass<KClass<TSActionExecutor>>(initParam.clazz!!, gameMode).primaryConstructor?.call(player, gameMode, initParam)
                ?: throw BoardGameException("创建实例失败!")
//        return getClass<Class<TSActionExecutor>>(initParam.clazz!!, gameMode)
//                .getConstructor(TSPlayer::class.java, TSGameMode::class.java, ExecutorInitParam::class.java)
//                .newInstance(player, gameMode, initParam)
    }

    fun createListenerExecutor(player: TSPlayer, gameMode: TSGameMode, initParam: ActionInitParam): TSListenerExecutor {
        return getClass<KClass<TSListenerExecutor>>(initParam.clazz!!, gameMode).primaryConstructor?.call(player, gameMode, initParam)
                ?: throw BoardGameException("创建实例失败!")
//        return getClass<Class<TSListenerExecutor>>(initParam.clazz!!, gameMode)
//                .getConstructor(TSPlayer::class.java, TSGameMode::class.java, ActionInitParam::class.java)
//                .newInstance(player, gameMode, initParam)
    }
}
