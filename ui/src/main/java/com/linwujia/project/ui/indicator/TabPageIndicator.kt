package com.linwujia.project.ui.indicator

import android.content.Context
import android.database.Observable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.linwujia.project.ui.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

open class TabPageIndicator(context: Context, attrs: AttributeSet? = null) : HorizontalScrollView(context, attrs), PageIndicator {

    private val mTabLayout: LinearLayout

    private val mObserver: TabPageIndicatorDataObserver = TabPageIndicatorDataObserver()

    private var mSelectedTabIndex = 0

    private val mAttachedScrap = ArrayList<PageIndicator.ViewHolder>()

    private var mTabSelector: Runnable? = null

    private val mOnPageScrolledListeners: MutableList<(position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit> by lazy {
        arrayListOf<(position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit>()
    }
    private val mOnPageSelectedListeners: MutableList<(position: Int) -> Unit> by lazy {
        arrayListOf<(position: Int) -> Unit>()
    }
    private val mOnPageScrollStateChangedListeners: MutableList<(state: Int) -> Unit> by lazy {
        arrayListOf<(state: Int) -> Unit>()
    }
    private var mOnPageChangedListener: ViewPager.OnPageChangeListener? = null

    private val mOnTabSelectedListeners: MutableList<(Int) -> Unit> by lazy {
        arrayListOf<(Int) -> Unit>()
    }
    private val mOnTabReselectedListeners: MutableList<(Int) -> Unit> by lazy {
        arrayListOf<(Int) -> Unit>()
    }

    private val mInternalOnPageChangedListener: TabPageOnPageChangedListener = TabPageOnPageChangedListener()
    private val mOnTabClickListener = OnClickListener {
        val position: Int = it.getTag(R.integer.tab_page_indicator_position) as Int
        if (mSelectedTabIndex == position) {
            dispatchOnTabSelected(position)
        }

        dispatchOnTabReselected(position)
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
        isHorizontalScrollBarEnabled = false
        isFillViewport = true

        mTabLayout = LinearLayout(context)
        addView(mTabLayout, LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val lockedExpanded = widthMode == MeasureSpec.EXACTLY
        isFillViewport = lockedExpanded

        val oldWidth = measuredWidth
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val newWidth = measuredWidth

        if (lockedExpanded && oldWidth != newWidth) {
            // Re-center the tab display if we're at a new (scrollable) size.
            updateTab()
        }
    }

    override fun setupViewPager(view: ViewPager?) {
        setupViewPager(view, view?.currentItem ?: 0)
    }

    override fun setupViewPager(view: ViewPager?, initialPosition: Int) {
        mViewPager = view
        moveToPage(initialPosition)
    }

    override fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?) {
        mOnPageChangedListener = listener
    }

    override fun onPageScrolledListener(listener: (position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit) {
        mOnPageScrolledListeners
        mOnPageScrolledListeners.add(listener)
    }

    override fun onPageSelectedListener(listener: (position: Int) -> Unit) {
        mOnPageSelectedListeners
        mOnPageSelectedListeners.add(listener)
    }

    override fun onPageScrollStateChangedListener(listener: (state: Int) -> Unit) {
        mOnPageScrollStateChangedListeners
        mOnPageScrollStateChangedListeners.add(listener)
    }

    override fun onTabSelectedListener(listener: (Int) -> Unit) {
        mOnTabSelectedListeners
        mOnTabSelectedListeners.add(listener)
    }

    override fun onTabReselectedListener(listener: (Int) -> Unit) {
        mOnTabReselectedListeners
        mOnTabReselectedListeners.add(listener)
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
            val scrollPos = tabView.left - (width - tabView.width) / 2
            smoothScrollTo(scrollPos, 0)
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

    private fun dispatchOnTabSelected(position: Int) {
        mOnTabSelectedListeners.forEach { listener ->
            listener(position)
        }
    }

    private fun dispatchOnTabReselected(position: Int) {
        mOnTabReselectedListeners.forEach { listener ->
            listener(position)
        }
    }

    private fun dispatchOnPageScrollStateChanged(state: Int) {
        mOnPageScrollStateChangedListeners.forEach { listener ->
            listener(state)
        }
    }

    private fun dispatchOnPageScrolled(position: Int,
                                       positionOffset: Float,
                                       positionOffsetPixels: Int) {
        mOnPageScrolledListeners.forEach { listener ->
            listener(position, positionOffset, positionOffsetPixels)
        }
    }

    private fun dispatchOnPageSelected(position: Int) {
        mOnPageSelectedListeners.forEach { listener ->
            listener(position)
        }
    }

    private inner class TabPageIndicatorDataObserver : PageIndicator.AdapterDataObserver {
        override fun onChanged() {
            notifyDataSetChanged()
        }
    }

    private inner class TabPageOnPageChangedListener : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
            mOnPageChangedListener?.onPageScrollStateChanged(state)
            dispatchOnPageScrollStateChanged(state)
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            mOnPageChangedListener?.onPageScrolled(position, positionOffset, positionOffsetPixels)
            dispatchOnPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            mOnPageChangedListener?.onPageSelected(position)
            dispatchOnPageSelected(position)
            moveToPage(position)
        }
    }

    companion object {
        private const val TAG: Int = 0xffffff
    }
}