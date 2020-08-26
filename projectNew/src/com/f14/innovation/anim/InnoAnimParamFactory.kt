package com.f14.innovation.anim

import com.f14.bg.anim.AnimObjectType
import com.f14.bg.anim.AnimParam
import com.f14.bg.anim.AnimType
import com.f14.bg.anim.AnimVar
import com.f14.innovation.InnoPlayer
import com.f14.innovation.component.InnoCard
import com.f14.innovation.consts.InnoAnimPosition
import com.f14.innovation.consts.InnoColor


object InnoAnimParamFactory {

    /**
     * 创建玩家计分的动画参数

     * @param player
     * @param card
     * @return
     */

    fun createAddScoreParam(player: InnoPlayer, card: InnoCard, from: AnimVar, animType: AnimType): AnimParam {
        return AnimParam(animType = animType, from = from, to = AnimVar.createAnimVar(InnoAnimPosition.PLAYER_SCORES, player.position), animObject = createAnimObject(card, animType))
    }

    /**
     * 按照动画方式创建动画对象

     * @param card
     * @param animType
     * @return
     */

    fun createAnimObject(card: InnoCard, animType: AnimType): AnimVar {
        return if (animType === AnimType.REVEAL) {
            // 如果动画类型是展示,则创建明牌
            AnimVar.createAnimObjectVar(AnimObjectType.CARD, card.id)
        } else {
            // 否则创建暗牌
            AnimVar.createAnimObjectVar(AnimObjectType.CARD_BACK, card.level)
        }
    }

    /**
     * 创建玩家触发卡牌效果的动画参数

     * @param player
     * @param card
     * @return
     */

    fun createDogmaCardParam(player: InnoPlayer, card: InnoCard): AnimParam {
        val res = AnimParam()
        res.animType = AnimType.REVEAL_FADEOUT
        res.from = AnimVar.createAnimVar(InnoAnimPosition.PLAYER_STACKS, player.position, card.color!!)
        res.animObject = createAnimObject(card, AnimType.REVEAL)
        return res
    }

    /**
     * 创建玩家拿成就牌的动画参数

     * @param player
     * @param card
     * @return
     */

    fun createDrawAchieveCardParam(player: InnoPlayer, card: InnoCard): AnimParam {
        val res = AnimParam()
        res.animType = AnimType.DIRECT
        res.from = AnimVar.createAnimVar(InnoAnimPosition.ACHIEVE_DECK, "", card.level)
        res.to = AnimVar.createAnimVar(InnoAnimPosition.PLAYER_ACHIEVES, player.position)
        res.animObject = createAnimObject(card, res.animType!!)
        return res
    }

    /**
     * 创建玩家摸牌的动画参数

     * @param player
     * @param card
     * @return
     */

    fun createDrawCardParam(player: InnoPlayer, card: InnoCard, from: AnimVar, animType: AnimType): AnimParam {
        val res = AnimParam()
        res.animType = animType
        res.from = from
        res.to = AnimVar.createAnimVar(InnoAnimPosition.PLAYER_HANDS, player.position)
        res.animObject = createAnimObject(card, animType)
        return res
    }

    /**
     * 创建玩家拿特殊成就牌的动画参数

     * @param player
     * @param card
     * @return
     */

    fun createDrawSpecialAchieveCardParam(player: InnoPlayer, card: InnoCard): AnimParam {
        val res = AnimParam()
        res.animType = AnimType.DIRECT
        res.from = AnimVar.createAnimVar(InnoAnimPosition.SPECIAL_ACHIEVE_DECK, "")
        res.to = AnimVar.createAnimVar(InnoAnimPosition.PLAYER_ACHIEVES, player.position)
        res.animObject = createAnimObject(card, AnimType.REVEAL)
        return res
    }

    /**
     * 创建玩家合并卡牌的动画参数

     * @param player
     * @param card
     * @return
     */

    fun createMeldCardParam(player: InnoPlayer, card: InnoCard, from: AnimVar): AnimParam {
        val res = AnimParam()
        res.animType = AnimType.REVEAL
        res.from = from
        res.to = AnimVar.createAnimVar(InnoAnimPosition.PLAYER_STACKS, player.position, card.color!!)
        res.animObject = createAnimObject(card, AnimType.REVEAL)
        return res
    }

    /**
     * 创建归还卡牌的动画参数

     * @param card
     * @return
     */

    fun createReturnCardParam(card: InnoCard, from: AnimVar, animType: AnimType): AnimParam {
        val res = AnimParam()
        res.animType = animType
        res.from = from
        res.to = AnimVar.createAnimVar(InnoAnimPosition.DRAW_DECK, "", card.level)
        res.animObject = createAnimObject(card, res.animType!!)
        return res
    }

    /**
     * 创建玩家展开牌堆的动画参数

     * @param player
     * @param color
     * @return
     */

    fun createSplayCardParam(player: InnoPlayer, color: InnoColor): AnimParam {
        val res = AnimParam()
        res.animType = AnimType.DIRECT
        res.from = AnimVar.createAnimVar(InnoAnimPosition.PLAYER_STACKS, player.position, color)
        return res
    }

}
