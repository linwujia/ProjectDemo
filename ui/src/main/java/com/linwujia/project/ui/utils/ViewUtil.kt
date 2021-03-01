package com.linwujia.project.ui.utils

import android.view.View

object ViewUtil {
    @JvmStatic
    fun getViewTopBaseOn(view: View, base: View): Int {
        if (view === base) {
            return view.top
        }

        val parent = getParent(view)
        if (parent != null) {
            return if (parent !== base) {
                view.top + getViewTopBaseOn(parent, base)
            } else {
                view.top
            }
        }
        throw IllegalArgumentException() // unexpected
    }

    @JvmStatic
    fun getViewLeftBaseOn(view: View, base: View): Int {
        if (view === base) {
            return view.left
        }

        val parent = getParent(view)
        if (parent != null) {
            if (parent !== base) {
                val left = view.left
                return left + getViewLeftBaseOn(parent, base)
            } else {
                return view.left
            }
        }
        throw IllegalArgumentException() // unexpected
    }

    @JvmStatic
    fun getViewRightBaseOn(view: View, base: View): Int {
        return getViewLeftBaseOn(view, base) + view.width
    }

    @JvmStatic
    fun getViewBottomBaseOn(view: View, base: View): Int {
        return getViewTopBaseOn(view, base) + view.height
    }

    @JvmStatic
    fun getParent(view: View): View? {
        val parent = view.parent
        return if (parent is View) {
            parent
        } else null
    }
}