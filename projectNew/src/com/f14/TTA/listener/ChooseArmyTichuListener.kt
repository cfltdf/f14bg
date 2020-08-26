package com.f14.TTA.listener

import com.f14.TTA.TTAGameMode
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.component.param.AuctionParam
import com.f14.TTA.consts.TTAGameCmd
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.listener.InterruptParam
import com.f14.bg.utils.CheckUtils

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class ChooseArmyTichuListener(gameMode: TTAGameMode, trigPlayer: TTAPlayer, private var tichuCard: TTACard) : ChooseArmyListener(gameMode, trigPlayer) {
    private var topPlayer: TTAPlayer? = null

    override fun beforeListeningCheck(player: TTAPlayer): Boolean {
        // 到玩家回合时,如果当前玩家是拍卖最高价的玩家时,则无需进行拍卖
        return this.topPlayer !== player
    }

    /**
     * 玩家确认拍卖
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun confirm(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        CheckUtils.check(player === this.topPlayer, "不能对自己出价!")
        val param = this.getParam<AuctionParam>(player.position)
        if (this.topPlayer != null) {
            // 如果存在最高出价的玩家,则需要检查出价是否高于他
            CheckUtils.check(param.getTotalValue() <= this.currentAuctionValue, "让分必须大于当前出价者!")
        }
        gameMode.report.playerBid(player, param.getTotalValue())
        // 出价成功,暂时完成回应,等待下一玩家出价
        this.topPlayer = player
        param.inputing = false
        this.setPlayerResponsedTemp(player)
        // 向所有玩家刷新当前出价的信息
        this.sendPlayerAuctionInfo(player, null)
    }

    override fun createAuctionInfoResponse(receiver: TTAPlayer?): BgResponse {
        val res = super.createAuctionInfoResponse(receiver)
        // 设置拍卖的殖民地
        res.public("showCardId", this.tichuCard.id)
        return res
    }


    override fun createInterruptParam(): InterruptParam {
        val result = super.createInterruptParam()
        if (this.topPlayer != null) {
            val param = this.getParam<AuctionParam>(this.topPlayer!!.position)
            result["topPlayer"] = this.topPlayer
            result[this.topPlayer!!.position] = param
        }
        return result
    }

    /**
     * 取得当前拍卖值
     * @return
     */
    private val currentAuctionValue: Int
        get() {
            return if (this.topPlayer == null) {
                0
            } else {
                this.getPlayerAuctionValue(this.topPlayer!!)
            }
        }

    override val validCode: Int
        get() = TTAGameCmd.GAME_CODE_AUCTION_TERRITORY

    override val isTichu: Boolean
        get() = true

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        this.sendAutionEnd(null)
        super.onAllPlayerResponsed()
    }

    /**
     * 玩家结束拍卖
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    override fun pass(action: GameAction) {
        val player = action.getPlayer<TTAPlayer>()
        val param = this.getParam<AuctionParam>(player.position)
        param.pass = true
        param.inputing = false
        this.setPlayerResponsed(player)
        // 向所有玩家刷新当前出价的信息
        this.sendPlayerAuctionInfo(player, null)
    }
}
