package com.linwujia.project.demo.base

import android.os.Bundle
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linwujia.project.base.BaseActivity
import com.linwujia.project.base.IContract
import com.linwujia.project.demo.R
import com.linwujia.project.ui.recyclerview.LoadMoreListView

abstract class RefreshLoadMoreActivity<P : IContract.IPresenter<IContract.IView, *>, VH: RecyclerView.ViewHolder, DataType> : BaseActivity<P>() {

    protected lateinit var mLoadMoreRecyclerView: LoadMoreListView
    protected val mAdapter = RefreshLoadMoreAdapter<VH, DataType>(::onCreateViewHolder).apply {
        onBindViewHolder(this@RefreshLoadMoreActivity::onBindViewHolder)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_refresh_load_more_layout)
        mLoadMoreRecyclerView = findViewById(R.id.load_more_recycler_view)
        mLoadMoreRecyclerView.onLoadMore {
            onLoadMore()
        }
        mLoadMoreRecyclerView.adapter = mAdapter
    }

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH

    abstract fun onBindViewHolder(holder: VH, position: Int)

    abstract fun onRefresh()

    abstract fun onLoadMore()

}