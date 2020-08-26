package com.f14.TTA.factory

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.ability.EventAbility
import com.f14.TTA.consts.EventType
import com.f14.TTA.listener.TTAEventListener
import com.f14.TTA.listener.event.*


object TTAListenerFactory {
    /**
     * 创建事件监听器的函数
     * @param trigPlayer
     * @return
     */
    fun createEventListener(gameMode: TTAGameMode, ability: EventAbility?, trigPlayer: TTAPlayer): TTAEventListener? {
        if (ability != null) {
            when (ability.eventType) {
                EventType.FOOD_RESOURCE // 选择食物/资源
                -> return ChooseResourceListener(gameMode, ability, trigPlayer)
                EventType.BUILD // 建造
                -> return BuildListener(gameMode, ability, trigPlayer)
                EventType.LOSE_POPULATION // 失去人口
                -> return LosePopulationListener(gameMode, ability, trigPlayer)
                EventType.DESTORY // 摧毁
                -> return DestoryListener(gameMode, ability, trigPlayer)
                EventType.LOSE_COLONY // 失去殖民地
                -> return ChooseColonyListener(gameMode, ability, trigPlayer)
                EventType.FLIP_WONDER // 废弃奇迹
                -> return FlipWonderListener(gameMode, ability, trigPlayer)
                EventType.DESTORY_OTHERS // 摧毁其他玩家的建筑
                -> return DestroyOthersListener(gameMode, ability, trigPlayer)
                EventType.INQUISITION // 宗教审判
                -> return InquisitionListener(gameMode, ability, trigPlayer)
                EventType.POLITICS_STRENGTH // 新版强权政治
                -> return PoliticsStrengthListener(gameMode, ability, trigPlayer)
                EventType.DEVELOP_OF_CIVILIZATION // 新版文明发展
                -> return DevOfCivListener(gameMode, ability, trigPlayer)
                EventType.FERE_MIGRATION // 自由搬迁
                -> return FreeMigrationListener(gameMode, ability, trigPlayer)
                else -> {
                }
            }
        }
        return null
    }
}
