package com.edwin.customer.colorpicker

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.edwin.customer.R
import kotlin.math.roundToInt

class OpacityBar : View {
    private var mBarThickness = 0
    private var mBarLength = 0
    private var mPreferredBarLength = 0
    private var mBarPointerRadius = 0
    private var mBarPointerHaloRadius = 0
    private var mBarPointerPosition = 0

    private var mBarPaint: Paint? = null

    private var mBarPointerPaint: Paint? = null

    private var mBarPointerHaloPaint: Paint? = null

    private val mBarRect = RectF()

    private var shader: Shader? = null

    private var mIsMovingPointer = false
    private var mColor = 0

    private val mHSVColor = FloatArray(3)

    private var mPosToOpacFactor = 0f

    private var mOpacToPosFactor = 0f

    var onOpacityChangedListener: OnOpacityChangedListener? = null

    private var oldChangedListenerOpacity = 0

    private var mPicker: ColorPicker? = null

    private var mOrientation = false

    constructor(context: Context?) : super(context) {
        init(null, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.ColorBars, defStyle, 0
        )
        val b = context.resources
        mBarThickness = a.getDimensionPixelSize(
            R.styleable.ColorBars_bar_thickness,
            b.getDimensionPixelSize(R.dimen.bar_thickness)
        )
        mBarLength = a.getDimensionPixelSize(
            R.styleable.ColorBars_bar_length,
            b.getDimensionPixelSize(R.dimen.bar_length)
        )
        mPreferredBarLength = mBarLength
        mBarPointerRadius = a.getDimensionPixelSize(
            R.styleable.ColorBars_bar_pointer_radius,
            b.getDimensionPixelSize(R.dimen.bar_pointer_radius)
        )
        mBarPointerHaloRadius = a.getDimensionPixelSize(
            R.styleable.ColorBars_bar_pointer_halo_radius,
            b.getDimensionPixelSize(R.dimen.bar_pointer_halo_radius)
        )
        mOrientation = a.getBoolean(
            R.styleable.ColorBars_bar_orientation_horizontal, ORIENTATION_DEFAULT
        )
        a.recycle()
        mBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBarPaint!!.shader = shader
        mBarPointerPosition = mBarLength + mBarPointerHaloRadius
        mBarPointerHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBarPointerHaloPaint!!.color = Color.BLACK
        mBarPointerHaloPaint!!.alpha = 0x50
        mBarPointerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mBarPointerPaint!!.color = -0x7e0100
        mPosToOpacFactor = 0xFF / mBarLength.toFloat()
        mOpacToPosFactor = mBarLength.toFloat() / 0xFF
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val intrinsicSize = (mPreferredBarLength
                + mBarPointerHaloRadius * 2)

        val measureSpec = if (mOrientation == ORIENTATION_HORIZONTAL) {
            widthMeasureSpec
        } else {
            heightMeasureSpec
        }
        val lengthMode = MeasureSpec.getMode(measureSpec)
        val lengthSize = MeasureSpec.getSize(measureSpec)
        val length = when (lengthMode) {
            MeasureSpec.EXACTLY -> {
                lengthSize
            }
            MeasureSpec.AT_MOST -> {
                intrinsicSize.coerceAtMost(lengthSize)
            }
            else -> {
                intrinsicSize
            }
        }
        val barPointerHaloRadiusx2 = mBarPointerHaloRadius * 2
        mBarLength = length - barPointerHaloRadiusx2
        if (mOrientation == ORIENTATION_VERTICAL) {
            setMeasuredDimension(
                barPointerHaloRadiusx2,
                mBarLength + barPointerHaloRadiusx2
            )
        } else {
            setMeasuredDimension(
                mBarLength + barPointerHaloRadiusx2,
                barPointerHaloRadiusx2
            )
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val x1: Int
        val y1: Int
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            x1 = mBarLength + mBarPointerHaloRadius
            y1 = mBarThickness
            mBarLength = w - mBarPointerHaloRadius * 2
            mBarRect[mBarPointerHaloRadius.toFloat(), (mBarPointerHaloRadius - (mBarThickness / 2)).toFloat(), (mBarLength + (mBarPointerHaloRadius)).toFloat()] =
                (mBarPointerHaloRadius + (mBarThickness / 2)).toFloat()
        } else {
            x1 = mBarThickness
            y1 = mBarLength + mBarPointerHaloRadius
            mBarLength = h - mBarPointerHaloRadius * 2
            mBarRect[(mBarPointerHaloRadius - (mBarThickness / 2)).toFloat(), mBarPointerHaloRadius.toFloat(), (mBarPointerHaloRadius + (mBarThickness / 2)).toFloat()] =
                (mBarLength + (mBarPointerHaloRadius)).toFloat()
        }

        if (!isInEditMode) {
            shader = LinearGradient(
                mBarPointerHaloRadius.toFloat(), 0f,
                x1.toFloat(), y1.toFloat(), intArrayOf(
                    Color.HSVToColor(0x00, mHSVColor),
                    Color.HSVToColor(0xFF, mHSVColor)
                ), null,
                Shader.TileMode.CLAMP
            )
        } else {
            shader = LinearGradient(
                mBarPointerHaloRadius.toFloat(), 0f,
                x1.toFloat(), y1.toFloat(), intArrayOf(
                    0x0081ff00, -0x7e0100
                ), null, Shader.TileMode.CLAMP
            )
            Color.colorToHSV(-0x7e0100, mHSVColor)
        }
        mBarPaint!!.shader = shader
        mPosToOpacFactor = 0xFF / mBarLength.toFloat()
        mOpacToPosFactor = mBarLength.toFloat() / 0xFF
        val hsvColor = FloatArray(3)
        Color.colorToHSV(mColor, hsvColor)
        mBarPointerPosition = if (!isInEditMode) {
            (mOpacToPosFactor * Color.alpha(mColor)
                    + mBarPointerHaloRadius).roundToInt()
        } else {
            mBarLength + mBarPointerHaloRadius
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(mBarRect, (mBarPaint)!!)

        val cX: Int
        val cY: Int
        if (mOrientation == ORIENTATION_HORIZONTAL) {
            cX = mBarPointerPosition
            cY = mBarPointerHaloRadius
        } else {
            cX = mBarPointerHaloRadius
            cY = mBarPointerPosition
        }

        // Draw the pointer halo.
        canvas.drawCircle(
            cX.toFloat(), cY.toFloat(), mBarPointerHaloRadius.toFloat(),
            (mBarPointerHaloPaint)!!
        )
        // Draw the pointer.
        canvas.drawCircle(
            cX.toFloat(), cY.toFloat(), mBarPointerRadius.toFloat(),
            (mBarPointerPaint)!!
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)

        val dimen = if (mOrientation == ORIENTATION_HORIZONTAL) {
            event.x
        } else {
            event.y
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mIsMovingPointer = true
                // Check whether the user pressed on (or near) the pointer
                if ((dimen >= (mBarPointerHaloRadius)
                            && dimen <= (mBarPointerHaloRadius + mBarLength))
                ) {
                    mBarPointerPosition = dimen.roundToInt()
                    calculateColor(dimen.roundToInt())
                    mBarPointerPaint!!.color = mColor
                    invalidate()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mIsMovingPointer) {
                    // Move the the pointer on the bar.
                    when {
                        dimen >= mBarPointerHaloRadius
                                && dimen <= (mBarPointerHaloRadius + mBarLength) -> {
                            mBarPointerPosition = dimen.roundToInt()
                            calculateColor(dimen.roundToInt())
                            mBarPointerPaint!!.color = mColor
                            mPicker?.apply {
                                setNewCenterColor(mColor)
                            }
                            invalidate()
                        }
                        dimen < mBarPointerHaloRadius -> {
                            mBarPointerPosition = mBarPointerHaloRadius
                            mColor = Color.TRANSPARENT
                            mBarPointerPaint!!.color = mColor
                            mPicker?.apply {
                                setNewCenterColor(mColor)
                            }
                            invalidate()
                        }
                        dimen > (mBarPointerHaloRadius + mBarLength) -> {
                            mBarPointerPosition = mBarPointerHaloRadius + mBarLength
                            mColor = Color.HSVToColor(mHSVColor)
                            mBarPointerPaint!!.color = mColor
                            mPicker?.apply {
                                setNewCenterColor(mColor)
                            }
                            invalidate()
                        }
                    }
                }
                if (onOpacityChangedListener != null && oldChangedListenerOpacity != opacity) {
                    onOpacityChangedListener!!.onOpacityChanged(opacity)
                    oldChangedListenerOpacity = opacity
                }
            }
            MotionEvent.ACTION_UP -> mIsMovingPointer = false
        }
        return true
    }

