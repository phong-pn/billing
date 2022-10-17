package com.proxglobal.proxlibiap

import android.app.Application

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        ProxPurchase.instance.apply {
            init(this@MyApp, listOf("one_time_payment"), listOf("lib_iap_premium"))
        }
    }
}