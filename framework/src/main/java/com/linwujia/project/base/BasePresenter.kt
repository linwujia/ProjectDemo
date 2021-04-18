package com.linwujia.project.base

import java.lang.ref.WeakReference

abstract class BasePresenter<V : IContract.IView, M> : IContract.IPresenter<V, M> {
    protected var mViewRef: WeakReference<V>? = null
    protected var mModel: M? = null

    init {
        mModel = createModel()
    }

    override fun attachedView(view : V) {
        mViewRef = WeakReference(view)
    }

    override fun detachedView() {
        mViewRef?.clear()
        mViewRef = null
    }

    override val view: V?
        get() = mViewRef?.get()

    override fun checkViewNotNull(): Boolean {
        return mViewRef?.get() != null
    }
}