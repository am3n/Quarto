package ir.officeofscore

import android.content.res.Resources

val Int.iPx2Dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.iDp2Px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.fPx2Dp: Float get() = this / Resources.getSystem().displayMetrics.density
val Int.fDp2Px: Float get() = this * Resources.getSystem().displayMetrics.density
val Float.iPx2Dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Float.iDp2Px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()
val Float.fPx2Dp: Float get() = this / Resources.getSystem().displayMetrics.density
val Float.fDp2Px: Float get() = this * Resources.getSystem().displayMetrics.density