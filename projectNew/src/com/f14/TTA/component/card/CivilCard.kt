package com.f14.TTA.component.card

import com.f14.TTA.consts.Token

/**
 * 政治牌

 * @author F14eagle
 */
open class CivilCard : TTACard() {

    /**
     * 调整蓝色指示物的数量
     * @param num
     */
    fun addBlues(num: Int) = this.addToken(Token.BLUE, num)

    /**
     * 调整指示物的数量
     * @param token
     * @param num
     */
    protected fun addToken(token: Token, num: Int) = when {
        num > 0 -> this.tokens.putPart(token, num)
        num < 0 -> this.tokens.takePart(token, -num)
        else -> 0
    }

    /**
     * 调整工人数量
     * @param num
     */
    fun addWorkers(num: Int) = this.addToken(Token.YELLOW, num)

    /**
     * 取得有效的基数,需要工人的牌返回当前工人数量,否则返回1
     * @return
     */
    override val availableCount: Int
        get() = if (this.needWorker) this.workers else super.availableCount

    /**
     * 取得卡牌上蓝色指示物的数量
     * @return
     */
    val blues: Int
        get() = this.tokens.getAvailableNum(Token.BLUE)

    /**
     * 取得所有指示物的数量
     * @return
     */
    open fun getTokens() = mapOf(Token.YELLOW.toString() to this.workers, Token.BLUE.toString() to this.blues)

    /**
     * 取得卡牌上的工人数量
     * @return
     */
    val workers: Int
        get() = this.tokens.getAvailableNum(Token.YELLOW)

    /**
     * 设置指示物的数量
     * @param token
     * @param num
     */
    protected fun setToken(token: Token, num: Int) = this.tokens.setPart(token, num)
}
