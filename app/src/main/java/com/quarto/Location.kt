package com.quarto

import android.view.View
import android.widget.RelativeLayout
import kotlin.math.pow
import kotlin.math.sqrt

open class Location(private val view: View?, private val rotated: Boolean) {

    var coordX: Float? = 0.toFloat()
    var coordY: Float? = 0.toFloat()
    var centerX: Float? = 0.toFloat()
    var centerY: Float? = 0.toFloat()
    val location: Location get() = this

    init {
        calc()
    }

    constructor(centerX: Float, centerY: Float) : this(null, false) {
        this.centerX = centerX
        this.centerY = centerY
    }

    internal fun calc() {
        val loc = IntArray(2)
        view?.getLocationOnScreen(loc)

        val top = view?.width?.div(2)
        val squareChordDivBy2 = (view?.width)?.toDouble()?.pow(2)?.times(2)?.let { sqrt(it) }?.div(2)?.toFloat()
        val right = view?.width?.div(2)?.let { squareChordDivBy2?.minus(it) }

        coordX = loc[0] -
                if (rotated && top!=null) top.toFloat()
                else 0f
        coordY = loc[1] +
                if (rotated && right!=null) right.toFloat()
                else 0f

        centerX = squareChordDivBy2?.let { coordX?.plus(it) }
        centerY = squareChordDivBy2?.let { coordY?.plus(it) }

    }

    companion object {

        fun find(view: View?, rotated: Boolean): Location {
            return Location(view, rotated)
        }

    }
}
