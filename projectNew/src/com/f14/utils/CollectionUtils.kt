package com.f14.utils


import java.util.*
import kotlin.math.ceil
import kotlin.math.min

object CollectionUtils {

    /**
     * 随机抽取一个对象
     *
     * @param <E>
     * @param coll
     * @return
     */
    fun <E> randomDraw(coll: List<E>): E? {
        return if (coll.isNotEmpty()) {
            val d = "d${coll.size}"
            val num = DiceUtils.roll(d)
            coll[num - 1]
        } else {
            null
        }
    }

    /**
     * 随机打乱指定的集合
     *
     * @param <E>
     * @param coll
     * @return
     */
    fun <E> shuffle(coll: MutableList<E>) {
        val tmp = ArrayList(coll)
        coll.clear()
        val ran = Random()
        val ran2 = Random()
        tmp.shuffle(ran)
        while (tmp.isNotEmpty()) {
            coll.add(tmp.removeAt(ran2.nextInt(tmp.size)))
        }
    }

    /**
     * 将指定的集合分成blockNum个集合后打乱再进行重组
     *
     * @param <E>
     * @param coll
     * @param blockNum
     * @return
     */
    fun <E> shuffle(coll: MutableList<E>, blockNum: Int) {
        if (coll.size < blockNum * 3) {
            coll.shuffle()
        } else {
            val list = ArrayList<E>()
            list.addAll(coll)
            list.shuffle()
            coll.clear()
            val blockSize = ceil(list.size.toDouble() / blockNum).toInt()
            val ll = ArrayList<List<E>>()
            for (i in 0 until blockNum) {
                val endIndex = min((i + 1) * blockSize, list.size)
                val sublist = list.subList(i * blockSize, endIndex)
                sublist.shuffle()
                ll.add(sublist)
            }
            for (i in 0 until blockSize) {
                for (sublist in ll) {
                    if (i < sublist.size) {
                        coll.add(sublist[i])
                    }
                }
            }
        }
    }

}
