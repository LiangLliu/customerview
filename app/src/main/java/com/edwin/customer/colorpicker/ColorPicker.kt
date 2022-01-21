package com.edwin.customer.colorpicker

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.edwin.customer.R
import kotlin.math.*

class ColorPicker : View {
    private var mColorWheelThickness = 0

    private var mColorWheelRadius = 0
    private var mPreferredColorWheelRadius = 0

    private var mColorCenterRadius = 0
    private var mPreferredColorCenterRadius = 0

    private var mColorCenterHaloRadius = 0
    private var mPreferredColorCenterHaloRadius = 0

    private var mColorPointerRadius = 0

    private var mColorPointerHaloRadius = 0
    private val mColorWheelRectangle = RectF()

    private val mCenterRectangle = RectF()

    private var mUserIsMovingPointer = false

    private var mColor = 0

    private var mCenterOldColor = 0
    private var mShowCenterOldColor = false

    private var mCenterNewColor = 0

    private var mTranslationOffset = 0f

    private var mSlopX = 0f
    private var mSlopY = 0f
    private var mAngle = 0f

    private var mColorWheelPaint: Paint? = null
    private var mPointerHaloPaint: Paint? = null
    private var mPointerColor: Paint? = null

    private var mCenterOldPaint: Paint? = null
    private var mCenterNewPaint: Paint? = null
    private var mCenterHaloPaint: Paint? = null

    private val mHSV = FloatArray(3)

    private var mSVbar: SVBar? = null

    private var mOpacityBar: OpacityBar? = null

    private var mSaturationBar: SaturationBar? = null

    private var mTouchAnywhereOnColorWheelEnabled = true

    private var mValueBar: ValueBar? = null
    var onColorChangedListener: OnColorChangedListener? = null

    var onColorSelectedListener: OnColorSelectedListener? = null

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

