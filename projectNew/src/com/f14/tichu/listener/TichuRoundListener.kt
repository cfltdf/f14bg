package com.f14.tichu.listener

import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.tichu.TichuGameMode
import com.f14.tichu.TichuPlayer
import com.f14.tichu.componet.RoundManager
import com.f14.tichu.componet.TichuCardCheck
import com.f14.tichu.componet.TichuCardGroup
import com.f14.tichu.consts.AbilityType
import com.f14.tichu.consts.TichuGameCmd
import com.f14.tichu.consts.TichuType
import java.util.*

class TichuRoundListener(gameMode: TichuGameMode, startPlayer: TichuPlayer) : TichuOrderListener(gameMode, startPlayer) {

    private val rankPlayers: MutableList<TichuPlayer> = ArrayList()
    private val roundManager: RoundManager = RoundManager(gameMode)

    /**
     * 判断本轮的得分情况
     * @param gameMode
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun checkScore() {
        gameMode.game.clearAllPlayerPlayedCard()
        val score = this.roundManager.currentScore
        if (score != 0) {
            this.roundManager.lastCardGroup?.let { group ->
                val lastPlayer = group.owner
                // 如果最后一轮牌中有龙,则需要由最后出牌的玩家选择将分数交给谁
                if (group.hasCard(AbilityType.DRAGON)) {
                    val l = TichuScoreInterruptListener(gameMode, lastPlayer, score)
                    val param = gameMode.insertListener(l)
                    val targetPlayer = param.get<TichuPlayer>("target")
                    if (targetPlayer != null) {
                        gameMode.game.playerGetScore(targetPlayer, score)
                    }
                } else {
                    gameMode.game.playerGetScore(lastPlayer, score)
                }
            }
        }
        this.roundManager.newRound()
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {

        val subact = action.getAsString("subact")
        val player = action.getPlayer<TichuPlayer>()
        if ("bomb" == subact) {
            // 炸弹是可以抢出的,无需在自己的回合
            this.playBomb(action)
        } else {
            super.doAction(action)
            when (subact) {
                "play" -> // 出牌
                    this.playCard(action)
                "pass" -> // 跳过
                    this.pass(action)
                "smallTichu" -> // 叫小地主
                    gameMode.game.playerCallTichu(player, TichuType.SMALL_TICHU)
                else -> throw BoardGameException("无效的行动指令!")
            }
        }
    }

    override val validCode: Int
        get() = TichuGameCmd.GAME_CODE_ROUND_PHASE

    /**
     * 检查该玩家是否是最后一个pass的玩家
     * @param gameMode
     * @param player
     * @return
     */
    private fun isLastPassedPlayer(player: TichuPlayer): Boolean {
        val lastPlayer = this.roundManager.lastCardGroup?.owner ?: return false
        val players = gameMode.game.getPlayersByOrder(player).drop(1)
        return lastPlayer == players.firstOrNull { !this.isPlayerResponsed(it.position) }
//        for (it in players) {
//            if (it === lastPlayer) return true
//            // 如果该玩家没有回应,则继续查找下一个
//            if (!this.isPlayerResponsed(it.position)) return false
//        }
//        return false
    }

