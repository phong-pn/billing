package com.proxglobal.proxlibiap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.proxglobal.proxlibiap.util.logd
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onResume() {
        super.onResume()
        val billing = ProxPurchase.instance


        billing.addPurchaseUpdateListener(object : PurchaseUpdateListener {
            override fun onProductPurchased(productId: String) {
                logd("Product with id: $productId owned")
            }
        })

        billing.checkPurchased().logd()
        bt_offer_year.postDelayed({

            val baseMonth = ProxPurchase.instance.getBasePlan("lib_iap_premium", listOf("monthly-premium"))
            val offersMonth = baseMonth?.let { ProxPurchase.instance.getOfferSubscription(it, listOf("offer-monthly")) }
            val baseYear = ProxPurchase.instance.getBasePlan("lib_iap_premium", listOf("yearly-premium"))
            val offerYear = baseYear?.let { ProxPurchase.instance.getOfferSubscription(it, listOf("offer-yearly")) }
            bt_base_month.setOnClickListener {
                if (baseMonth != null) {
                    billing.subscribe(this, baseMonth)
                }
            }

            bt_offer_month.setOnClickListener {
                offersMonth?.get(0)?.let {
                    billing.subscribe(this,  it ) }

            }

            bt_base_year.setOnClickListener {
                if (baseYear != null) {
                    billing.subscribe(this, baseYear)
                }
            }

            bt_offer_year.setOnClickListener {
                offerYear?.get(0)?.let { billing.subscribe(this,  it) }
            }
        }, 2000)
    }
}