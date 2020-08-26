package com.f14.TS.listener

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.component.TSCard
import com.f14.TS.consts.TSCmdString
import com.f14.TS.consts.TSGameCmd
import com.f14.TS.listener.initParam.OPActionInitParam
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam


/**
 * 使用OP执行行动的监听器

 * @author F14eagle
 */
class TSOpActionListener(trigPlayer: TSPlayer, gameMode: TSGameMode, initParam: OPActionInitParam) : TSParamInterruptListener(trigPlayer, gameMode, initParam) {
    private var action: String? = null

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        super.beforeStartListen()
        // 为监听的玩家创建参数
        // for(TSPlayer p : this.getListeningPlayers()){
        // CoupParam param = new CoupParam((TSPlayer)p);
        // this.setParam(p, param);
        // }
    }

    @Throws(BoardGameException::class)
    override fun confirmCheck(action: GameAction) {
    }

    override fun createInterruptParam(): InterruptParam {
        val res = super.createInterruptParam()
        res["action"] = this.action
        return res
    }

    /**
     * 执行放置影响力的行动

     * @param gameMode

     * @param action

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doAddInfluenceAction(action: GameAction) {
        val iparam = this.opActionInitParam
        if (!iparam.canAddInfluence) {
            throw BoardGameException("你不能进行这个行动!")
        }
        this.action = TSCmdString.ACTION_ADD_INFLUENCE
        val player = action.getPlayer<TSPlayer>()
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doConfirm(action: GameAction) {
    }

    /**
     * 执行政变的行动

     * @param gameMode

     * @param action

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doCoupAction(action: GameAction) {
        val iparam = this.opActionInitParam
        if (!iparam.canCoup) {
            throw BoardGameException("你不能进行这个行动!")
        }
        val player = action.getPlayer<TSPlayer>()
        this.action = TSCmdString.ACTION_COUP
        this.setPlayerResponsed(player)
    }

    /**
     * 执行调整阵营的行动

     * @param gameMode

     * @param action

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun doRealignmentAction(action: GameAction) {
        val iparam = this.opActionInitParam
        if (!iparam.canRealignment) {
            throw BoardGameException("你不能进行这个行动!")
        }
        this.action = TSCmdString.ACTION_REALIGNMENT
        val player = action.getPlayer<TSPlayer>()
        this.setPlayerResponsed(player)
    }

    @Throws(BoardGameException::class)
    override fun doSubact(action: GameAction) {
        when (action.getAsString("subact")) {
            TSCmdString.ACTION_ADD_INFLUENCE -> this.doAddInfluenceAction(action)
            TSCmdString.ACTION_REALIGNMENT -> this.doRealignmentAction(action)
            TSCmdString.ACTION_COUP -> this.doCoupAction(action)
        }
    }


    override val actionString: String
        get() = TSCmdString.ACTION_SELECT_CARD


    private val opActionInitParam: OPActionInitParam
        get() = super.initParam as OPActionInitParam

    override fun getMsg(player: TSPlayer): String {
        return this.opActionInitParam.msg.replace("""\{num\}""".toRegex(), this.getOP(player, null).toString())
    }

    /**
     * 取得实际用于该监听器的OP值

     * @param player

     * @return
     */
    private fun getOp(player: TSPlayer): Int {
        return if (this.opActionInitParam.num > 0) {
            // 如果有设置过num,则取num
            this.opActionInitParam.num
        } else {
            // 否则取得所用卡牌的OP值
            player.getOp(this.selectedCard!!)
        }
    }

    /**
     * 取得选中的卡牌

     * @return
     */

    private val selectedCard: TSCard?
        get() = this.opActionInitParam.card

    override val validCode: Int
        get() = TSGameCmd.GAME_CODE_OP_ACTION
}
