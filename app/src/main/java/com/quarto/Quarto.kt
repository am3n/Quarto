package com.quarto

import android.annotation.SuppressLint
import android.content.Context
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import com.quarto.QShape.CIRCLE
import com.quarto.QShape.SQUARE
import com.quarto.QSize.LARGE

data class Quarto(
        @Transient val context: Context?,
        @Transient val layoutParams: RelativeLayout.LayoutParams,
        var id: Int,
        val size: QSize,
        val shape: QShape,
        val inside: QInside,
        val color: QColor,
        @Transient val imgDrw: Int
) {

    var inTable: Boolean = false

    @Transient var view: ElevationImageView? = context?.let { ElevationImageView(it) }

    init {
        view?.layoutParams = layoutParams
        view?.setImageResource(imgDrw)
        view?.scaleType = ImageView.ScaleType.FIT_CENTER
        view?.bringToFront()
    }

    val location: Location get() { return Location.find(view, false) }

    fun pick(flag: Boolean) {

        view?.setElevationDp(if (flag) 4f else 0f)
        if (flag)
            view?.setPadding(2, 4, 2, 4)
        else
            view?.setPadding(2, 2, 2, 2)

        if (flag) {
            var scale = .3f
            if (size==LARGE && shape==CIRCLE) scale += .2f
            if (size==LARGE && shape==SQUARE) scale += .03f
            view?.animate()
                    ?.scaleXBy(scale)
                    ?.scaleYBy(scale)
                    ?.setInterpolator(BounceInterpolator())
                    ?.setDuration(300)
                    ?.start()
        } else {
            var scale = -.3f
            if (size==LARGE && shape==CIRCLE) scale += -.05f
            if (size==LARGE && shape==SQUARE) scale += -.05f
            view?.animate()
                    ?.scaleXBy(scale)
                    ?.scaleYBy(scale)
                    ?.setDuration(200)
                    ?.start()
        }
    }


    fun quarto(quarto: Boolean) {
        if (quarto)
            view?.alpha = 1f
        else
            view?.alpha = .4f
    }

    fun hide(hide: Boolean) {
        if (hide)
            view?.alpha = .2f
        else
            view?.alpha = 1f
    }

    fun enable(flag: Boolean) {
        view?.isEnabled = flag
    }

    fun moveTo(location: Location?, fraction: Int = 0) {
        location?.coordX?.let { cX ->
            location.coordY?.let { cY ->
                view?.animate()?.x(cX + fraction)?.y(cY + fraction)?.setDuration(0)?.start()
            }
        }
    }

    fun animateTo(location: Location?, dX: Float = 0f, dY: Float = 0f) {
        location?.coordX?.let { cX ->
            location.coordY?.let { cY ->
                view?.animate()?.x(cX + dX)?.y(cY + dY)?.setDuration(250)?.start()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun listenTo(listener: QuartoTouchListener) {
        view?.setOnTouchListener(listener)
    }

}