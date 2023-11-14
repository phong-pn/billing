package com.proxglobal.proxpurchase

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.proxglobal.proxpurchase.store.StoreFragment
import com.proxglobal.purchase.billing.ProxPurchase
import com.proxglobal.purchase.util.logd

val subId = "remove_ads"
val onetimeProductId = "in_app_product_1"

class MainActivity : AppCompatActivity() {
    private var positions: List<Float>
    private var humanPosition: Int

    init {
        val w = Resources.getSystem().displayMetrics.widthPixels.toFloat()
        positions = listOf(
            w / 8f,
            3 * w / 8f,
            5 * w / 8f,
            7 * w / 8f
        )
        humanPosition = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ProxPurchase.getInstance().addInitBillingFinishListener {
            logd("Init oke")
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, StoreFragment(), null)
                .commit()
        }

    }
}