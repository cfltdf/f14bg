package com.f14.PuertoRico.game

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.PuertoRico.component.PRTile
import com.f14.PuertoRico.component.PrPartPool
import com.f14.PuertoRico.consts.Ability
import com.f14.PuertoRico.consts.Character
import com.f14.PuertoRico.consts.GameCmdConst
import com.f14.PuertoRico.consts.Part
import com.f14.bg.FixedOrderBoardGame
import com.f14.bg.action.BgResponse
import com.f14.bg.consts.BgVersion
import com.f14.bg.exception.BoardGameException
import com.f14.bg.utils.BgUtils
import com.f14.utils.StringUtils
import net.sf.json.JSONObject

/**
 * Puerto Rico

 * @author F14eagle
 */
class PuertoRico : FixedOrderBoardGame<PRPlayer, PrConfig, PrReport>() {
    override val mode
        get() = gameMode
    lateinit var gameMode: PRGameMode

    var governor: PRPlayer? = null

    /**
     * 取得当前回合属于的玩家
     * @return
     */
    var roundPlayer: PRPlayer? = null

    /**
     * 玩家选择角色并发送回应到客户端
     * @param player
     * @param cardId
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun chooseCharacter(player: PRPlayer, cardId: String) {
        val card = this.gameMode.getCharacterCard(cardId)
        if (!card.isCanUse) {
            throw BoardGameException("不能选择该角色!")
        }
        player.character = card.character
        this.sendChooseCharacterResponse(player, cardId)

        var doubloon = card.doubloon
        // 2010-02-21 该能力在选择船长时直接触发
        if (card.character == Character.CAPTAIN) {
            // 如果拥有运货得到金钱的能力,则得到1个金钱
            if (player.hasAbility(Ability.DOUBLOON_SHIP)) {
                doubloon += 1
                // 如果拥有双倍特权则再得到1个金钱
                if (player.canUseDoublePriv) {
                    doubloon += 1
                }
            }
        }
        this.getDoubloon(player, doubloon)
        this.report.chooseCharacter(player, card)

        card.isCanUse = false
        card.doubloon = 0
    }

    /**
     * 创建建筑的信息
     */

