package com.f14.TTA

import com.f14.TTA.consts.CivilizationProperty
import java.util.*

/**
 * TTA的游戏排名
 * @author 吹风奈奈
 */
class TTAGameRank(val players: MutableList<TTAPlayer>, var currentPlayer: TTAPlayer) {


    /**
     * @return
     */
    val playerNumber: Int
        get() = synchronized(players) {
            return players.size
        }

    /**
     * 按照指定的属性取得玩家的排名
     * @param player
     * @param property
     * @return
     */
    fun getPlayerRank(player: TTAPlayer, property: CivilizationProperty, weekest: Boolean): Int {
        val idx = this.getPlayersByRank(property).indexOf(player)
        return if (weekest) this.getPlayers().size - idx else idx + 1
    }

    /**
     * @return
     */
    fun getPlayers(): Collection<TTAPlayer> = synchronized(players) {
        return players
    }

    /**
     * 按照指定的属性取得玩家的排名状况
     * @param property
     * @return
     */
    fun getPlayersByRank(property: CivilizationProperty) = getPlayers().sortedWith(CivilizationComparator(property, currentPlayer))

    /**
     * @param player
     */
    fun removePlayer(player: TTAPlayer) {
        synchronized(players) {
            players.remove(player)
        }
    }

    /**
     * 玩家文明排名的比较器
     * @author F14eagle
     */
    internal inner class CivilizationComparator
    /**
     * 比较用的属性,暂时只处理CULTURE和MILITARY
     * @param property
     */
    (
            /**
             * 比较用的属性
             */
            val property: CivilizationProperty,
            /**
             * 当前玩家
             */
            val currentPlayer: TTAPlayer) : Comparator<TTAPlayer> {

        override fun compare(o1: TTAPlayer, o2: TTAPlayer): Int {
            var i1: Int
            var i2: Int
            when (property) {
                CivilizationProperty.CULTURE // 文明点数来排名
                -> {
                    i1 = o1.culturePoint
                    i2 = o2.culturePoint
                }
                CivilizationProperty.MILITARY -> {
                    i1 = o1.defenceMilitary
                    i2 = o2.defenceMilitary
                }
                else // 其他都按属性值排行
                -> {
                    i1 = o1.getProperty(property)
                    i2 = o2.getProperty(property)
                }
            }
            when {
                i1 > i2 -> return -1
                i1 < i2 -> return 1
                else -> {
                    // 点数相同时,按当前玩家的顺位排序
                    i1 = o1.position
                    if (i1 < this.currentPlayer.position) {
                        i1 += 10
                    }
                    i2 = o2.position
                    if (i2 < this.currentPlayer.position) {
                        i2 += 10
                    }
                    // 顺位靠前的玩家比较强力
                    return when {
                        i1 > i2 -> 1
                        i1 < i2 -> -1
                        else -> 0
                    }
                }
            }
        }

    }

}
