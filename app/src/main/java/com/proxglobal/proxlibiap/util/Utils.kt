package com.proxglobal.proxlibiap.util

import android.util.Log
import com.proxglobal.proxlibiap.BuildConfig
val TAG = "LOG_PROX"
fun Any.logd(message: String? = null) {
//    if (BuildConfig.DEBUG) {
        message?.let {
            Log.d(TAG, it)
        } ?: kotlin.run { Log.d(TAG, toString()) }
//    }
}

fun Any.loge(message: String? = null) {
//    if (BuildConfig.DEBUG) {
        message?.let {
            Log.e(TAG, it)
        } ?: kotlin.run { Log.e(TAG, toString()) }
//    }
}