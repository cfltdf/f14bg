package com.f14.bg.utils

import com.f14.F14bg.manager.ResourceManager
import com.f14.F14bg.network.F14bgServer
import com.f14.bg.component.Card
import com.f14.bg.component.Convertable
import java.io.File
import java.io.FileNotFoundException

/**
 * 工具类
 * @author F14eagle
 */
object BgUtils {
    /**
     * 将卡牌的id转换成string
     * @param cards
     * @return
     */
    inline fun <reified C : Card> card2String(cards: Collection<C>) = cards.joinToString(",") { it.id }

    /**
     * 将逗号分割的字符串变成数组
     * @params str
     */
    fun string2Array(str: String) = str.split(",".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()

    /**
     * clone card列表
     * @param <C>
     * @param cards
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified C : Card> cloneList(cards: Collection<C>) = cards.map { it.clone() as C }

    /**
     * 取得文件
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    fun getFile(path: String) = File(F14bgServer::class.java.classLoader.getResource(path)?.file)

    /**
     * 取得文件输入流
     * @param path
     * @return
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    fun getFileInputStream(rm: ResourceManager, path: String) = rm.loader.getResourceAsStream(path)
            ?: throw FileNotFoundException("文件错误!")

    /**
     * 将对象转换成list
     * @param <C>
     * @param o
     * @return
    </C> */
    fun <C : Any> toList(o: C?) = listOfNotNull(o)

    /**
     * 将array转换成map对象的list
     * @param <C>
     * @param array
     * @return
    </C> */
    fun <C : Convertable> toMapList(array: Array<C>) = array.map { it.toMap() }

    /**
     * 将list转换成map对象的list
     * @param <C>
     * @param list
     * @return
    </C> */

    fun <C : Convertable> toMapList(list: Collection<C>) = list.map { it.toMap() }

    fun escapeHtml(str: String) = str
            .replace("\"", "&quot;")
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace(" ", "&nbsp;")

    fun <T> withDefault(default: T, block: () -> T?) = try {
        block() ?: default
    } catch (e: Exception) {
        default
    }

    fun randomName(): String {
        return ChineseName.name
    }

}
