package com.f14.tichu.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.action.BgResponse
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.tichu.TichuGameMode
import com.f14.tichu.TichuPlayer
import com.f14.tichu.consts.TichuGameCmd
import com.f14.tichu.consts.TichuType
import java.util.*

class TichuResultListener(gameMode: TichuGameMode) : TichuActionListener(gameMode) {
    private lateinit var resultResponse: BgResponse

    init {
        this.createResultResponse()
    }

    /**
     * 创建回合结果的参数
     */
    private fun createResultResponse() {
        val res = this.createSubactResponse(null, "loadParam")
        // 将所有玩家按照排名排序
        val rankPlayers = ArrayList<TichuPlayer>()
        rankPlayers.addAll(gameMode.game.players)
        rankPlayers.sortWith(RankComparator())

        if (gameMode.isFriendlyPlayer(rankPlayers[0], rankPlayers[1])) {
            // 检查前面的2个玩家是否是同一组,如果是,则是双关
            // bothCatchGroup = rankPlayers.get(0).groupIndex;
            // 双关则是+200分
            val group = gameMode.getPlayerGroup(rankPlayers[0])
            gameMode.report.info(group.reportString + " 双关")
            group.bothCatchScore = 200
            // res.public("bothCatch" + bothCatchGroup, 200);
        } else {
            // 如果不是双关,则需要将最后1名的手牌中的分数给对家
            val lastPlayer = rankPlayers[3]
            val group = gameMode.getOppositeGroup(lastPlayer)
            group.addScore = lastPlayer.handScore

            // 最后1名的分数给头家
            rankPlayers[0].score += lastPlayer.score
            lastPlayer.score = 0
        }
        // 计算所有玩家的分数
        val playerResults = ArrayList<Map<String, Any>>()
        for (player in gameMode.game.players) {
            // 如果叫了大地主,则200分,小地主100分
            if (player.tichuType == TichuType.BIG_TICHU) {
                player.tichuScore = 200 * if (player.rank == 1) 1 else -1
                gameMode.report.action(player, "大地主" + if (player.rank == 1) "成功" else "失败")
            } else if (player.tichuType == TichuType.SMALL_TICHU) {
                player.tichuScore = 100 * if (player.rank == 1) 1 else -1
                gameMode.report.action(player, "小地主" + if (player.rank == 1) "成功" else "失败")
            }
            val map = player.toMap()
            playerResults.add(map)
        }

        // 计算回合得分
        for (group in gameMode.groups) {
            group.calculateRoundScore()
            gameMode.report.info(group.reportString + " 获得 " + group.roundScore + "分")
        }
        val groupResults = BgUtils.toMapList(gameMode.groups)

        res.public("playerResults", playerResults)
        res.public("groupResults", groupResults)

        this.resultResponse = res
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        val player = action.getPlayer<TichuPlayer>()
        this.setPlayerResponsed(player)
    }

    override val validCode: Int
        get() = TichuGameCmd.GAME_CODE_ROUND_RESULT

    override fun sendPlayerListeningInfo(receiver: TichuPlayer?) {
        super.sendPlayerListeningInfo(receiver)
        // 发送回合得分的指令
        gameMode.game.sendResponse(receiver, resultResponse)
    }

    /**
     * 排名排序对象

     * @author F14eagle
     */
    internal inner class RankComparator : Comparator<TichuPlayer> {

        override fun compare(o1: TichuPlayer, o2: TichuPlayer): Int {
            // rank=0则被双关,往后排
            val i1 = if (o1.rank != 0) o1.rank else 5
            val i2 = if (o2.rank != 0) o2.rank else 5
            return when {
                i1 > i2 -> 1
                i1 < i2 -> -1
                else -> 0
            }
        }

    }
}
