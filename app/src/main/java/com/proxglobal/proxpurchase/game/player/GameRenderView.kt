package com.proxglobal.proxpurchase.game.player

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.proxglobal.proxpurchase.boundingBox
import com.proxglobal.purchase.util.logd
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class GameRenderView(context: Context, attrs: AttributeSet? = null) : View(context, attrs), CoroutineScope {
    lateinit var listRender: List<Render>

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        human?.let {
            if (hasShitIn(it.boundingBox)) {
                listRender.forEach { it.stopRender() }
            }
        }
        listRender.forEach {
            it.draw(canvas)
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private var xGesture = 0f
    private var yGesture = 0f
    private var positions: List<Float>
    private var humanPosition: Int

    init {
        val w = Resources.getSystem().displayMetrics.widthPixels.toFloat()
        positions = listOf(
            w / 8f,
            3 * w / 8f,
            5 * w / 8f,
            7 * w / 8f
        )
        humanPosition = 2
    }

    init {
        launch {
            initRender()
            while (isActive) {
                delay(16)
                postInvalidate()
            }
        }
    }

    var isPlaying = false
    var human: View? = null

    fun startGame() {
        isPlaying = true
        listRender.forEach { it.startRender() }
    }

    private fun initRender() {
        listRender = positions.map { Render(it) }
    }

    fun hasShitIn(rectF: RectF): Boolean {
        return listRender.find { it.hasShitIn(rectF) } != null
    }


}