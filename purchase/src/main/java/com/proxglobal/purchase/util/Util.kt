package com.proxglobal.purchase.util

import android.util.Log

const val TAG = "LOG_PROXGLOBAL"

internal var enableLog = true
fun disableLog() {
    enableLog = false
}

fun Any.logdSelf() {
    if (enableLog) {
        Log.d(TAG, toString())
    }
}

fun Any.logeSelf() {
    if (enableLog) {
        Log.e(TAG, toString())
    }
}

fun logd(message: String? = null, tag: String = TAG) {
    if (enableLog) {
        Log.d(tag, message ?: "null")
    }
}

fun loge(message: String? = null, tag: String = TAG) {
    if (enableLog) {
        Log.e(tag, message ?: "null")
    }
}