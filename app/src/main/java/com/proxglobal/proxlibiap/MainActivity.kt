package com.proxglobal.proxlibiap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.proxglobal.proxlibiap.util.logd
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val billing = BillingManager.instance
        billing.addOwnedProductListener(object : OwnedProductListener {
            override fun onOwned(productId: String) {
                logd("Product with id: $productId owned")
            }

        })
        bt_offer_year.postDelayed({

            val baseMonth = BillingManager.instance.getBasePlan("lib_iap_premium", listOf("monthly-premium"))
            val offersMonth = BillingManager.instance.getOfferSubscription(baseMonth!!, listOf("offer-monthly"))
            val baseYear = BillingManager.instance.getBasePlan("lib_iap_premium", listOf("yearly-premium"))
            val offerYear = BillingManager.instance.getOfferSubscription(baseYear!!, listOf("offer-yearly"))
            bt_base_month.setOnClickListener {
                billing.subscribe(this, baseMonth)
            }

            bt_offer_month.setOnClickListener {
                billing.subscribe(this, offersMonth[0])
            }

            bt_base_year.setOnClickListener {
                billing.subscribe(this, baseYear)
            }

            bt_offer_year.setOnClickListener {
                billing.subscribe(this, offerYear[0])
            }
        }, 4000)



    }
}