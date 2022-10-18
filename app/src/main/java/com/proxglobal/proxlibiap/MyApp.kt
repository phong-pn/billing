package com.proxglobal.proxlibiap

import android.app.Application
import com.example.sale_lib.sale.controller.ProxSale
import com.proxglobal.proxlibiap.data.sharepreference.ProxPreferences
import com.proxglobal.proxlibiap.util.DataState
import com.proxglobal.proxlibiap.util.logd

class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        ProxPurchase.instance.apply {
            init(this@MyApp)
        }

        ProxPreferences.init(this)
        ProxSale.fetch {
            ProxSale.defaultSaleEvent?.logd()
            if (it is DataState.Success) {
                it.data?.apply {
                    ProxPurchase.instance.apply {
                        addOneTimeProductId(getAllOneTimeProduct().map { it.productId })
                        addSubscriptionId(getAllSubscription().map { it.productId })
                    }
                }
            }
        }
    }
}