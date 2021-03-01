package com.linwujia.project.demo

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.linwujia.project.ui.indicator.PageIndicator

class TabPageIndicatorAdapter : PageIndicator.Adapter<TabPageIndicatorAdapter.TabPageIndicatorViewHolder>() {

    private val mDatas = arrayListOf<String>()

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): TabPageIndicatorViewHolder {
        return TabPageIndicatorViewHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.indicator_item_layout, viewGroup, false))
    }

    override fun onBindViewHolder(
        viewHolder: TabPageIndicatorViewHolder,
        position: Int,
        isSelected: Boolean
    ) {
        viewHolder.mTitleView?.apply {
            text = mDatas[position]
            setTextColor(if (isSelected) Color.RED else Color.BLACK)
            typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

        viewHolder.itemView.setBackgroundColor(if (isSelected) Color.WHITE else Color.GRAY)
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    class TabPageIndicatorViewHolder(itemView: View) : PageIndicator.ViewHolder(itemView) {
        val mTitleView: TextView? by lazy {
            itemView.findViewById<TextView>(R.id.indicator_title)
        }
    }

    fun notifyDataSetChanged(datas: ArrayList<String>) {
        mDatas.clear()
        mDatas.addAll(datas)
        notifyDataSetChanged()
    }
}