package com.f14.utils

import java.util.ArrayList
import java.util.Collections
import java.util.Random

object CollectionUtils {

    /**
     * 将指定的集合分成blockNum个集合后打乱再进行重组

     * @param <E>
     * *
     * @param coll
     * *
     * @param blockNum
     * *
     * @return
    </E> */
    fun <E> shuffle(coll: MutableList<E>, blockNum: Int) {
        if (coll.size < blockNum * 3) {
            Collections.shuffle(coll)
            return
        }
        val list = ArrayList<E>()
        list.addAll(coll)
        Collections.shuffle(list)
        coll.clear()
        val blockSize = Math.ceil(list.size.toDouble() / blockNum).toInt()
        val ll = ArrayList<List<E>>()
        for (i in 0..blockNum - 1) {
            val endIndex = Math.min((i + 1) * blockSize, list.size)
            val sublist = list.subList(i * blockSize, endIndex)
            Collections.shuffle(sublist)
            ll.add(sublist)
        }
        for (i in 0..blockSize - 1) {
            for (sublist in ll) {
                if (i < sublist.size) {
                    coll.add(sublist[i])
                }
            }
        }
    }

    /**
     * 随机打乱指定的集合

     * @param <E>
     * *
     * @param coll
     * *
     * @return
    </E> */
    fun <E> shuffle(coll: MutableList<E>) {
        val tmp = ArrayList(coll)
        coll.clear()
        val ran = Random()
        val ran2 = Random()
        Collections.shuffle(tmp, ran)
        while (!tmp.isEmpty()) {
            coll.add(tmp.removeAt(ran2.nextInt(tmp.size)))
        }
    }

    @JvmStatic fun main(args: Array<String>) {
        val list = ArrayList<Int>()
        for (i in 0..99) {
            list.add(i)
        }
        shuffle(list)
        for (i in list) {
            print(i.toString() + ",")
        }
    }

}
