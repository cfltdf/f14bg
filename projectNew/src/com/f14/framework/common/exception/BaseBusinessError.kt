package com.f14.framework.common.exception

/**
 * 不需要处理的业务异常，在业务层抛出此错误会导致事务回滚
 *
 * @author evan
 */
class BaseBusinessError : RuntimeException {

    var key: String? = null

    constructor()

    constructor(key: String, message: String) : super(message) {
        this.key = key
    }

    companion object {
        private const val serialVersionUID = 3863270329072664358L
    }
}
