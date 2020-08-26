package com.f14.innovation

import com.f14.bg.exception.BoardGameException
import com.f14.innovation.achieve.InnoAchieveChecker
import com.f14.innovation.achieve.custom.*
import com.f14.innovation.checker.*
import com.f14.innovation.checker.custom.InnoCustom017Checker
import com.f14.innovation.component.ability.InnoAbility
import com.f14.innovation.component.ability.InnoAbilityGroup
import com.f14.innovation.component.ability.InnoAchieveAbility
import com.f14.innovation.executor.*
import com.f14.innovation.executor.custom.*
import com.f14.innovation.listener.*
import com.f14.innovation.listener.custom.*
import com.f14.innovation.param.InnoResultParam
import org.apache.log4j.Logger
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

object InnoClassFactory {
    private var log = Logger.getLogger(InnoClassFactory::class.java)

    private var classMap: MutableMap<String, KClass<*>> = HashMap()

    init {
        // EXECUTOR
        classMap["DRAW_CARD"] = InnoDrawCardExecutor::class
        classMap["ADD_HAND"] = InnoAddHandExecutor::class
        classMap["REMOVE_HAND"] = InnoRemoveHandExecutor::class
        classMap["REVEAL_HAND"] = InnoRevealHandExecutor::class
        classMap["MELD"] = InnoMeldExecutor::class
        classMap["TUCK"] = InnoTuckExecutor::class
        classMap["REMOVE_TOPCARD"] = InnoRemoveTopCardByCardExecutor::class
        classMap["ADD_SCORE"] = InnoAddScoreExecutor::class
        classMap["RESET_RESULT"] = InnoResetResultExecutor::class
        classMap["PICK_SCORE"] = InnoPickScoreExecutor::class
        classMap["REMOVE_SCORE"] = InnoRemoveScoreExecutor::class
        classMap["RETURN_CARD"] = InnoReturnCardExecutor::class
        classMap["EMPTY_EXECUTOR"] = InnoEmptyExecutor::class
        classMap["CHOOSE_CARD"] = InnoChooseCardExecutor::class
        // CHECKER
        classMap["CHECK_CARD"] = InnoCardChecker::class
        classMap["HAS_SCORE"] = InnoHasScoreCardChecker::class
        classMap["HAS_TOPCARD"] = InnoHasTopCardChecker::class
        classMap["CHECK_HAND_NUM"] = InnoHandNumChecker::class
        classMap["CHECK_SPLAY"] = InnoSplayChecker::class
        // LISTENER
        classMap["RETURN_HAND"] = InnoReturnHandListener::class
        classMap["CHOOSE_HAND"] = InnoChooseHandListener::class
        classMap["CHOOSE_SPLAY"] = InnoChooseSplayListener::class
        classMap["SPLAY_CONFIRM"] = InnoSplayListener::class
        classMap["CHOOSE_STACK"] = InnoChooseStackListener::class
        classMap["CHOOSE_SCORE"] = InnoChooseScoreListener::class
        classMap["PROCESS_ABILITY"] = InnoProcessAbilityListener::class

        // Customizing
        classMap["InnoCustom017Checker"] = InnoCustom017Checker::class
        classMap["InnoHasSameTopCardChecker"] = InnoHasSameTopCardChecker::class
        classMap["InnoCustom011Executor"] = InnoCustom011Executor::class
        classMap["InnoCustom018Executor"] = InnoCustom018Executor::class
        classMap["InnoCustom020Executor"] = InnoCustom020Executor::class
        classMap["InnoCustom027Executor"] = InnoCustom027Executor::class
        classMap["InnoCustom029Executor"] = InnoCustom029Executor::class
        classMap["InnoCustom031Executor"] = InnoCustom031Executor::class
        classMap["InnoCustom032Executor"] = InnoCustom032Executor::class
        classMap["InnoCustom033Executor"] = InnoCustom033Executor::class
        classMap["InnoCustom035Executor"] = InnoCustom035Executor::class
        classMap["InnoCustom037Executor"] = InnoCustom037Executor::class
        classMap["InnoCustom038Executor"] = InnoCustom038Executor::class
        classMap["InnoCustom046Executor"] = InnoCustom046Executor::class
        classMap["InnoCustom047Executor"] = InnoCustom047Executor::class
        classMap["InnoCustom048Executor"] = InnoCustom048Executor::class
        classMap["InnoCustom050Executor"] = InnoCustom050Executor::class
        classMap["InnoCustom051Executor"] = InnoCustom051Executor::class
        classMap["InnoCustom054Executor"] = InnoCustom054Executor::class
        classMap["InnoCustom057Executor"] = InnoCustom057Executor::class
        classMap["InnoCustom059Executor"] = InnoCustom059Executor::class
        classMap["InnoCustom060Executor"] = InnoCustom060Executor::class
        classMap["InnoCustom066Executor"] = InnoCustom066Executor::class
        classMap["InnoCustom072Executor"] = InnoCustom072Executor::class
        classMap["InnoCustom078Executor"] = InnoCustom078Executor::class
        classMap["InnoCustom083VictoryExecutor"] = InnoCustom083VictoryExecutor::class
        classMap["InnoCustom086Executor"] = InnoCustom086Executor::class
        classMap["InnoCustom088Executor"] = InnoCustom088Executor::class
        classMap["InnoCustom088VictoryExecutor"] = InnoCustom088VictoryExecutor::class
        classMap["InnoCustom089Executor"] = InnoCustom089Executor::class
        classMap["InnoCustom091Executor"] = InnoCustom091Executor::class
        classMap["InnoCustom094Executor"] = InnoCustom094Executor::class
        classMap["InnoCustom095Executor"] = InnoCustom095Executor::class
        classMap["InnoCustom096Executor"] = InnoCustom096Executor::class
        classMap["InnoCustom097VictoryExecutor"] = InnoCustom097VictoryExecutor::class
        classMap["InnoCustom098VictoryExecutor"] = InnoCustom098VictoryExecutor::class
        classMap["InnoCustom100Executor"] = InnoCustom100Executor::class
        classMap["InnoCustom102Executor"] = InnoCustom102Executor::class
        classMap["InnoCustom103VictoryExecutor"] = InnoCustom103VictoryExecutor::class
        classMap["InnoCustom104Executor"] = InnoCustom104Executor::class
        classMap["InnoCustom104VictoryExecutor"] = InnoCustom104VictoryExecutor::class
        classMap["InnoCustom105Executor"] = InnoCustom105Executor::class
        classMap["InnoCustom002Listener"] = InnoCustom002Listener::class
        classMap["InnoCustom003Listener"] = InnoCustom003Listener::class
        classMap["InnoCustom005Listener"] = InnoCustom005Listener::class
        classMap["InnoCustom006Listener"] = InnoCustom006Listener::class
        classMap["InnoCustom007Listener"] = InnoCustom007Listener::class
        classMap["InnoCustom011Listener"] = InnoCustom011Listener::class
        classMap["InnoCustom014Listener"] = InnoCustom014Listener::class
        classMap["InnoCustom015Listener"] = InnoCustom015Listener::class
        classMap["InnoCustom016Listener"] = InnoCustom016Listener::class
        classMap["InnoCustom021Listener"] = InnoCustom021Listener::class
        classMap["InnoCustom022Listener"] = InnoCustom022Listener::class
        classMap["InnoCustom024Listener"] = InnoCustom024Listener::class
        classMap["InnoCustom025Listener"] = InnoCustom025Listener::class
        classMap["InnoCustom026Listener"] = InnoCustom026Listener::class
        classMap["InnoCustom030Listener"] = InnoCustom030Listener::class
        classMap["InnoCustom034Listener"] = InnoCustom034Listener::class
        classMap["InnoCustom035Listener"] = InnoCustom035Listener::class
        classMap["InnoCustom039Listener"] = InnoCustom039Listener::class
        classMap["InnoCustom044Listener"] = InnoCustom044Listener::class
        classMap["InnoCustom045Listener"] = InnoCustom045Listener::class
        classMap["InnoCustom049Listener"] = InnoCustom049Listener::class
        classMap["InnoCustom053Listener"] = InnoCustom053Listener::class
        classMap["InnoCustom061Listener"] = InnoCustom061Listener::class
        classMap["InnoCustom063Listener"] = InnoCustom063Listener::class
        classMap["InnoCustom064Listener"] = InnoCustom064Listener::class
        classMap["InnoCustom068Listener"] = InnoCustom068Listener::class
        classMap["InnoCustom069Listener"] = InnoCustom069Listener::class
        classMap["InnoCustom070Listener"] = InnoCustom070Listener::class
        classMap["InnoCustom071Listener"] = InnoCustom071Listener::class
        classMap["InnoCustom073Listener"] = InnoCustom073Listener::class
        classMap["InnoCustom074Listener"] = InnoCustom074Listener::class
        classMap["InnoCustom076Listener"] = InnoCustom076Listener::class
        classMap["InnoCustom077Listener"] = InnoCustom077Listener::class
        classMap["InnoCustom080Listener"] = InnoCustom080Listener::class
        classMap["InnoCustom081Listener"] = InnoCustom081Listener::class
        classMap["InnoCustom082Listener"] = InnoCustom082Listener::class
        classMap["InnoCustom083Listener"] = InnoCustom083Listener::class
        classMap["InnoCustom089Listener"] = InnoCustom089Listener::class
        classMap["InnoCustom090Listener"] = InnoCustom090Listener::class
        classMap["InnoCustom091Listener"] = InnoCustom091Listener::class
        classMap["InnoCustom099Listener"] = InnoCustom099Listener::class
        classMap["InnoCustom101Listener"] = InnoCustom101Listener::class
        classMap["InnoCustom103Listener"] = InnoCustom103Listener::class
        classMap["InnoReturnAllHandListener"] = InnoReturnAllHandListener::class

        //Achieve
        classMap["InnoAchieveEmpireChecker"] = InnoAchieveEmpireChecker::class
        classMap["InnoAchieveMemorialChecker"] = InnoAchieveMemorialChecker::class
        classMap["InnoAchieveWonderChecker"] = InnoAchieveWonderChecker::class
        classMap["InnoAchieveWorldChecker"] = InnoAchieveWorldChecker::class
        classMap["InnoAchieveUniverseChecker"] = InnoAchieveUniverseChecker::class
    }

