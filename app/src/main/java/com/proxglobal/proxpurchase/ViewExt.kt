package com.proxglobal.proxpurchase

import android.graphics.Rect
import android.graphics.RectF
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View

fun View.isVisible(): Boolean {
    return visibility == View.VISIBLE
}

fun View.isInvisible(): Boolean {
    return visibility == View.INVISIBLE
}

fun View.isGone(): Boolean {
    return visibility == View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}


fun View.setOnPressListener(
    onPress: (view: View) -> Unit,
    onRelease: (view: View) -> Unit
) {
    setOnTouchListener { v, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                onPress(v)
            }
            MotionEvent.ACTION_UP -> {
                onRelease(v)
            }
        }
        false
    }
}

fun View.increaseClickArea(size: Int) {
    (parent as View).post {
        val r = Rect()
        getHitRect(r)
        r.top -= size
        r.bottom += size
        r.left -= size
        r.right += size
        (parent as View).touchDelegate = TouchDelegate(r, this)
    }
}

val View.screenLocation
    get(): IntArray {
        val point = IntArray(2)
        getLocationInWindow(point)
        return point
    }

val View.boundingBox
    get(): RectF {
        val (x, y) = screenLocation
        return RectF(x.toFloat(), y.toFloat(), (x + width).toFloat(), (y + height).toFloat())
    }
