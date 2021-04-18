package com.linwujia.project.demo.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.linwujia.project.base.BaseFragment
import com.linwujia.project.base.IContract
import com.linwujia.project.demo.R
import com.linwujia.project.ui.recyclerview.LoadMoreListView

abstract class RefreshLoadMoreFragment<P: IContract.IPresenter<IContract.IView, *>, VH : RecyclerView.ViewHolder, DataType>() : BaseFragment<P>() {
    protected lateinit var mLoadMoreRecyclerView: LoadMoreListView
    protected val mAdapter = RefreshLoadMoreAdapter<VH, DataType>(::onCreateViewHolder).apply {
        onBindViewHolder(this@RefreshLoadMoreFragment::onBindViewHolder)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.base_refresh_load_more_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mLoadMoreRecyclerView = view.findViewById(R.id.load_more_recycler_view)
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