    /**
     * 创建InnoAchieveChecker
     * @param ability
     * @param gameMode
     * @return
     */
    fun createAchieveChecker(ability: InnoAchieveAbility, gameMode: InnoGameMode): InnoAchieveChecker = try {
        val clazz = getClass<KClass<InnoAchieveChecker>>(ability.achieveClass)!!
        clazz.primaryConstructor!!.call(gameMode)
//            val constructor = clazz.getConstructor(InnoGameMode::class.java)
//            constructor.newInstance(gameMode)
    } catch (e: Exception) {
        log.fatal("创建InnoAchieveChecker时发生错误: " + ability.achieveClass, e)
        throw BoardGameException("创建InnoAchieveChecker时发生错误: " + ability.achieveClass)
    }

    /**
     * 创建Checker
     * @param ability
     * @param gameMode
     * @param player
     * @param result
     * @return
     */
    fun createChecker(ability: InnoAbility, gameMode: InnoGameMode, player: InnoPlayer, result: InnoResultParam): InnoConditionChecker {
        return try {
            val clazz = getClass<KClass<InnoConditionChecker>>(ability.abilityClass)!!
            clazz.primaryConstructor!!.call(gameMode, player, ability.initParam, result, ability)
//            val constructor = clazz.getConstructor(
//                    InnoGameMode::class,
//                    InnoPlayer::class,
//                    InnoInitParam::class,
//                    InnoResultParam::class,
//                    InnoAbility::class
//            )
//            constructor.newInstance(gameMode, player, ability.initParam, result, ability)
        } catch (e: Exception) {
            log.fatal("创建InnoConditionChecker时发生错误: " + ability.abilityClass, e)
            throw BoardGameException("创建InnoConditionChecker时发生错误: " + ability.abilityClass)
        }

    }