    private fun createBuildingsInfoResponse(): BgResponse {
        return CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_REFRESH_BUILDING, -1).public("buildings", this.gameMode.buildingPool.toMap())
    }

    /**
     * 创建角色卡的详细信息
     */

    private fun createCharacterCardInfoResponse(): BgResponse {
        val cards = BgUtils.toMapList(this.gameMode.ccards.cards)
        return CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_REFRESH_CHARACTER, -1).public("characterCards", cards)
    }

    /**
     * 创建所有关于移民的信息
     * @return
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    private fun createColonistInfo(): BgResponse {
        val parts = arrayOf(Part.SHIP_COLONIST, Part.COLONIST).map { it.toString() to gameMode.partPool.getAvailableNum(it) }.toMap()
//        parts[.toString()] = gameMode.partPool.getAvailableNum(Part.SHIP_COLONIST)
//        parts[.toString()] = gameMode.partPool.getAvailableNum(Part.COLONIST)
        return CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_REFRESH_PART, -1).public("parts", parts)

    }


    @Throws(BoardGameException::class)
    override fun createConfig(obj: JSONObject): PrConfig {
        val config = PrConfig()
        config.versions.add(BgVersion.BASE)
        val versions = obj.getString("versions")
        if (!versions.isNullOrEmpty()) {
            val vs = BgUtils.string2Array(versions)
            config.versions.addAll(vs)
        }
        if (obj.getBoolean("random") && config.isBaseGame) {
            throw BoardGameException("只有选择扩充游戏后才能随机选择建筑!")
        }
        if (config.versions.size > 1) {
            // 如果有选择扩充,则设置是否随机选择建筑参数
            config.isRandom = obj.getBoolean("random")
        }
        return config
    }

    /**
     * 创建配件的信息
     */
    private fun createPartsInfoResponse(): BgResponse {
        val map = this.gameMode.partPool.resources.toMap() + mapOf("vp" to gameMode.totalVp) + arrayOf(Part.QUARRY, Part.COLONIST, Part.SHIP_COLONIST).map { it.toString() to gameMode.getAvailablePartNum(it) }.toMap()
//        map[Part.QUARRY.toString()] = this.gameMode.getAvailablePartNum(Part.QUARRY)
//        map[Part.COLONIST.toString()] = this.gameMode.getAvailablePartNum(Part.COLONIST)
//        map[Part.SHIP_COLONIST.toString()] = this.gameMode.getAvailablePartNum(Part.SHIP_COLONIST)
//        map["vp"] = this.gameMode.totalVp
        return CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_REFRESH_PART, -1).public("parts", map)

    }

    /**
     * 创建种植园板块的信息
     * @return
     */
    private fun createPlantationsInfoResponse(): BgResponse {
        return CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_REFRESH_PLANTATIONS, -1).public("cardIds", BgUtils.card2String(this.gameMode.plantations.cards))

    }

    /**
     * 创建玩家行动的信息
     * @return
     */
    private fun createPlayerActionInfoResponse(): BgResponse {
        val list = players.map {
            mapOf("position" to it.position, "character" to (it.character
                    ?: ""), "governer" to (it === this.governor), "currentRound" to (it === this.roundPlayer))
        }
//        for (player in this.players) {
//            val map = HashMap<String, Any>()
//            map["position"] = player.position
//            map["character"] = player.character ?: ""
//            map["governor"] = player === this.governor
//            map["currentRound"] = player === this.roundPlayer
//            list.add(map)
//        }
        return CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_SHOW_ACTION, -1).public("players", list)

    }

    /**
     * 将玩家的移民分配情况发送到客户端
     * @param player
     */
    private fun createPlayerColonistInfo(player: PRPlayer): BgResponse {
        val maps = player.tiles.cards.map { mapOf("id" to it.id, "colonist" to it.colonistNum) }
        return CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_COLONIST_INFO, player.position).public("restNum", player.colonist).public("details", maps)
    }

    /**
     * 创建货船的信息
     */
    private fun createShipsInfoResponse(): BgResponse {
        return CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_REFRESH_SHIP, -1).public("ships", this.gameMode.shipPort.ships.values)

    }

    /**
     * 创建交易所的信息
     */
    private fun createTradeHouseInfoResponse(): BgResponse {
        return CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_REFRESH_TRADEHOUSE, -1).public("goods", StringUtils.list2String(gameMode.tradeHouse.goods))
    }


    /**
     * 玩家得到金钱
     * @param player
     * @param doubloon
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getDoubloon(player: PRPlayer, doubloon: Int) {
        player.doubloon += doubloon
        this.sendGetDoubloonResponse(player, doubloon)
    }

    /**
     * 从当前回合玩家开始取得所有玩家的序列
     * @return
     */
    override val playersByOrder: List<PRPlayer>
        get() = this.getPlayersByOrder(this.roundPlayer!!)


    /**
     * 玩家得到VP并发送信息到客户端
     * @param player
     * @param vp
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun getVP(player: PRPlayer, vp: Int) {
        player.vp += vp
        this.gameMode.totalVp -= vp
        this.sendGetVPResponse(player, vp, this.gameMode.totalVp)
    }

    override fun initConfig() {
        this.config = PrConfig()
        this.config.versions.add(BgVersion.BASE)
    }

    override fun initConst() {

    }

    override fun initReport() {
        super.report = PrReport(this)
    }

    /**
     * 前进到下一玩家的回合,如果当前玩家为空,则返回总督

     * @return
     */

    fun nextPlayerRound(): PRPlayer {
        if (this.roundPlayer == null) {
            this.roundPlayer = this.governor
        } else {
            this.roundPlayer = this.getNextPlayer(this.roundPlayer)
        }
        return this.roundPlayer!!
    }

    /**
     * 刷新玩家的资源状态

     * @param player
     */
    fun refreshPlayerResource(player: PRPlayer) {
        CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_REFRESH_PLAYER_RESOURCE, player.position).public("resources", player.resources.resources).send(this)
    }

    /**
     * 重新排列玩家,并设定总督
     */
    override fun regroupPlayers() {
        super.regroupPlayers()
        this.governor = this.players[0]
        this.roundPlayer = this.governor
    }

    /**
     * 发送建筑板块信息到客户端

     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendBuildingInfo() {
        this.createBuildingsInfoResponse().send(this)
    }

    /**
     * 发送角色卡的详细信息
     */
    fun sendCharacterCardInfo() {
        this.createCharacterCardInfoResponse().send(this)
    }

    /**
     * 发送玩家选择建筑的信息
     * @param player
     * @param cardNo
     */
    fun sendChooseBuildingResponse(player: PRPlayer, cardNo: String) {
        CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_CHOOSE_BUILDING, player.position).public("cardNo", cardNo).public("userName", player.name).send(this)
    }

    /**
     * 发送选择角色的回应
     * @param player
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendChooseCharacterResponse(player: PRPlayer, cardId: String) {
        CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_CHOOSE_CHARACTER, player.position).public("cardId", cardId).send(this)
    }

    /**
     * 发送所有关于移民的信息
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendColonistInfo() {
        this.createColonistInfo().send(this)
    }

    /**
     * 向玩家发送游戏信息
     * @param receiver
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    public override fun sendGameInfo(receiver: PRPlayer?) {
        this.createPlantationsInfoResponse().send(this, receiver)
        this.createPartsInfoResponse().send(this, receiver)
        this.createShipsInfoResponse().send(this, receiver)
        this.createBuildingsInfoResponse().send(this, receiver)
        this.createTradeHouseInfoResponse().send(this, receiver)
        this.createPlayerActionInfoResponse().send(this, receiver)
        this.createColonistInfo().send(this, receiver)
        this.createCharacterCardInfoResponse().send(this, receiver)
    }

    /**
     * 发送玩家得到金钱的指令
     * @param player
     * @param doubloon
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendGetDoubloonResponse(player: PRPlayer, doubloon: Int) {
        CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_GET_DOUBLOON, player.position).public("doubloon", doubloon).send(this)
    }

    /**
     * 发送玩家得到VP的指令
     * @param player
     * @param vp
     * @param remainvp
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendGetVPResponse(player: PRPlayer, vp: Int, remainvp: Int) {
        CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_GET_VP, player.position).private("vp", vp).public("totalVP", remainvp).send(this)
    }

    @Throws(BoardGameException::class)
    override fun sendInitInfo(receiver: PRPlayer?) {
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_SETUP, -1).public("totalVP", this.gameMode.totalVp).public("characterCards", BgUtils.card2String(this.gameMode.ccards.cards)).public("ships", this.gameMode.shipPort.ships.values).send(this)
    }

    /**
     * 发送配件信息到客户端
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendPartsInfo() {
        this.createPartsInfoResponse().send(this)
    }

    /**
     * 发送种植园板块信息到客户端
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendPlantationsInfo() {
        this.createPlantationsInfoResponse().send(this)
    }

    // @Override
    // public void sendPlayingInfo(PRPlayer player) throws BoardGameException {
    // player.sendResponse(this.createInitInfo());
    //
    // //发送游戏的信息
    // this.sendGameInfo(player);
    //
    // player.sendResponse(this.createPlayingInfo());
    // //将所有玩家的移民分配情况发送给玩家
    // for(PRPlayer p : this.getPlayers()){
    // player.sendResponse(this.createPlayerColonistInfo(p));
    // }
    // }

    /**
     * 发送玩家行动信息到客户端
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendPlayerActionInfo() {
        this.createPlayerActionInfoResponse().send(this)
    }

    /**
     * 将玩家的移民分配情况发送到客户端
     * @param player
     */
    fun sendPlayerColonistInfo(player: PRPlayer) {
        this.createPlayerColonistInfo(player).send(this)
    }

    /**
     * 将玩家指定板块的移民分配情况发送到客户端
     * @param player
     * @param tile
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendPlayerColonistInfo(player: PRPlayer, tile: PRTile) {
        CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_COLONIST_INFO, player.position).public("restNum", player.colonist).public("details", listOf(mapOf("id" to tile.id, "colonist" to tile.colonistNum))).send(this)
    }

    /**
     * 发送玩家得到资源的信息
     * @param player
     * @param parts
     * @param i      资源的倍数
     */
    fun sendPlayerGetPartResponse(player: PRPlayer, parts: PrPartPool, i: Int) {
        return CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_GET_PART, player.position).public("parts", parts.getParts(i)).send(this)
    }

    @Throws(BoardGameException::class)
    override fun sendPlayerPlayingInfo(receiver: PRPlayer?) {
        val players = this.players.map {
            mapOf("chinese" to it.user.name, "position" to it.position, "doubloon" to it.doubloon, "vp" to it.vp, "resource" to it.resources.resources, "buildings" to BgUtils.card2String(it.buildings), "plantations" to BgUtils.card2String(it.fields))
        }
//        for (p in this.players) {
//            val m = HashMap<Any, Any>()
//            m["chinese"] = p.user.name
//            m["position"] = p.position
//            m["doubloon"] = p.doubloon
//            m["vp"] = p.vp
//            m["resources"] = p.resources.resources
//            m["buildings"] = BgUtils.card2String(p.buildings)
//            m["plantations"] = BgUtils.card2String(p.fields)
//            players.add(m)
//        }
        CmdFactory.createGameResponse(CmdConst.GAME_CODE_PLAYING_INFO, -1).public("players", players).send(this, receiver)

        // 发送玩家分配移民的情况
        for (p in this.players) {
            this.createPlayerColonistInfo(p).send(this, receiver)
        }

    }

    /**
     * 发送货船信息到客户端
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendShipsInfo() {
        this.createShipsInfoResponse().send(this)
    }

    /**
     * 发送公共资源得到资源的信息
     * @param parts
     * @param i     资源的倍数
     */
    fun sendSupplyGetPartResponse(parts: PrPartPool, i: Int) {
        CmdFactory.createGameResponse(GameCmdConst.GAME_CODE_GET_SUPPLY_PART, -1).public("parts", parts.getParts(i)).send(this)
    }

    /**
     * 发送交易所信息到客户端
     * @throws BoardGameException
     */
    @Throws(BoardGameException::class)
    fun sendTradeHouseInfo() {
        this.createTradeHouseInfoResponse().send(this)
    }

    /**
     * 设置游戏, 该方法中设置gameMode
     * @throws BoardGameException
     */
    @Synchronized
    @Throws(BoardGameException::class)
    override fun setupGame() {
        log.info("设置游戏...")
        val num = this.currentPlayerNumber
        log.info("游戏人数: $num")
        if (num == 2) {
            this.gameMode = PRGameMode2P(this)
        } else {
            this.gameMode = PRGameMode(this)
        }
    }
}
