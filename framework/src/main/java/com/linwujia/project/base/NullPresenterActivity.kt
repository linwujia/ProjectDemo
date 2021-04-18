package com.linwujia.project.base

open class NullPresenterActivity : BaseActivity<NullContract.NullPresenter>() {
    override fun createPresenter(): NullContract.NullPresenter {
        return NullContract.NullPresenter()
    }
}