package com.proxglobal.proxlibiap

import com.proxglobal.proxads.adsv2.admax.openads.MaxOpenAdsApplication
import com.proxglobal.purchase.billing.ProxPurchase
import com.proxglobal.purchase.controller.ProxSale
import com.proxglobal.purchase.data.sharepreference.ProxPreferences
import com.proxglobal.purchase.util.DataState
import com.proxglobal.util.logd

class MyApp : MaxOpenAdsApplication() {
    override fun onCreate() {
        super.onCreate()
        ProxSale.currentSaleEvent
        ProxSale.defaultSaleEvent
    }

    override fun getOpenAdsId(): String = ""
}