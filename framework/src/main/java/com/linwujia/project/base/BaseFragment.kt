package com.linwujia.project.base

import android.os.Bundle
import androidx.fragment.app.Fragment


abstract class BaseFragment<P: IContract.IPresenter<IContract.IView, *>> : Fragment(), IContract.IView {

    protected var mPresenter: P? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindView()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindView()
    }

    override fun bindView() {
        mPresenter = createPresenter()
        mPresenter?.attachedView(this)
    }

    override fun unbindView() {
        mPresenter?.detachedView()
        mPresenter = null
    }

    protected abstract fun createPresenter(): P
}