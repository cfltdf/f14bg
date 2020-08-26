package com.f14.innovation.component

import com.f14.bg.component.Convertable
import com.f14.bg.utils.BgUtils
import com.f14.innovation.consts.InnoColor
import com.f14.innovation.consts.InnoIcon
import com.f14.innovation.consts.InnoSplayDirection

class InnoCardStack(card: InnoCard) : InnoCardDeck(), Convertable {
    /**
     * 取得展开的方向
     * @return
     */
    var splayDirection: InnoSplayDirection = InnoSplayDirection.NULL
        private set
    val iconCounter = InnoIconCounter()

    init {
        this.meld(card)
    }

    /**
     * 取得置底牌
     * @return
     */
    val bottomCard: InnoCard?
        get() = this.cards.lastOrNull()

    /**
     * 取得该牌堆的颜色
     * @return
     */
    val color: InnoColor
        get() = this.topCard!!.color!!

    /**
     * 取得指定符号的数量
     * @param icon
     * @return
     */
    fun getIconCount(icon: InnoIcon) = this.iconCounter.getProperty(icon)

    /**
     * 取得置顶牌
     * @return
     */
    val topCard: InnoCard?
        get() = this.cards.firstOrNull()

    /**
     * 合并,将该牌设为置顶牌
     * @param card
     */
    fun meld(card: InnoCard) {
        this.cards.add(0, card)
        this.refreshIconCounter()
    }

    /**
     * 刷新符号计数器
     */
    private fun refreshIconCounter() {
        this.iconCounter.clear()
        val topCard = this.topCard
        if (topCard != null) {
            // 如果只剩1张牌,就不能展开了
            if (this.size <= 1) {
                this.splayDirection = InnoSplayDirection.NULL
            }
            // 置顶牌的所有符号都算
            this.iconCounter.addTopIcons(topCard)
            // 如果展开,则计算展开的符号
            if (this.splayDirection != InnoSplayDirection.NULL) {
                this.cards
                        // 不重复计算置顶牌
                        .filter { it !== topCard }.forEach { this.iconCounter.addSplayIcons(it, this.splayDirection) }
            }
        }
    }

    /**
     * 移除置底牌
     */

    fun removeBottomCard(): InnoCard? {
        val card = this.bottomCard
        this.removeCard(card)
        this.refreshIconCounter()
        return card
    }

    /**
     * 移除牌堆中的牌
     */
    fun removeStackCard(card: InnoCard): Boolean {
        val res = this.removeCard(card)
        this.refreshIconCounter()
        return res
    }

    /**
     * 移除置顶牌
     */

    fun removeTopCard(): InnoCard? {
        val card = this.topCard
        this.removeCard(card)
        this.refreshIconCounter()
        return card
    }

    /**
     * 用指定的牌替换掉当前牌堆中的牌
     * @param cards
     */
    fun replaceCards(cards: List<InnoCard>) {
        this.clear()
        this.addCards(cards)
        this.refreshIconCounter()
    }

    /**
     * 展开牌堆
     * @param splayDirection
     */
    fun splay(splayDirection: InnoSplayDirection) {
        this.splayDirection = splayDirection
        this.refreshIconCounter()
    }


    override fun toMap(): Map<String, Any> = mapOf("topCardId" to (this.topCard?.id
            ?: ""), "color" to this.color, "splayDirection" to this.splayDirection, "icons" to this.iconCounter.toMap(), "cardIds" to BgUtils.card2String(this.cards), "stackCardNum" to this.size)

    /**
     * 追加该牌
     * @param card
     */
    fun tuck(card: InnoCard) {
        this.cards.add(card)
        this.refreshIconCounter()
    }

}
