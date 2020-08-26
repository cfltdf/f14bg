package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.executor.TSAdjustInfluenceExecutor
import com.f14.TS.factory.InitParamFactory
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException


class TSSetupListener(gameMode: TSGameMode) : TSOrderListener(gameMode) {

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {

    }

    /*
     * protected void onStartListen(TSGameMode mode) throws
	 * BoardGameException { super.onStartListen(mode);
	 * //mode.getReport().system(player.getReportString() + "开始放置影响力...");
	 * //玩家回合开始时,为玩家创建分配影响力的中断监听器 for(TSPlayer player :
	 * mode.getGame().getPlayersByOrder()){ ActionInitParam initParam =
	 * InitParamFactory.createSetupInfluence(player.superPower);
	 * TSAdjustInfluenceListener l = new TSAdjustInfluenceListener(player,
	 * mode, initParam); this.insertInterrupteListener(l, mode); } }
	 */

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_SETUP_PHASE

    @Throws(BoardGameException::class)
    override fun onPlayerTurn(player: TSPlayer) {
        super.onPlayerTurn(player)
        // 玩家回合开始时,为玩家创建分配影响力的监听器
        val initParam = InitParamFactory.createSetupInfluence(player.superPower)!!
        val executor = TSAdjustInfluenceExecutor(player, gameMode, initParam)
        executor.execute()
        // 完成后,设置玩家为已回应状态
        this.setPlayerResponsed(player)
    }

}
