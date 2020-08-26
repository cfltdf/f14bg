package com.f14.TS.component

import com.f14.bg.component.CardDeck

class TSCardDeck : CardDeck<TSCard> {

    constructor() : super()

    constructor(autoReshuffle: Boolean) : super(autoReshuffle)

    constructor(defaultCards: List<TSCard>) : super(defaultCards)

    constructor(defaultCards: List<TSCard>, autoReshuffle: Boolean) : super(defaultCards, autoReshuffle)

}
