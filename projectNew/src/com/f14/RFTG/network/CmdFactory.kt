package com.f14.RFTG.network

import com.f14.bg.action.BgResponse
import com.f14.bg.utils.BgUtils


/**
 * 指令工厂类
 * @author F14eagle
 */
object CmdFactory {

    /**
     * 创建玩家的卡牌能力生效的指令
     * @param position
     * @param cardIds
     * @return
     */
    fun createCardEffectResponse(position: Int, cardIds: String): BgResponse{
        val ids = BgUtils.string2Array(cardIds)
        return BgResponse(CmdConst.GAME_CMD, CmdConst.GAME_CODE_CARD_EFFECT, position)
                .public("cardIds", cardIds)
                .public("cardNum", ids.size)
    }

    /**
     * 创建玩家直接打出牌的指令
     * @param position
     * @param cardIds
     * @return
     */
    fun createDirectPlayCardResponse(position: Int, cardIds: String): BgResponse {
        val ids = BgUtils.string2Array(cardIds)
        return BgResponse(CmdConst.GAME_CMD, CmdConst.GAME_CODE_DIRECT_PLAY_CARD, position)
                .public("cardIds", cardIds)
                .public("cardNum", ids.size)
    }

    /**
     * 创建玩家弃掉货物的指令
     * @param position
     * @param cardIds
     * @return
     */

    fun createDiscardGoodResponse(position: Int, cardIds: String): BgResponse {
        val ids = BgUtils.string2Array(cardIds)
        return BgResponse(CmdConst.GAME_CMD, CmdConst.GAME_CODE_DISCARD_GOOD, position)
                .public("cardIds", cardIds)
                .public("cardNum", ids.size)
    }

    /**
     * 创建玩家弃掉已打出卡牌的指令
     * @param position
     * @param cardIds
     * @return
     */
    fun createDiscardPlayedCardResponse(position: Int, cardIds: String): BgResponse {
        val ids = BgUtils.string2Array(cardIds)
        return BgResponse(CmdConst.GAME_CMD, CmdConst.GAME_CODE_DISCARD_PLAYED_CARD, position)
                .public("cardIds", cardIds)
                .public("cardNum", ids.size)
    }

    /**
     * 创建玩家摸牌的指令
     * @param position
     * @param cardIds
     * @return
     */
    fun createDiscardResponse(position: Int, cardIds: String): BgResponse {
        val ids = BgUtils.string2Array(cardIds)
        return BgResponse(CmdConst.GAME_CMD, CmdConst.GAME_CODE_DISCARD, position)
                .public("cardIds", cardIds)
                .public("cardNum", ids.size)
    }

    /**
     * 创建玩家摸牌的指令
     * @param position
     * @param cardIds
     * @return
     */
    fun createDrawCardResponse(position: Int, cardIds: String): BgResponse {
        val ids = BgUtils.string2Array(cardIds)
        return BgResponse(CmdConst.GAME_CMD, CmdConst.GAME_CODE_DRAW_CARD, position)
                .public("cardIds", cardIds)
                .public("cardNum", ids.size)
    }

    /**
     * 创建请求输入的游戏指令
     * @param code
     * @param position
     * @return
     */
    fun createGameResponse(code: Int, position: Int): BgResponse {
        return BgResponse(CmdConst.GAME_CMD, code, position)
    }

    /**
     * 创建对输入回应的游戏指令
     * @param code
     * @param position
     * @return
     */
    fun createGameResultResponse(code: Int, position: Int): BgResponse {
        return BgResponse(CmdConst.GAME_CMD, code, position)
    }

    /**
     * 创建玩家得到VP的指令
     * @param position
     * @param vp       得到的vp
     * @param remainvp 游戏剩余的vp
     * @return
     */
    fun createGetVPResponse(position: Int, vp: Int, remainvp: Int): BgResponse {
        return BgResponse(CmdConst.GAME_CMD, CmdConst.GAME_CODE_GET_VP, position)
                .public("vp", vp)
                .public("remainvp", remainvp)
    }

    /**
     * 创建玩家从手牌中打出牌的指令
     * @param position
     * @param cardIds
     * @return
     */
    fun createPlayCardResponse(position: Int, cardIds: String): BgResponse {
        val ids = BgUtils.string2Array(cardIds)
        return BgResponse(CmdConst.GAME_CMD, CmdConst.GAME_CODE_PLAY_CARD, position)
                .public("cardIds", cardIds)
                .public("cardNum", ids.size)
    }

    /**
     * 创建星球生产货物的指令
     * @param position
     * @param cardIds
     * @return
     */
    fun createProduceGoodResponse(position: Int, cardIds: String): BgResponse {
        val ids = BgUtils.string2Array(cardIds)
        return BgResponse(CmdConst.GAME_CMD, CmdConst.GAME_CODE_PRODUCE_GOOD, position)
                .public("cardIds", cardIds)
                .public("cardNum", ids.size)
    }

    /**
     * 创建玩家使用卡牌的指令
     * @param position
     * @param cardIds
     * @return
     */
    fun createUseCardResponse(position: Int, cardIds: String): BgResponse {
        val ids = BgUtils.string2Array(cardIds)
        return BgResponse(CmdConst.GAME_CMD, CmdConst.GAME_CODE_USE_CARD, position)
                .public("cardIds", cardIds)
                .public("cardNum", ids.size)
    }
}
