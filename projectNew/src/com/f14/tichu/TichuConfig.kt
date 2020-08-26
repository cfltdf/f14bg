package com.f14.tichu

import com.f14.bg.BoardGameConfig

class TichuConfig : BoardGameConfig() {
    var score = 1000
    lateinit var mode: String
}
