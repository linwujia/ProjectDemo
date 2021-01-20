package com.linwujia.project.ui.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.*

open class HeaderFooterRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    init {
        overScrollMode = View.OVER_SCROLL_NEVER
        ViewCompat.setNestedScrollingEnabled(this, true)
    }

    override fun setAdapter(adapter: Adapter<RecyclerView.ViewHolder>?) {
        var resultAdapter = adapter
        val oldAdapter = getAdapter()
        if (adapter != null && oldAdapter is RecyclerHeaderFooterAdapter) {
            val headerFooterAdapter = RecyclerHeaderFooterAdapter(context, adapter)
            val headerCount = oldAdapter.getHeaderViewCount()
            val headerViews: MutableList<View> =
                ArrayList()
            for (i in 0 until headerCount) {
                val view = oldAdapter.getHeaderView(i)
                if (view != null) {
                    headerViews.add(view)
                }
            }
            val footerCount = oldAdapter.getFooterViewCount()
            val footerViews: MutableList<View> = ArrayList()
            for (i in 0 until footerCount) {
                val view = oldAdapter.getFooterView(i)
                if (view != null) {
                    footerViews.add(view)
                }
            }
            oldAdapter.runPenddingDetach()
            for (view in headerViews) {
                headerFooterAdapter.addHeaderView(view)
            }
            for (view in footerViews) {
                headerFooterAdapter.addFooterView(view)
            }
            resultAdapter = headerFooterAdapter
        }
        if (resultAdapter != null && resultAdapter !is RecyclerHeaderFooterAdapter) {
            resultAdapter = RecyclerHeaderFooterAdapter(context, adapter)
        }
        super.setAdapter(resultAdapter)
    }

    open fun setHeaderView(header: View?) {
        val adapter = adapter
        if (adapter is RecyclerHeaderFooterAdapter) {
            adapter.setHeaderView(header)
        } else {
            val headerFooterAdapter =
                RecyclerHeaderFooterAdapter(context, adapter)
            headerFooterAdapter.setHeaderView(header)
            setAdapter(headerFooterAdapter)
        }
    }

    open fun addHeaderView(header: View?) {
        val adapter = adapter
        if (adapter is RecyclerHeaderFooterAdapter) {
            adapter.addHeaderView(header)
        } else {
            val headerFooterAdapter =
                RecyclerHeaderFooterAdapter(context, adapter)
            headerFooterAdapter.addHeaderView(header)
            setAdapter(headerFooterAdapter)
        }
    }

    open fun addHeaderView(
        header: View?,
        layoutParams: ViewGroup.LayoutParams?
    ) {
        if (layoutParams == null) {
            addHeaderView(header)
            return
        }
        val adapter = adapter
        if (adapter is RecyclerHeaderFooterAdapter) {
            adapter.addHeaderView(header, layoutParams)
        } else {
            val headerFooterAdapter =
                RecyclerHeaderFooterAdapter(context, adapter)
            headerFooterAdapter.addHeaderView(header, layoutParams)
            setAdapter(headerFooterAdapter)
        }
    }

    open fun removeHeaderView() {
        val adapter = adapter
        if (adapter is RecyclerHeaderFooterAdapter) {
            adapter.removeHeader()
        }
    }

    open fun setFooterView(footer: View?) {
        val adapter = adapter
        if (adapter is RecyclerHeaderFooterAdapter) {
            adapter.setFooterView(footer)
        } else {
            val headerFooterAdapter =
                RecyclerHeaderFooterAdapter(context, adapter)
            headerFooterAdapter.setFooterView(footer)
            setAdapter(headerFooterAdapter)
        }
    }

    open fun addFooterView(footer: View?) {
        val adapter = adapter
        if (adapter is RecyclerHeaderFooterAdapter) {
            adapter.addFooterView(footer)
        } else {
            val headerFooterAdapter =
                RecyclerHeaderFooterAdapter(context, adapter)
            headerFooterAdapter.addFooterView(footer)
            setAdapter(headerFooterAdapter)
        }
    }

    open fun removeFooterView() {
        val adapter = adapter
        if (adapter is RecyclerHeaderFooterAdapter) {
            adapter.removeFooter()
        }
    }

    open fun getHeaderView(index: Int): View? {
        val adapter = adapter
        return if (adapter is RecyclerHeaderFooterAdapter) {
            adapter.getHeaderView(index)
        } else null
    }

    open fun getFooterView(index: Int): View? {
        val adapter = adapter
        return if (adapter is RecyclerHeaderFooterAdapter) {
            adapter.getFooterView(index)
        } else null
    }

    private class RecyclerHeaderFooterAdapter(context: Context, adapter: Adapter<RecyclerView.ViewHolder>?) : Adapter<RecyclerView.ViewHolder>() {

        private val mHeaderView: LinearLayout = WrapContentLinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        private val mFooterView: LinearLayout = WrapContentLinearLayout(context).apply { orientation = LinearLayout.VERTICAL }

        private var mWrappedAdapter: Adapter<RecyclerView.ViewHolder>? = adapter

        fun getHeaderViewContainer(): ViewGroup? {
            return mHeaderView
        }

        fun getFooterViewContainer(): ViewGroup? {
            return mFooterView
        }

        fun getHeaderViewCount(): Int {
            return mHeaderView.childCount
        }

        fun addHeaderView(view: View?) {
            mHeaderView.addView(view)
        }

        fun addHeaderView(
            view: View?,
            layoutParams: ViewGroup.LayoutParams?
        ) {
            mHeaderView.addView(view, layoutParams)
        }

        fun getHeaderView(index: Int): View? {
            return if (mHeaderView.childCount > index) {
                mHeaderView.getChildAt(index)
            } else null
        }

        fun setHeaderView(header: View?) {
            if (header == null) {
                removeHeader()
                return
            }
            mHeaderView.removeAllViews()
            mHeaderView.addView(header)
        }

        fun getFooterViewCount(): Int {
            return mFooterView.childCount
        }

        fun addFooterView(view: View?) {
            mFooterView.addView(view)
        }

        fun getFooterView(index: Int): View? {
            return if (mFooterView.childCount > index) {
                mFooterView.getChildAt(index)
            } else null
        }

        fun setFooterView(footer: View?) {
            if (footer == null) {
                removeFooter()
                return
            }
            mFooterView.removeAllViews()
            mFooterView.addView(footer)
        }

        fun removeHeader() {
            mHeaderView.removeAllViews()
        }

        fun removeFooter() {
            mFooterView.removeAllViews()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when(viewType) {
                VIEW_TYPE_HEAD -> ViewHolder(mHeaderView)
                VIEW_TYPE_FOOT -> ViewHolder(mFooterView)
                else -> mWrappedAdapter?.onCreateViewHolder(parent, viewType) ?: ViewHolder(View(parent.context))
            }
        }

        override fun getItemId(position: Int): Long {
            return when(position) {
                0, 1 -> -1
                else -> mWrappedAdapter?.getItemId(position -1) ?: 0
            }
        }

        override fun getItemCount(): Int {
            val itemCount = mWrappedAdapter?.itemCount ?: 0
            return itemCount + 2
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder.itemViewType != VIEW_TYPE_HEAD && holder.itemViewType != VIEW_TYPE_FOOT) {
                mWrappedAdapter?.onBindViewHolder(holder, position)
            }
        }

        override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int,
            payloads: MutableList<Any>
        ) {
            if (holder.itemViewType != VIEW_TYPE_HEAD && holder.itemViewType != VIEW_TYPE_FOOT) {
                mWrappedAdapter?.onBindViewHolder(holder, position, payloads)
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when(position) {
                0 -> VIEW_TYPE_HEAD
                itemCount - 1 -> VIEW_TYPE_FOOT
                else -> mWrappedAdapter?.getItemViewType(position) ?: super.getItemViewType(position)
            }
        }

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            mWrappedAdapter?.onAttachedToRecyclerView(recyclerView)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            mWrappedAdapter?.onDetachedFromRecyclerView(recyclerView)
        }

        override fun onFailedToRecycleView(holder: RecyclerView.ViewHolder): Boolean {
            if (holder !is ViewHolder && mWrappedAdapter != null) {
                return mWrappedAdapter!!.onFailedToRecycleView(holder)
            }

            return super.onFailedToRecycleView(holder)
        }

        override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
            if (holder !is ViewHolder) {
                mWrappedAdapter?.onViewDetachedFromWindow(holder)
            }
        }

        override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
            if (holder !is ViewHolder) {
                mWrappedAdapter?.onViewDetachedFromWindow(holder)
            }
        }

        fun runPenddingDetach() {
            mHeaderView.removeAllViews()
            mFooterView.removeAllViews()
        }

        companion object {
            private const val VIEW_TYPE_HEAD = -1
            private const val VIEW_TYPE_FOOT = -2
        }
    }

    private class WrapContentLinearLayout internal constructor(context: Context) :
        LinearLayout(context) {
        override fun onViewRemoved(child: View) {
            if (childCount <= 0) {
                visibility = View.GONE
            }
        }

        override fun onViewAdded(child: View) {
            visibility = View.VISIBLE
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)

            super.onMeasure(
                widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )

            var heightToDetermind = 0
            val childCount = childCount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                val lp = child.layoutParams as LayoutParams
                if (child.visibility != View.GONE) {
                    if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT) { //measure again
                        var childMeasureHeightSize: Int
                        var childMeasureHeightMode: Int
                        when (heightMode) {
                            MeasureSpec.AT_MOST, MeasureSpec.EXACTLY -> {
                                childMeasureHeightMode = heightMode
                                childMeasureHeightSize = heightSize
                            }
                            MeasureSpec.UNSPECIFIED -> {
                                childMeasureHeightMode = MeasureSpec.UNSPECIFIED
                                childMeasureHeightSize = 0
                            }
                            else -> throw IllegalArgumentException()
                        }
                        measureChildWithMargins(
                            child,
                            widthMeasureSpec,
                            0,
                            MeasureSpec.makeMeasureSpec(
                                childMeasureHeightSize,
                                childMeasureHeightMode
                            ),
                            0
                        )
                    }
                    heightToDetermind += child.measuredHeight + lp.topMargin + lp.bottomMargin
                }
            }
            setMeasuredDimension(measuredWidth, heightToDetermind)
        }

        init {
            visibility = View.GONE
            layoutParams = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    private class ViewHolder internal constructor(itemView: View) :
        RecyclerView.ViewHolder(itemView)
}