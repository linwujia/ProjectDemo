package com.linwujia.project.demo.base

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class RefreshLoadMoreAdapter<VH : RecyclerView.ViewHolder, DataType>(private val viewHolderCreator: (parent: ViewGroup, viewType: Int) -> VH, data: List<DataType>? = null) : RecyclerView.Adapter<VH>() {

    private val mData = arrayListOf<DataType>()
    private var mOnBindViewHolder: ((holder: VH, position: Int, data: DataType) -> Unit)? = null

    init {
        mData.clear()
        data?.also {
            mData.addAll(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = viewHolderCreator(parent, viewType)

    override fun getItemCount() = mData.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        mOnBindViewHolder?.invoke(holder, position, mData[position])
    }

    fun notifyDataSetChanged(data: List<DataType>) {
        mData.apply {
            clear()
            addAll(data)
            notifyDataSetChanged()
        }
    }

    fun onBindViewHolder(onBindViewHolder: (holder: VH, position: Int, data: DataType) -> Unit) {
        mOnBindViewHolder = onBindViewHolder
    }

    fun getItemData(position: Int): DataType = mData[position]
}