    /**
     * 玩家出牌后的行动
     * @param gameMode
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun onPlayedCard(player: TichuPlayer) {
        if (player.hasCard) {
            // 如果出牌后玩家还有手牌,则暂时完成行动
            this.setPlayerResponsedTemp(player)
        } else {
            // 如果没有手牌,则设置排名
            this.rankPlayers.add(player)
            val rank = this.rankPlayers.size
            gameMode.game.playerGetRank(player, rank)
            gameMode.report.playerGetRank(player, rank)
            when (rank){
                2 -> {
                    // 如果这次跑掉的是第2名,则检查第1名是否和这个玩家同一组,如果是则双关
                    if (gameMode.isFriendlyPlayer(*this.rankPlayers.toTypedArray())){
                        // 双关时玩家的得分都不算了
                        for (p in gameMode.game.players) p.score = 0
                        // 直接设置回合结束
                        this.setAllPlayerResponsed()
                    }
                }
                3 -> {
                    // 如果是最后一名,先结算当前轮的分数
                    this.checkScore()
                    // 则当前回合结束,设置所有玩家结束行动
                    val nextPlayer = this.getNextAvailablePlayer(player) ?: throw BoardGameException("UNSOLVED BUG!")
                    gameMode.game.playerGetRank(nextPlayer, 4)
                    gameMode.report.info("出牌步骤结束")
                    this.setAllPlayerResponsed()
                }
            }
        }
    }

    @Throws(BoardGameException::class)
    override fun onPlayerTurn(player: TichuPlayer) {
        super.onPlayerTurn(player)
        if (player.pass) {
            // 如果需要跳过,则设置玩家直接结束回合
            player.pass = false
            this.setPlayerResponsedTemp(player)
        } else {
            gameMode.game.sendSimpleResponse("turn", player)
        }
    }

    @Throws(BoardGameException::class)
    override fun onStartListen() {
        super.onStartListen()
        // 开始新的回合
        this.roundManager.newRound()
        gameMode.report.info("出牌步骤开始")
    }

    /**
     * 玩家跳过
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun pass(action: GameAction) {
        val player = action.getPlayer<TichuPlayer>()
        this.roundManager.checkPass(player)
        gameMode.game.playerPass(player)

        if (this.isLastPassedPlayer(player)) {
            // 如果是本轮最后一个跳过的玩家,则结算当前轮的分数
            checkScore()
        }
        // 暂时完成行动
        this.setPlayerResponsedTemp(player)
    }

    /**
     * 玩家抢出炸弹
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun playBomb(action: GameAction) {
        val player = action.getPlayer<TichuPlayer>()

        val check = TichuCardCheck(player, player.hands.cards)
        val bombs = check.bombs
        if (bombs.isEmpty()) {
            throw BoardGameException("你没有炸弹!")
        }
        // 该方法中检查的是在其他玩家的回合出炸弹的情况,所以这时炸弹不能空炸
        val lastGroup = this.roundManager.lastCardGroup
        if (lastGroup == null || lastGroup.hasCard(AbilityType.DOG)) {
            // 炸弹不能用来抢牌权
            throw BoardGameException("不能用炸弹抢出牌权!")
        } else {
            // 检查是否拥有比最后一轮大的炸弹,如果有,才能出
            if (bombs.none { it > lastGroup }) {
                throw BoardGameException("你没有比对方大的炸弹!")
            }
        }

        // 添加出炸弹的中断监听器
        val l = TichuBombActionListener(gameMode, player, this.roundManager)
        val param = gameMode.insertListener(l)
        // 如果是炸弹,则检查是否出了炸弹
        val bomb = param.get<TichuCardGroup>("bomb") ?: return
        // 执行出炸弹
        gameMode.game.playerPlayCards(player, bomb)
        this.roundManager.addCardGroup(bomb)
        // 并将当前玩家设置为出炸弹的玩家
        this.setCurrentListeningPlayer(player)
        // 然后由出炸弹的玩家的下家继续出牌
        this.onPlayedCard(player)
    }

    /**
     * 玩家出牌
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun playCard(action: GameAction) {
        val player = action.getPlayer<TichuPlayer>()
        val cardIds = action.getAsString("cardIds").takeUnless(String?::isNullOrEmpty)
                ?: throw BoardGameException("请选择要出的牌!")

        val cards = player.hands.getCards(cardIds).toMutableList()
        // 检查凤的规则
        this.roundManager.checkPhoenix(cards, action)

        // 整理组合
        val group = TichuCardGroup(player, cards)
        this.roundManager.checkPlayCard(group)

        // 执行出牌
        gameMode.game.playerPlayCards(player, group)
        this.roundManager.addCardGroup(group)

        // 出牌完成后,检查是否有雀/狗的效果
        when {
            this.roundManager.checkDog(cards) -> {
                // 有狗的话,给当前玩家的下家需要跳过出牌...
                val nextPlayer = gameMode.game.getNextPlayersByOrder(player)
                nextPlayer.pass = true
                roundManager.newRound()
            }
            this.roundManager.checkMahJong(cards) -> {
                // 如果有雀,则需要添加许愿的监听器
                val l = TichuWishInterruptListener(gameMode, player)
                val param = gameMode.insertListener(l)
                // 如果是许愿,则设置许愿的数
                val wishedPoint = param.getInteger("wishedPoint")
                gameMode.game.playerWishPoint(player, wishedPoint)
                // 当前玩家完成出牌行动
                this.onPlayedCard(player)
            }
            else -> {
                // 完成出牌
                this.onPlayedCard(player)
                // 检查出牌是否符合许愿,如果符合则取消许愿
                if (group.hasCard(gameMode.wishedPoint.toDouble())) {
                    gameMode.game.playerWishPoint(player, 0)
                }
            }
        }
    }

}
