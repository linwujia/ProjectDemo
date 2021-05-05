package com.linwujia.project.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.linwujia.project.base.NullContract
import com.linwujia.project.demo.base.RefreshLoadMoreActivity
import com.linwujia.project.demo.tab.HorizontalTabPageIndicatorActivity
import com.linwujia.project.demo.tab.VerticalTabPageIndicatorActivity

class MainActivity : RefreshLoadMoreActivity<NullContract.NullPresenter, MainActivity.MenuViewHolder, MainActivity.MenuItem>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onRefresh()
    }

    data class MenuItem(val title: String, val dest: Class<out Activity>)

    class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mTitleView: TextView by lazy {
            itemView.findViewById<TextView>(R.id.menu_title)
        }

        fun applyData(data: MenuItem) {
            itemView.setOnClickListener {
                it.context.startActivity(Intent(it.context, data.dest))
            }

            mTitleView.text = data.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.menu_item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int, data: MenuItem) {
        holder.applyData(data)
    }

    override fun onRefresh() {
        val list = arrayListOf<MenuItem>()
        list.add(MenuItem("Horizontal Tab Page Indicator", HorizontalTabPageIndicatorActivity::class.java))
        list.add(MenuItem("Vertical Tab Page Indicator", VerticalTabPageIndicatorActivity::class.java))

        mAdapter.notifyDataSetChanged(list)
    }

    override fun onLoadMore() {

    }

    override fun createPresenter() = NullContract.NullPresenter()

}