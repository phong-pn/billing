package com.proxglobal.proxlibiap

import com.proxglobal.proxads.adsv2.admax.openads.MaxOpenAdsApplication
import com.proxglobal.purchase.controller.ProxSale

class MyApp : MaxOpenAdsApplication() {
    override fun onCreate() {
        super.onCreate()
        ProxSale.useOnlySaleDefault = true
    }
    override fun getOpenAdsId(): String = ""
}