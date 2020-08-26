package com.f14.framework.common.exception

/**
 * 需要处理的业务异常，在业务层抛出此错误不会导致事务回滚
 *
 * @author evan
 */
class BaseBusinessException : Exception {

    var key: String? = null

    constructor()

    constructor(key: String, message: String) : super(message) {
        this.key = key
    }

    companion object {
        private const val serialVersionUID = -5772133545430282802L
    }
}
