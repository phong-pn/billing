package com.proxglobal.proxlibiap

import com.proxglobal.proxads.ads.openads.ProxOpenAdsApplication
import com.proxglobal.purchase.ProxPurchase
import com.proxglobal.purchase.controller.ProxSale
import com.proxglobal.purchase.util.DataState
import com.proxglobal.util.logd

class MyApp : ProxOpenAdsApplication() {
    override fun onCreate() {
        super.onCreate()
        ProxSale.fetch {
            logd("Fetch proxsale success")
            if (it is DataState.Success) {
                it.data?.apply {
                    pricePlans.forEach {
                        it.saleSubscriptions.logd()
                    }
                    ProxPurchase.getInstance().run {
//                        getAllSubscription().size.logd()
//                        addOneTimeProductId(getAllOneTimeProduct().map {
//                            it.productId })
//                        addSubscriptionId(getAllSubscription().map { it.productId })
                    }
                }
            }
        }
    }

    override fun getOpenAdsId(): String = ""

    override fun getListTestDeviceId(): MutableList<String> = mutableListOf()
}