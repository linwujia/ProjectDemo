package com.linwujia.project.base


interface IContract {
    /**
     * View基本接口
     */
    interface IView {
        fun bindView()
        fun unbindView()
    }

    /**
     * Presenter基本接口
     * @param <V>
     * @param <M>
    </M></V> */
    interface IPresenter<out V : IView, out M> {
        fun attachedView(view: @UnsafeVariance V)
        fun detachedView()
        fun createModel(): M
        val view: V?

        fun checkViewNotNull(): Boolean
    }
}