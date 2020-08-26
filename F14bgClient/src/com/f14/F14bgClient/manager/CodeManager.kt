package com.f14.F14bgClient.manager

import com.f14.F14bg.consts.CmdConst
import com.f14.F14bg.network.CmdFactory
import com.f14.F14bgClient.vo.CodeDetail
import com.f14.bg.action.BgAction
import net.sf.json.JSONObject
import org.apache.log4j.Logger
import org.jetbrains.annotations.Mutable
import java.util.*
import javax.swing.plaf.synth.SynthTextAreaUI

class CodeManager {
    private val lock = Object()
    private val map = HashMap<String, MutableMap<String, CodeDetail>>()
    private val list = HashMap<String, MutableList<CodeDetail>>()

    fun clear() {
        map.clear()
        list.clear()
    }

    /**
     * 取得codeType对应的map

     * @param codeType
     * *
     * @return
     */
    private fun getMap(codeType: String): MutableMap<String, CodeDetail> {
        var res: MutableMap<String, CodeDetail>? = map[codeType]
        if (res == null) {
            res = HashMap<String, CodeDetail>()
            map.put(codeType, res)
        }
        return res
    }

    /**
     * 取得codeKey对应的map list

     * @param codeType
     * *
     * @return
     */
    private fun getList(codeType: String): MutableList<CodeDetail> {
        var res: MutableList<CodeDetail>? = list[codeType]
        if (res == null) {
            res = ArrayList<CodeDetail>()
            list.put(codeType, res)
        }
        return res
    }

    /**
     * 添加代码对象到缓存中

     * @param o
     */
    private fun addCode(o: CodeDetail) {
        val codeType = o.codeType
        val codeMap = getMap(codeType)
        val codeList = getList(codeType)
        codeMap.put(o.value, o)
        codeList.add(o)
    }

    /**
     * 取得指定类型的代码列表

     * @param codeType
     * *
     * @return
     */
    fun getCodes(codeType: String): List<CodeDetail> {
        val res = getList(codeType)
        return res
    }

    /**
     * 取得指定类型的代码值对应的显示值

     * @param codeType
     * *
     * @param codeValue
     * *
     * @return
     */
    fun getCodeLabel(codeType: String, codeValue: String): String? {
        val o = this.getMap(codeType)[codeValue]
        if (o != null) {
            return o.label
        } else {
            return null
        }
    }

    /**
     * 从服务器装载所有代码,该方法会等待结果返回后继续执行
     */
    fun loadAllCodes() {
        val res = CmdFactory.createClientResponse(CmdConst.CLIENT_LOAD_CODE)
        ManagerContainer.connectionManager.sendResponse(res)
        synchronized(lock) {
            try {
                lock.wait()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }

    /**
     * 装载代码参数

     * @param act
     */
    fun loadCodeParam(act: BgAction) {
        this.clear()
        val codes = act.getAsObject("codes")
        if (codes != null && !codes.isEmpty) {
            val it = codes.keys()
            while (it.hasNext()) {
                val codeType = it.next() as String
                val array = codes.getJSONArray(codeType)
                if (array != null && !array.isEmpty) {
                    val oit = array.iterator()
                    while (oit.hasNext()) {
                        val code = oit.next() as JSONObject
                        println(code.toString())
                        val o = JSONObject.toBean(code, CodeDetail::class.java) as CodeDetail
                        this.addCode(o)
                    }
                }
            }
        }
        this.notifyLock()
    }

    /**
     * 解除等待
     */
    fun notifyLock() {
        synchronized(lock) {
            lock.notifyAll()
        }
    }

    companion object {
        protected var log = Logger.getLogger(CodeManager::class.java)
    }

}
