package com.f14.RFTG.listener

import com.f14.RFTG.RacePlayer
import com.f14.RFTG.card.Ability
import com.f14.RFTG.consts.GameState
import com.f14.RFTG.consts.RaceActionType
import com.f14.RFTG.mode.RaceGameMode
import com.f14.RFTG.network.CmdConst
import com.f14.RFTG.network.CmdFactory
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.CheckUtils
import com.f14.utils.StringUtils
import java.util.*

/**
 * 银河竞逐选择行动的监听器基类
 * @author F14eagle
 */
class ChooseActionListener(gameMode: RaceGameMode) : RaceActionListener(gameMode) {

    /**
     * 创建玩家的选择行动参数
     * @param player
     * @return
     */
    private fun createChooseParam(player: RacePlayer): ChooseParam {
        val p = ChooseParam()
        this.setParam(player.position, p)
        return p
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<RacePlayer>()
        val p = this.createChooseParam(player)
        if (p.types.isNotEmpty()) throw BoardGameException("不能重复选择行动!")
        // 取得行动
        val actionCode = action.getAsString("actionCode").takeUnless(String::isNullOrEmpty)
                ?: throw BoardGameException("请选择行动!")
        val actions = StringUtils.string2List(actionCode).map(RaceActionType.Companion::getActionType).toTypedArray()
        // 如果行动数大于1,则检查是否直接选择了第2张开发或扩张指令,如果是,则替换成第1张
        if (gameMode.actionNum > 1) {
            if (RaceActionType.DEVELOP !in actions) {
                val i = actions.indexOf(RaceActionType.DEVELOP_2)
                if (i >= 0) actions[i] = RaceActionType.DEVELOP
            }
            if (RaceActionType.SETTLE !in actions) {
                val i = actions.indexOf(RaceActionType.SETTLE_2)
                if (i >= 0) actions[i] = RaceActionType.SETTLE
            }
        }
        if (gameMode.state != GameState.CHOOSE_ACTION || player.state != GameState.CHOOSE_ACTION) {
            throw BoardGameException("状态错误,不能执行该行动!")
        }
        if (actions.size != gameMode.actionNum) throw BoardGameException("选择行动出错!")
        // 判断游戏状态是否允许执行该行动
        CheckUtils.check(!actions.all(gameMode::isActionValid), "无效的行动!")
        // 选择行动并通知客户端
        p.types.addAll(actions)
        // 设置玩家行动完成
        this.setPlayerResponsed(player)
    }


    override val ability: Class<Ability>?
        get() = null

    override val validCode: Int
        get() = CmdConst.GAME_CODE_CHOOSE_ACTION

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        // 将所有玩家选择的行动返回到客户端
        gameMode.game.players.map { it to this.getParam<ChooseParam>(it.position).types }.forEach { (p, types) ->
            p.actionTypes.addAll(types)
            gameMode.report.playerChooseAction(p, types)
            CmdFactory.createGameResultResponse(CmdConst.GAME_CODE_SHOW_ACTION, p.position)
                    .public("actionTypes", StringUtils.list2String(p.actionTypes))
                    .send(gameMode)
        }
    }

    internal inner class ChooseParam(val types: MutableList<RaceActionType> = ArrayList())
}
