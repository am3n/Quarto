package com.quarto

import android.annotation.SuppressLint
import android.content.Context
import android.view.animation.BounceInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import com.quarto.QSize.*
import com.quarto.QShape.*
import com.quarto.QInside.*
import com.quarto.QColor.*

class Quarto(context: Context,
             layoutParams: RelativeLayout.LayoutParams,
             val id: Int,
             val qSize: QSize,
             val qShape: QShape,
             val qInside: QInside,
             val qColor: QColor,
             imgDrw: Int) {

    var view: ElevationImageView = ElevationImageView(context)
    var inTable: Boolean = false

    init {
        view.layoutParams = layoutParams
        view.setImageResource(imgDrw)
        view.scaleType = ImageView.ScaleType.FIT_CENTER
        view.bringToFront()
    }

    val location: Location get() { return Location.find(view, false) }

    fun pick(flag: Boolean) {

        view.setElevationDp(if (flag) 12f else 0f)
        if (flag)
            view.setPadding(6, 12, 6, 12)
        else
            view.setPadding(6, 6, 6, 6)

        if (flag) {
            var scale = .35f
            if (qSize==LARGE && qShape==SQUARE) scale = .3f
            view.animate()
                    .scaleXBy(scale)
                    .scaleYBy(scale)
                    .setInterpolator(BounceInterpolator())
                    .setDuration(300)
                    .start()
        }
    }


    fun quarto(quarto: Boolean) {
        if (quarto)
            view.alpha = 1f
        else
            view.alpha = .3f
    }

    fun hide(hide: Boolean) {
        if (hide)
            view.alpha = .5f
        else
            view.alpha = 1f
    }

    fun moveTo(location: Location?, fraction: Int = 0) {
        location?.coordX?.let { cX ->
            location.coordY?.let { cY ->
                view.animate()?.x(cX + fraction)?.y(cY + fraction)?.setDuration(0)?.start()
            }
        }
    }

    fun animateTo(location: Location?, dX: Float = 0f, dY: Float = 0f) {
        location?.coordX?.let { cX ->
            location.coordY?.let { cY ->
                view.animate()?.x(cX + dX)?.y(cY + dY)?.setDuration(250)?.start()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun listenTo(listener: QuartoTouchListener) {
        view.setOnTouchListener(listener)
    }

}