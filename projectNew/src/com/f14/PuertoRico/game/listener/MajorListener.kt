package com.f14.PuertoRico.game.listener

import com.f14.PuertoRico.component.PrPartPool
import com.f14.PuertoRico.consts.Ability
import com.f14.PuertoRico.consts.Character
import com.f14.PuertoRico.consts.GameCmdConst
import com.f14.PuertoRico.consts.Part
import com.f14.PuertoRico.game.PRGameMode
import com.f14.PuertoRico.game.PRPlayer
import com.f14.bg.action.BgAction.GameAction
import com.f14.bg.exception.BoardGameException
import com.f14.utils.StringUtils
import kotlin.math.max

class MajorListener(gameMode: PRGameMode) : PROrderActionListener(gameMode) {

    /**
     * 自动为玩家分配移民,返回是否分配成功
     * @param gameMode
     * @param player
     * @return
     * @throws BoardGameException
     */
    private fun autoDispatch(player: PRPlayer): Boolean {
        // 如果玩家剩余的移民数大于等于空闲的移民数,则自动为玩家填满所有的建筑和种植园
        val empty = player.emptyAllColonistNum
        if (player.colonist >= empty) {
            for (tile in player.tiles.cards) {
                tile.colonistNum = tile.colonistMax
            }
            player.colonist -= empty
            // 将玩家的移民分配情况发送到客户端
            gameMode.game.sendPlayerColonistInfo(player)
            gameMode.report.doMajor(player)
            return true
        }
        return false
    }

    override fun beforeListeningCheck(player: PRPlayer) = // 执行自动分配,如果可以自动分配,则不需要回应
            !this.autoDispatch(player)

    @Throws(BoardGameException::class)
    override fun beforeStartListen() {
        // 阶段开始前,给所有玩家分配移民
        val colonistNum = gameMode.getAvailablePartNum(Part.SHIP_COLONIST)
        val players = gameMode.game.playersByOrder
        val dispatcher = ColonistDispatcher(players, colonistNum)
        for (player in players) {
            var num = dispatcher.getColonistNum(player)
            // 选择市长的玩家可以从资源堆拿1个移民
            if (player.character == Character.MAYOR) {
                num += gameMode.partPool.takePart(Part.COLONIST)
                // 如果拥有双倍特权则再拿1个移民
                if (player.canUseDoublePriv) {
                    num += gameMode.partPool.takePart(Part.COLONIST)
                }
            }
            // 如果玩家拥有得到移民的能力,则从资源堆拿1个移民
            if (player.hasAbility(Ability.COLONIST_1)) {
                num += gameMode.partPool.takePart(Part.COLONIST)
            }
            player.colonist += num
            // 发送得到移民的消息
            val parts = PrPartPool()
            parts.putPart(Part.COLONIST, num)
            gameMode.game.sendPlayerGetPartResponse(player, parts, 1)
            gameMode.report.getColonist(player, num)
            // 检查玩家是否使用了双倍特权
            player.checkUsedDoublePriv()
        }
        // 清空船上的移民
        gameMode.partPool.takePartAll(Part.SHIP_COLONIST)
        // 将移民信息发送给客户端
        gameMode.game.sendColonistInfo()
    }

    /**
     * 判断玩家是否可以结束市长阶段
     * @param player
     * @return
     */
    private fun canEndPhase(player: PRPlayer) = // 玩家必须分配完所有可以分配的移民才能结束回合
            player.colonist <= 0 || !player.hasEmptyTile()

    /**
     * 分配移民船上的移民数量
     * @param gameMode
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun dispatchColonist() {
        val tnum = gameMode.game.players.sumBy(PRPlayer::emptyBuildingColonistNum)
        // 统计所有玩家建筑空闲的移民数
        // 移民数最小也要等于当前玩家数
        val needNum = max(gameMode.game.currentPlayerNumber, tnum)
        // 实际能取得的移民数量
        val realNum = gameMode.partPool.takePart(Part.COLONIST, needNum)
        if (needNum > realNum) {
            // 如果移民不够上船,则设置移民数量不够的标记
            gameMode.notEnoughColonist = true
        }
        gameMode.partPool.putPart(Part.SHIP_COLONIST, realNum)
        gameMode.report.getColonistShip(realNum)
        // 将移民信息发送给客户端
        gameMode.game.sendColonistInfo()
    }

    @Throws(BoardGameException::class)
    override fun doAction(action: GameAction) {
        super.doAction(action)
        val player = action.getPlayer<PRPlayer>()
        val subact = action.getAsString("subact")
        if ("major" == subact) {
            // 分配移民,并设置回应
            this.major(action)
            this.setPlayerResponsed(player)
        } else {
            throw BoardGameException("无效的行动代码!")
        }
    }

    override val validCode: Int
        get() = GameCmdConst.GAME_CODE_MAJOR

    /**
     * 玩家进行移民分配的行动
     * @param gameMode
     * @param action
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun major(action: GameAction) {
        val player = action.getPlayer<PRPlayer>()
        // 检查输入参数和移民数量是否正确
        val ids = action.getAsString("ids")
        val nums = action.getAsString("nums")
        val restNum = action.getAsInt("restNum")
        val tiles = player.tiles.getCards(ids)
        val colonistNums = StringUtils.string2int(nums)
        if (tiles.size != colonistNums.size) {
            throw BoardGameException("参数数量错误!")
        }
        var totalColonistNum = restNum
        for (i in colonistNums.indices) {
            if (tiles[i].colonistMax < colonistNums[i]) {
                throw BoardGameException("超出建筑允许的移民上限!")
            }
            totalColonistNum += colonistNums[i]
        }
        if (totalColonistNum != player.totalColonist) {
            throw BoardGameException("分配移民数量错误!")
        }
        // 为玩家分配移民
        player.colonist = restNum
        for (i in colonistNums.indices) {
            tiles[i].colonistNum = colonistNums[i]
        }
        // 将玩家的移民分配情况发送到客户端
        gameMode.game.sendPlayerColonistInfo(player)

        // 检查是否可以结束回合
        if (!this.canEndPhase(player)) {
            throw BoardGameException("你必须分配所有可以分配的移民!")
        }
        gameMode.report.doMajor(player)
    }

    @Throws(BoardGameException::class)
    override fun onAllPlayerResponsed() {
        // 给移民船分配移民
        this.dispatchColonist()
    }

    /**
     * 分配移民
     * @author F14eagle
     */
    internal inner class ColonistDispatcher
    /**
     * 构造函数
     * @param players          参与分配的玩家
     * @param totalColonistNum 参与分配的移民总数
     */
    (players: List<PRPlayer>, totalColonistNum: Int) {
        val colonistMap: Map<PRPlayer, Int> = (0 until totalColonistNum).groupBy { it % players.count() }.mapValues { it.value.count() }.mapKeys { players[it.key] }

        /**
         * 取得玩家分配后移民数
         * @param player
         * @return
         */
        fun getColonistNum(player: PRPlayer): Int {
            return colonistMap[player] ?: 0
        }
    }
}