    /**
     * 创建Executor
     * @param ability
     * @param gameMode
     * @param player
     * @param result
     * @return
     */
    fun createExecutor(ability: InnoAbility, gameMode: InnoGameMode, player: InnoPlayer, result: InnoResultParam, group: InnoAbilityGroup): InnoActionExecutor = try {
        val clazz = getClass<KClass<InnoActionExecutor>>(ability.abilityClass)!!
        clazz.primaryConstructor!!.call(gameMode, player, ability.initParam, result, ability, group)
//            val constructor = clazz!!.getConstructor(
//                    InnoGameMode::class,
//                    InnoPlayer::class,
//                    InnoInitParam::class,
//                    InnoResultParam::class,
//                    InnoAbility::class,
//                    InnoAbilityGroup::class
//            )
//            constructor.newInstance(gameMode, player, ability.initParam, result, ability, group)
    } catch (e: Exception) {
        log.fatal("创建InnoActionExecutor时发生错误: " + ability.abilityClass, e)
        throw BoardGameException("创建InnoActionExecutor时发生错误: " + ability.abilityClass)
    }

    /**
     * 创建Listener
     * @param ability
     * @param gameMode
     * @param abilityGroup
     * @param player
     * @param result
     * @return
     */
    fun createListener(ability: InnoAbility, gameMode: InnoGameMode, abilityGroup: InnoAbilityGroup, player: InnoPlayer, result: InnoResultParam): InnoInterruptListener = try {
        val clazz = getClass<KClass<InnoInterruptListener>>(ability.abilityClass)!!
        clazz.primaryConstructor!!.call(gameMode, player, ability.initParam, result, ability, abilityGroup)
//            val constructor = clazz!!.getConstructor(
//                    InnoGameMode::class,
//                    InnoPlayer::class,
//                    InnoInitParam::class,
//                    InnoResultParam::class,
//                    InnoAbility::class,
//                    InnoAbilityGroup::class
//            )
//            constructor.newInstance(gameMode, player, ability.initParam, result, ability, abilityGroup)
    } catch (e: Exception) {
        log.fatal("创建InnoInterruptListener时发生错误: " + ability.abilityClass, e)
        throw BoardGameException("创建InnoInterruptListener时发生错误: " + ability.abilityClass)
    }

    /**
     * 取得类
     * @param abilityClass
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    fun <C : KClass<*>> getClass(abilityClass: String): C? {
        val res = getClassCache(abilityClass)
        return res as? C?
    }

    private fun getClassCache(abilityClass: String): KClass<*>? {
        return classMap[abilityClass]
    }

}
