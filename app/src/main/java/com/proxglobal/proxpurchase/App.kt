package com.proxglobal.proxpurchase

import android.app.Application
import com.proxglobal.purchase.billing.ProxPurchase
import com.proxglobal.purchase.data.sharepreference.ProxPreferences

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        ProxPurchase.getInstance().init(this)
        ProxPurchase.getInstance().apply {
            addSubscriptionId(listOf(subId))
            addOneTimeProductId(listOf(onetimeProductId))
            addConsumableId(listOf())
        }
    }

    companion object {
        lateinit var instance: Application
    }
}