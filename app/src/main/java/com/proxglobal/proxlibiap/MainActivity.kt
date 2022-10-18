package com.proxglobal.proxlibiap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.proxglobal.purchasev2.ProxPurchase
import com.proxglobal.purchasev2.PurchaseUpdateListener
import com.proxglobal.purchasev2.util.logd
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onResume() {
        super.onResume()
        val billing = ProxPurchase.getInstance()


        billing.addPurchaseUpdateListener(object : PurchaseUpdateListener {
            override fun onProductPurchased(productId: String) {
                logd("Product with id: $productId owned")
            }
        })

        bt_offer_year.postDelayed({
            billing.checkPurchased().logd()

            val baseMonth =
                ProxPurchase.getInstance().getBasePlan("lib_iap_premium", listOf("monthly-premium"))
            val offersMonth = baseMonth?.let {
                ProxPurchase.getInstance().getOfferSubscription(
                    it,
                    listOf("offer-monthly")
                )
            }
            val baseYear =
                ProxPurchase.getInstance().getBasePlan("lib_iap_premium", listOf("yearly-premium"))
            val offerYear = baseYear?.let {
                ProxPurchase.getInstance().getOfferSubscription(
                    it,
                    listOf("offer-yearly")
                )
            }
            bt_base_month.setOnClickListener {
                if (baseMonth != null) {
                    billing.subscribe(this, baseMonth)
                }
            }

            bt_offer_month.setOnClickListener {
                offersMonth?.get(0)?.let {
                    billing.subscribe(this, it)
                }

            }

            bt_base_year.setOnClickListener {
                if (baseYear != null) {
                    billing.subscribe(this, baseYear)
                }
            }

            bt_offer_year.setOnClickListener {
                offerYear?.get(0)?.let { billing.subscribe(this, it) }
            }
        }, 2000)
    }
}