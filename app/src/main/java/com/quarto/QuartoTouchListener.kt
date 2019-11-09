package com.quarto

import android.annotation.SuppressLint
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

class QuartoTouchListener(private val qid: Int, private val quartoListener: QuartoListener) : View.OnTouchListener {

    // Coordinates
    private var widgetXFirst: Float = 0F
    private var widgetDX: Float = 0F
    private var widgetYFirst: Float = 0F
    private var widgetDY: Float = 0F
    private var widgetLastAction: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, event: MotionEvent): Boolean {

        if (view.isEnabled) {

            when (event.action and MotionEvent.ACTION_MASK) {

                MotionEvent.ACTION_DOWN -> {

                    view.bringToFront()
                    if (quartoListener.onDown(qid)) {
                        this.widgetDX = view.x - event.rawX
                        this.widgetDY = view.y - event.rawY
                        this.widgetXFirst = view.x
                        this.widgetYFirst = view.y
                        this.widgetLastAction = MotionEvent.ACTION_DOWN
                        //view.animate().scaleXBy(.2f).scaleYBy(.2f).setDuration(200).start()
                    }
                }

                MotionEvent.ACTION_MOVE -> {

                    if (quartoListener.onMove(qid)) {

                        val viewParent: View = (view.parent as View)
                        val parentHeight = viewParent.height
                        val parentWidth = viewParent.width

                        // Screen border Collision
                        var newX = event.rawX + this.widgetDX
                        newX = max(0F, newX)
                        newX = min((parentWidth - view.width).toFloat(), newX)
                        view.x = newX

                        var newY = event.rawY + this.widgetDY
                        newY = max(0F, newY)
                        newY = min((parentHeight - view.height).toFloat(), newY)
                        view.y = newY

                        this.widgetLastAction = MotionEvent.ACTION_MOVE

                        quartoListener.onMoved(parentWidth, parentHeight, newX, newY)
                    }
                }

                MotionEvent.ACTION_UP -> {

                    if (quartoListener.onDrop(qid)) {

                    }
                }

                else -> return false

            }

        }

        return true
    }
}