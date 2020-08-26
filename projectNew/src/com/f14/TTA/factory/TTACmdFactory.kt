package com.f14.TTA.factory

import com.f14.F14bg.network.CmdFactory
import com.f14.TTA.TTAPlayer
import com.f14.TTA.component.card.CivilCard
import com.f14.TTA.component.card.EventCard
import com.f14.TTA.component.card.TTACard
import com.f14.TTA.consts.*
import com.f14.bg.action.BgResponse
import com.f14.bg.utils.BgUtils

object TTACmdFactory {

    /**
     * 向玩家发送奖励牌堆的信息
     * @param bonusCards
     */
    fun createBonusCardResponse(bonusCards: Collection<EventCard>): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_BONUS_CARD, -1).public("cardIds", BgUtils.card2String(bonusCards))
    }

    /**
     * 发送卡牌序列失去卡牌的消息
     */
    fun createCardRowRemoveCardResponse(cardId: String): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_REMOVE_CARDROW, -1).public("cardId", cardId)
    }

    /**
     * @param cmd
     * @param player
     * @param cards
     * @param lazyMemory
     * @return
     */
    fun createDetailInfoResponse(cmd: Int, player: TTAPlayer, cards: List<TTACard>, lazyMemory: Boolean): BgResponse {
        val group = cards.groupBy(TTACard::actionType)
        val civil = group.getOrDefault(ActionType.CIVIL, emptyList())
        val military = group.getOrDefault(ActionType.MILITARY, emptyList())
        val civilLevels = civil.joinToString(",") { it.level.toString() }
        val militaryLevels = military.joinToString(",") { it.level.toString() }
        return CmdFactory.createGameResponse(cmd, player.position).public("civilNum", civil.size).public("civilLevels", civilLevels).public("civilCards", BgUtils.card2String(civil).takeIf { lazyMemory }).private("civilCards", BgUtils.card2String(civil)).public("militaryNum", military.size).public("militaryLevels", militaryLevels).private("militaryCards", BgUtils.card2String(military))
    }

    /**
     * 发送玩家间持续效果卡牌的信息
     * @param cards
     */
    fun createOvertimeCardsInfoResponse(cards: List<TTACard>): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_OVERTIME_CARD, -1).public("cards", BgUtils.toMapList(cards))
    }

    /**
     * 发送玩家请求行动的信息,card为使用的卡牌
     * @param player
     * @param cmdString 请求的命令字符串
     * @param msg       显示的信息
     * @param showCard  展示的card
     * @param param     其他参数
     */
    fun createPlayerActionRequestResponse(player: TTAPlayer, cmdString: String, msg: String, showCard: TTACard?, param: Map<String, Any>?): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_ACTION_REQUEST, player.position).public("cmdString", cmdString).public("msg", msg)
                // 如果showCard为空,则取玩家的临时actionCard
                .public("showCardId", showCard?.id).public("param", param)
    }

    /**
     * 向玩家发送他可激活卡牌的列表
     * @param activeStep
     * @param player
     */
    fun createPlayerActivableCardsResponse(activeStep: RoundStep, player: TTAPlayer): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_ACTIVABLE_CARD, player.position).public("activableCardIds", BgUtils.card2String(player.getActiveCards(activeStep)))
    }

    /**
     * 发送玩家添加打出卡牌的信息
     * @param player
     */
    fun createPlayerAddCardResponse(player: TTAPlayer, card: TTACard): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_ADD_CARD, player.position).public("cardIds", card.id)
    }

    /**
     * 发送玩家添加打出卡牌的信息
     * @param player
     * @param cards
     */
    fun createPlayerAddCardsResponse(player: TTAPlayer, cards: List<TTACard>): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_ADD_CARD, player.position).public("cardIds", BgUtils.card2String(cards))
    }

    /**
     * 发送玩家得到手牌的消息
     * @param player
     * @param cards
     * @param lazyMemory
     */
    fun createPlayerAddHandResponse(player: TTAPlayer, cards: List<TTACard>, lazyMemory: Boolean): BgResponse {
        return createDetailInfoResponse(TTAGameCmd.GAME_CODE_ADD_HAND, player, cards, lazyMemory)
    }

    /**
     * 发送玩家叠放卡牌的信息
     * @param player
     */
    fun createPlayerAttachCardResponse(player: TTAPlayer, card: TTACard, destCard: TTACard): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_ATTACH_CARD, player.position).public("cardIds", card.id).public("destCardIds", destCard.id)
    }

    /**
     * 发送玩家桌面标志物的数量(该方法在发送玩家文明信息时被调用)
     * @param player
     */
    fun createPlayerBoardTokensResponse(player: TTAPlayer): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_BOARD_TOKEN, player.position).public(Token.AVAILABLE_WORKER.toString(), player.tokenPool.availableWorkers).public(Token.AVAILABLE_BLUE.toString(), player.tokenPool.availableBlues).public(Token.UNUSED_WORKER.toString(), player.tokenPool.unusedWorkers)
                // 与幸福度相关的参数也在这里传入
                .public(Token.UNHAPPY_WORKER.toString(), player.tokenPool.unhappyWorkers).public(CivilizationProperty.HAPPINESS.toString(), player.getProperty(CivilizationProperty.HAPPINESS))
    }

    /**
     * 发送玩家卡牌的标志物信息
     * @param player
     * @param cards
     */
    fun createPlayerCardTokenResponse(player: TTAPlayer, cards: Collection<TTACard>): BgResponse {
        val tokens = cards.filter { it.actionType == ActionType.CIVIL }.filterIsInstance<CivilCard>().map { it.id to it.getTokens() }.toMap()
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_CARD_TOKEN, player.position).public("cards", tokens)
    }

    /**
     * 发送玩家文明的基本属性信息
     * @param player
     */
    fun createPlayerCivilizationInfoResponse(player: TTAPlayer): BgResponse {
        val property = player.properties.allProperties + mapOf(CivilizationProperty.MILITARY to player.defenceMilitary, CivilizationProperty.CIVIL_HANDS to player.civilHands.size, CivilizationProperty.MILITARY_HANDS to player.militaryHands.size, CivilizationProperty.CULTURE_POINT to player.culturePoint, CivilizationProperty.SCIENCE_POINT to player.sciencePoint, CivilizationProperty.HAPPINESS to player.getProperty(CivilizationProperty.HAPPINESS))
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_CIVILIZATION_INFO, player.position).public("property", property)
    }

    /**
     * 发送玩家得到奇迹的信息
     * @param player
     * @param card
     */
    fun createPlayerGetWonderResponse(player: TTAPlayer, card: TTACard): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_GET_WONDER, player.position).public("cardId", card.id)
    }

    /**
     * 发送玩家失去打出卡牌的信息
     * @param player
     */
    fun createPlayerRemoveCardResponse(player: TTAPlayer, card: TTACard): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_REMOVE_CARD, player.position).public("cardIds", card.id)
    }

    /**
     * 发送玩家失去打出卡牌的信息
     * @param player
     * @param cards
     */
    fun createPlayerRemoveCardsResponse(player: TTAPlayer, cards: List<TTACard>): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_REMOVE_CARD, player.position).public("cardIds", BgUtils.card2String(cards))
    }

    /**
     * 发送玩家失去手牌的消息
     * @param player
     * @param cards
     * @param lazyMemory
     */
    fun createPlayerRemoveHandResponse(player: TTAPlayer, cards: List<TTACard>, lazyMemory: Boolean): BgResponse {
        return createDetailInfoResponse(TTAGameCmd.GAME_CODE_REMOVE_HAND, player, cards, lazyMemory)
    }

    /**
     * 发送玩家当前选中手牌的消息
     * @param player
     * @param cards
     */
    fun createPlayerSelectHandResponse(player: TTAPlayer, cards: List<TTACard>): BgResponse {
        return CmdFactory.createGameResponse(TTAGameCmd.GAME_CODE_SELECT_HAND, player.position).private("selectedCards", BgUtils.card2String(cards))
    }
}
