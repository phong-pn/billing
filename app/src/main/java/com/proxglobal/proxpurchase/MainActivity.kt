package com.proxglobal.proxpurchase

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.proxglobal.purchase.billing.ProxPurchase
import com.proxglobal.purchase.util.logd
import com.proxglobal.purchase.util.logdSelf
import kotlin.math.log

val subId = "lib_iap_premium"
val onetimeProductId = "one_time_payment"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ProxPurchase.getInstance().addInitBillingFinishListener {
            logd("Init oke")
        }

//
//        Handler(Looper.myLooper()!!).postDelayed({
//            ProxPurchase.getInstance().getBasePlan("premium-monthly").run {
//                logd("basePlanId = $basePlanId,  price = $price")
//            }
//            ProxPurchase.getInstance().getOffer("free-trial").run {
//                logd("offerId = $offerId, price = ${getDiscountPhase()}")
//            }
//            ProxPurchase.getInstance().getOneTimeProduct(onetimeProductId).logdSelf()
//        }, 2500)

    }
}