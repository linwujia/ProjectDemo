package com.linwujia.project.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


abstract class BaseActivity<P : IContract.IPresenter<IContract.IView, *>> : AppCompatActivity(), IContract.IView {

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