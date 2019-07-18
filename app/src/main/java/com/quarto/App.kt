package com.quarto

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Point
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

class App : Application() {

    companion object {

        fun getScreenSize(context: Context?): Point {
            val wm = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
            val display = wm?.defaultDisplay
            val size = Point()
            display?.getSize(size)
            return size
        }

        fun hideKeyboard(context: Context?, view: View?) {
            try {
                val imm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
                imm?.hideSoftInputFromWindow(view?.windowToken, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun hideNavAndStus(window: Window?) {
            val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_IMMERSIVE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            window?.decorView?.systemUiVisibility = flags

            val decorView = window?.decorView
            decorView?.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    decorView.systemUiVisibility = flags
                }
            }
        }

    }

    val statusBarHeight: Int get() {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    val actionBarHeight: Int get() {
        val result : Int
        val styledAttributes = baseContext.theme.obtainStyledAttributes(
                intArrayOf(android.R.attr.actionBarSize)
        )
        result = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()
        return result
    }

    val navigaionBarHeight: Int get() {
        var navigationBarHeight = 0
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return navigationBarHeight
    }


    override fun onCreate() {
        super.onCreate()
    }

}
