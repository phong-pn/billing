package com.proxglobal.proxlibiap

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import com.phongpn.countdown.util.logd
import com.proxglobal.proxlibiap.ui.SaleDialog
import com.proxglobal.proxlibiap.utils.showSale
import com.proxglobal.purchase.controller.ProxSale
import com.proxglobal.purchase.sale.ProductPlan
import com.proxglobal.purchase.sale.Script
import com.proxglobal.util.logd
import com.proxglobal.util.logdSelf
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var premiumViewModel: PremiumViewModel
    private lateinit var mainViewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
//        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        premiumViewModel = ViewModelProviders.of(this)[PremiumViewModel::class.java]
        mainViewModel = ViewModelProviders.of(this)[MainViewModel::class.java]
        addObserver()
        addEvent()

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

    private fun addEvent() {
        bt_cta.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, PremiumFragment.newInstance(false), null)
                .commit()
        }

        bt_add_point.setOnClickListener {
            mainViewModel.onClickAdd(
                this,
                onShowPremium = {
                    bt_cta.performClick()
                },
                onCancelShowSale = {
                    Toast.makeText(this, "Click Add", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}