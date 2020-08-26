package com.f14.framework.common.exception

/**
 * DAO层异常
 *
 * @version 1.0
 */
class DAOException
/**
 * construction
 *
 * @param cause throwable
 */
(
        /**
         * 异常发生时，正在访问的实体的名称
         */
        val entity: String,
        /**
         * 异常发生时，正在访问的实体id
         */
        val entityid: Int?, message: String, cause: Throwable) : RuntimeException(message, cause) {
    companion object {

        /**
         * serialVersionUID
         */
        private const val serialVersionUID = 3257284725541254961L
    }
}
