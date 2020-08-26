package com.f14.PuertoRico.game.listener

import com.f14.PuertoRico.consts.GameCmdConst
import com.f14.PuertoRico.game.PRGameMode
import com.f14.PuertoRico.game.PRPlayer
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException


/**
 * 使用扩充时,在游戏开始前所有玩家选择所使用的建筑

 * @author F14eagle
 */
class ChooseBuildingListener(gameMode: PRGameMode) : PROrderActionListener(gameMode) {

    override fun createPhaseStartCommand(): BgResponse {
        val res = super.createPhaseStartCommand()
        // 需要将所有建筑列表以及选择的建筑信息发送到客户端
        res.public("buildings", gameMode.buildingPool.allBuildings)
        res.public("selectedBuildings", gameMode.buildingPool.selectedBuildings)
        return res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        val player = action.getPlayer<PRPlayer>()
        val cardNo = action.getAsString("cardNo")
        val tile = gameMode.buildingPool.chooseBuilding(cardNo, player.name)
        gameMode.game.sendChooseBuildingResponse(player, cardNo)
        gameMode.report.chooseBuilding(player, tile)
        // 如果已经选择完所有的建筑,则将所有玩家设为已回应状态
        if (gameMode.buildingPool.isSelectedBuildingFull) {
            this.setAllPlayerResponsed()
        } else {
            // 否则就设为暂时完成
            this.setPlayerResponsedTemp(player)
        }
    }

    override val validCode: Int
        get() = GameCmdConst.GAME_CODE_CHOOSE_BUILDING_PHASE

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        super.onAllPlayerResponsed()
        // 将选择的建筑添加到建筑池中
        gameMode.buildingPool.initBuildingPool()
        gameMode.game.sendBuildingInfo()
        gameMode.report.system("选择建筑完成!")
    }
}
