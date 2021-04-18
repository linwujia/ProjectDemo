package com.linwujia.project.base

class NullContract {
    class NullPresenter() : BasePresenter<NullView, Any>() {
        override fun createModel(): Any {
            return Any()
        }
    }

    class NullView : IContract.IView {
        override fun bindView() {

        }

        override fun unbindView() {

        }

    }
}