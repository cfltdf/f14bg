package com.f14.bg.component

abstract class AbstractCondition<P> : ICondition<P>, Cloneable {

    public override fun clone() = super.clone()

}
