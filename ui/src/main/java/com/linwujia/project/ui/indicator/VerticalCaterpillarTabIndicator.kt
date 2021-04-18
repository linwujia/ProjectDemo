package com.linwujia.project.ui.indicator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.linwujia.project.ui.R
import com.linwujia.project.ui.utils.ViewUtil

class VerticalCaterpillarTabIndicator(context: Context, attrs: AttributeSet? = null) : VerticalTabPageIndicator(context, attrs) {
    private var mCaterpillarDrawable: Drawable?
    private val mCaterpillarHeight = resources.getDimensionPixelOffset(R.dimen.dp_size_17dp)
    private val mCaterpillarLeftMargin: Int
    private val mCaterpillarWidth: Int

    private var mCurrentPosition = 0
    private var mCurrentPositionOffset = 0f
    private val mDirty = Rect(0, 0, 0, 0)

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CaterpillarTabIndicator)
        mCaterpillarDrawable = ta.getDrawable(R.styleable.CaterpillarTabIndicator_caterpillar_drawable) ?: ContextCompat.getDrawable(context, R.drawable.circle_b1_shape)
        mCaterpillarWidth = ta.getDimensionPixelSize(
            R.styleable.CaterpillarTabIndicator_caterpillar_height,
            resources.getDimensionPixelOffset(R.dimen.dp_size_2dp)
        )

        mCaterpillarLeftMargin = ta.getDimensionPixelSize(
            R.styleable.CaterpillarTabIndicator_caterpillar_margin_bottom,
            resources.getDimensionPixelOffset(R.dimen.dp_size_5dp)
        )
        ta.recycle()

        onPageScrolledListener {position, positionOffset, _ ->
            mCurrentPosition = position
            mCurrentPositionOffset = positionOffset
            invalidate()
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        updateDirty()
    }

    private fun updateDirty() {
        val dirtyLeft: Int = mCaterpillarLeftMargin + mCaterpillarWidth
        mDirty.set(dirtyLeft, 0, dirtyLeft + mCaterpillarWidth, mCaterpillarHeight)
    }

    override fun onDrawForeground(canvas: Canvas) {
        super.onDrawForeground(canvas)
        drawCaterpillar(canvas)
    }

    private fun drawCaterpillar(canvas: Canvas) {
        val caterpillarDrawable = mCaterpillarDrawable
        caterpillarDrawable?.run {
            val topViewHolder = getViewHolderAtPosition(mCurrentPosition)
            val bottomViewHolder = getViewHolderAtPosition(mCurrentPosition + 1)

            if (topViewHolder == null) return@run

            var topViewToMeasure = if (topViewHolder is CaterpillarTabIndicator.CaterpillarViewHolder) topViewHolder.caterpillarRelyOn else topViewHolder.itemView
            var bottomViewToMeasure = if (bottomViewHolder is CaterpillarTabIndicator.CaterpillarViewHolder) bottomViewHolder.caterpillarRelyOn else bottomViewHolder?.itemView

            val drawableBound = getCaterpillarBound(topViewToMeasure, bottomViewToMeasure, mCurrentPositionOffset)
            caterpillarDrawable.setBounds(0, 0, mCaterpillarWidth, drawableBound.second - drawableBound.first)

            val saveCount = canvas.save()
            try {
                canvas.translate(mCaterpillarLeftMargin.toFloat(), drawableBound.first.toFloat())
                caterpillarDrawable.draw(canvas)
            } finally {
                canvas.restoreToCount(saveCount)
            }
        }
    }

    private fun getCaterpillarBound(topView: View, bottomView: View?, positionOffset: Float): Pair<Int, Int> {
        val container: View = mContainer

        val topViewTop: Int = ViewUtil.getViewTopBaseOn(topView, container)
        val topViewBottom: Int = ViewUtil.getViewBottomBaseOn(topView, container)
        val topViewCenter = topViewTop + topViewBottom ushr 1

        val startTop: Int = topViewCenter - mCaterpillarHeight / 2
        val startBottom: Int = topViewCenter + mCaterpillarHeight / 2

        if (bottomView == null) return Pair(startTop, startBottom)

        val bottomViewTop: Int = ViewUtil.getViewTopBaseOn(bottomView, container)
        val bottomViewBottom: Int = ViewUtil.getViewBottomBaseOn(bottomView, container)
        val bottomViewCenter = bottomViewTop + bottomViewBottom ushr 1

        val endTop: Int = bottomViewCenter - mCaterpillarHeight / 2
        val endBottom: Int = bottomViewCenter + mCaterpillarHeight / 2

        val drawableTop: Int
        val drawableBottom: Int

        drawableTop = if (positionOffset < .5f) {
            startTop
        } else {
            (startTop + (endTop - startTop) * ((positionOffset - .5f) * 2)).toInt()
        }

        drawableBottom = if (positionOffset > .5f) {
            endBottom
        } else {
            (startBottom + (endBottom - startBottom) * (positionOffset * 2)).toInt()
        }

        return Pair(drawableTop, drawableBottom)
    }
}