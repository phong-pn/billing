package com.proxglobal.proxlibiap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.proxglobal.purchase.controller.ProxSale
import kotlinx.android.synthetic.main.premium_layout.*

class PremiumFragment: Fragment() {
    private lateinit var premiumViewModel: PremiumViewModel

    private var onClose: (() -> Unit)? = null
    private var showAdsAfterClose: Boolean = false

    companion object {
        fun newInstance(showAdsAfterClose: Boolean = false, onClose: (() -> Unit)? = null): PremiumFragment {
            return PremiumFragment().apply {
                this.onClose = onClose
                this.showAdsAfterClose = showAdsAfterClose
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.premium_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        premiumViewModel = ViewModelProviders.of(requireActivity())[PremiumViewModel::class.java]
        super.onViewCreated(view, savedInstanceState)
        addEvent()
        addObserver()
        bt_buy_year.setOnClickListener {
            premiumViewModel.buyMonth(requireActivity())
        }
    }

    private fun addEvent() {
        close_premium.setOnClickListener {
            if (showAdsAfterClose) {
                //showAds, after that call onClose
            } else {
                onClose?.invoke()
                parentFragmentManager.beginTransaction().remove(this).commit()
            }
        }
    }

    private fun addObserver() {
        premiumViewModel.uiState.observe(viewLifecycleOwner) {
            if (ProxSale.currentSaleEvent?.isSaleOff == true) {
                base_month_price.text =
                    "base = ${it.baseMonthlyPrice}, sale soc sap san ${it.discountMonthly}%, con ${it.offerMonthlyPrice}"
                offer_month_price.text =
                    "base = ${it.basePlanYearlyPrice}, sale soc sap san ${it.discountYearly}%, con ${it.offerYearlyPrice}"
            } else {
                base_month_price.text =
                    "base = ${it.baseMonthlyPrice}"
                offer_month_price.text =
                    "base = ${it.basePlanYearlyPrice}"
            }
        }
    }
}