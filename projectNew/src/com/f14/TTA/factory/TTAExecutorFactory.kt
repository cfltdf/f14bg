package com.f14.TTA.factory

import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.ActiveAbility
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.component.card.*
import com.f14.TTA.component.param.RoundParam
import com.f14.TTA.consts.*
import com.f14.TTA.executor.*
import com.f14.TTA.executor.action.*
import com.f14.TTA.executor.active.*
import com.f14.TTA.executor.event.*
import com.f14.TTA.executor.war.*
import com.f14.bg.exception.BoardGameException


/**
 * 创建处理器的代码

 * @author 吹风奈奈
 */
object TTAExecutorFactory {

    fun createActionCardExecutor(param: RoundParam, card: ActionCard): TTAActionCardExecutor {
        return when (card.actionAbility.abilityType) {
            ActionAbilityType.BUILD_WONDER // 工程天才
            -> TTAActionBuildWonderExecutor(param, card)
            ActionAbilityType.INCREASE_POPULATION // 节俭
            -> TTAActionIncreasePopExecutor(param, card)
            ActionAbilityType.SCORE // 直接给玩家提供分数/科技/资源/食物的黄牌
            -> TTAActionInstantScoreExecutor(param, card, card.actionAbility.property)
            ActionAbilityType.TEMPLATE_PROPERTY // 爱国等
            -> TTAActionTempPropertyExecutor(param, card, card.actionAbility.property)
            ActionAbilityType.SCORE_BY_RANK // 按照排名得分
            -> TTAActionInstantScoreExecutor(param, card, param.player.getFinalRankValue(card))
            ActionAbilityType.TEMPLATE_PROPERTY_BY_RANK // 按照排名得到临时资源(民浪、军建)
            -> TTAActionTempPropertyExecutor(param, card, param.player.getFinalRankValue(card))
            ActionAbilityType.BUILD // 建造
            -> TTAActionRequestBuildExecutor(param, card)
            ActionAbilityType.UPGRADE // 升级
            -> TTAActionRequestUpgradeExecutor(param, card)
            ActionAbilityType.BUILD_OR_UPGRADE // 新版建造或升级
            -> TTAActionBuildOrUpgradeExecutor(param, card)
            ActionAbilityType.PLAY_CARD // 打手牌
            -> TTAActionRequestPlayCardExecutor(param, card)
            ActionAbilityType.TAKE_CARD // 从卡牌列拿牌
            -> TTAActionTakeCardExecutor(param, card)
            ActionAbilityType.PA_LA_PORXADA // 谷物交易
            -> TTAActionExtrangeExecutor(param, card)
            ActionAbilityType.SCORE_BETWEEN // 新版储备
            -> TTAActionScoreBetweenExecutor(param, card)
            else -> throw BoardGameException("建造器创造失败!")
        }
    }

    fun createActiveExecutor(param: RoundParam, card: TTACard): TTAActiveExecutor {
        return when (card.activeAbility!!.abilityType) {
            ActiveAbilityType.INCREASE_POPULATION // 扩张人口
            -> TTAActiveIncreasePopExecutor(param, card)
            ActiveAbilityType.INCREASE_UNIT // 扩张人口+建造部队
            -> TTAActiveIncreaesUnitExecutor(param, card)
            ActiveAbilityType.PLAY_TERRITORY // 直接打出殖民地
            -> TTAActivePlayTerritoryExecutor(param, card)
            ActiveAbilityType.PLAY_EVENT // 直接打出事件牌
            -> TTAActivePlayEventExecutor(param, card)
            ActiveAbilityType.GET_LEADER // 直接获得领袖
            -> TTAActiveGetLeaderExecutor(param, card)
            ActiveAbilityType.TRADE_RESOURCE // 交易能力
            -> TTAActiveTradeExecutor(param, card)
            ActiveAbilityType.PA_CHARLEMAGNE // 查理曼
            -> TTAActiveCharlemagneExecutor(param, card)
            ActiveAbilityType.INSTANT_SCORE // 立即获得
            -> TTAActiveInstantScoreTokenExecutor(param, card)
            ActiveAbilityType.PA_BLOOD_AND_THREAT // 新版丘吉尔
            -> TTAActiveBloodAndThreatExecutor(param, card)
            ActiveAbilityType.TAKE_CARD // 获取卡牌
            -> TTAActiveTakeCardExecutor(param, card)
            ActiveAbilityType.PA_BUILD_WONDER_POP // 用人口造奇迹
            -> TTAActiveBuildWonderPopExecutor(param, card)
            ActiveAbilityType.TRADE_RESOURCE_CHOICE // 交换粮食和资源
            -> TTAActiveTradeResourceChoiceExecutor(param, card)
            ActiveAbilityType.PA_XIAOPING // 小平
            -> TTAActiveXiaoPingExecutor(param, card)
            ActiveAbilityType.PA_HEPINGCHENG // 和平城
            -> TTAActiveHepingChengExecutor(param, card)
            ActiveAbilityType.PA_HUBATIAN // 翔霸天
            -> TTAActiveHubatianExecutor(param, card)
            ActiveAbilityType.PA_WESTPOINT // 西点军校
            -> TTAActiveWestPointExecutor(param, card)
            ActiveAbilityType.PA_EXTRANGE // 交换
            -> TTAActiveExtrangeExecutor(param, card)
            ActiveAbilityType.PA_HYPPOCRATES -> TTAActiveHypocratesExecutor(param, card)
            ActiveAbilityType.PA_BONDICA -> TTAActiveBondicaExecutor(param, card)
            ActiveAbilityType.PA_CONFUCIUS -> TTAActiveConfuciusExecutor(param, card)
            ActiveAbilityType.PA_GUTENBURG -> TTAActiveGutenburgExecutor(param, card)
            ActiveAbilityType.PA_CATHARINE -> TTAActiveCatharineExecutor(param, card)
            ActiveAbilityType.RAISE_OLYMPICS -> TTARaiseOlympicsExecutor(param, card)
            ActiveAbilityType.VIEW_MILITARY_HAND -> TTAViewMilitaryHandExecutor(param, card)
            ActiveAbilityType.PA_LOUVRE -> TTAActiveLouvreExecutor(param, card)
            ActiveAbilityType.PA_RED_CROSS -> TTAActiveRedCrossExecutor(param, card)
            else -> throw BoardGameException("建造器创造失败!")
        }
    }

