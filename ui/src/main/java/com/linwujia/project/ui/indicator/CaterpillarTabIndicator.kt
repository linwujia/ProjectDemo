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

class CaterpillarTabIndicator(context: Context, attrs: AttributeSet) : TabPageIndicator(context, attrs) {
    private var mCaterpillarDrawable: Drawable?
    private val mCaterpillarHeight: Int
    private val mCaterpillarBottomMargin: Int
    private val mCaterpillarWidth = resources.getDimensionPixelOffset(R.dimen.dp_size_17dp)

    private var mCurrentPosition = 0
    private var mCurrentPositionOffset = 0f
    private val mDirty = Rect(0, 0, 0, 0)

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.CaterpillarTabIndicator)
        mCaterpillarDrawable = ta.getDrawable(R.styleable.CaterpillarTabIndicator_caterpillar_drawable) ?: ContextCompat.getDrawable(context, R.drawable.circle_b1_shape)
        mCaterpillarHeight = ta.getDimensionPixelSize(
            R.styleable.CaterpillarTabIndicator_caterpillar_height,
            resources.getDimensionPixelOffset(R.dimen.dp_size_2dp)
        )

        mCaterpillarBottomMargin = ta.getDimensionPixelSize(
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
        val dirtyTop: Int = mContainer.height - mCaterpillarBottomMargin - mCaterpillarHeight
        mDirty.set(0, dirtyTop, width, dirtyTop + mCaterpillarHeight)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCaterpillar(canvas)
    }

    private fun drawCaterpillar(canvas: Canvas) {
        val caterpillarDrawable = mCaterpillarDrawable
        caterpillarDrawable?.run {
            val leftViewHolder = getViewHolderAtPosition(mCurrentPosition)
            val rightViewHolder = getViewHolderAtPosition(mCurrentPosition + 1)

            if (leftViewHolder == null) return@run

            var leftViewToMeasure = if (leftViewHolder is CaterpillarViewHolder) leftViewHolder.caterpillarRelyOn else leftViewHolder.itemView
            var rightViewToMeasure = if (rightViewHolder is CaterpillarViewHolder) rightViewHolder.caterpillarRelyOn else rightViewHolder?.itemView

            val drawableBound = getCaterpillarBound(leftViewToMeasure, rightViewToMeasure, mCurrentPositionOffset)
            caterpillarDrawable.setBounds(0, 0, drawableBound.second - drawableBound.first, mCaterpillarHeight)

            val saveCount = canvas.save()
            try {
                canvas.translate(drawableBound.first.toFloat(), (mContainer.height - mCaterpillarBottomMargin - mCaterpillarHeight).toFloat())
                caterpillarDrawable.draw(canvas)
            } finally {
                canvas.restoreToCount(saveCount)
            }
        }
    }

    private fun getCaterpillarBound(leftView: View, rightView: View?, positionOffset: Float): Pair<Int, Int> {
        val container: View = mContainer

        val leftViewLeft: Int = ViewUtil.getViewLeftBaseOn(leftView, container)
        val leftViewRight: Int = ViewUtil.getViewRightBaseOn(leftView, container)
        val leftViewCenter = leftViewLeft + leftViewRight ushr 1

        val startLeft: Int = leftViewCenter - mCaterpillarWidth / 2
        val startRight: Int = leftViewCenter + mCaterpillarWidth / 2

        if (rightView == null) return Pair(startLeft, startRight)

        val rightViewLeft: Int = ViewUtil.getViewLeftBaseOn(rightView, container)
        val rightViewRight: Int = ViewUtil.getViewRightBaseOn(rightView, container)
        val rightViewCenter = rightViewLeft + rightViewRight ushr 1

        val endLeft: Int = rightViewCenter - mCaterpillarWidth / 2
        val endRight: Int = rightViewCenter + mCaterpillarWidth / 2

        val drawableLeft: Int
        val drawableRight: Int

        drawableLeft = if (positionOffset < .5f) {
            startLeft
        } else {
            (startLeft + (endLeft - startLeft) * ((positionOffset - .5f) * 2)).toInt()
        }

        drawableRight = if (positionOffset > .5f) {
            endRight
        } else {
            (startRight + (endRight - startRight) * (positionOffset * 2)).toInt()
        }

        return Pair(drawableLeft, drawableRight)
    }

    abstract class CaterpillarViewHolder(itemView: View) : PageIndicator.ViewHolder(itemView) {
        abstract val caterpillarRelyOn: View
    }
}