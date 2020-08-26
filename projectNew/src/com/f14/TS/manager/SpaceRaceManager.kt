package com.f14.TS.manager

import com.f14.TS.TSGameMode
import com.f14.TS.TSPlayer
import com.f14.TS.action.TSEffect
import com.f14.TS.component.TSCard
import com.f14.TS.consts.EffectType
import com.f14.TS.consts.TSProperty
import com.f14.bg.exception.BoardGameException
import java.util.*

/**
 * Created by 吹风奈奈 on 2017/7/20.
 */

class SpaceRaceManager(private var gameMode: TSGameMode) {

    private var spaceRaceRanks: MutableMap<Int, SpaceRaceRank> = LinkedHashMap()

    init {
        this.init()
    }

    /**
     * 添加太空竞赛阶段

     * @param rank
     */
    private fun addSpaceRaceRank(rank: SpaceRaceRank) {
        this.spaceRaceRanks[rank.rank] = rank
    }

    /**
     * 执行太空竞赛

     * @param player

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkRoll(player: TSPlayer, roll: Int): Boolean {
        val rank = this.getNextRank(player) ?: throw BoardGameException("你不能再进行太空竞赛了!")
        return roll <= rank.maxRoll
    }

    /**
     * 检查玩家是否可以进行太空竞赛

     * @param player

     * @param card

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun checkSpaceRace(player: TSPlayer, card: TSCard) {
        val rank = this.getNextRank(player)
        val spaceRaceChance = player.availableSpaceRaceTimes
        if (spaceRaceChance <= 0) {
            throw BoardGameException("本回合不能再进行太空竞赛了!")
        }
        if (rank == null) {
            throw BoardGameException("你不能再进行太空竞赛了!")
        }
        if (player.getOp(card) < rank.op) {
            throw BoardGameException("选择的牌行动点数不足,不能进行太空竞赛!")
        }
    }

    /**
     * 检查并设置所有玩家应有的太空竞赛的特权效果
     */
    fun checkSpaceRacePrivilege() {
        for (player in this.gameMode.game.players) {
            // 取得玩家应有的特权
            val availablePrivileges = this.getAvailablePrivileges(player)
            // 遍历所有的特权
            this.spaceRaceRanks.values.filter(SpaceRaceRank::hasPrivilege).forEach {
                if (availablePrivileges.contains(it)) {
                    // 如果玩家应该有该特权
                    if (!player.hasCardEffect(it.card!!)) {
                        // 如果玩家还没有该特权,则给玩家添加该特权
                        player.addEffect(it.card!!, it.effect!!)
                    }
                } else {
                    // 如果玩家不应该有该特权
                    if (player.hasCardEffect(it.card!!)) {
                        // 如果玩家已经有该特权,则从玩家移除该特权
                        player.removeEffect(it.card!!)
                    }
                }
            }
        }
    }

    /**
     * 取得玩家拥有的所有特权

     * @param player

     * @return
     */

    private fun getAvailablePrivileges(player: TSPlayer): List<SpaceRaceRank> {
        val opposite = gameMode.game.getOppositePlayer(player.superPower)
        // 取得各自的太空竞赛等级
        val psr = player.getProperty(TSProperty.SPACE_RACE)
        val osr = opposite.getProperty(TSProperty.SPACE_RACE)
        // 只有当自己的太空竞赛等级比对手高时,才可能会有特权
        return (osr + 1..psr).map { spaceRaceRanks[it]!! }.filter(SpaceRaceRank::hasPrivilege)
    }

    /**
     * 取得玩家太空竞赛当前等级

     * @param player

     * @return
     */
    private fun getCurrentRank(player: TSPlayer): SpaceRaceRank? {
        val rank = player.getProperty(TSProperty.SPACE_RACE)
        return this.spaceRaceRanks[rank]
    }

    /**
     * 取得玩家太空竞赛的下一等级

     * @param player

     * @return
     */
    private fun getNextRank(player: TSPlayer): SpaceRaceRank? {
        val rank = player.getProperty(TSProperty.SPACE_RACE) + 1
        return this.spaceRaceRanks[rank]
    }

    /**
     * 初始化
     */
    private fun init() {
        if (!gameMode.game.config.isNewSpaceRace) {
            var rank = SpaceRaceRank()
            rank.rank = 1
            rank.op = 2
            rank.maxRoll = 3
            rank.addVp(2, 1)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 2
            rank.op = 2
            rank.maxRoll = 4
            rank.createTSEffect(EffectType.SR_PRIVILEGE_1)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 3
            rank.op = 2
            rank.maxRoll = 3
            rank.addVp(2, 0)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 4
            rank.op = 2
            rank.maxRoll = 4
            rank.createTSEffect(EffectType.SR_PRIVILEGE_2)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 5
            rank.op = 3
            rank.maxRoll = 3
            rank.addVp(3, 1)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 6
            rank.op = 3
            rank.maxRoll = 4
            rank.createTSEffect(EffectType.SR_PRIVILEGE_3)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 7
            rank.op = 3
            rank.maxRoll = 3
            rank.addVp(4, 2)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 8
            rank.op = 4
            rank.maxRoll = 2
            rank.addVp(2, 0)
            rank.createTSEffect(EffectType.SR_PRIVILEGE_4)
            this.addSpaceRaceRank(rank)
        } else {
            var rank = SpaceRaceRank()
            rank.rank = 1
            rank.op = 2
            rank.maxRoll = 3
            rank.addVp(2, 1)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 2
            rank.op = 2
            rank.maxRoll = 4
            rank.createTSEffect(EffectType.SR_PRIVILEGE_N1)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 3
            rank.op = 2
            rank.maxRoll = 3
            rank.addVp(2, 0)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 4
            rank.op = 2
            rank.maxRoll = 4
            rank.createTSEffect(EffectType.SR_PRIVILEGE_N2)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 5
            rank.op = 3
            rank.maxRoll = 3
            rank.addVp(3, 1)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 6
            rank.op = 3
            rank.maxRoll = 4
            rank.createTSEffect(EffectType.SR_PRIVILEGE_N3)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 7
            rank.op = 3
            rank.maxRoll = 3
            rank.createTSEffect(EffectType.SR_PRIVILEGE_N4)
            this.addSpaceRaceRank(rank)

            rank = SpaceRaceRank()
            rank.rank = 8
            rank.op = 4
            rank.maxRoll = 2
            rank.addVp(4, 2)
            this.addSpaceRaceRank(rank)
        }
    }

    /**
     * 取得太空竞赛得到的VP

     * @param player

     * @return
     */
    fun takeVp(player: TSPlayer): Int {
        val rank = this.getCurrentRank(player)
        return if (rank != null && rank.vp.isNotEmpty()) {
            // 按顺序取得VP
            rank.vp.removeAt(0)
        } else {
            0
        }
    }

    /**
     * 太空竞赛的各个阶段

     * @author F14eagle
     */
    private inner class SpaceRaceRank {
        var rank: Int = 0
        var op: Int = 0
        var maxRoll: Int = 0
        var vp: MutableList<Int> = ArrayList()
        var card: TSCard? = null
        var effect: TSEffect? = null

        /**
         * 添加VP
         * @param vps
         */
        fun addVp(vararg vps: Int) {
            vps.forEach { this.vp.add(it) }
        }

        /**
         * 创建特权效果...
         * @param effectType
         */
        fun createTSEffect(effectType: EffectType) {
            this.card = TSCard()
            this.effect = TSEffect()
            this.effect!!.effectType = effectType
        }

        /**
         * 判断该太空竞赛等级是否有特权
         * @return
         */
        fun hasPrivilege() = this.card != null
    }
}
