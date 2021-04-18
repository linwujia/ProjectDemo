package com.linwujia.project.base

open class NullPresenterFragment : BaseFragment<NullContract.NullPresenter>() {
    override fun createPresenter(): NullContract.NullPresenter {
        return NullContract.NullPresenter()
    }
}