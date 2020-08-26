package com.f14.framework.exception

/**
 * DAO Exception Create at 2005-3-28
 *
 *
 *
 * @author shitianyu_kf
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