    fun createBuildExecutor(param: RoundParam, card: TTACard): TTABuildExecutor {
        return when (card.cardType) {
            CardType.WONDER -> TTABuildWonderExecutor(param, card as WonderCard)
            CardType.BUILDING, CardType.PRODUCTION ->
                // 建造建筑和矿场农场用的是内政行动点
                TTABuildCivilExecutor(param, card as TechCard)
            CardType.UNIT -> TTABuildUnitExecutor(param, card as TechCard)
            else -> throw BoardGameException("建造器创造失败!")
        }
    }

    fun createEventAbilityExecutor(param: RoundParam, ability: EventAbility): TTAEventAbilityExecutor {
        return when (ability.trigType) {
            EventTrigType.INSTANT -> TTAInstantAbilityExecutor(param, ability)
            EventTrigType.ALTERNATE -> {
                when (ability.eventType) {
                    EventType.FOOD_RESOURCE // 选择食物/资源
                    -> return TTAEventChooseResourceExecutor(param, ability)
                    EventType.BUILD // 建造
                    -> return TTAEventBuildExecutor(param, ability)
                    EventType.LOSE_POPULATION // 失去人口
                    -> return TTAEventLosePopulationExecutor(param, ability)
                    EventType.DESTORY // 摧毁
                    -> return TTAEventDestoryExecutor(param, ability)
                    EventType.LOSE_COLONY // 失去殖民地
                    -> return TTAEventChooseColonyExecutor(param, ability)
                    EventType.TAKE_CARD // 拿牌
                    -> return TTAEventTakeCardExecutor(param, ability)
                    EventType.FLIP_WONDER // 废弃奇迹
                    -> return TTAEventFlipWonderExecutor(param, ability)
                    EventType.DESTORY_OTHERS // 摧毁其他玩家的建筑
                    -> return TATEventDestroyOthersExecutor(param, ability)
                    EventType.INQUISITION // 宗教审判
                    -> return TTAEventInquisitionExecutor(param, ability)
                    EventType.POLITICS_STRENGTH // 新版强权政治
                    -> return TTAEventPoliticsStrengthExecutor(param, ability)
                    EventType.DEVELOP_OF_CIVILIZATION // 新版文明发展
                    -> return TTAEventDevOfCivExecutor(param, ability)
                    EventType.FERE_MIGRATION // 自由搬迁
                    -> return TTAEventFreeMigrationExecutor(param, ability)
                    else -> throw BoardGameException("建造器创造失败!")
                }
            }
            else -> throw BoardGameException("建造器创造失败!")
        }
    }

    fun createEventCardExecutor(param: RoundParam, card: MilitaryCard): TTAEventCardExecutor {
        return when (card.cardSubType) {
            CardSubType.EVENT // 事件
            -> TTAEventExecutor(param, card as EventCard)
            CardSubType.TERRITORY // 殖民地
            -> TTATerrotoryExecutor(param, card as EventCard)
            else -> TTAConfuciousEventExecuter(param, card)
        }
    }

