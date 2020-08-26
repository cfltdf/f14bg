package com.f14.f14bgdb

import org.springframework.context.ApplicationContext
import org.springframework.context.support.FileSystemXmlApplicationContext

object F14bgdb {
    private lateinit var context: ApplicationContext

    fun init() {
        context = FileSystemXmlApplicationContext("classpath:applicationContext.xml")
    }

    fun init(context: ApplicationContext) {
        F14bgdb.context = context
    }

    @Suppress("UNCHECKED_CAST")
    fun <C> getBean(bean: String): C {
        return context.getBean(bean) as C
    }

}
