package com.f14.F14bgClient.manager

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

import org.apache.log4j.Logger

class PropertiesManager {

    protected var log = Logger.getLogger(this.javaClass)
    /**
     * 取得本地参数

     * @return
     */
    lateinit var localProperties: Properties
        protected set
    lateinit protected var configs: Properties

    init {
        this.init()
    }

    protected fun init() {
        // 装载用户的本地参数
        localProperties = Properties()
        try {
            localProperties.load(FileInputStream(PROP_PATH))
        } catch (e: Exception) {
            log.warn("本地参数装载失败!", e)
        }

        // 装载配置文件
        configs = Properties()
        try {
            configs.load(FileInputStream(CONFIG_PATH))
        } catch (e: Exception) {
            log.warn("配置文件装载失败!", e)
        }

    }

    /**
     * 保存本地参数

     * @param key
     * *
     * @param value
     */
    fun saveLocalProperty(key: String, value: String) {
        try {
            this.localProperties.put(key, value)
            val file = File(PROP_PATH)
            if (!file.exists()) {
                file.createNewFile()
            }
            val os = FileOutputStream(file, false)
            this.localProperties.store(os, null)
        } catch (e: Exception) {
            log.error(e, e)
        }

    }

    /**
     * 取得本地属性

     * @param key
     * *
     * @return
     */
    fun getLocalProperty(key: String): String {
        return this.localProperties.getProperty(key)
    }

    /**
     * 取得配置值

     * @param key
     * *
     * @return
     */
    fun getConfigValue(key: String): String {
        return this.configs.getProperty(key) ?: ""
    }

    companion object {
        /**
         * 本地参数文件存放的位置
         */
        protected val PROP_PATH = "./local.properties"
        /**
         * 配置文件存放的位置
         */
        protected val CONFIG_PATH = "./config.properties"
    }
}
