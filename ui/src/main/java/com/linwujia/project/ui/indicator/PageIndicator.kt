package com.linwujia.project.ui.indicator

import android.database.Observable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener

interface PageIndicator {
    val mCurrentPage: Int

    var mAdapter: Adapter<ViewHolder>?

    /**
     * Bind the indicator to a ViewPager.
     */
    fun setViewPager(view: ViewPager?)

    /**
     * Bind the indicator to a ViewPager.
     */
    fun setViewPager(view: ViewPager?, initialPosition: Int)

    /**
     * Set a page change listener which will receive forwarded events.
     */
    fun setOnPageChangeListener(listener: OnPageChangeListener?)

    /**
     * This method will be invoked when the current page is scrolled, either as part
     * of a programmatically initiated smooth scroll or a user initiated touch scroll.
     *
     * @param position Position index of the first page currently being displayed.
     * Page position+1 will be visible if positionOffset is nonzero.
     * @param positionOffset Value from [0, 1) indicating the offset from the page at position.
     * @param positionOffsetPixels Value in pixels indicating the offset from position.
     */
    fun onPageScrolledListener(listener: (position: Int,
                                       positionOffset: Float,
                                       positionOffsetPixels: Int) -> Unit
    )

    /**
     * This method will be invoked when a new page becomes selected. Animation is not
     * necessarily complete.
     *
     * @param position Position index of the new selected page.
     */
    fun onPageSelectedListener(listener: (position: Int) -> Unit)

    /**
     * Called when the scroll state changes. Useful for discovering when the user
     * begins dragging, when the pager is automatically settling to the current page,
     * or when it is fully stopped/idle.
     *
     * @param state The new scroll state.
     * @see ViewPager.SCROLL_STATE_IDLE
     *
     * @see ViewPager.SCROLL_STATE_DRAGGING
     *
     * @see ViewPager.SCROLL_STATE_SETTLING
     */
    fun onPageScrollStateChangedListener(listener: (state: Int) -> Unit)

    fun onTabSelectedListener(listener: (Int) -> Unit)

    fun onTabReselectedListener(listener: (Int) -> Unit)

    /**
     * Notify the indicator that the fragment list has changed.
     */
    fun notifyDataSetChanged()

    /**
     * move to Page
     * @param position page index
     */
    fun moveToPage(position: Int)

    /**
     * get viewHolder At Position
     * @param position position
     * @return viewHolder?
     */
    fun getViewHolderAtPosition(position: Int) : ViewHolder?

    class AdapterDataObservable : Observable<AdapterDataObserver>() {
        val hasObservers: Boolean
            get() =  mObservers.isNotEmpty()

        fun notifyChanged() {
            // since onChanged() is implemented by the app, it could do anything, including
            // removing itself from {@link mObservers} - and that could cause problems if
            // an iterator is used on the ArrayList {@link mObservers}.
            // to avoid such problems, just march thru the list in the reverse order.
            for (i in mObservers.indices.reversed()) {
                mObservers[i].onChanged()
            }
        }
    }

    interface AdapterDataObserver {
        fun onChanged()
    }

    abstract class Adapter<out VH : ViewHolder> {
        private val mObservable = AdapterDataObservable()

        /**
         * Returns true if one or more observers are attached to this adapter.
         *
         * @return true if this adapter has observers
         */
        val hasObservers: Boolean = mObservable.hasObservers

        /**
         * Register a new observer to listen for data changes.
         *
         * <p>The adapter may publish a variety of events describing specific changes.
         * Not all adapters may support all change types and some may fall back to a generic
         * {@link TabPageIndicator.AdapterDataObserver#onChanged()
         * "something changed"} event if more specific data is not available.</p>
         *
         * <p>Components registering observers with an adapter are responsible for
         * {@link #unregisterAdapterDataObserver(TabPageIndicator.AdapterDataObserver)
         * unregistering} those observers when finished.</p>
         *
         * @param observer Observer to register
         *
         * @see #unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver)
         */
        fun registerAdapterDataObserver(observer: AdapterDataObserver) {
            mObservable.registerObserver(observer)
        }

        /**
         * Unregister an observer currently listening for data changes.
         *
         *
         * The unregistered observer will no longer receive events about changes
         * to the adapter.
         *
         * @param observer Observer to unregister
         *
         * @see .registerAdapterDataObserver
         */
        fun unregisterAdapterDataObserver(observer: AdapterDataObserver) {
            mObservable.unregisterObserver(observer)
        }

        fun notifyDataSetChanged() {
            mObservable.notifyChanged()
        }

        abstract fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) : VH
        abstract fun onBindViewHolder(viewHolder: @UnsafeVariance VH, position: Int, isSelected: Boolean)
        fun getItemType(position: Int) = 0
        abstract fun getItemCount() : Int
    }

    public abstract class ViewHolder(val itemView: View) {

    }
}