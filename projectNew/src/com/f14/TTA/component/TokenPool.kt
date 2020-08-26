package com.f14.TTA.component

import com.f14.TTA.consts.Token
import com.f14.bg.component.PartPool

/**
 * TTA玩家的指示物
 * @author F14eagle
 */
class TokenPool {
    /**
     * 配件池
     */
    private val parts: PartPool = TTAPartPool()

    /**
     * 调整蓝色指示物的数量
     * @param num
     * @return
     */
    fun addAvailableBlues(num: Int): Int {
        return this.addToken(Token.AVAILABLE_BLUE, num)
    }

    /**
     * 调整可用工人数量
     * @param num
     * @return
     */
    fun addAvailableWorker(num: Int): Int {
        return this.addToken(Token.AVAILABLE_WORKER, num)
    }

    /**
     * 调整配件的数量
     * @param token
     * @param num
     * @return
     */
    private fun addToken(token: Token, num: Int): Int {
        return if (num > 0) {
            this.parts.putPart(token, num)
        } else {
            -this.parts.takePart(token, -num)
        }
    }

    /**
     * 调整空闲工人数量
     * @param num
     */
    fun addUnusedWorker(num: Int) {
        this.addToken(Token.UNUSED_WORKER, num)
    }

    /**
     * 取得所有可用的蓝色指示物数量
     * @return
     */
    val availableBlues: Int
        get() = this.parts.getAvailableNum(Token.AVAILABLE_BLUE)

    /**
     * 取得所有可用的工人数
     * @return
     */
    val availableWorkers: Int
        get() = this.parts.getAvailableNum(Token.AVAILABLE_WORKER)

    /**
     * 不愉快的工人数
     */
    var unhappyWorkers: Int
        get() = this.parts.getAvailableNum(Token.UNHAPPY_WORKER)
        set(num) = this.parts.setPart(Token.UNHAPPY_WORKER, num)

    /**
     * 所有空闲的工人数
     */
    val unusedWorkers: Int
        get() = this.parts.getAvailableNum(Token.UNUSED_WORKER)

    /**
     * 初始化
     */
    fun init(yellow: Int, blue: Int, free: Int, unhappy: Int) {
        this.parts.clear()
        this.parts.setPart(Token.AVAILABLE_WORKER, yellow)
        this.parts.setPart(Token.AVAILABLE_BLUE, blue)
        this.parts.setPart(Token.UNUSED_WORKER, free)
        this.parts.setPart(Token.UNHAPPY_WORKER, unhappy)
    }

    /**
     * 放入指定数量的蓝色指示物
     * @param num
     */
    fun putAvailableBlues(num: Int): Int {
        return this.parts.putPart(Token.AVAILABLE_BLUE, num)
    }

    /**
     * 取出指定数量的蓝色指示物
     * @param num
     * @return
     */
    fun takeAvailableBlues(num: Int): Int {
        return this.parts.takePart(Token.AVAILABLE_BLUE, num)
    }

    fun clear() {
        this.parts.clear()
    }

}
