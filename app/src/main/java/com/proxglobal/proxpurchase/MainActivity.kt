package com.proxglobal.proxpurchase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.createSavedStateHandle
import com.proxglobal.purchase.billing.ProxPurchase
import com.proxglobal.purchase.util.logd
import com.proxglobal.purchase.util.logdSelf

val subId = "lib_iap_premium"
val onetimeProductId = "one_time_payment"
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Handler(Looper.myLooper()!!).postDelayed({
            ProxPurchase.getInstance().getBasePlan("premium-monthly").run {
                logd("basePlanId = $id,  price = $price")
                offers.logdSelf()
                offers.forEach {
                    logd("offerId = ${it.id},  price = ${it.getDiscountPrice()}")
                }
            }

            ProxPurchase.getInstance().getBasePlan("yearly-lib-premium").run {
                logd("basePlanId = $id,  price = $price")
                offers.forEach {
                    logd("offerId = ${it.id},  price = ${it.getDiscountPrice()}")
                }            }

            ProxPurchase.getInstance().getOneTimeProduct(onetimeProductId).logdSelf()
        }, 2500)

    }
}