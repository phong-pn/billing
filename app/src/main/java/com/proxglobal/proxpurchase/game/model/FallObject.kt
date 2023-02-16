package com.proxglobal.proxpurchase.game.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import com.proxglobal.proxpurchase.App
import com.proxglobal.proxpurchase.R

data class FallObject(
    val width: Int = 100,
    val height: Int = 100,
    val x: Float = 0f,
    var y: Float = -0f,
    val type: TypeObject
) {
    private var runningThread: Thread? = null
    fun run(target: Float, onFinished: () -> Unit) {
        runningThread = Thread {
            while (y < target) {
                Thread.sleep(4)
                y += 1.7f
            }
            onFinished()
            runningThread!!.interrupt()
            runningThread = null
        }
        runningThread!!.start()
    }

    val bound =  initBound()
        fun getCurrentBound() = bound.apply {
            top = y - height / 2
            bottom = y + height / 2
        }

    private fun initBound(): RectF {
        val rectF = RectF()
        rectF.left = x - width / 2
        rectF.top = y - height / 2
        rectF.right = x + width / 2
        rectF.bottom = y + height / 2
        return rectF
    }

    companion object {
        fun randomType(): TypeObject {
            return when ((0..4).random()) {
                0, 1 -> TypeObject.Shit()
                2, 3 -> TypeObject.ChungCake()
                else -> TypeObject.Bomb()
            }
        }

        fun getBitmap(resId: Int, width: Int, height: Int): Bitmap {
            val raw = BitmapFactory.decodeResource(
                App.instance.applicationContext.resources,
                resId
            )
            return Bitmap.createScaledBitmap(raw, width, height, true).apply { raw.recycle() }
        }

        val pomp by lazy { getBitmap(R.drawable.ic_poop, 100, 100) }
        val chungCake by lazy { getBitmap(R.drawable.ic_chung_cake, 100, 100) }
        val bomb by lazy { getBitmap(R.drawable.ic_bomb, 100, 100) }

    }

    sealed class TypeObject(val name: String, val image: Bitmap) {
        class Shit : TypeObject("Shit", pomp)
        class ChungCake : TypeObject("Chung Cake", chungCake)
        class Bomb : TypeObject("Bomb", bomb)
    }
}