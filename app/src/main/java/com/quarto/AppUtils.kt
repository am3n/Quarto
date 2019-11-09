package com.quarto

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.telephony.TelephonyManager
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow
import kotlin.math.sqrt

object AppUtils {

    fun getScreenSize(context: Context?): Point {
        val wm = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        val display = wm?.defaultDisplay
        val size = Point()
        display?.getSize(size)
        return size
    }

    fun hideNavAndStus(window: Window?) {
        val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                //View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                //View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window?.decorView?.systemUiVisibility = flags
        window?.decorView?.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                window.decorView.systemUiVisibility = flags
            }
        }
    }


    val statusBarHeight: Int get() {
        var result = 0
        val resourceId = Resources.getSystem().getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
                result = Resources.getSystem().getDimensionPixelSize(resourceId)
        }
        return result
    }

    fun actionBarHeight(context: Context?): Int {
        val styledAttributes = context?.theme?.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        val result = styledAttributes?.getDimension(0, 0f)?.toInt()
        styledAttributes?.recycle()
        return result?:0
    }

    val navigaionBarHeight: Int get() {
        var navigationBarHeight = 0
        val resourceId = Resources.getSystem().getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            navigationBarHeight = Resources.getSystem().getDimensionPixelSize(resourceId)
        }
        return navigationBarHeight
    }


    fun hideKeyboard(context: Context?, view: View?) {
        try {
            val imm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(view?.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun havePermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun device(context: Context?): HashMap<String, String> {
        val map = HashMap<String, String>()
        context?.let { ctx ->

            map["appVersionCode"] = BuildConfig.VERSION_CODE.toString()

            map["appVersionName"] = BuildConfig.VERSION_NAME

            map["androidApiLevel"] = Build.VERSION.SDK_INT.toString()

            map["deviceImei"] = ""
            if (havePermission(ctx, Manifest.permission.READ_PHONE_STATE)) {
                try {
                    val telephonyManager = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
                    @SuppressLint("MissingPermission", "HardwareIds")
                    map["deviceImei"] = telephonyManager?.deviceId.toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            map["deviceModel"] = Build.BRAND
            try {
                //map["deviceModel"] += " : " + DeviceName.getDeviceName()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            map["deviceScreenClass"] = "Unknown"
            when {
                ctx.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE ->
                    map["deviceScreenClass"] = "Large"
                ctx.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_NORMAL ->
                    map["deviceScreenClass"] = "Normal"
                ctx.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_SMALL ->
                    map["deviceScreenClass"] = "Small"
            }


            val metrics = ctx.resources.displayMetrics
            val density = metrics.density
            map["deviceDpiClass"] = "Unknown"
            when {
                density <= 0.75f -> map["deviceDpiClass"] = "ldpi"
                density <= 1.0f -> map["deviceDpiClass"] = "mdpi"
                density <= 1.5f -> map["deviceDpiClass"] = "hdpi"
                density <= 2.0f -> map["deviceDpiClass"] = "xhdpi"
                density <= 3.0f -> map["deviceDpiClass"] = "xxhdpi"
                density <= 4.0f -> map["deviceDpiClass"] = "xxxhdpi"
            }


            val orientation = ctx.resources.configuration.orientation
            val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
            val display = wm?.defaultDisplay
            val screenSize = Point()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display?.getRealSize(screenSize)
            } else {
                display?.getSize(screenSize)
            }
            val screensizeX = screenSize.x
            val screensizeY = screenSize.y
            val width = if (orientation == Configuration.ORIENTATION_PORTRAIT) screensizeX else screensizeY
            val height = if (orientation == Configuration.ORIENTATION_PORTRAIT) screensizeY else screensizeX
            val wi = width.toDouble() / metrics.xdpi.toDouble()
            val hi = height.toDouble() / metrics.ydpi.toDouble()
            val x = wi.pow(2.0)
            val y = hi.pow(2.0)
            val screenInches = sqrt(x + y)
            map["deviceScreenSize"] = String.format(Locale.US, "%.2f", screenInches)


            map["deviceScreenDimensionsDpis"] = (width / density).toInt().toString() + " x " + (height / density).toInt()


            map["deviceScreenDimensionsPixels"] = "$width x $height"

        }
        return map
    }

}

val Int.iPx2Dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.iDp2Px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.fPx2Dp: Float get() = this / Resources.getSystem().displayMetrics.density
val Int.fDp2Px: Float get() = this * Resources.getSystem().displayMetrics.density
val Float.iPx2Dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Float.iDp2Px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Float.fPx2Dp: Float get() = this / Resources.getSystem().displayMetrics.density
val Float.fDp2Px: Float get() = this * Resources.getSystem().displayMetrics.density

