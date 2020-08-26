package com.f14.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import org.apache.log4j.Logger
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.hssf.usermodel.HSSFRow
import org.apache.poi.hssf.usermodel.HSSFSheet
import java.util.*

object ExcelUtils {

    /**
     * 取得boolean
     * @param row
     * @param cellNum
     * @return
     */
    fun getBoolean(row: HSSFRow, cellNum: Int): Boolean {
        val cell = row.getCell(cellNum) ?: return false
        return cell.booleanCellValue
    }

    /**
     * 取得double
     * @param row
     * @param cellNum
     * @return
     */
    fun getDouble(row: HSSFRow, cellNum: Int): Double {
        val cell = row.getCell(cellNum) ?: return 0.0
        return cell.numericCellValue
    }

    /**
     * 取得int
     * @param row
     * @param cellNum
     * @return
     */
    fun getInteger(row: HSSFRow, cellNum: Int): Int {
        val cell = row.getCell(cellNum) ?: return 0
        return cell.numericCellValue.toInt()
    }

    /**
     * 取得字符串
     * @param row
     * @param cellNum
     * @return
     */
    fun getString(row: HSSFRow, cellNum: Int): String? {
        val cell = row.getCell(cellNum) ?: return null
        return getString(cell)
    }

    /**
     * 取得字符串
     * @param cell
     * @return
     */
    fun getString(cell: HSSFCell) = when (cell.cellType) {
        HSSFCell.CELL_TYPE_BLANK -> null
        HSSFCell.CELL_TYPE_STRING -> cell.richStringCellValue.string
        HSSFCell.CELL_TYPE_NUMERIC -> cell.numericCellValue.toString()
        else -> null
    }

    /**
     * 将sheet转换成资源列表
     * @param sheet
     * @param clazz 类名
     * @return
     */
    fun <A : Any> sheetToList(sheet: HSSFSheet, clazz: Class<A>, block: (A) -> Unit) {
        val head = rowToStringArray(sheet.getRow(0))
        (1..sheet.lastRowNum).map(sheet::getRow).mapNotNull { rowToObject(it, head, clazz) }.forEach(block)
    }

    /**
     * 将row转换成json字符串
     * @param row
     * @param head 表头字段名
     * @return
     */
    fun rowToJsonStr(row: HSSFRow, head: Array<String>): String {
        val cellNum = row.lastCellNum.toInt()
        return (0 until minOf(head.size, cellNum)).map { head[it] to row.getCell(it) }.toMap().filterKeys { it.isNotEmpty() && !(it.startsWith("[") && it.endsWith("]")) }.filterValues { it != null && !getString(it).isNullOrEmpty() }.entries.joinToString(",", "{", "}") { (k, v) -> "\"$k\":${asJSONString(v)}" }
    }

    /**
     * 将cell转换成json字符串
     * @param cell
     * @return
     */
    private fun asJSONString(cell: HSSFCell): String {
        val str = getString(cell)!!
        return when (cell.cellType) {
            HSSFCell.CELL_TYPE_STRING -> when {
                str.startsWith("{") && str.endsWith("}") -> str
                str.startsWith("[") && str.endsWith("]") -> str
                else -> "\"$str\""
            }
            else -> str
        }
    }

    /**
     * 将row转换成object
     * @param row
     * @param head  表头字段名
     * @param clazz
     * @return
     */
    fun <A> rowToObject(row: HSSFRow, head: Array<String>, clazz: Class<A>): A? {
        val log = Logger.getLogger("rowToObject")
        return try {
            val gson = GsonBuilder().create()
            val jstr = rowToJsonStr(row, head)
//            log.debug(jstr)
            gson.fromJson(jstr, clazz)
        } catch (e: JsonSyntaxException) {
            log.error(e.message + " | " + rowToJsonStr(row, head))
            null
        }
    }

    /**
     * 将row转换成string数组
     * @param row
     * @return
     */
    fun rowToStringArray(row: HSSFRow): Array<String> {
        val strs = ArrayList<String>()
        for (cell in row) strs.add(getString(cell as HSSFCell) ?: "")
        return strs.toTypedArray()
    }

}
