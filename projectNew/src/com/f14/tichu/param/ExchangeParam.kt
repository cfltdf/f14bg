package com.f14.tichu.param

import com.f14.tichu.TichuPlayer
import com.f14.tichu.componet.TichuCard
import java.util.*

/**
 * 交易参数
 * @author F14eagle
 */
class ExchangeParam {
    private val map: MutableMap<TichuPlayer, MutableMap<TichuPlayer, TichuCard>> = HashMap()

    /**
     * 添加卡牌
     * @param receiver 接收牌的玩家
     * @param owner    牌的所有者
     * @param card     交易的牌
     */
    fun addCard(receiver: TichuPlayer, owner: TichuPlayer, card: TichuCard) {
        this.getPlayerCards(receiver)[owner] = card
    }

    /**
     * 取得指定玩家收到的牌
     * @param player
     * @return
     */
    fun getPlayerCards(player: TichuPlayer): MutableMap<TichuPlayer, TichuCard> {
        return map.computeIfAbsent(player) { LinkedHashMap() }
    }

    /**
     * 清空参数
     */
    fun clear() {
        map.clear()
    }
}
