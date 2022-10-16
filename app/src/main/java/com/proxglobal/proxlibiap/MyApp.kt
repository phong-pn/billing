package com.proxglobal.proxlibiap

import android.app.Application

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        ProxPurchase.instance.init(this)
    }
}