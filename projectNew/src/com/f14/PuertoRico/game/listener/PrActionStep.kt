package com.f14.PuertoRico.game.listener

import com.f14.PuertoRico.consts.GameCmdConst
import com.f14.PuertoRico.game.PRPlayer
import com.f14.PuertoRico.game.PrConfig
import com.f14.PuertoRico.game.PrReport
import com.f14.bg.action.BgResponse
import com.f14.bg.listener.ActionListener
import com.f14.bg.listener.ActionStep


abstract class PrActionStep(listener: ActionListener<PRPlayer, PrConfig, PrReport>) : ActionStep<PRPlayer, PrConfig, PrReport>(listener) {

    override fun createStepStartResponse(player: PRPlayer): BgResponse {
        val res = super.createStepStartResponse(player)
        res.private("message", this.message)
        return res
    }

    override val actionCode: Int
        get() = GameCmdConst.GAME_CODE_COMMON_CONFIRM

    /**
     * 取得步骤开始时的提示信息

     * @return
     */
    protected abstract val message: String

    // @Override
    // protected void onStepStart(PRGameMode mode, PRPlayer) throws
    // BoardGameException {
    // BgResponse res =
    // CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_COMMON_CONFIRM,
    // player.position);
    // res.private("stepCode", this.getStepCode());
    // res.private("message", this.getMessage());
    // player.sendResponse(res);
    // }

    // @Override
    // protected void onStepOver(PRGameMode mode, PRPlayer) throws
    // BoardGameException {
    // BgResponse res =
    // CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_COMMON_CONFIRM,
    // player.position);
    // res.private("stepCode", this.getStepCode());
    // res.private("ending", true);
    // player.sendResponse(res);
    // }
}
