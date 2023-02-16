package com.proxglobal.proxpurchase.game.player

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.proxglobal.proxpurchase.game.model.FallObject
import com.proxglobal.purchase.util.logdSelf
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class Render(private val xPosition: Float) : CoroutineScope {


    private val listActiveRender = java.util.ArrayDeque<FallObject>()
    private val listPoolRender = java.util.ArrayDeque<FallObject>()
    var isPlaying = false

    var paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
    }

    var addNewFallObjectJob: Job = Job()
    var changeFallObjectPositionJob: Job = Job()

    fun startRender() {
        isPlaying = true
        addNewFallObjectJob.cancel()
        changeFallObjectPositionJob.cancel()

        addNewFallObjectJob = launch {
            while (isPlaying) {
                addObjectToActiveList()
                delay((400..1000).random().toLong())
            }
        }

        changeFallObjectPositionJob = launch {
            while (isPlaying) {
                try {
                    val list = listActiveRender.toList()
                    for (index in list.indices) {
                        list[index].y += 1.5f
                    }
                    val first = listActiveRender.peek()
                    if (first != null) {
                        if (first.y >= Resources.getSystem().displayMetrics.heightPixels) {
                            val firstActive = listActiveRender.pop()
                            firstActive.y = 0f
                            listPoolRender.push(firstActive)
                        }
                    }
                    delay(2)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun draw(canvas: Canvas) {
        val list = listActiveRender.toList()
        list.forEach {
            val rect = it.getCurrentBound()
            canvas.drawBitmap(it.type.image, rect.left, rect.top, paint)
        }
    }

    fun hasShitIn(rectF: RectF): Boolean {
        return listActiveRender.find {
            it.getCurrentBound().intersect(rectF)
        } != null
    }

    fun stopRender() {
        isPlaying = false
    }

    private fun addObjectToActiveList() {
        if (listPoolRender.size == 0) {
            val newFallObject = FallObject(x =  xPosition, type = FallObject.randomType())
            listActiveRender.push(newFallObject)
        } else {
            listActiveRender.push(listPoolRender.pop())
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default
}