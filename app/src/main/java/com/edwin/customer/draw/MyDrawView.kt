package com.edwin.customer.draw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.withRotation
import androidx.core.graphics.withTranslation
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.edwin.customer.R
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
    onFinishInflate....
    onAttachedToWindow....
    onMeasure....
    onMeasure....
    onSizeChanged....
    onLayout....
    onDraw....
 */
class MyDrawView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), LifecycleObserver {

    private var mAngle = 10f // 初始化角
    private var mRadius = 0f // 半径
    private var mWidth = 0f
    private var mHeight = 0f

    private var rotatingJob: Job? = null
    private var sineWaveSamplePath = Path()

    //  配置画笔
    private val solidLinePaint = Paint().apply {
        style = Paint.Style.STROKE // 填充效果
        strokeWidth = 5f   // 线条宽度
        color = ContextCompat.getColor(context, R.color.colorWhite)
    }
    private val textPaint = Paint().apply {
        textSize = 40f
        typeface = Typeface.DEFAULT_BOLD
        color = ContextCompat.getColor(context, R.color.colorWhite)
    }
    private val vectorLinePaint = Paint().apply {
        style = Paint.Style.STROKE // 填充效果
        strokeWidth = 5f   // 线条宽度
        color = ContextCompat.getColor(context, R.color.colorAccent)
    }

    // 虚线画笔
    private val dashedLinePaint = Paint().apply {
        style = Paint.Style.STROKE // 填充效果
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f) // 虚线设置
        color = ContextCompat.getColor(context, R.color.colorYellow)
        strokeWidth = 5f   // 线条宽度
    }

    //
    private val fillCirclePaint = Paint().apply {
        style = Paint.Style.FILL // 填充效果
        strokeWidth = 5f   // 线条宽度
        color = ContextCompat.getColor(context, R.color.colorWhite)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mWidth = w.toFloat()
        mHeight = h.toFloat()
        mRadius = if (w < h / 2) w / 2.toFloat() else h / 4.toFloat()
        mRadius -= 20f

        Log.d("Hello", "onSizeChanged....")
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawAxises(this)
            drawLabel(this)
            drawDashedCircle(this)
            drawVector(this)
            drawProjections(this)
            drawSineWave(this)
        }
    }

    /**
     * canvas.save()
     * canvas.translate()
     * canvas.drawLine()
     * canvas.restore()
     */
    private fun drawAxises(canvas: Canvas) {
        canvas.withTranslation(mWidth / 2, mHeight / 2) {
            drawLine(-mWidth / 2, 0f, mWidth / 2, 0f, solidLinePaint)
            drawLine(0f, -mHeight / 2, 0f, mHeight / 2, solidLinePaint)
        }

        canvas.withTranslation(mWidth / 2, mHeight / 4 * 3) {
            drawLine(-mWidth / 2, 0f, mWidth / 2, 0f, solidLinePaint)
        }
    }

    /**
     * 画文字
     */
    private fun drawLabel(canvas: Canvas) {
        canvas.withTranslation(mWidth / 4, mHeight / 16) {
            drawRect(-mWidth / 16 * 3, -mHeight / 32, mWidth / 16 * 3, mHeight / 32, solidLinePaint)
            drawText("指数函数与旋转矢量", -mWidth / 256 * 43, mHeight / 256, textPaint)
        }
    }

    private fun drawDashedCircle(canvas: Canvas) {
        canvas.withTranslation(mWidth / 2, mHeight / 4 * 3) {
            drawCircle(0f, 0f, mRadius, dashedLinePaint)
        }
    }

    private fun drawVector(canvas: Canvas) {
        canvas.withTranslation(mWidth / 2, mHeight / 4 * 3) {
            // 角度旋转
            withRotation(-mAngle) {
                drawLine(0f, 0f, mRadius, 0f, vectorLinePaint)
            }
        }
    }

    // 画小白点
    private fun drawProjections(canvas: Canvas) {
        canvas.withTranslation(mWidth / 2, mHeight / 2) {
            drawCircle(mRadius * cos(mAngle.toRadians()), 0f, 10f, fillCirclePaint)
        }

        canvas.withTranslation(mWidth / 2, mHeight / 4 * 3) {
            drawCircle(mRadius * cos(mAngle.toRadians()), 0f, 10f, fillCirclePaint)
        }

        canvas.withTranslation(mWidth / 2, mHeight / 4 * 3) {
            val x = mRadius * cos(mAngle.toRadians())
            val y = mRadius * sin(mAngle.toRadians())

            canvas.withTranslation(x, -y) {
                canvas.drawLine(0f, 0f, 0f, y, solidLinePaint)
                canvas.drawLine(0f, 0f, 0f, -mHeight / 4 + y, dashedLinePaint)
            }
        }
    }

    private fun drawSineWave(canvas: Canvas) {
        canvas.withTranslation(mWidth / 2, mHeight / 2) {
            val samplesCount = 50
            val dy = mHeight / 2 / samplesCount
            sineWaveSamplePath.reset()
            sineWaveSamplePath.moveTo(mRadius * cos(mAngle.toRadians()), 0f)  // 路径起点
            repeat(samplesCount) {
                val x = mRadius * cos(it * -0.15 + mAngle.toRadians())
                val y = -dy * it
                sineWaveSamplePath.quadTo(x.toFloat(), y, x.toFloat(), y)
            }
            drawPath(sineWaveSamplePath, vectorLinePaint)
            drawTextOnPath("hello world", sineWaveSamplePath, 1000f, 0f, textPaint)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public fun startRotating() {
        rotatingJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(100)
                mAngle += 5f
                invalidate()
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public fun pauseRotating() {
        rotatingJob?.cancel()
    }

    // 角度转弧度
    private fun Float.toRadians() = this / 180 * PI.toFloat()

}

