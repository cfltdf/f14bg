package com.f14.TS

import com.f14.TS.component.TSCard
import com.f14.bg.component.Convertable
import java.util.*

/**
 * 行动记录

 * @author F14eagle
 */
class ActionRecord(var player: TSPlayer, var card: TSCard, var message: String) : Convertable {


    override fun toMap(): Map<String, Any> {
        val res = HashMap<String, Any>()
        res["superPower"] = player.superPower
        res["playerName"] = player.name
        res["cardId"] = card.id
        res["message"] = message
        return res
    }

}
