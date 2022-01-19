package com.edwin.customer.surfaceview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class BubbleSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs) {

    private var maxRadius = 0f

    private val colors = arrayOf(
        Color.BLACK,
        Color.DKGRAY,
        Color.GRAY,
        Color.LTGRAY,
        Color.WHITE,
        Color.RED,
        Color.GREEN,
        Color.BLUE,
        Color.YELLOW,
        Color.CYAN,
        Color.MAGENTA,
    )

    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 5f
        // 离散 实现波浪
        pathEffect = ComposePathEffect(
            CornerPathEffect(30f),
            DiscretePathEffect(30f, 20f)
        )
    }
    private val bubblesList = mutableListOf<Bubble>()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        maxRadius = sqrt((w * w + h * h).toFloat())
        Log.i("11111", "maxRadius: $maxRadius")
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x = event?.x ?: 0f
        val y = event?.y ?: 0f
        val color = colors.random()
        val bubble = Bubble(x, y, color, 1f)
        bubblesList.add(bubble)
        if (bubblesList.size > 30) {
            bubblesList.removeFirst()
        }

        return super.onTouchEvent(event)
    }

    init {
        // 使用协程
        CoroutineScope(Dispatchers.Default).launch {
            Log.i("11111", "CoroutineScope: $maxRadius")
            while (true) {
                if (holder.surface.isValid) {
                    val lockCanvas = holder.lockCanvas()

                    // 重置画图背景色
                    lockCanvas.drawColor(Color.BLACK)
                    bubblesList.toList()
                        .filter { it.radius < maxRadius }
                        .forEach {
                            paint.color = it.color
                            lockCanvas.drawCircle(it.x, it.y, it.radius, paint)
                            it.radius += 10f
                        }
                    holder.unlockCanvasAndPost(lockCanvas)
                }
            }
        }
    }
}

data class Bubble(
    val x: Float,
    val y: Float,
    val color: Int,
    var radius: Float
)