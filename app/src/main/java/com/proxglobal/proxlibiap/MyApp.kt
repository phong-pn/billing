package com.proxglobal.proxlibiap

import com.proxglobal.proxads.ads.openads.ProxOpenAdsApplication
import com.proxglobal.purchase.ProxPurchase
import com.proxglobal.purchasev2.controller.ProxSale
import com.proxglobal.purchasev2.util.DataState
import com.proxglobal.purchasev2.util.logd

class MyApp : ProxOpenAdsApplication() {
    override fun onCreate() {
        super.onCreate()
//        ProxPurchase.getInstance().addOneTimeProductId(listOf("one_time_payment"))
        ProxSale.fetch {
            ProxSale.defaultSaleEvent?.logd()
            if (it is DataState.Success) {
                it.data?.apply {
                    pricePlans.forEach {
                        it.saleSubscriptions.logd()
                    }
                    ProxPurchase.getInstance().run {
                        getAllSubscription().size.logd()
                        addOneTimeProductId(getAllOneTimeProduct().map {
                            it.logd()
                            it.productId })
                        addSubscriptionId(getAllSubscription().map { it.productId })
                    }
                }
            }
        }
    }

    override fun getOpenAdsId(): String = ""

    override fun getListTestDeviceId(): MutableList<String> = mutableListOf()
}