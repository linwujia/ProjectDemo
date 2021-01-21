package com.linwujia.project.ui.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.IntDef
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.linwujia.project.ui.R
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.*

open class BaseLoadMoreRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : HeaderFooterRecyclerView(context, attrs, defStyleAttr) {

    @StatusInt
    var mState: Int = STATUS_IDLE
        private set

    private val mOnLoadMoreListener by lazy {
        LinkedList<OnLoadMoreListener>()
    }
    private var mAutoLoadMore = false
    private var mInterceptLoadMore = true
    var mLoadMoreThreshold = 0
    private var mScrollListener: OnScrollListener? = null

    var isAutoLoadMore: Boolean
        get() = mAutoLoadMore
        set(autoLoadMore) {
            if (autoLoadMore != mAutoLoadMore) {
                if (autoLoadMore) {
                    if (mScrollListener == null) {
                        mScrollListener = OnScrollChangerListener()
                    }
                    addOnScrollListener(mScrollListener!!)
                } else {
                    mScrollListener?.let { removeOnScrollListener(it) }
                }
                mAutoLoadMore = autoLoadMore
            }
        }

    init {
        var autoLoadMore = false
        var loadMoreThreshold = AUTO_LOAD_MORE_THRESHOLD
        var orientation = LinearLayoutManager.VERTICAL
        if (attrs != null) {
            val t = context.obtainStyledAttributes(attrs, R.styleable.BaseLoadMoreRecyclerView)
            if (t != null) {
                try {
                    autoLoadMore = t.getBoolean(R.styleable.BaseLoadMoreRecyclerView_auto_load_more, false)
                    loadMoreThreshold = t.getInteger(R.styleable.BaseLoadMoreRecyclerView_auto_load_more_threshold, AUTO_LOAD_MORE_THRESHOLD)
                    orientation = t.getInteger(R.styleable.BaseLoadMoreRecyclerView_orientation, LinearLayoutManager.VERTICAL)
                } finally {
                    t.recycle()
                }
            }
        }

        layoutManager = DefaultLinearLayoutManager(context, orientation, false)
        mLoadMoreThreshold = loadMoreThreshold
        isAutoLoadMore = autoLoadMore
    }

    fun performLoadMore() {
        post {
            if (mState != STATUS_ON_LOAD_MORE) {
                onLoadMore()
            }
        }
    }

    fun performLoadMoreCompleted() {
        post {
            if (mState != STATUS_IDLE) {
                onLoadMoreCompleted()
            }
        }
    }

    fun addOnLoadMoreListener(listener: OnLoadMoreListener?) {
        listener?.apply {
            mOnLoadMoreListener.add(this)
        }
    }

    fun onLoadMoreCompleted(onLoadMoreCompleted: () -> Unit) {
        mOnLoadMoreListener.add(SimpleOnLoadMoreListener.buildOnLoadMoreListener(onLoadMoreCompleted = onLoadMoreCompleted))
    }

    fun onLoadMore(onLoadMore: () -> Unit) {
        mOnLoadMoreListener.add(SimpleOnLoadMoreListener.buildOnLoadMoreListener(onLoadMore = onLoadMore))
    }

    @CallSuper
    protected open fun onLoadMoreCompleted() {
        mState = STATUS_IDLE
        for (l in mOnLoadMoreListener) {
            l.onLoadMoreCompleted()
        }
    }

    override fun setAdapter(adapter: Adapter<RecyclerView.ViewHolder>?) {
        super.setAdapter(adapter)
        mInterceptLoadMore = false
    }

    @CallSuper
    protected open fun onLoadMore() {
        mState = STATUS_ON_LOAD_MORE
        mInterceptLoadMore = true
        for (l in mOnLoadMoreListener) {
            l.onLoadMore()
        }
    }

    @IntDef(
        STATUS_IDLE,
        STATUS_ON_LOAD_MORE
    )
    @Retention(RetentionPolicy.SOURCE)
    private annotation class StatusInt

    interface OnLoadMoreListener {
        fun onLoadMore()
        fun onLoadMoreCompleted()
    }

    class SimpleOnLoadMoreListener private constructor(): OnLoadMoreListener{

        private var mOnLoadMore: (() -> Unit)? = null
        private var mOnLoadMoreCompleted: (() -> Unit)? = null

        override fun onLoadMore() {
            mOnLoadMore?.invoke()
        }

        override fun onLoadMoreCompleted() {
            mOnLoadMoreCompleted?.invoke()
        }

        companion object {
            fun buildOnLoadMoreListener(onLoadMore: (() -> Unit)? = null, onLoadMoreCompleted: (() -> Unit)? = null) : SimpleOnLoadMoreListener {
                return SimpleOnLoadMoreListener().also {
                    it.mOnLoadMore = onLoadMore
                    it.mOnLoadMoreCompleted = onLoadMoreCompleted
                }
            }
        }

    }

    class DefaultLinearLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean) : LinearLayoutManager(context, orientation, reverseLayout) {

        override fun generateDefaultLayoutParams(): LayoutParams {
            return LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private inner class OnScrollChangerListener : OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (mState != STATUS_IDLE) {
                return
            }
            val layout: LayoutManager = layoutManager ?: return
            val tempAdapter = adapter ?: return
            if (layout is LinearLayoutManager) {
                val lastVisibleItem = layout.findLastVisibleItemPosition()
                if (lastVisibleItem >= 0 && tempAdapter.itemCount - lastVisibleItem <= mLoadMoreThreshold) {
                    if (!ViewCompat.isInLayout(recyclerView) && !mInterceptLoadMore) {
                        onLoadMore()
                    }
                } else {
                    if (mInterceptLoadMore) {
                        mInterceptLoadMore = false
                    }
                }
            }
        }
    }

    companion object {
        protected const val STATUS_IDLE = 1 shl 0
        protected const val STATUS_ON_LOAD_MORE = 1 shl 1
        private const val AUTO_LOAD_MORE_THRESHOLD = 2
    }
}