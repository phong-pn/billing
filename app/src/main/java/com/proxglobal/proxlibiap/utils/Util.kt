package com.proxglobal.proxlibiap.utils

import android.content.res.Resources
import android.view.View

val Number.dp: Float
    get() {
        val scale = Resources.getSystem().displayMetrics.density
        return this.toFloat() * scale + 0.5f
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