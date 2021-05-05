package com.linwujia.project.demo.tab

import android.content.Context
import android.database.DataSetObserver
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.linwujia.project.ui.R
import com.linwujia.project.ui.indicator.PageIndicator
import kotlin.math.max
import kotlin.math.min

class DrawablePageIndicator @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr),  PageIndicator {
    override val mCurrentPage: Int
        get() = mSelectedTabIndex

    private var mSelectedTabIndex = 0
        set(value) {
            val item = max(0, min(value, mViewPager?.adapter?.count ?: 0))
            field = item
        }

    override var mAdapter: PageIndicator.Adapter<PageIndicator.ViewHolder>? = null

    private val mOnPageScrolledListeners: MutableList<(position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit> by lazy {
        arrayListOf<(position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit>()
    }
    private val mOnPageSelectedListeners: MutableList<(position: Int) -> Unit> by lazy {
        arrayListOf<(position: Int) -> Unit>()
    }
    private val mOnPageScrollStateChangedListeners: MutableList<(state: Int) -> Unit> by lazy {
        arrayListOf<(state: Int) -> Unit>()
    }

    private val mOnTabSelectedListeners: MutableList<(Int) -> Unit> by lazy {
        arrayListOf<(Int) -> Unit>()
    }
    private val mOnTabReselectedListeners: MutableList<(Int) -> Unit> by lazy {
        arrayListOf<(Int) -> Unit>()
    }

    private var mViewPager: ViewPager? = null
        set(value) {
            field?.apply {
                mOnPageChangedListener?.also(::removeOnPageChangeListener)
                mOnAdapterChangedListener?.also(::removeOnAdapterChangeListener)
            }

            field = value

            value?.apply {
                if (mOnPageChangedListener == null) {
                    mOnPageChangedListener = DrawablePageIndicatorOnPageChangedListener()
                }
                mOnPageChangedListener?.also(::addOnPageChangeListener)

                if (mOnAdapterChangedListener == null) {
                    mOnAdapterChangedListener = DrawablePageIndicatorOnAdapterChangedListener()
                }

                mOnAdapterChangedListener?.also(::addOnAdapterChangeListener)

                adapter?.apply {

                }
            }
        }
    private var mOnPageChangedListener: ViewPager.OnPageChangeListener? = null
    private var mOnAdapterChangedListener: ViewPager.OnAdapterChangeListener? = null

    private var mPagerAdapterObserver: DataSetObserver? = null

    private var mPagerAdapter: PagerAdapter? = null
        set(value) {
            field?.apply {
                mPagerAdapterObserver?.also(::unregisterDataSetObserver)
            }

            field = value
            value?.apply {
                if (mPagerAdapterObserver == null) {
                    mPagerAdapterObserver = PagerAdapterObserver()
                }
                mPagerAdapterObserver?.also(::registerDataSetObserver)
            }

            notifyDataSetChanged()
        }

    private val mOnDrawableIndicatorClickListener = OnClickListener {
        val position: Int = it.getTag(R.integer.tab_page_indicator_position) as Int
        if (mSelectedTabIndex == position) {
            dispatchOnTabReselected(position)
        }

        dispatchOnTabSelected(position)
        moveToPage(position)
    }

    override fun setupViewPager(view: ViewPager?) {
        setupViewPager(view, view?.currentItem ?: 0)
    }

    override fun setupViewPager(view: ViewPager?, initialPosition: Int) {
        mViewPager = view
        moveToPage(initialPosition)
    }

    override fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?) {
        TODO("Not yet implemented")
    }

    override fun onPageScrolledListener(listener: (position: Int, positionOffset: Float, positionOffsetPixels: Int) -> Unit) {
        mOnPageScrolledListeners.add(listener)
    }

    override fun onPageSelectedListener(listener: (position: Int) -> Unit) {
        mOnPageSelectedListeners.add(listener)
    }

    override fun onPageScrollStateChangedListener(listener: (state: Int) -> Unit) {
        mOnPageScrollStateChangedListeners.add(listener)
    }

    override fun onTabSelectedListener(listener: (Int) -> Unit) {
        mOnTabSelectedListeners.add(listener)
    }

    override fun onTabReselectedListener(listener: (Int) -> Unit) {
        mOnTabReselectedListeners.add(listener)
    }

    override fun notifyDataSetChanged() {
        populateFromPagerAdapter()
    }

    override fun moveToPage(position: Int) {
        mSelectedTabIndex = position
        selectItem()
    }

    override fun getViewHolderAtPosition(position: Int): PageIndicator.ViewHolder? {
        return null
    }

    private fun populateFromPagerAdapter() {
        removeAllViews()
        mPagerAdapter?.apply {
            val adapterCount: Int = count
            for (i in 0 until adapterCount) {
                addView(View(context).apply {
                    setTag(R.integer.tab_page_indicator_position, i)
                    setOnClickListener(mOnDrawableIndicatorClickListener)
                    isSelected = (i == mSelectedTabIndex)
                })
            }
        }
    }

    private fun selectItem() {
        mViewPager?.currentItem = mSelectedTabIndex
        val count = childCount
        var view: View
        for (i in 0 until count) {
            view = getChildAt(i)
            view.isSelected = (i == mSelectedTabIndex)
        }
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

    private inner class DrawablePageIndicatorOnPageChangedListener : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
            dispatchOnPageScrollStateChanged(state)
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            dispatchOnPageScrolled(position, positionOffset, positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            dispatchOnPageSelected(position)
        }

    }

    private inner class DrawablePageIndicatorOnAdapterChangedListener : ViewPager.OnAdapterChangeListener {
        override fun onAdapterChanged(
            viewPager: ViewPager,
            oldAdapter: PagerAdapter?,
            newAdapter: PagerAdapter?
        ) {
            mPagerAdapter = newAdapter
        }

    }

    private inner class PagerAdapterObserver internal constructor() : DataSetObserver() {
        override fun onChanged() {
            notifyDataSetChanged()
        }

        override fun onInvalidated() {
            notifyDataSetChanged()
        }
    }
}