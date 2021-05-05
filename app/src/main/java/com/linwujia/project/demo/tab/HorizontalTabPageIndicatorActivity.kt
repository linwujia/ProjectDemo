package com.linwujia.project.demo.tab

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.linwujia.project.base.NullPresenterActivity
import com.linwujia.project.demo.R
import com.linwujia.project.ui.indicator.TabPageIndicator
import com.linwujia.project.ui.indicator.VerticalTabPageIndicator

class HorizontalTabPageIndicatorActivity : NullPresenterActivity() {

    private lateinit var mTabIndicator: TabPageIndicator
    private lateinit var mAdapter: TabPageIndicatorAdapter
    private lateinit var mViewPager: ViewPager
    private lateinit var mViewPagerAdapter: PagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_horizontal_tab_page)

        mTabIndicator = findViewById(R.id.indicator)
        mTabIndicator.onPageScrolledListener { position, positionOffset, _ ->
            val leftViewHolder = mTabIndicator.getViewHolderAtPosition(position) as TabPageIndicatorAdapter.TabPageIndicatorViewHolder
            if (positionOffset > 0f) {
                val rightViewHolder = mTabIndicator.getViewHolderAtPosition(position + 1) as TabPageIndicatorAdapter.TabPageIndicatorViewHolder
                rightViewHolder.mTitleView?.textSize = 15 + 5 * positionOffset
            }
            leftViewHolder.mTitleView?.textSize = 15 + 5 * (1 - positionOffset)
        }
        mViewPager = findViewById(R.id.viewPager)

        initAdapter()
        mViewPager.adapter = mViewPagerAdapter
        mTabIndicator.mAdapter = mAdapter
        mTabIndicator.setupViewPager(mViewPager)
    }

    private fun initAdapter() {
        mAdapter = TabPageIndicatorAdapter()
        var datas = arrayListOf<String>()
        for (i in 0 until 20) {
            datas.add("标题$i")
        }
        mAdapter.notifyDataSetChanged(datas)

        mViewPagerAdapter = object : PagerAdapter() {
            override fun isViewFromObject(view: View, `object`: Any): Boolean {
                return view === `object`
            }

            override fun getCount(): Int {
                return 20
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = View(container.context)
                val color = when(position % 6) {
                    1, 3 -> Color.RED
                    5 -> Color.BLUE
                    else -> Color.YELLOW
                }
                view.setBackgroundColor(color)
                container.addView(view)
                return view
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View?)
            }
        }
    }
}