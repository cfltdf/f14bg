package com.f14.loveletter

import com.f14.bg.BoardGameConfig

class LLConfig : BoardGameConfig() {

    val score: Int
        get() {
            when (this.playerNumber) {
                2 -> return 7
                3 -> return 5
                4 -> return 4
            }
            return 0
        }

}