    private var oldChangedListenerColor = 0
    private var oldSelectedListenerColor = 0

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.ColorPicker, defStyle, 0
        )
        val b = context.resources
        mColorWheelThickness = a.getDimensionPixelSize(
            R.styleable.ColorPicker_color_wheel_thickness,
            b.getDimensionPixelSize(R.dimen.color_wheel_thickness)
        )
        mColorWheelRadius = a.getDimensionPixelSize(
            R.styleable.ColorPicker_color_wheel_radius,
            b.getDimensionPixelSize(R.dimen.color_wheel_radius)
        )
        mPreferredColorWheelRadius = mColorWheelRadius
        mColorCenterRadius = a.getDimensionPixelSize(
            R.styleable.ColorPicker_color_center_radius,
            b.getDimensionPixelSize(R.dimen.color_center_radius)
        )
        mPreferredColorCenterRadius = mColorCenterRadius
        mColorCenterHaloRadius = a.getDimensionPixelSize(
            R.styleable.ColorPicker_color_center_halo_radius,
            b.getDimensionPixelSize(R.dimen.color_center_halo_radius)
        )
        mPreferredColorCenterHaloRadius = mColorCenterHaloRadius
        mColorPointerRadius = a.getDimensionPixelSize(
            R.styleable.ColorPicker_color_pointer_radius,
            b.getDimensionPixelSize(R.dimen.color_pointer_radius)
        )
        mColorPointerHaloRadius = a.getDimensionPixelSize(
            R.styleable.ColorPicker_color_pointer_halo_radius,
            b.getDimensionPixelSize(R.dimen.color_pointer_halo_radius)
        )
        a.recycle()
        mAngle = (-Math.PI / 2).toFloat()
        val s: Shader = SweepGradient(0f, 0f, COLORS, null)
        mColorWheelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mColorWheelPaint!!.shader = s
        mColorWheelPaint!!.style = Paint.Style.STROKE
        mColorWheelPaint!!.strokeWidth = mColorWheelThickness.toFloat()
        mPointerHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPointerHaloPaint!!.color = Color.BLACK
        mPointerHaloPaint!!.alpha = 0x50
        mPointerColor = Paint(Paint.ANTI_ALIAS_FLAG)
        mPointerColor!!.color = calculateColor(mAngle)
        mCenterNewPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCenterNewPaint!!.color = calculateColor(mAngle)
        mCenterNewPaint!!.style = Paint.Style.FILL
        mCenterOldPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCenterOldPaint!!.color = calculateColor(mAngle)
        mCenterOldPaint!!.style = Paint.Style.FILL
        mCenterHaloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mCenterHaloPaint!!.color = Color.BLACK
        mCenterHaloPaint!!.alpha = 0x00
        mCenterNewColor = calculateColor(mAngle)
        mCenterOldColor = calculateColor(mAngle)
        mShowCenterOldColor = true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.translate(mTranslationOffset, mTranslationOffset)

        canvas.drawOval(mColorWheelRectangle, mColorWheelPaint!!)
        val pointerPosition = calculatePointerPosition(mAngle)

        canvas.drawCircle(
            pointerPosition[0], pointerPosition[1],
            mColorPointerHaloRadius.toFloat(), mPointerHaloPaint!!
        )

        canvas.drawCircle(
            pointerPosition[0], pointerPosition[1],
            mColorPointerRadius.toFloat(), mPointerColor!!
        )

        canvas.drawCircle(0f, 0f, mColorCenterHaloRadius.toFloat(), mCenterHaloPaint!!)
        if (mShowCenterOldColor) {
            canvas.drawArc(mCenterRectangle, 90f, 180f, true, mCenterOldPaint!!)

            canvas.drawArc(mCenterRectangle, 270f, 180f, true, mCenterNewPaint!!)
        } else {
            canvas.drawArc(mCenterRectangle, 0f, 360f, true, mCenterNewPaint!!)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val intrinsicSize = 2 * (mPreferredColorWheelRadius + mColorPointerHaloRadius)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> {
                widthSize
            }
            MeasureSpec.AT_MOST -> {
                intrinsicSize.coerceAtMost(widthSize)
            }
            else -> {
                intrinsicSize
            }
        }
        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> {
                heightSize
            }
            MeasureSpec.AT_MOST -> {
                intrinsicSize.coerceAtMost(heightSize)
            }
            else -> {
                intrinsicSize
            }
        }
        val min = width.coerceAtMost(height)
        setMeasuredDimension(min, min)
        mTranslationOffset = min * 0.5f

        // fill the rectangle instances.
        mColorWheelRadius = min / 2 - mColorWheelThickness - mColorPointerHaloRadius
        mColorWheelRectangle[-mColorWheelRadius.toFloat(), -mColorWheelRadius.toFloat(), mColorWheelRadius.toFloat()] =
            mColorWheelRadius.toFloat()
        mColorCenterRadius =
            (mPreferredColorCenterRadius.toFloat() * (mColorWheelRadius.toFloat() / mPreferredColorWheelRadius.toFloat())).toInt()
        mColorCenterHaloRadius =
            (mPreferredColorCenterHaloRadius.toFloat() * (mColorWheelRadius.toFloat() / mPreferredColorWheelRadius.toFloat())).toInt()
        mCenterRectangle[-mColorCenterRadius.toFloat(), -mColorCenterRadius.toFloat(), mColorCenterRadius.toFloat()] =
            mColorCenterRadius.toFloat()
    }

    private fun ave(s: Int, d: Int, p: Float): Int {
        return s + (p * (d - s)).roundToInt()
    }

    private fun calculateColor(angle: Float): Int {
        var unit = (angle / (2 * Math.PI)).toFloat()
        if (unit < 0) {
            unit += 1f
        }
        if (unit <= 0) {
            mColor = COLORS[0]
            return COLORS[0]
        }
        if (unit >= 1) {
            mColor = COLORS[COLORS.size - 1]
            return COLORS[COLORS.size - 1]
        }
        var p = unit * (COLORS.size - 1)
        val i = p.toInt()
        p -= i.toFloat()
        val c0 = COLORS[i]
        val c1 = COLORS[i + 1]
        val a = ave(Color.alpha(c0), Color.alpha(c1), p)
        val r = ave(Color.red(c0), Color.red(c1), p)
        val g = ave(Color.green(c0), Color.green(c1), p)
        val b = ave(Color.blue(c0), Color.blue(c1), p)
        mColor = Color.argb(a, r, g, b)
        return Color.argb(a, r, g, b)
    }

    var color: Int = 0
        get() = mCenterNewColor


    fun colorToAngle(color: Int): Float {
        val colors = FloatArray(3)
        Color.colorToHSV(color, colors)
        return Math.toRadians((-colors[0]).toDouble()).toFloat()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)

        // Convert coordinates to our internal coordinate system
        val x = event.x - mTranslationOffset
        val y = event.y - mTranslationOffset
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check whether the user pressed on the pointer.
                val pointerPosition = calculatePointerPosition(mAngle)
                if (x >= pointerPosition[0] - mColorPointerHaloRadius && x <= pointerPosition[0] + mColorPointerHaloRadius && y >= pointerPosition[1] - mColorPointerHaloRadius && y <= pointerPosition[1] + mColorPointerHaloRadius) {
                    mSlopX = x - pointerPosition[0]
                    mSlopY = y - pointerPosition[1]
                    mUserIsMovingPointer = true
                    invalidate()
                } else if (x >= -mColorCenterRadius && x <= mColorCenterRadius && y >= -mColorCenterRadius && y <= mColorCenterRadius && mShowCenterOldColor) {
                    mCenterHaloPaint!!.alpha = 0x50
                    color = getOldCenterColor()
                    invalidate()
                } else if (sqrt((x * x + y * y).toDouble()) <= mColorWheelRadius + mColorPointerHaloRadius && sqrt(
                        (x * x + y * y).toDouble()
                    ) >= mColorWheelRadius - mColorPointerHaloRadius && mTouchAnywhereOnColorWheelEnabled
                ) {
                    mUserIsMovingPointer = true
                    invalidate()
                } else {
                    parent.requestDisallowInterceptTouchEvent(false)
                    return false
                }
            }
            MotionEvent.ACTION_MOVE -> if (mUserIsMovingPointer) {
                mAngle = atan2((y - mSlopY).toDouble(), (x - mSlopX).toDouble()).toFloat()
                mPointerColor!!.color = calculateColor(mAngle)
                setNewCenterColor(calculateColor(mAngle).also { mCenterNewColor = it })

                mOpacityBar?.apply {
                    color = mColor
                }

                mValueBar?.apply {
                    color = mColor
                }

                mSaturationBar?.apply {
                    color = mColor
                }

                mSVbar?.apply {
                    color = mColor
                }

                invalidate()
            } else {
                parent.requestDisallowInterceptTouchEvent(false)
                return false
            }
            MotionEvent.ACTION_UP -> {
                mUserIsMovingPointer = false
                mCenterHaloPaint!!.alpha = 0x00
                if (onColorSelectedListener != null && mCenterNewColor != oldSelectedListenerColor) {
                    onColorSelectedListener!!.onColorSelected(mCenterNewColor)
                    oldSelectedListenerColor = mCenterNewColor
                }
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> if (onColorSelectedListener != null && mCenterNewColor != oldSelectedListenerColor) {
                onColorSelectedListener!!.onColorSelected(mCenterNewColor)
                oldSelectedListenerColor = mCenterNewColor
            }
        }
        return true
    }

    private fun calculatePointerPosition(angle: Float): FloatArray {
        val x = (mColorWheelRadius * cos(angle.toDouble())).toFloat()
        val y = (mColorWheelRadius * sin(angle.toDouble())).toFloat()
        return floatArrayOf(x, y)
    }

    fun addSVBar(bar: SVBar?) {
        mSVbar = bar
        mSVbar?.setColorPicker(this)
        mSVbar?.color = mColor
    }

    fun addOpacityBar(bar: OpacityBar?) {
        mOpacityBar = bar
        mOpacityBar?.setColorPicker(this)
        mOpacityBar?.color = mColor
    }

    fun addSaturationBar(bar: SaturationBar?) {
        mSaturationBar = bar
        mSaturationBar?.setColorPicker(this)
        mSaturationBar?.color = mColor
    }

    fun addValueBar(bar: ValueBar?) {
        mValueBar = bar
        mValueBar?.setColorPicker(this)
        mValueBar?.color = mColor
    }

    fun setNewCenterColor(color: Int) {
        mCenterNewColor = color
        mCenterNewPaint!!.color = color
        if (mCenterOldColor == 0) {
            mCenterOldColor = color
            mCenterOldPaint!!.color = color
        }
        if (onColorChangedListener != null && color != oldChangedListenerColor) {
            onColorChangedListener!!.onColorChanged(color)
            oldChangedListenerColor = color
        }
        invalidate()
    }

    fun setOldCenterColor(color: Int) {
        mCenterOldColor = color
        mCenterOldPaint!!.color = color
        invalidate()
    }

    fun getOldCenterColor(): Int {
        return mCenterOldColor
    }

    fun setShowOldCenterColor(show: Boolean) {
        mShowCenterOldColor = show
        invalidate()
    }

    fun getShowOldCenterColor(): Boolean {
        return mShowCenterOldColor
    }

    fun changeOpacityBarColor(color: Int) {

        mOpacityBar?.apply {
            this.color = color
        }
    }

    fun changeSaturationBarColor(color: Int) {

        mSaturationBar?.apply {
            this.color = color
        }
    }

    fun changeValueBarColor(color: Int) {
        mValueBar?.apply {
            this.color = color
        }
    }

    fun hasOpacityBar(): Boolean {
        return mOpacityBar != null
    }

    fun hasValueBar(): Boolean {
        return mValueBar != null
    }

    fun hasSaturationBar(): Boolean {
        return mSaturationBar != null
    }

    fun hasSVBar(): Boolean {
        return mSVbar != null
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val state = Bundle()
        state.putParcelable(STATE_PARENT, superState)
        state.putFloat(STATE_ANGLE, mAngle)
        state.putInt(STATE_OLD_COLOR, mCenterOldColor)
        state.putBoolean(STATE_SHOW_OLD_COLOR, mShowCenterOldColor)
        return state
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val savedState = state as Bundle
        val superState = savedState.getParcelable<Parcelable>(STATE_PARENT)
        super.onRestoreInstanceState(superState)
        mAngle = savedState.getFloat(STATE_ANGLE)
        setOldCenterColor(savedState.getInt(STATE_OLD_COLOR))
        mShowCenterOldColor = savedState.getBoolean(STATE_SHOW_OLD_COLOR)
        val currentColor = calculateColor(mAngle)
        mPointerColor!!.color = currentColor
        setNewCenterColor(currentColor)
    }

    fun setTouchAnywhereOnColorWheelEnabled(TouchAnywhereOnColorWheelEnabled: Boolean) {
        mTouchAnywhereOnColorWheelEnabled = TouchAnywhereOnColorWheelEnabled
    }

    fun getTouchAnywhereOnColorWheel(): Boolean {
        return mTouchAnywhereOnColorWheelEnabled
    }

    companion object {
        private const val STATE_PARENT = "parent"
        private const val STATE_ANGLE = "angle"
        private const val STATE_OLD_COLOR = "color"
        private const val STATE_SHOW_OLD_COLOR = "showColor"

        private val COLORS = intArrayOf(
            0xFFFF0000.toInt(),
            0xFFFF00FF.toInt(),
            0xFF0000FF.toInt(),
            0xFF00FFFF.toInt(),
            0xFF00FF00.toInt(),
            0xFFFFFF00.toInt(),
            0xFFFF0000.toInt()
        )

    }
}