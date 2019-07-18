package com.quarto

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.renderscript.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

open class ElevationImageView : AppCompatImageView {

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {

        val a = context.obtainStyledAttributes(attrs, R.styleable.ElevationImageView)

        val elevation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            customElevation.toInt()
        } else {
            0
        }

        customElevation = a.getDimensionPixelSize(R.styleable.ElevationImageView_compatEvelation, elevation).toFloat()

        drawAngle = a.getInteger(R.styleable.ElevationImageView_drawAngle, 0)

        clipShadow = a.getBoolean(R.styleable.ElevationImageView_clipShadow, false)

        isTranslucent = a.getBoolean(R.styleable.ElevationImageView_isTranslucent, false)

        forceClip = a.getBoolean(R.styleable.ElevationImageView_forceClip, false)

        a.recycle()
    }

    private var clipShadow = false

    private var shadowBitmap: Bitmap? = null

    private var customElevation = 0f

    private var drawAngle = 0

    private var rect = Rect()

    private var forceClip = false
        set(value) {
            field = value
            invalidate()
        }

    var isTranslucent = false
        set(value) {
            field = value
            invalidate()
        }

    private lateinit var rs: RenderScript
    private lateinit var blurScript: ScriptIntrinsicBlur
    private lateinit var colorMatrixScript: ScriptIntrinsicColorMatrix

    override fun setElevation(elevation: Float) {
        customElevation = elevation
        invalidate()
    }

    fun setElevationDp(elevation: Float) {
        customElevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, elevation, resources.displayMetrics)
        invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onDraw(canvas: Canvas?) {
        try {
            if (canvas != null) {
                if (shadowBitmap == null && customElevation > 0) {
                    generateShadow()
                }
                drawable?.let { drawable ->
                    val bounds = drawable.copyBounds()
                    shadowBitmap?.let {
                        canvas.save()

                        if (!clipShadow) {
                            canvas.getClipBounds(rect)
                            rect.inset(-2 * getBlurRadius().toInt(), -2 * getBlurRadius().toInt())
                            if (forceClip) {
                                canvas.clipRect(rect)
                            } else {
                                canvas.save()
                                canvas.clipRect(rect)
                            }
                            canvas.drawBitmap(
                                it,
                                bounds.left.toFloat() - getBlurRadius(),
                                bounds.top - getBlurRadius() / 2f,
                                null
                            )
                        }

                        canvas.restore()
                    }
                }
            }

            super.onDraw(canvas)

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun invalidate() {
        shadowBitmap = null
        super.invalidate()
    }

    override fun onDetachedFromWindow() {
        try {
            if (!isInEditMode) {
                blurScript.destroy()
                colorMatrixScript.destroy()
                rs.destroy()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDetachedFromWindow()
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onAttachedToWindow() {
        if (!isInEditMode) {
            try {
                if (forceClip)
                    (parent as ViewGroup?)?.clipChildren = false
                rs = RenderScript.create(context)
                val element = Element.U8_4(rs)
                blurScript = ScriptIntrinsicBlur.create(rs, element)
                colorMatrixScript = ScriptIntrinsicColorMatrix.create(rs, element)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        super.onAttachedToWindow()
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun generateShadow() {
        drawable?.let {
            try {
                shadowBitmap = getShadowBitmap(getBitmapFromDrawable())
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun getBitmapFromDrawable(): Bitmap {
        try {
            val drawable = drawable

            val blurRadius = getBlurRadius()

            val width = width + 2 * blurRadius.toInt()
            val height = height + 2 * blurRadius.toInt()

            val bitmap = if (width <= 0 || height <= 0) {
                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            } else {
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            }

            val canvas = Canvas(bitmap)

            val imageMatrix = imageMatrix

            if (drawAngle == 0)
                canvas.translate(paddingLeft + blurRadius, paddingTop.toFloat() + (blurRadius/2/**3/4*/))
            else if (drawAngle == 180)
                canvas.translate(paddingLeft + blurRadius, paddingTop.toFloat() /*+ blurRadius*/)

            if (imageMatrix != null) {
                canvas.concat(imageMatrix)
            }
            drawable.draw(canvas)

            return bitmap

        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return Bitmap.createBitmap(0, 0, Bitmap.Config.ARGB_8888)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun getShadowBitmap(bitmap: Bitmap): Bitmap {
        try {
            val allocationIn = Allocation.createFromBitmap(rs, bitmap)
            val allocationOut = Allocation.createTyped(rs, allocationIn.type)

            val matrix = if (isTranslucent) {
                Matrix4f(
                    floatArrayOf(
                        .2f, 0f, 0f, 0f,
                        0f, .2f, 0f, 0f,
                        0f, 0f, .2f, 0f,
                        0f, 0f, 0f, .45f
                    )
                )
            } else {
                val a = .15f
                Matrix4f(
                        floatArrayOf(
                                a, a, a, a,
                                a, a, a, a,
                                a, a, a, a,
                                a, a, a, .9f
                        )
                )
            }

            colorMatrixScript.setColorMatrix(matrix)
            colorMatrixScript.forEach(allocationIn, allocationOut)

            blurScript.setRadius(getBlurRadius())

            blurScript.setInput(allocationOut)
            blurScript.forEach(allocationIn)

            allocationIn.copyTo(bitmap)

            allocationIn.destroy()
            allocationOut.destroy()

        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return bitmap
    }

    private fun getBlurRadius(): Float {
        val maxElevation = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics)
        return min(25f * (customElevation / maxElevation), 25f)
    }

}