    var opacity: Int
        get() {
            val opacity =
                (mPosToOpacFactor * (mBarPointerPosition - mBarPointerHaloRadius)).roundToInt()
            return when {
                opacity < 5 -> {
                    0x00
                }
                opacity > 250 -> {
                    0xFF
                }
                else -> {
                    opacity
                }
            }
        }
        set(opacity) {
            mBarPointerPosition = ((mOpacToPosFactor * opacity).roundToInt()
                    + mBarPointerHaloRadius)
            calculateColor(mBarPointerPosition)
            mBarPointerPaint!!.color = mColor
            mPicker?.apply {
                setNewCenterColor(mColor)
            }
            invalidate()
        }

    private fun calculateColor(coord: Int) {
        var coord = coord
        coord -= mBarPointerHaloRadius
        if (coord < 0) {
            coord = 0
        } else if (coord > mBarLength) {
            coord = mBarLength
        }
        mColor = Color.HSVToColor(
            (mPosToOpacFactor * coord).roundToInt(),
            mHSVColor
        )
        if (Color.alpha(mColor) > 250) {
            mColor = Color.HSVToColor(mHSVColor)
        } else if (Color.alpha(mColor) < 5) {
            mColor = Color.TRANSPARENT
        }
    }

    var color: Int
        get() = mColor
        set(color) {
            val x1: Int
            val y1: Int
            if (mOrientation == ORIENTATION_HORIZONTAL) {
                x1 = (mBarLength + mBarPointerHaloRadius)
                y1 = mBarThickness
            } else {
                x1 = mBarThickness
                y1 = (mBarLength + mBarPointerHaloRadius)
            }
            Color.colorToHSV(color, mHSVColor)
            shader = LinearGradient(
                mBarPointerHaloRadius.toFloat(), 0f,
                x1.toFloat(), y1.toFloat(), intArrayOf(
                    Color.HSVToColor(0x00, mHSVColor), color
                ), null,
                Shader.TileMode.CLAMP
            )
            mBarPaint!!.shader = shader
            calculateColor(mBarPointerPosition)
            mBarPointerPaint!!.color = mColor
            mPicker?.apply {
                setNewCenterColor(mColor)
            }
            invalidate()
        }

    fun setColorPicker(picker: ColorPicker?) {
        mPicker = picker
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val state = Bundle()
        state.putParcelable(STATE_PARENT, superState)
        state.putFloatArray(STATE_COLOR, mHSVColor)
        state.putInt(STATE_OPACITY, opacity)
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as Bundle
        val superState = savedState.getParcelable<Parcelable>(STATE_PARENT)
        super.onRestoreInstanceState(superState)
        color =
            Color.HSVToColor(savedState.getFloatArray(STATE_COLOR))
        opacity = savedState.getInt(STATE_OPACITY)
    }

    companion object {
        private const val STATE_PARENT = "parent"
        private const val STATE_COLOR = "color"
        private const val STATE_OPACITY = "opacity"
        private const val STATE_ORIENTATION = "orientation"
        private const val ORIENTATION_HORIZONTAL = true
        private const val ORIENTATION_VERTICAL = false
        private const val ORIENTATION_DEFAULT = ORIENTATION_HORIZONTAL
    }
}