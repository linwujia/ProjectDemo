package com.linwujia.project.ui.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class LoadMoreListView(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    BaseLoadMoreRecyclerView(context, attrs, defStyleAttr) {

    private var mEmptyViewHolder: EmptyViewHolder? = null
    private var mLoadMoreViewHolder: LoadMoreViewHolder? = null

    private var mOnEmptyViewHolderCreator: (() -> EmptyViewHolder)? = null
    private var mOnLoadMoreViewHolderCreator: (() -> LoadMoreViewHolder)? = null

    private var mOnEmptyViewHolderBinder: ((EmptyViewHolder) -> Unit)? = null
    private var mOnLoadMoreViewHolderBinder: ((LoadMoreViewHolder, Int, Boolean, Boolean) -> Unit)? = null

    init {
        attrs?.also {

        }

        descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
        modifyMaxFlingVelocity(8000)
        modifyMinFlingVelocity(300)
        showLoadMoreView()
    }

    private val mAdapterObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            showEmptyViewIfNeed()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            showEmptyViewIfNeed()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            showEmptyViewIfNeed()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            showEmptyViewIfNeed()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            showEmptyViewIfNeed()
        }
    }

    /*
     * 是否能加载更多标志
     */
    private var mCanLoadMore: Boolean = false

    /**
     * 设置是否能加载更多
     * */
    var isCanLoadMore: Boolean
        get() = mCanLoadMore
        set(canLoadMore) {
            mCanLoadMore = canLoadMore
            refreshLoadMoreStatus()
        }


    /*
    * 判断加载更多完成后是否有数据
    * 这是个带Header和Footer的View，所以没数据是itemCount - 2 <= 0
    * */
    private val hasDataOrigin: Boolean
        get() {
            val adapter = adapter
            return adapter?.itemCount ?: 0 > 2
        }

    /*
    * 是否在没数据时自动显示没有数据的那个View
    * */
    var showEmptyViewAutomatic = true
        set(value) {
            field = value
            showEmptyViewIfNeed()
        }

    private fun showEmptyViewIfNeed() {
        if (showEmptyViewAutomatic) {
            if (hasDataOrigin) {
                hideEmptyView()
            } else {
                showEmptyView()
            }
        } else {
            hideEmptyView()
        }
    }

    fun showEmptyView() {
        val viewHolder = mEmptyViewHolder ?: mOnEmptyViewHolderCreator?.invoke()?.also {
            mOnEmptyViewHolderBinder?.invoke(it)
        }
        mEmptyViewHolder = viewHolder?.apply {
            emptyView.visibility = View.VISIBLE
            if (!ViewCompat.isAttachedToWindow(emptyView)) {
                addFooterView(emptyView)
            }
        }
    }

    fun hideEmptyView() {
        mEmptyViewHolder?.apply {
            emptyView.visibility = View.GONE
        }
    }

    fun showLoadMoreView() {
        val viewHolder = mLoadMoreViewHolder ?: mOnLoadMoreViewHolderCreator?.invoke()?.also {
            mOnLoadMoreViewHolderBinder?.invoke(it, mState, isCanLoadMore, hasDataOrigin)
        }
        mLoadMoreViewHolder = viewHolder?.apply {
            loadMoreView.visibility = View.VISIBLE
            if (!ViewCompat.isAttachedToWindow(loadMoreView)) {
                addFooterView(loadMoreView)
            }
        }
    }

    fun hideLoadMoreView() {
        mLoadMoreViewHolder?.apply {
            loadMoreView.visibility = View.GONE
        }
    }

    fun onCreateEmptyViewHolder(scope: () -> EmptyViewHolder) {
        mOnEmptyViewHolderCreator = scope
    }

    fun onCreateLoadMoreViewHolder(scope: () -> LoadMoreViewHolder) {
        mOnLoadMoreViewHolderCreator = scope
    }

    fun onBindLoadMoreViewHolder(listener: (LoadMoreViewHolder, Int, Boolean, Boolean) -> Unit) {
        mOnLoadMoreViewHolderBinder = listener
    }

    fun onBindEmptyViewHolder(listener: (EmptyViewHolder) -> Unit) {
        mOnEmptyViewHolderBinder = listener
    }

    override fun onLoadMore() {
        super.onLoadMore()
        mLoadMoreViewHolder?.let { mOnLoadMoreViewHolderBinder?.invoke(it, mState, isCanLoadMore, hasDataOrigin) }
    }

    override fun onLoadMoreCompleted() {
        super.onLoadMoreCompleted()
        mLoadMoreViewHolder?.let { mOnLoadMoreViewHolderBinder?.invoke(it, mState, isCanLoadMore, hasDataOrigin) }
        showEmptyViewIfNeed()
    }

    private fun refreshLoadMoreStatus() {
        mLoadMoreViewHolder?.let { mOnLoadMoreViewHolderBinder?.invoke(it, mState, isCanLoadMore, hasDataOrigin) }
    }

    abstract class LoadMoreViewHolder(val loadMoreView: View)

    abstract class EmptyViewHolder(val emptyView: View)

    private fun modifyMaxFlingVelocity(maxFlingVelocity: Int) {
        try {
            val recyclerViewClazz = RecyclerView::class.java
            val mMaxFlingVelocityField = recyclerViewClazz.getDeclaredField("mMaxFlingVelocity")
            mMaxFlingVelocityField.isAccessible = true

            mMaxFlingVelocityField.set(this, maxFlingVelocity)

            mMaxFlingVelocityField.isAccessible = false
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }

    private fun modifyMinFlingVelocity(minFlingVelocity: Int) {
        try {
            val recyclerViewClazz = RecyclerView::class.java
            val mMaxFlingVelocityField = recyclerViewClazz.getDeclaredField("mMinFlingVelocity")
            mMaxFlingVelocityField.isAccessible = true

            mMaxFlingVelocityField.set(this, minFlingVelocity)

            mMaxFlingVelocityField.isAccessible = false
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun setAdapter(adapter: Adapter<RecyclerView.ViewHolder>?) {
        this@LoadMoreListView.adapter?.unregisterAdapterDataObserver(mAdapterObserver)
        super.setAdapter(adapter)
        this@LoadMoreListView.adapter?.registerAdapterDataObserver(mAdapterObserver)
    }
}