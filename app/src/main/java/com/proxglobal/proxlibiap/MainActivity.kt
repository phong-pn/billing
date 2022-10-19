package com.proxglobal.proxlibiap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.proxglobal.purchase.ProxPurchase
import com.proxglobal.purchase.PurchaseUpdateListener
import com.proxglobal.util.logd
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var premiumViewModel: PremiumViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
//        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        bt_cta.postDelayed({
            premiumViewModel = ViewModelProviders.of(this)[PremiumViewModel::class.java]
            addObserver()
            bt_cta.setOnClickListener {
                supportFragmentManager.beginTransaction()
                    .add(android.R.id.content, PremiumFragment(), null)
                    .commit()
            }
//        }, 200)

    }

    private fun addObserver() {
        premiumViewModel.uiState.observe(this) {
            if (it.isPurchased) {
                bt_cta.isVisible = false
            } else {
                premium_content.isVisible = false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val billing = ProxPurchase.getInstance()


        billing.addPurchaseUpdateListener(object : PurchaseUpdateListener {
            override fun onProductPurchased(productId: String) {
                logd("Product with id: $productId owned")
            }
        })

//        bt_offer_year.postDelayed({
//            billing.checkPurchased().logd()
//
//            val baseMonth =
//                ProxPurchase.getInstance().getBasePlan("lib_iap_premium", listOf("monthly-premium"))
//            val offersMonth = baseMonth?.let {
//                ProxPurchase.getInstance().getOfferSubscription(
//                    it,
//                    listOf("offer-monthly")
//                )
//            }
//            val baseYear =
//                ProxPurchase.getInstance().getBasePlan("lib_iap_premium", listOf("yearly-premium"))
//            val offerYear = baseYear?.let {
//                ProxPurchase.getInstance().getOfferSubscription(
//                    it,
//                    listOf("offer-yearly")
//                )
//            }
//            bt_base_month.setOnClickListener {
//                if (baseMonth != null) {
//                    billing.subscribe(this, baseMonth)
//                }
//            }
//
//            bt_offer_month.setOnClickListener {
//                offersMonth?.get(0)?.let {
//                    billing.subscribe(this, it)
//                }
//
//            }
//
//            bt_base_year.setOnClickListener {
//                if (baseYear != null) {
//                    billing.subscribe(this, baseYear)
//                }
//            }
//
//            bt_offer_year.setOnClickListener {
//                offerYear?.get(0)?.let { billing.subscribe(this, it) }
//            }
//        }, 2000)
    }
}