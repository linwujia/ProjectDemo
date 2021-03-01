package com.linwujia.project.ui.indicator

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.viewpager.widget.ViewPager
import com.linwujia.project.ui.R
import kotlin.math.min

class VerticalTabPageIndicator(context: Context, attrs: AttributeSet? = null
) : ScrollView(context, attrs), PageIndicator {

    private val mTabLayout: LinearLayout

    private val mObserver: TabPageIndicatorDataObserver = TabPageIndicatorDataObserver()

    private var mSelectedTabIndex = 0

    private val mAttachedScrap = ArrayList<PageIndicator.ViewHolder>()

    private var mTabSelector: Runnable? = null

    private var mOnPageScrolledListener: ((position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit)? = null
    private var mOnPageSelectedListener: ((position: Int) -> Unit)? = null
    private var mOnPageScrollStateChangedListener: ((state: Int) -> Unit)? = null
    private var mOnPageChangedListener: ViewPager.OnPageChangeListener? = null

    private var mOnTabSelectedListener: ((Int) -> Unit)? = null
    private var mOnTabReselectedListener: ((Int) -> Unit)? = null

    private val mInternalOnPageChangedListener: TabPageOnPageChangedListener = TabPageOnPageChangedListener()
    private val mOnTabClickListener = OnClickListener {
        val position: Int = it.getTag(R.integer.tab_page_indicator_position) as Int
        if (mSelectedTabIndex == position) {
            mOnTabReselectedListener?.invoke(position)
        }

        mOnTabSelectedListener?.invoke(position)
        moveToPage(position)
    }

    val mContainer: ViewGroup
        get() = mTabLayout

    override var mAdapter: PageIndicator.Adapter<PageIndicator.ViewHolder>? = null
        set(value) {
            field?.apply {
                unregisterAdapterDataObserver(mObserver)
            }

            field = value
            value?.run {
                registerAdapterDataObserver(mObserver)
            }
            notifyDataSetChanged()
        }

    private var mViewPager: ViewPager? = null
        set(value) {
            if (value == field) return
            field?.run {
                removeOnPageChangeListener(mInternalOnPageChangedListener)
            }
            field = value
            value?.run {
                addOnPageChangeListener(mInternalOnPageChangedListener)
            }
        }

    override val mCurrentPage: Int
        get() = mSelectedTabIndex

    init {
        tag = TAG
        isVerticalScrollBarEnabled = false
        isFillViewport = true

        mTabLayout = LinearLayout(context).also { it.orientation = LinearLayout.VERTICAL }
        addView(mTabLayout, LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val lockedExpanded = heightMode == MeasureSpec.EXACTLY
        isFillViewport = lockedExpanded

        val oldHeight = measuredHeight
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val newHeight = measuredHeight

        if (lockedExpanded && oldHeight != newHeight) {
            // Re-center the tab display if we're at a new (scrollable) size.
            updateTab()
        }
    }

    override fun setViewPager(view: ViewPager?) {
        setViewPager(view, view?.currentItem ?: 0)
    }

    override fun setViewPager(view: ViewPager?, initialPosition: Int) {
        mViewPager = view
        moveToPage(initialPosition)
    }

    override fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?) {
        mOnPageChangedListener = listener
    }

    override fun onPageScrolledListener(listener: (position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit) {
        mOnPageScrolledListener = listener
    }

    override fun onPageSelectedListener(listener: (position: Int) -> Unit) {
        mOnPageSelectedListener = listener
    }

    override fun onPageScrollStateChangedListener(listener: (state: Int) -> Unit) {
        mOnPageScrollStateChangedListener = listener
    }

    override fun onTabSelectedListener(listener: (Int) -> Unit) {
        mOnTabSelectedListener = listener
    }

    override fun onTabReselectedListener(listener: (Int) -> Unit) {
        mOnTabReselectedListener = listener
    }

    override fun notifyDataSetChanged() {
        mTabLayout.removeAllViews()
        mAttachedScrap.clear()
        mSelectedTabIndex = 0

        mAdapter?.run {
            val count = getItemCount()
            for (position in 0 until count) {
                val itemType = getItemType(position)
                val viewHolder = onCreateViewHolder(mTabLayout, itemType)
                viewHolder.itemView.setTag(R.integer.tab_page_indicator_position, position)
                viewHolder.itemView.setOnClickListener(mOnTabClickListener)
                mTabLayout.addView(viewHolder.itemView)
                mAttachedScrap.add(viewHolder)
                onBindViewHolder(viewHolder, position, mSelectedTabIndex == position)
            }
        }
    }

    override fun moveToPage(position: Int) {
        mAdapter?.run {
            val count = getItemCount()
            val target = min(count, mViewPager?.adapter?.count ?: 0)
            if (position < 0 || position > target) return

            mSelectedTabIndex = position
            mViewPager?.currentItem = position
            updateTab()
        }
    }

    private fun updateTab() {
        mAdapter?.run {
            val count = getItemCount()
            for (position in 0 until count) {
                val viewHolder = mAttachedScrap[position]
                val isSelected = mSelectedTabIndex == position
                onBindViewHolder(viewHolder, position, isSelected)
                if (isSelected) {
                    animateToTab(position)
                }
            }
        }
    }

    private fun animateToTab(position: Int) {
        val tabView = mAttachedScrap[position].itemView
        if (mTabSelector != null) {
            removeCallbacks(mTabSelector)
        }
        mTabSelector = Runnable {
            val scrollPos = tabView.top - (height - tabView.height) / 2
            smoothScrollTo(0, scrollPos)
            mTabSelector = null
        }
        post(mTabSelector)
    }

    override fun getViewHolderAtPosition(position: Int) : PageIndicator.ViewHolder? {
        if (position in 0 until mAttachedScrap.size) {
            return mAttachedScrap[position]
        }
        return null
    }

    private inner class TabPageIndicatorDataObserver : PageIndicator.AdapterDataObserver {
        override fun onChanged() {
            notifyDataSetChanged()
        }
    }

    private inner class TabPageOnPageChangedListener : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
            mOnPageChangedListener?.onPageScrollStateChanged(state)
            mOnPageScrollStateChangedListener?.invoke(state)
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            mOnPageChangedListener?.onPageScrolled(position, positionOffset, positionOffsetPixels)
            mOnPageScrolledListener?.invoke(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            mOnPageChangedListener?.onPageSelected(position)
            mOnPageSelectedListener?.invoke(position)
            moveToPage(position)
        }
    }

    companion object {
        private const val TAG: Int = 0xffffff
    }
}