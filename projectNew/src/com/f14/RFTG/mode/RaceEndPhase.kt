package com.f14.RFTG.mode

import com.f14.RFTG.RacePlayer
import com.f14.RFTG.card.BonusAbility
import com.f14.RFTG.card.RaceCard
import com.f14.RFTG.consts.GoalType
import com.f14.RFTG.consts.Skill
import com.f14.bg.GameEndPhase
import com.f14.bg.VPCounter
import com.f14.bg.VPResult
import org.apache.log4j.Logger

/**
 * 结束阶段
 * @author F14eagle
 */
class RaceEndPhase : GameEndPhase<RaceGameMode>() {

    override fun createVPResult(gameMode: RaceGameMode): VPResult {
//        val mostvps = HashMap<RacePlayer, VPCounter>()
        // 如果使用goal,则计算是否存在平局的MOST目标,所有平局的玩家能得到3VP
        if (gameMode.game.config.isUseGoal) {
            for (goal in gameMode.goalManager.getGoals(GoalType.MOST)) {
                val gvs = goal.getGoalPlayers(gameMode.game.players)
                if (gvs.isNotEmpty()) {
                    val goalPlayer: RacePlayer? = goal.currentGoalValue?.player
                    // 存在平局玩家时,如果该玩家不是该goal的拥有着,则添加到缓存中
                    for (gv in gvs.filterNot { it.player === goalPlayer }) gv.player.goals.add(goal)
//                        mostvps.computeIfAbsent(gv.player) { VPCounter(gv.player) }
//                                .addVp(goal.name, goal.vp)
                }
            }
        }
        val result = VPResult(gameMode.game)
        for (player in gameMode.game.players) {
            log.debug("玩家 [" + player.user.name + "] 的分数:")
            val vpc = VPCounter(player)
            vpc.addVp("VP(筹码)", player.vp)
            vpc.addVp("VP(设施/星球)", this.getBuiltVP(player))
            // 计算有额外VP功能的牌的得分
            for (card in player.getCardsByAbilityType(BonusAbility::class.java)) {
                val vp = this.getBonus(player, card)
                if (vp != 0) vpc.addVp(card.name, vp)
            }
            // 如果使用goal,则计算玩家goal的分数
            if (gameMode.game.config.isUseGoal) {
                for (goal in player.goals) vpc.addVp(goal.name, goal.vp)
                // 计算MOST平局产生的分数
//                val vpct = mostvps[player]
//                mostvps[player]?.primaryVps?.forEach { vpc.addVp(it.label, it.vp) }
//                if (vpct != null && !vpct.primaryVps.isEmpty()) {
//                    for (v in vpct.primaryVps) vpc.addVp(v.label, v.vp)
//                }
            }
            val totalVP = vpc.totalVP
            log.debug("总计 : $totalVP")
            vpc.addSecondaryVp("手牌+货物", player.handSize + player.goods.size)
            result += vpc
        }
        return result
    }

    /**
     * 取得指定卡牌能得到的额外VP
     * @param player
     * @return
     */
    private fun getBonus(player: RacePlayer, bonusCard: RaceCard) = bonusCard.bonusAbilities.groupBy(BonusAbility::skill).entries.sumBy { (skill, abilities) ->
        when (skill) {
            null -> player.builtCards.mapNotNull { card -> abilities.firstOrNull { it test card } }.sumBy(BonusAbility::vp)
            Skill.VP_BONUS_MILITARY -> player.baseMilitary // 额外VP - 军事力 - 计算所有星球的军事力
            Skill.VP_BONUS_CHIP_PER_VP -> player.vp / abilities.first().vp  // 额外VP - VP筹码 - 每chip个筹码得1VP
            Skill.VP_BONUS_DIFFERENT_KINDS_WORLD -> { // 额外VP - 不同类型的星球 - 每种货物类型的星球得分
                val goodTypes = player.builtCards.mapNotNull(RaceCard::goodType).distinct()
                mapOf(1 to 1, 2 to 2, 3 to 6, 4 to 10)[goodTypes.size] ?: 0
            }
            else -> 0
        }
    }

    /**
     * 取得玩家所有设施和星球的VP
     * @param player
     * @return
     */
    private fun getBuiltVP(player: RacePlayer) = player.builtCards.sumBy(RaceCard::vp)

    companion object {
        private val log = Logger.getLogger(RaceEndPhase::class.java)!!
    }
}