    fun createPlayCardExecutor(param: RoundParam, card: TTACard): TTAPlayCardExecutor {
        return when (card.cardType) {
            CardType.LEADER // 领袖
            -> TTAPlayLeaderCardExecutor(param, card as LeaderCard)
            CardType.ACTION // 行动牌
            -> TTAPlayActionCardExecutor(param, card as ActionCard)
            CardType.GOVERMENT // 政府
            -> TTARequestChangeGovernmentExecutor(param, card as GovermentCard)
            CardType.BUILDING, // 建筑
            CardType.PRODUCTION, // 农矿场
            CardType.UNIT, // 部队
            CardType.SPECIAL // 特殊科技
            -> TTAPlayTechCardExecutor(param, card as TechCard)
            CardType.TACTICS // 战术牌
            -> TTAPlayTacticsCardExecutor(param, card as TacticsCard)
            else -> throw BoardGameException("不能打出这张牌!!")
        }
    }

    fun createPoliticalExecutor(param: RoundParam, card: TTACard): TTAPoliticalCardExecutor {
        return when (card.cardType) {
            CardType.EVENT // 事件和殖民地牌
            -> TTAPlayEventExecutor(param, card as EventCard)
            CardType.AGGRESSION, // 侵略
            CardType.WAR // 战争
            -> TTAPlayWarCardExecutor(param, card as AttackCard)
            CardType.PACT // 条约
            -> TTAPlayPactCardExecutor(param, card as PactCard)
            else -> throw BoardGameException("不能打出这张牌!!")
        }
    }

    fun createWarResultExecutor(param: RoundParam, card: AttackCard, winner: TTAPlayer, loser: TTAPlayer, advantage: Int): TTAWarResultExecutor {
        return when (card.loserEffect.trigType) {
            EventTrigType.INSTANT -> TTAInstantWarResultExecutor(param, card, winner, loser, advantage)
            EventTrigType.ALTERNATE -> {
                when (card.loserEffect.eventType) {
                    EventType.LOSE_POPULATION // 失去人口
                    -> return TTAWarLosePopulationExecutor(param, card, winner, loser, advantage)
                    EventType.FOOD_RESOURCE // 夺取资源
                    -> return TTAWarChooseResourceExecutor(param, card, winner, loser, advantage)
                    EventType.LOSE_COLONY // 夺取殖民地
                    -> return TTAWarChooseColonyExecutor(param, card, winner, loser, advantage)
                    EventType.DESTORY // 拆除别人建筑
                    -> return TTAWarDestroyOthersExecutor(param, card, winner, loser, advantage)
                    EventType.DESTORY_MAX_LEVEL // 拆除别人多种建筑
                    -> return TTAWarDestroyOthersLimitExecutor(param, card, winner, loser, advantage)
                    EventType.CHOOSE_INFILTRATE // 刺杀或破坏
                    -> return TTAWarInfiltrateExecutor(param, card, winner, loser, advantage)
                    EventType.CHOOSE_CARD // 抢夺卡牌
                    -> return TTAWarChooseCardExecutor(param, card, winner, loser, advantage)
                    EventType.LOSE_SCIENCE // 失去科技
                    -> return TTAWarChooseScienceExecutor(param, card, winner, loser, advantage)
                    else -> throw BoardGameException("建造器创造失败!")
                }
            }
            else -> throw BoardGameException("建造器创造失败!")
        }
    }

    fun createWillExecutor(param: RoundParam, card: TTACard, ability: ActiveAbility): TTAWillExecutor {
        return when (ability.abilityType) {
            ActiveAbilityType.PA_CHOPIN -> TTAWillChopinExecutor(param, card)
            ActiveAbilityType.PA_HOMER -> TTAWillHomerExecutor(param, card)
            ActiveAbilityType.PA_NEW_GATES_ABILITY -> TTAWillNewGatesExecutor(param, card)
            ActiveAbilityType.PA_SUNTZU -> TTAWillSuntzuExecutor(param, card)
            ActiveAbilityType.PA_BUILD -> TTAWillBuildExecutor(param, card)
            ActiveAbilityType.PA_GETWONDER -> TTAWillGetWonderExecutor(param, card)
            ActiveAbilityType.PA_SUNTZU2 -> TTAWillSuntzu2Executor(param, card)
            ActiveAbilityType.PA_AQUITANE -> TTAWillAquitaneExecutor(param, card)
            ActiveAbilityType.PA_NOBEL -> TTAWillNobelExecutor(param, card)
            else -> throw BoardGameException("建造器创造失败!")
        }
    }
}
